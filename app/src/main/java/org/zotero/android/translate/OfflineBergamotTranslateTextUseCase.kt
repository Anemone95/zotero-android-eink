package org.zotero.android.translate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import net.wenyuanxu.translate.ITranslateCallback
import net.wenyuanxu.translate.ITranslator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.zotero.android.architecture.Result
import org.zotero.android.architecture.Result.Failure
import org.zotero.android.architecture.Result.Success
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

// Talks to net.wenyuanxu.translate's TranslateService over AIDL for fully offline
// English→Chinese translation (Mozilla Bergamot). Each translate() call
// binds, runs, and unbinds; the model stays loaded inside net.wenyuanxu.translate
// for as long as Android keeps that process alive, so subsequent calls
// pay only the bind round-trip + ~200ms inference.
class OfflineBergamotTranslateTextUseCase @Inject constructor(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend fun translate(text: String): Result<String> = withContext(dispatcher) {
        val normalized = TranslationTextFormatter.normalizeInput(text)
        if (normalized.isEmpty()) {
            return@withContext Failure(Exception("Offline translate: empty input"))
        }

        val started = System.currentTimeMillis()
        val bound = bind() ?: return@withContext Failure(
            Exception("Offline translation app (net.wenyuanxu.translate) is not installed"),
        )
        try {
            val result = invokeTranslate(bound.translator, normalized)
            Timber.tag(LOG_TAG).i(
                "translated %d chars in %d ms",
                normalized.length, System.currentTimeMillis() - started,
            )
            result
        } finally {
            runCatching { context.unbindService(bound.connection) }
        }
    }

    private suspend fun invokeTranslate(
        translator: ITranslator,
        text: String,
    ): Result<String> = suspendCancellableCoroutine { cont ->
        val callback = object : ITranslateCallback.Stub() {
            override fun onResult(translation: String?) {
                cont.resume(
                    if (translation.isNullOrEmpty()) {
                        Failure(Exception("Offline translate: empty result"))
                    } else {
                        Success(translation)
                    },
                )
            }

            override fun onError(code: Int, message: String?) {
                cont.resume(
                    Failure(Exception("Offline translate error $code: ${message.orEmpty()}")),
                )
            }
        }
        val token = try {
            translator.translateAsync(text, "en", "zh", callback)
        } catch (e: Throwable) {
            cont.resume(Failure(Exception("Offline translate failed to dispatch", e)))
            return@suspendCancellableCoroutine
        }
        if (token == 0L) {
            // The service rejected synchronously and already invoked onError.
            return@suspendCancellableCoroutine
        }
        cont.invokeOnCancellation {
            runCatching { translator.cancel(token) }
        }
    }

    private suspend fun bind(): Bound? = suspendCancellableCoroutine { cont ->
        val intent = Intent(ACTION_BIND).apply { setPackage(TARGET_PACKAGE) }
        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val translator = ITranslator.Stub.asInterface(service)
                runCatching { translator.warmUp() }
                cont.resume(Bound(translator, this))
            }

            override fun onServiceDisconnected(name: ComponentName) = Unit
        }
        val ok = try {
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            Timber.tag(LOG_TAG).w(e, "bindService threw")
            false
        }
        if (!ok) {
            runCatching { context.unbindService(conn) }
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        cont.invokeOnCancellation {
            runCatching { context.unbindService(conn) }
        }
    }

    private data class Bound(
        val translator: ITranslator,
        val connection: ServiceConnection,
    )

    private companion object {
        const val TARGET_PACKAGE = "net.wenyuanxu.translate"
        const val ACTION_BIND = "net.wenyuanxu.translate.action.BIND"
        const val LOG_TAG = "OfflineBergamotTranslate"
    }
}
