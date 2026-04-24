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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.zotero.android.architecture.Defaults
import org.zotero.android.architecture.Result
import org.zotero.android.architecture.Result.Failure
import org.zotero.android.architecture.Result.Success
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

class ViwoodsTranslateTextUseCase @Inject constructor(
    private val context: Context,
    private val defaults: Defaults,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend fun translate(text: String): Result<String> = withContext(dispatcher) {
        val t0 = System.currentTimeMillis()
        val prompt = TranslationTextFormatter.applyPromptTemplate(
            promptTemplate = defaults.getTranslateViwoodsPrompt(),
            text = text,
        )
        if (prompt.isEmpty()) {
            return@withContext Failure(Exception("Viwoods prompt is empty"))
        }
        val model = defaults.getTranslateViwoodsModel().modelId
        Timber.tag(LOG_TAG).i(
            "start model=%s promptLen=%d textLen=%d",
            model, prompt.length, text.length,
        )

        suspendCancellableCoroutine<Result<String>> { cont ->
            val handler = Handler(Looper.getMainLooper())
            val original = object : ResultReceiver(handler) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    if (!cont.isActive) return
                    val total = System.currentTimeMillis() - t0
                    when (resultCode) {
                        RESULT_OK -> {
                            val answer = resultData?.getString(KEY_ANSWER)?.trim()
                            if (answer.isNullOrEmpty()) {
                                Timber.tag(LOG_TAG).w("empty answer in %d ms", total)
                                cont.resume(Failure(Exception("Viwoods response is empty")))
                            } else {
                                Timber.tag(LOG_TAG).i(
                                    "answer received in %d ms, answerLen=%d",
                                    total, answer.length,
                                )
                                cont.resume(Success(answer))
                            }
                        }

                        else -> {
                            val error = resultData?.getString(KEY_ERROR) ?: "Viwoods AI call failed"
                            Timber.tag(LOG_TAG).w("error=%s in %d ms", error, total)
                            cont.resume(Failure(Exception(error)))
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
                val tStart = System.currentTimeMillis()
                val started = context.startService(intent)
                val startDt = System.currentTimeMillis() - tStart
                if (started == null) {
                    Timber.tag(LOG_TAG).w("startService returned null in %d ms", startDt)
                    cont.resume(Failure(Exception("Viwoods AI service not found on this device")))
                } else {
                    Timber.tag(LOG_TAG).i(
                        "startService returned in %d ms (prep=%d ms)",
                        startDt, tStart - t0,
                    )
                }
            } catch (e: Exception) {
                Timber.tag(LOG_TAG).w(e, "startService threw")
                cont.resume(Failure(e))
            }
        }
    }

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

        const val LOG_TAG = "ViwoodsTranslate"
    }
}
