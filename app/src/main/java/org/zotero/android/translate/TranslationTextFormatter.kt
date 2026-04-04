package org.zotero.android.translate

internal object TranslationTextFormatter {
    private val newlineRegex = Regex("[\\r\\n]+")
    private val whitespaceRegex = Regex("\\s{2,}")

    fun normalizeInput(text: String): String {
        return text
            .replace(newlineRegex, " ")
            .replace(whitespaceRegex, " ")
            .trim()
    }

    fun applyPromptTemplate(promptTemplate: String, text: String): String {
        val normalizedText = normalizeInput(text)
        if (normalizedText.isEmpty()) {
            return ""
        }
        val normalizedTemplate = promptTemplate.trim()
        if (normalizedTemplate.isEmpty()) {
            return normalizedText
        }
        return if (normalizedTemplate.contains(textPlaceholder)) {
            normalizedTemplate.replace(textPlaceholder, normalizedText)
        } else {
            "$normalizedTemplate\n\n$normalizedText"
        }
    }

    const val textPlaceholder = "{text}"
}
