package org.zotero.android.pdf.annotationmore.blocks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import org.zotero.android.pdf.annotationstyle.AnnotationColorToken
import org.zotero.android.uicomponents.foundation.debounceClickable
import org.zotero.android.uicomponents.theme.CustomPalette

@Composable
internal fun PdfAnnotationMoreFilterCircle(
    hex: String,
    isSelected: Boolean,
    useGrayscaleEInkStyles: Boolean,
    onClick: () -> Unit,
) {
    val clickableModifier = Modifier
        .size(28.dp)
        .debounceClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )

    if (useGrayscaleEInkStyles) {
        Box(modifier = clickableModifier) {
            AnnotationColorToken(
                hex = hex,
                isSelected = isSelected,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        val color = hex.toColorInt()
        Canvas(modifier = clickableModifier, onDraw = {
            drawCircle(color = Color(color))
            if (isSelected) {
                drawCircle(
                    color = CustomPalette.White,
                    radius = 11.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        })
    }
}
