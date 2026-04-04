package org.zotero.android.pdf.annotationstyle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

private val tokenShape = RoundedCornerShape(10.dp)
private val headerShape = RoundedCornerShape(8.dp)
private val contentShape = RoundedCornerShape(12.dp)
private val inkColor = Color(0xFF202020)
private val secondaryInkColor = Color(0xFF4A4A4A)

@Composable
internal fun AnnotationColorToken(
    hex: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val style = AnnotationColorStyles.styleFor(hex)
    Box(
        modifier = modifier
            .clip(tokenShape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawPanelBackground(style = style, cornerRadius = 10.dp)
            val strokeWidth = if (isSelected) 2.5.dp.toPx() else 1.dp.toPx()
            drawRoundRect(
                color = if (isSelected) inkColor else secondaryInkColor.copy(alpha = 0.35f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = Stroke(width = strokeWidth),
            )
        }
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.8f)
        ) {
            drawGlyph(style.glyph)
        }
    }
}

@Composable
internal fun AnnotationColorHeaderBadge(
    hex: String,
    modifier: Modifier = Modifier,
) {
    val style = AnnotationColorStyles.styleFor(hex)
    Box(
        modifier = modifier
            .clip(headerShape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawPanelBackground(style = style, cornerRadius = 8.dp)
            drawRoundRect(
                color = secondaryInkColor.copy(alpha = 0.3f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.82f)
        ) {
            drawGlyph(style.glyph)
        }
    }
}

@Composable
internal fun AnnotationPatternPanel(
    hex: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = AnnotationColorStyles.styleFor(hex)
    Box(
        modifier = modifier
            .clip(contentShape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawPanelBackground(style = style, cornerRadius = 12.dp)
            drawRoundRect(
                color = secondaryInkColor.copy(alpha = 0.22f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            content = content,
        )
    }
}

private fun DrawScope.drawPanelBackground(
    style: AnnotationColorStyle,
    cornerRadius: Dp,
) {
    drawRoundRect(
        color = AnnotationColorStyles.subtleFill(style.hex),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
    )
    drawTexture(style.texture)
}

private fun DrawScope.drawTexture(texture: AnnotationTexture) {
    val strokeWidth = 1.dp.toPx()
    val step = 8.dp.toPx()

    when (texture) {
        AnnotationTexture.None -> Unit
        AnnotationTexture.Horizontal -> {
            var y = step / 2f
            while (y < size.height) {
                drawLine(
                    color = secondaryInkColor.copy(alpha = 0.22f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
                y += step
            }
        }

        AnnotationTexture.RisingDiagonal -> {
            var offset = -size.height
            while (offset < size.width) {
                drawLine(
                    color = secondaryInkColor.copy(alpha = 0.2f),
                    start = Offset(offset, size.height),
                    end = Offset(offset + size.height, 0f),
                    strokeWidth = strokeWidth,
                )
                offset += step
            }
        }

        AnnotationTexture.FallingDiagonal -> {
            var offset = 0f
            while (offset < size.width + size.height) {
                drawLine(
                    color = secondaryInkColor.copy(alpha = 0.2f),
                    start = Offset(offset, 0f),
                    end = Offset(offset - size.height, size.height),
                    strokeWidth = strokeWidth,
                )
                offset += step
            }
        }

        AnnotationTexture.Dots -> {
            val radius = 1.1.dp.toPx()
            var x = step / 2f
            while (x < size.width) {
                var y = step / 2f
                while (y < size.height) {
                    drawCircle(
                        color = secondaryInkColor.copy(alpha = 0.24f),
                        radius = radius,
                        center = Offset(x, y),
                    )
                    y += step
                }
                x += step
            }
        }

        AnnotationTexture.Crosshatch -> {
            drawTexture(AnnotationTexture.RisingDiagonal)
            drawTexture(AnnotationTexture.FallingDiagonal)
        }

        AnnotationTexture.Grid -> {
            var x = step / 2f
            while (x < size.width) {
                drawLine(
                    color = secondaryInkColor.copy(alpha = 0.18f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokeWidth,
                )
                x += step
            }
            var y = step / 2f
            while (y < size.height) {
                drawLine(
                    color = secondaryInkColor.copy(alpha = 0.18f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
                y += step
            }
        }
    }
}

private fun DrawScope.drawGlyph(glyph: AnnotationGlyph) {
    when (glyph) {
        AnnotationGlyph.Unknown -> drawUnknown()
        AnnotationGlyph.Marker -> drawMarker()
        AnnotationGlyph.Info -> drawInfoSolid()
        AnnotationGlyph.Question -> drawCenteredText("?", 0.95f)
        AnnotationGlyph.Exclamation -> drawCenteredText("!", 0.98f)
        AnnotationGlyph.Quotes -> drawQuoteLeftSolid()
        AnnotationGlyph.Language -> drawLanguageIcon()
    }
}

private fun DrawScope.drawUnknown() {
    drawCenteredText("?", 0.9f)
}

private fun DrawScope.drawCenteredText(text: String, scale: Float) {
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#202020")
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = min(size.width, size.height) * scale
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        drawText(
            text,
            size.width / 2f,
            size.height * 0.77f,
            paint,
        )
    }
}

private fun DrawScope.drawMarker() {
    rotate(degrees = -32f, pivot = Offset(size.width / 2f, size.height / 2f)) {
        drawRoundRect(
            color = inkColor,
            topLeft = Offset(size.width * 0.14f, size.height * 0.3f),
            size = Size(size.width * 0.62f, size.height * 0.23f),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
        )
        val tip = Path().apply {
            moveTo(size.width * 0.76f, size.height * 0.3f)
            lineTo(size.width * 0.95f, size.height * 0.415f)
            lineTo(size.width * 0.76f, size.height * 0.53f)
            close()
        }
        drawPath(path = tip, color = inkColor)
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(size.width * 0.23f, size.height * 0.365f),
            end = Offset(size.width * 0.63f, size.height * 0.365f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawInfoSolid() {
    drawCircle(
        color = inkColor,
        radius = min(size.width, size.height) * 0.11f,
        center = Offset(size.width * 0.5f, size.height * 0.2f),
    )
    drawRoundRect(
        color = inkColor,
        topLeft = Offset(size.width * 0.35f, size.height * 0.35f),
        size = Size(size.width * 0.3f, size.height * 0.4f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
    )
    drawRoundRect(
        color = inkColor,
        topLeft = Offset(size.width * 0.27f, size.height * 0.72f),
        size = Size(size.width * 0.46f, size.height * 0.12f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
    )
}

private fun DrawScope.drawQuoteLeftSolid() {
    val path = PathParser()
        .parsePathString(quoteLeftSvgPathData)
        .toPath()
    val margin = min(size.width, size.height) * 0.08f
    val scale = (min(size.width, size.height) - margin * 2f) / quoteLeftSvgViewBoxSize
    path.transform(
        Matrix().apply {
            scale(scale, scale)
            translate(
                x = (size.width - quoteLeftSvgViewBoxSize * scale) / 2f + size.width * quoteLeftGlyphOffsetXFactor,
                y = (size.height - quoteLeftSvgViewBoxSize * scale) / 2f,
            )
        }
    )
    drawPath(path = path, color = inkColor, style = Fill)
}

private fun DrawScope.drawLanguageIcon() {
    val stroke = 2.2.dp.toPx()
    val pageLight = Color.White
    val leftPanel = Path().apply {
        moveTo(size.width * 0.14f, size.height * 0.26f)
        lineTo(size.width * 0.45f, size.height * 0.16f)
        lineTo(size.width * 0.45f, size.height * 0.72f)
        lineTo(size.width * 0.14f, size.height * 0.82f)
        close()
    }
    val rightPanel = Path().apply {
        moveTo(size.width * 0.55f, size.height * 0.16f)
        lineTo(size.width * 0.86f, size.height * 0.26f)
        lineTo(size.width * 0.86f, size.height * 0.82f)
        lineTo(size.width * 0.55f, size.height * 0.72f)
        close()
    }
    val backFrame = Path().apply {
        moveTo(size.width * 0.24f, size.height * 0.18f)
        lineTo(size.width * 0.5f, size.height * 0.1f)
        lineTo(size.width * 0.76f, size.height * 0.18f)
        lineTo(size.width * 0.76f, size.height * 0.3f)
    }
    drawPath(backFrame, color = inkColor, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))

    drawPath(leftPanel, color = pageLight)
    drawPath(leftPanel, color = inkColor, style = Stroke(width = stroke))
    drawPath(rightPanel, color = inkColor)
    drawPath(rightPanel, color = inkColor, style = Stroke(width = stroke))

    drawLine(
        color = inkColor,
        start = Offset(size.width * 0.28f, size.height * 0.36f),
        end = Offset(size.width * 0.34f, size.height * 0.36f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
    drawPath(
        path = Path().apply {
            moveTo(size.width * 0.26f, size.height * 0.44f)
            quadraticBezierTo(size.width * 0.33f, size.height * 0.52f, size.width * 0.18f, size.height * 0.62f)
            moveTo(size.width * 0.33f, size.height * 0.46f)
            quadraticBezierTo(size.width * 0.31f, size.height * 0.56f, size.width * 0.22f, size.height * 0.68f)
        },
        color = inkColor,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )

    val aPath = Path().apply {
        moveTo(size.width * 0.62f, size.height * 0.67f)
        lineTo(size.width * 0.7f, size.height * 0.34f)
        lineTo(size.width * 0.78f, size.height * 0.67f)
        moveTo(size.width * 0.65f, size.height * 0.55f)
        lineTo(size.width * 0.75f, size.height * 0.55f)
    }
    drawPath(aPath, color = pageLight, style = Stroke(width = stroke, cap = StrokeCap.Round))

    drawPath(
        path = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.86f)
            quadraticBezierTo(size.width * 0.54f, size.height * 1.0f, size.width * 0.8f, size.height * 0.86f)
            lineTo(size.width * 0.74f, size.height * 0.82f)
            moveTo(size.width * 0.8f, size.height * 0.86f)
            lineTo(size.width * 0.72f, size.height * 0.9f)
        },
        color = inkColor,
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
    )
}
