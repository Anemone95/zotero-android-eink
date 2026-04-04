package org.zotero.android.screens.settings.translate

import androidx.annotation.StringRes
import org.zotero.android.screens.settings.elements.SettingsOption
import org.zotero.android.uicomponents.Strings

enum class TranslateService(
    @StringRes override val titleResId: Int,
) : SettingsOption {
    DeepLFreePlan(Strings.settings_translate_service_deepl_free_plan),
    Gemini(Strings.settings_translate_service_gemini);

    companion object {
        fun default(): TranslateService = DeepLFreePlan
    }
}
