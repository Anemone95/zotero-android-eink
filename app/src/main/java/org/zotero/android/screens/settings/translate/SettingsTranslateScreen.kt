package org.zotero.android.screens.settings.translate

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import org.zotero.android.uicomponents.CustomScaffoldM3
import org.zotero.android.uicomponents.themem3.AppThemeM3

@Composable
internal fun SettingsTranslateScreen(
    onBack: () -> Unit,
    viewModel: SettingsTranslateViewModel = hiltViewModel(),
) {
    AppThemeM3 {
        val viewState by viewModel.viewStates.observeAsState(SettingsTranslateViewState())

        LaunchedEffect(key1 = viewModel) {
            viewModel.init()
        }

        CustomScaffoldM3(
            topBar = {
                SettingsTranslateTopBar(
                    onBack = onBack,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    SettingsTranslateSections(
                        viewState = viewState,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}
