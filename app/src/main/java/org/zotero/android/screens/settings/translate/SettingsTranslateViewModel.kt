package org.zotero.android.screens.settings.translate

import dagger.hilt.android.lifecycle.HiltViewModel
import org.zotero.android.architecture.BaseViewModel2
import org.zotero.android.architecture.Defaults
import org.zotero.android.architecture.ViewEffect
import org.zotero.android.architecture.ViewState
import javax.inject.Inject

@HiltViewModel
internal class SettingsTranslateViewModel @Inject constructor(
    private val defaults: Defaults,
) : BaseViewModel2<SettingsTranslateViewState, SettingsTranslateViewEffect>(
    SettingsTranslateViewState()
) {

    fun init() = initOnce {
        val service = defaults.getTranslateService()
        updateState {
            copy(
                selectedService = service,
                deepLSecret = defaults.getTranslateDeepLSecret(),
                geminiSecret = defaults.getTranslateGeminiSecret(),
                geminiPrompt = defaults.getTranslateGeminiPrompt(),
                viwoodsModel = defaults.getTranslateViwoodsModel(),
                viwoodsPrompt = defaults.getTranslateViwoodsPrompt(),
            )
        }
    }

    fun onTranslateServiceChanged(service: TranslateService) {
        defaults.setTranslateService(service)
        updateState {
            copy(selectedService = service)
        }
    }

    fun onSecretChanged(secret: String) {
        when (viewState.selectedService) {
            TranslateService.DeepLFreePlan -> {
                defaults.setTranslateDeepLSecret(secret)
                updateState {
                    copy(deepLSecret = secret)
                }
            }

            TranslateService.Gemini -> {
                defaults.setTranslateGeminiSecret(secret)
                updateState {
                    copy(geminiSecret = secret)
                }
            }

            TranslateService.Viwoods,
            TranslateService.OfflineBergamot -> Unit
        }
    }

    fun onPromptChanged(prompt: String) {
        when (viewState.selectedService) {
            TranslateService.Gemini -> {
                defaults.setTranslateGeminiPrompt(prompt)
                updateState {
                    copy(geminiPrompt = prompt)
                }
            }

            TranslateService.Viwoods -> {
                defaults.setTranslateViwoodsPrompt(prompt)
                updateState {
                    copy(viwoodsPrompt = prompt)
                }
            }

            TranslateService.DeepLFreePlan,
            TranslateService.OfflineBergamot -> Unit
        }
    }

    fun onViwoodsModelChanged(model: ViwoodsModel) {
        defaults.setTranslateViwoodsModel(model)
        updateState {
            copy(viwoodsModel = model)
        }
    }
}

internal data class SettingsTranslateViewState(
    val selectedService: TranslateService = TranslateService.default(),
    val deepLSecret: String = "",
    val geminiSecret: String = "",
    val geminiPrompt: String = "",
    val viwoodsModel: ViwoodsModel = ViwoodsModel.default(),
    val viwoodsPrompt: String = "",
) : ViewState {
    val showSecret: Boolean
        get() = selectedService == TranslateService.DeepLFreePlan ||
                selectedService == TranslateService.Gemini

    val showPrompt: Boolean
        get() = selectedService == TranslateService.Gemini ||
                selectedService == TranslateService.Viwoods

    val showViwoodsModel: Boolean
        get() = selectedService == TranslateService.Viwoods

    val visibleSecret: String
        get() = when (selectedService) {
            TranslateService.DeepLFreePlan -> deepLSecret
            TranslateService.Gemini -> geminiSecret
            TranslateService.Viwoods,
            TranslateService.OfflineBergamot -> ""
        }

    val visiblePrompt: String
        get() = when (selectedService) {
            TranslateService.Gemini -> geminiPrompt
            TranslateService.Viwoods -> viwoodsPrompt
            TranslateService.DeepLFreePlan,
            TranslateService.OfflineBergamot -> ""
        }
}

internal sealed class SettingsTranslateViewEffect : ViewEffect
