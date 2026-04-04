package org.zotero.android.pdf.settings.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.animation.with
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import org.zotero.android.pdf.reader.PdfReaderViewModel
import org.zotero.android.pdf.reader.PdfReaderViewState
import org.zotero.android.pdf.settings.PdfSettingsScreen
import org.zotero.android.uicomponents.theme.CustomTheme

@Composable
internal fun PdfSettingsView(
    viewState: PdfReaderViewState,
    viewModel: PdfReaderViewModel
) {
    if (viewModel.isEInkModeEnabled) {
        val showView = viewState.pdfSettingsArgs != null
        if (showView) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            //Prevent tap to be propagated to composables behind this screen.
                        }
                    }) {
                val args = viewState.pdfSettingsArgs
                if (args != null) {
                    PdfSettingsScreen(
                        args = args,
                        onBack = viewModel::hidePdfSettingsView
                    )
                }
            }
        }
    } else {
        androidx.compose.animation.AnimatedContent(
            targetState = viewState.pdfSettingsArgs != null,
            transitionSpec = {
                createCopyCitationTransitionSpec()
            }, label = ""
        ) { showView ->
            if (showView) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CustomTheme.colors.pdfAnnotationsFormBackground)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                //Prevent tap to be propagated to composables behind this screen.
                            }
                        }) {
                    val args = viewState.pdfSettingsArgs
                    if (args != null) {
                        PdfSettingsScreen(
                            args = args,
                            onBack = viewModel::hidePdfSettingsView
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.animation.AnimatedContentTransitionScope<Boolean>.createCopyCitationTransitionSpec(): androidx.compose.animation.ContentTransform {
    val intOffsetSpec = androidx.compose.animation.core.tween<androidx.compose.ui.unit.IntOffset>()
    return (androidx.compose.animation.slideInHorizontally(intOffsetSpec) { it } with
            androidx.compose.animation.slideOutHorizontally(intOffsetSpec) { it }).using(
        // Disable clipping since the faded slide-in/out should
        // be displayed out of bounds.
        androidx.compose.animation.SizeTransform(
            clip = false,
            sizeAnimationSpec = { _, _ -> androidx.compose.animation.core.tween() }
        ))
}
