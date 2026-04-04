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
                showPrompt = service == TranslateService.Gemini,
            )
        }
    }

    fun onTranslateServiceChanged(service: TranslateService) {
        defaults.setTranslateService(service)
        updateState {
            copy(
                selectedService = service,
                showPrompt = service == TranslateService.Gemini,
            )
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
        }
    }

    fun onPromptChanged(prompt: String) {
        defaults.setTranslateGeminiPrompt(prompt)
        updateState {
            copy(geminiPrompt = prompt)
        }
    }
}

internal data class SettingsTranslateViewState(
    val selectedService: TranslateService = TranslateService.default(),
    val deepLSecret: String = "",
    val geminiSecret: String = "",
    val geminiPrompt: String = "",
    val showPrompt: Boolean = false,
) : ViewState {
    val visibleSecret: String
        get() = when (selectedService) {
            TranslateService.DeepLFreePlan -> deepLSecret
            TranslateService.Gemini -> geminiSecret
        }
}

internal sealed class SettingsTranslateViewEffect : ViewEffect
