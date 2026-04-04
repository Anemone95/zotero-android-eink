package org.zotero.android.pdf.reader.sidebar.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.zotero.android.pdf.annotationstyle.AnnotationPatternPanel
import org.zotero.android.pdf.data.PDFAnnotation
import org.zotero.android.pdf.reader.sidebar.sectionHorizontalPadding
import org.zotero.android.pdf.reader.sidebar.sectionVerticalPadding

@Composable
internal fun PdfReaderAnnotationsSidebarHighlightedTextSection(
    annotationColor: Color,
    annotation: PDFAnnotation,
    useGrayscaleEInkStyles: Boolean,
) {
    if (useGrayscaleEInkStyles) {
        AnnotationPatternPanel(
            hex = annotation.displayColor,
            modifier = Modifier
                .sectionHorizontalPadding()
                .sectionVerticalPadding()
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.72f))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = annotation.text ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .sectionHorizontalPadding()
                .sectionVerticalPadding()
                .height(IntrinsicSize.Max)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(annotationColor)
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = annotation.text ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
