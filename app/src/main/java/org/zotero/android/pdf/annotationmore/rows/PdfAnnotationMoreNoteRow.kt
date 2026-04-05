package org.zotero.android.pdf.annotationmore.rows

import androidx.compose.runtime.Composable
import org.zotero.android.pdf.annotationmore.PdfAnnotationMoreViewModel
import org.zotero.android.pdf.annotationmore.PdfAnnotationMoreViewState
import org.zotero.android.pdf.annotationmore.blocks.PdfAnnotationMoreColorPicker

@Composable
internal fun PdfAnnotationMoreNoteRow(
    viewState: PdfAnnotationMoreViewState,
    viewModel: PdfAnnotationMoreViewModel,
) {
    PdfAnnotationMoreColorPicker(viewState, viewModel)
}
