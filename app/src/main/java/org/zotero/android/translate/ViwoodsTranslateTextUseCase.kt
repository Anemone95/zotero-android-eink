package org.zotero.android.translate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.ResultReceiver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import org.zotero.android.architecture.Defaults
import org.zotero.android.architecture.Result
import org.zotero.android.architecture.Result.Failure
import org.zotero.android.architecture.Result.Success
import timber.log.Timber
import javax.inject.Inject

class ViwoodsTranslateTextUseCase @Inject constructor(
    private val context: Context,
    private val defaults: Defaults,
    private val dispatcher: CoroutineDispatcher,
) {

    // Emits one Result per receiver callback:
    //   * every RESULT_PARTIAL (code 2) becomes Success(cumulativeAnswer)
    //   * terminal RESULT_OK (code 0) becomes Success(finalAnswer), flow completes
    //   * terminal RESULT_ERROR (code 1) becomes Failure, flow completes
    //
    // Consumers can redraw on every Success emission and treat flow completion
    // as "loading finished". The headless service can't be cancelled from our
    // side — if the collector is cancelled mid-stream, the service will still
    // run to completion and deliver to a receiver whose channel is closed
    // (trySend becomes a no-op).
    fun translate(text: String): Flow<Result<String>> = channelFlow {
        val t0 = System.currentTimeMillis()
        val prompt = TranslationTextFormatter.applyPromptTemplate(
            promptTemplate = defaults.getTranslateViwoodsPrompt(),
            text = text,
        )
        if (prompt.isEmpty()) {
            send(Failure(Exception("Viwoods prompt is empty")))
            return@channelFlow
        }
        val model = defaults.getTranslateViwoodsModel().modelId
        Timber.tag(LOG_TAG).i(
            "start model=%s promptLen=%d textLen=%d",
            model, prompt.length, text.length,
        )

        val handler = Handler(Looper.getMainLooper())
        var firstPartialLogged = false
        val original = object : ResultReceiver(handler) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                val elapsed = System.currentTimeMillis() - t0
                when (resultCode) {
                    RESULT_PARTIAL -> {
                        val answer = resultData?.getString(KEY_ANSWER).orEmpty()
                        if (answer.isNotEmpty()) {
                            if (!firstPartialLogged) {
                                Timber.tag(LOG_TAG).i(
                                    "first partial in %d ms, len=%d",
                                    elapsed, answer.length,
                                )
                                firstPartialLogged = true
                            }
                            trySend(Success(answer))
                        }
                    }

                    RESULT_OK -> {
                        val answer = resultData?.getString(KEY_ANSWER).orEmpty()
                        if (answer.isEmpty()) {
                            Timber.tag(LOG_TAG).w("empty final in %d ms", elapsed)
                            trySend(Failure(Exception("Viwoods response is empty")))
                        } else {
                            Timber.tag(LOG_TAG).i(
                                "final in %d ms, len=%d",
                                elapsed, answer.length,
                            )
                            trySend(Success(answer))
                        }
                        close()
                    }

                    else -> {
                        val error = resultData?.getString(KEY_ERROR) ?: "Viwoods AI call failed"
                        Timber.tag(LOG_TAG).w("error=%s in %d ms", error, elapsed)
                        trySend(Failure(Exception(error)))
                        close()
                    }
                }
            }
        }

        val wrapped = parcelRoundTrip(original)

        val intent = Intent().apply {
            component = ComponentName(TARGET_PACKAGE, TARGET_CLASS)
            putExtra(EXTRA_PROMPT, prompt)
            putExtra(EXTRA_MODEL, model)
            putExtra(EXTRA_RECEIVER, wrapped)
        }

        try {
            val started = context.startService(intent)
            if (started == null) {
                Timber.tag(LOG_TAG).w("startService returned null")
                trySend(Failure(Exception("Viwoods AI service not found on this device")))
                close()
                return@channelFlow
            }
        } catch (e: Exception) {
            Timber.tag(LOG_TAG).w(e, "startService threw")
            trySend(Failure(e))
            close()
            return@channelFlow
        }

        awaitClose { /* service runs to completion on its own */ }
    }.flowOn(dispatcher)

    private fun parcelRoundTrip(receiver: ResultReceiver): ResultReceiver {
        val parcel = Parcel.obtain()
        return try {
            receiver.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            ResultReceiver.CREATOR.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }

    private companion object {
        const val TARGET_PACKAGE = "com.wisky.wiskyai"
        const val TARGET_CLASS = "com.wisky.wiskyai.service.AiHeadlessService"

        const val EXTRA_PROMPT = "prompt"
        const val EXTRA_MODEL = "model"
        const val EXTRA_RECEIVER = "receiver"

        const val KEY_ANSWER = "answer"
        const val KEY_ERROR = "error"

        const val RESULT_OK = 0
        const val RESULT_PARTIAL = 2

        const val LOG_TAG = "ViwoodsTranslate"
    }
}
