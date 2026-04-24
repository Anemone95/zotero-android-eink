package org.zotero.android.screens.settings.translate

import androidx.annotation.StringRes
import org.zotero.android.screens.settings.elements.SettingsOption
import org.zotero.android.uicomponents.Strings

enum class ViwoodsModel(
    val modelId: String,
    @StringRes override val titleResId: Int,
) : SettingsOption {
    Gpt4oMini("GPT-4o-mini", Strings.settings_translate_viwoods_model_gpt_4o_mini),
    Gpt4o("GPT-4o", Strings.settings_translate_viwoods_model_gpt_4o),
    Gpt5("GPT-5", Strings.settings_translate_viwoods_model_gpt_5),
    DeepSeekV3("DeepSeek-V3", Strings.settings_translate_viwoods_model_deepseek_v3),
    DeepSeekR1("DeepSeek-R1", Strings.settings_translate_viwoods_model_deepseek_r1),
    Gemini25Flash("Gemini-2.5-flash", Strings.settings_translate_viwoods_model_gemini_25_flash),
    Gemini25Pro("Gemini-2.5-pro", Strings.settings_translate_viwoods_model_gemini_25_pro),
    Gemini3Pro("Gemini-3-pro", Strings.settings_translate_viwoods_model_gemini_3_pro),
    QwenMax("Qwen-Max", Strings.settings_translate_viwoods_model_qwen_max),
    QwenPlus("Qwen-Plus", Strings.settings_translate_viwoods_model_qwen_plus);

    companion object {
        fun default(): ViwoodsModel = Gpt4oMini

        fun fromModelId(id: String?): ViwoodsModel =
            entries.firstOrNull { it.modelId == id } ?: default()
    }
}
