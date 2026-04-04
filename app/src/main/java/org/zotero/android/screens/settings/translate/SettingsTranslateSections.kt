package org.zotero.android.screens.settings.translate

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.zotero.android.screens.settings.elements.NewSettingsDivider
import org.zotero.android.screens.settings.elements.NewSettingsOptionItem
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.textinput.CustomOutlineTextField

@Composable
internal fun SettingsTranslateSections(
    viewState: SettingsTranslateViewState,
    viewModel: SettingsTranslateViewModel,
) {
    NewSettingsOptionItem(
        title = stringResource(Strings.settings_translate_service),
        options = TranslateService.entries,
        selectedOption = viewState.selectedService,
        onOptionSelected = viewModel::onTranslateServiceChanged,
    )

    NewSettingsDivider()

    CustomOutlineTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        value = viewState.visibleSecret,
        labelText = stringResource(id = Strings.settings_translate_secret),
        placeholderText = stringResource(id = Strings.settings_translate_secret_placeholder),
        visualTransformation = PasswordVisualTransformation(),
        onValueChange = viewModel::onSecretChanged,
        maxLines = 1,
        singleLine = true,
    )

    if (viewState.showPrompt) {
        NewSettingsDivider()

        CustomOutlineTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            value = viewState.geminiPrompt,
            labelText = stringResource(id = Strings.settings_translate_prompt),
            placeholderText = stringResource(id = Strings.settings_translate_prompt_placeholder),
            onValueChange = viewModel::onPromptChanged,
            minLines = 5,
            maxLines = 8,
            ignoreTabsAndCaretReturns = false,
        )
    }
}
