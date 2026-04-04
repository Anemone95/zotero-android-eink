package org.zotero.android.translate

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.zotero.android.api.NonZoteroApi
import org.zotero.android.architecture.Defaults
import org.zotero.android.architecture.Result
import org.zotero.android.architecture.Result.Failure
import org.zotero.android.architecture.Result.Success
import javax.inject.Inject

class GeminiTranslateTextUseCase @Inject constructor(
    private val api: NonZoteroApi,
    private val defaults: Defaults,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend fun translate(text: String): Result<String> = withContext(dispatcher) {
        val secret = defaults.getTranslateGeminiSecret().trim()
        if (secret.isEmpty()) {
            return@withContext Failure(Exception("Missing Gemini secret"))
        }
        val prompt = TranslationTextFormatter.applyPromptTemplate(
            promptTemplate = defaults.getTranslateGeminiPrompt(),
            text = text,
        )
        if (prompt.isEmpty()) {
            return@withContext Failure(Exception("Gemini prompt is empty"))
        }

        val body = JsonObject().apply {
            add("contents", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    add("parts", JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("text", prompt)
                        })
                    })
                })
            })
            add("generationConfig", JsonObject().apply {
                addProperty("temperature", 0.2)
            })
        }

        val response = api.sendWebViewPost(
            url = GEMINI_GENERATE_CONTENT_URL,
            headers = mapOf(
                "x-goog-api-key" to secret,
                "Content-Type" to "application/json",
            ),
            textBody = gson.toJson(body),
        )

        if (!response.isSuccessful) {
            return@withContext Failure(Exception("Gemini request failed with HTTP ${response.code()}"))
        }

        val responseBody = response.body()?.string()
            ?: return@withContext Failure(Exception("Gemini response body is empty"))
        val payload = gson.fromJson(responseBody, JsonObject::class.java)
        val translatedText = payload
            .getAsJsonArray("candidates")
            ?.firstOrNull()
            ?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?.mapNotNull { part ->
                part?.asJsonObject
                    ?.get("text")
                    ?.asString
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            }
            ?.joinToString("\n")
            ?.trim()

        if (translatedText.isNullOrEmpty()) {
            return@withContext Failure(Exception("Gemini response missing translated text"))
        }

        Success(translatedText)
    }

    private companion object {
        // Inference from current official Gemini Developer API pricing/model docs:
        // gemini-3.1-flash-lite-preview currently exists and is described as the most cost-efficient
        // Gemini model, explicitly positioned for translation and other high-volume low-latency tasks.
        const val GEMINI_MODEL = "gemini-3.1-flash-lite-preview"
        const val GEMINI_GENERATE_CONTENT_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent"
    }
}
