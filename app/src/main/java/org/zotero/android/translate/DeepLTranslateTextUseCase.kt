package org.zotero.android.translate

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.zotero.android.api.NonZoteroApi
import org.zotero.android.architecture.Defaults
import org.zotero.android.architecture.Result
import org.zotero.android.architecture.Result.Failure
import org.zotero.android.architecture.Result.Success
import java.util.Locale
import javax.inject.Inject

class DeepLTranslateTextUseCase @Inject constructor(
    private val api: NonZoteroApi,
    private val defaults: Defaults,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend fun translate(text: String): Result<String> = withContext(dispatcher) {
        val secret = defaults.getTranslateDeepLSecret().trim()
        if (secret.isEmpty()) {
            return@withContext Failure(Exception("Missing DeepL secret"))
        }
        val normalizedText = text
            .replace(newlineRegex, " ")
            .replace(whitespaceRegex, " ")
            .trim()
        if (normalizedText.isEmpty()) {
            return@withContext Failure(Exception("DeepL text is empty after normalization"))
        }

        val body = JsonObject().apply {
            addProperty("target_lang", targetLanguageFor(Locale.getDefault()))
            addProperty("split_sentences", "0")
            add("text", gson.toJsonTree(listOf(normalizedText)))
        }

        val response = api.sendWebViewPost(
            url = DEEPL_FREE_TRANSLATE_URL,
            headers = mapOf(
                "Authorization" to "DeepL-Auth-Key $secret",
                "Content-Type" to "application/json",
            ),
            textBody = gson.toJson(body),
        )

        if (!response.isSuccessful) {
            return@withContext Failure(Exception("DeepL request failed with HTTP ${response.code()}"))
        }

        val responseBody = response.body()?.string()
            ?: return@withContext Failure(Exception("DeepL response body is empty"))
        val payload = gson.fromJson(responseBody, JsonObject::class.java)
        val translatedText = payload
            .getAsJsonArray("translations")
            ?.firstOrNull()
            ?.asJsonObject
            ?.get("text")
            ?.asString
            ?.trim()

        if (translatedText.isNullOrEmpty()) {
            return@withContext Failure(Exception("DeepL response missing translated text"))
        }

        Success(translatedText)
    }

    private fun targetLanguageFor(locale: Locale): String {
        val language = locale.language.lowercase(Locale.ROOT)
        val country = locale.country.uppercase(Locale.ROOT)
        return when (language) {
            "en" -> if (country == "GB") "EN-GB" else "EN-US"
            "pt" -> if (country == "BR") "PT-BR" else "PT-PT"
            "zh" -> when (country) {
                "TW", "HK", "MO" -> "ZH-HANT"
                else -> "ZH-HANS"
            }

            "es" -> if (country in latinAmericanSpanishRegions) "ES-419" else "ES"
            else -> language.takeIf { it.length == 2 }?.uppercase(Locale.ROOT) ?: "EN-US"
        }
    }

    private companion object {
        const val DEEPL_FREE_TRANSLATE_URL = "https://api-free.deepl.com/v2/translate"
        val newlineRegex = Regex("[\\r\\n]+")
        val whitespaceRegex = Regex("\\s{2,}")

        val latinAmericanSpanishRegions = setOf(
            "AR", "BO", "CL", "CO", "CR", "CU", "DO", "EC", "GT", "HN", "MX",
            "NI", "PA", "PE", "PR", "PY", "SV", "UY", "VE",
        )
    }
}
