package org.zotero.android.pdf.reader.sidebar.rows

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.zotero.android.pdf.data.PDFAnnotation
import org.zotero.android.pdf.reader.PdfReaderVMInterface
import org.zotero.android.pdf.reader.PdfReaderViewState
import org.zotero.android.pdf.reader.sidebar.sections.PdfReaderAnnotationsSidebarTagsAndCommentsSection
import org.zotero.android.pdf.reader.sidebar.sections.PdfReaderAnnotationsSidebarUnderlineTextSection

@Composable
internal fun PdfReaderAnnotationsSidebarUnderlineRow(
    annotation: PDFAnnotation,
    viewState: PdfReaderViewState,
    vMInterface: PdfReaderVMInterface,
    annotationColor: Color,
    useGrayscaleEInkStyles: Boolean,
) {
    PdfReaderAnnotationsSidebarUnderlineTextSection(
        annotationColor = annotationColor,
        annotation = annotation,
        useGrayscaleEInkStyles = useGrayscaleEInkStyles,
    )

    PdfReaderAnnotationsSidebarTagsAndCommentsSection(
        annotation = annotation,
        viewState = viewState,
        vMInterface = vMInterface,
        shouldAddTopPadding = false,
    )
}
