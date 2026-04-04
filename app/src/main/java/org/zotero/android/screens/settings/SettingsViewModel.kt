package org.zotero.android.screens.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import org.zotero.android.architecture.BaseViewModel2
import org.zotero.android.architecture.Defaults
import org.zotero.android.architecture.ViewEffect
import org.zotero.android.architecture.ViewState
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val defaults: Defaults,
) : BaseViewModel2<SettingsViewState, SettingsViewEffect>(SettingsViewState()) {

    fun init() = initOnce {
        updateState {
            copy(selectedEInkMode = defaults.getEInkMode())
        }
    }

    fun onDone() {
        triggerEffect(SettingsViewEffect.OnBack)
    }

    fun openPrivacyPolicy() {
        val uri = "https://www.zotero.org/support/privacy?app=1"
        triggerEffect(SettingsViewEffect.OpenWebpage(uri))
    }

    fun openSupportAndFeedback() {
        val uri = "https://forums.zotero.org/"
        triggerEffect(SettingsViewEffect.OpenWebpage(uri))
    }

    fun onEInkModeChanged(newValue: EInkMode) {
        defaults.setEInkMode(newValue)
        updateState {
            copy(selectedEInkMode = newValue)
        }
    }
}

internal data class SettingsViewState(
    val selectedEInkMode: EInkMode = EInkMode.Off,
) : ViewState

internal sealed class SettingsViewEffect : ViewEffect {
    object OnBack : SettingsViewEffect()
    data class OpenWebpage(val url: String) : SettingsViewEffect()
}
