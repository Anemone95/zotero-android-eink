package org.zotero.android.pdf.annotationmore.sidebar

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.with
import org.zotero.android.pdf.annotationmore.PdfAnnotationMoreNavigation
import org.zotero.android.pdf.reader.PdfReaderViewModel
import org.zotero.android.pdf.reader.PdfReaderViewState

@Composable
internal fun PdfAnnotationMoreNavigationView(
    viewState: PdfReaderViewState,
    viewModel: PdfReaderViewModel
) {
    if (viewModel.isEInkModeEnabled) {
        val showView = viewState.pdfAnnotationMoreArgs != null
        if (showView) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            //Prevent tap to be propagated to composables behind this screen.
                        }
                    }) {
                val args = viewState.pdfAnnotationMoreArgs
                if (args != null) {
                    PdfAnnotationMoreNavigation(
                        args = args,
                        onBack = viewModel::hidePdfAnnotationMoreView,
                        disableAnimations = true,
                    )
                }
            }
        }
    } else {
        androidx.compose.animation.AnimatedContent(
            targetState = viewState.pdfAnnotationMoreArgs != null,
            transitionSpec = {
                createAnnotationTransitionSpec()
            }, label = ""
        ) { showView ->
            if (showView) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                //Prevent tap to be propagated to composables behind this screen.
                            }
                        }) {
                    val args = viewState.pdfAnnotationMoreArgs
                    if (args != null) {
                        PdfAnnotationMoreNavigation(
                            args = args,
                            onBack = viewModel::hidePdfAnnotationMoreView,
                            disableAnimations = false,
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.animation.AnimatedContentTransitionScope<Boolean>.createAnnotationTransitionSpec(): androidx.compose.animation.ContentTransform {
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
