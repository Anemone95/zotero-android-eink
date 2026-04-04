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
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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
            if (isSelected) {
                drawRoundRect(
                    color = inkColor,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.82f)
        ) {
            drawIconBadge(style, 10.dp)
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
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.84f)
        ) {
            drawIconBadge(style, 8.dp)
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
            drawPanelBorder(style, 12.dp)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            content = content,
        )
    }
}

private fun DrawScope.drawIconBadge(style: AnnotationColorStyle, cornerRadius: Dp) {
    if (style.showsDecorativeBorder()) {
        drawBadgeBorder(style)
    }
    val glyphInset = if (style.showsDecorativeBorder()) size.minDimension * 0.2f else size.minDimension * 0.06f
    inset(glyphInset, glyphInset, glyphInset, glyphInset) {
        drawGlyph(style.glyph)
    }
}

private fun DrawScope.drawPanelBorder(
    style: AnnotationColorStyle,
    cornerRadius: Dp,
) {
    if (!style.showsDecorativeBorder()) {
        return
    }
    drawStyledBorder(style, cornerRadius)
}

private fun DrawScope.drawBadgeBorder(style: AnnotationColorStyle) {
    val strokeWidth = 1.5.dp.toPx()
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension * 0.42f

    when (style.glyph) {
        AnnotationGlyph.Marker -> drawCircle(
            color = inkColor,
            radius = radius,
            center = center,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f, 6f)),
            ),
        )
        AnnotationGlyph.Question,
        AnnotationGlyph.Unknown,
        -> drawCircle(
            color = inkColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth),
        )

        AnnotationGlyph.Exclamation -> drawDoubleCircleBorder(center, radius, strokeWidth)

        AnnotationGlyph.Language -> drawCircle(
            color = inkColor,
            radius = radius,
            center = center,
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)),
            ),
        )

        AnnotationGlyph.Info -> drawRadialPatternBorder(
            center = center,
            baseRadius = radius,
            amplitude = size.minDimension * 0.03f,
            cycles = 20,
            angularSamples = 40,
            strokeWidth = strokeWidth,
            rounded = false,
        )

        AnnotationGlyph.Quotes -> drawRadialPatternBorder(
            center = center,
            baseRadius = radius,
            amplitude = size.minDimension * 0.025f,
            cycles = 14,
            angularSamples = 112,
            strokeWidth = strokeWidth,
            rounded = true,
        )
    }
}

private fun AnnotationColorStyle.showsDecorativeBorder(): Boolean {
    return true
}

private fun DrawScope.drawStyledBorder(
    style: AnnotationColorStyle,
    cornerRadius: Dp,
) {
    val insetPx = 1.5.dp.toPx()
    val left = insetPx
    val top = insetPx
    val right = size.width - insetPx
    val bottom = size.height - insetPx
    val strokeWidth = 2.dp.toPx()

    when (style.glyph) {
        AnnotationGlyph.Marker -> {
            drawRoundRect(
                color = inkColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f, 8f)),
                ),
            )
        }
        AnnotationGlyph.Question,
        AnnotationGlyph.Unknown,
        -> {
            drawRoundRect(
                color = inkColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                style = Stroke(width = strokeWidth),
            )
        }

        AnnotationGlyph.Exclamation -> drawDoubleRoundedBorder(left, top, right, bottom, cornerRadius, strokeWidth)

        AnnotationGlyph.Language -> {
            drawRoundRect(
                color = inkColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f)),
                ),
            )
        }

        AnnotationGlyph.Info -> drawSawtoothBorder(left, top, right, bottom)
        AnnotationGlyph.Quotes -> drawWavyBorder(left, top, right, bottom)
    }
}

private fun DrawScope.drawDoubleCircleBorder(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
) {
    drawCircle(
        color = inkColor,
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth),
    )
    val inset = 4.dp.toPx()
    if (radius <= inset) {
        return
    }
    drawCircle(
        color = inkColor,
        radius = radius - inset,
        center = center,
        style = Stroke(width = strokeWidth),
    )
}

private fun DrawScope.drawDoubleRoundedBorder(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    cornerRadius: Dp,
    strokeWidth: Float,
) {
    drawRoundRect(
        color = inkColor,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(width = strokeWidth),
    )
    val inset = 5.dp.toPx()
    if (right - left <= inset * 2f || bottom - top <= inset * 2f) {
        return
    }
    val innerCorner = (cornerRadius.toPx() - inset / 2f).coerceAtLeast(2.dp.toPx())
    drawRoundRect(
        color = inkColor,
        topLeft = Offset(left + inset, top + inset),
        size = Size(right - left - inset * 2f, bottom - top - inset * 2f),
        cornerRadius = CornerRadius(innerCorner, innerCorner),
        style = Stroke(width = strokeWidth),
    )
}

private fun DrawScope.drawSawtoothBorder(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
) {
    val amplitude = 1.5.dp.toPx()
    val step = 8.dp.toPx()
    drawSawtoothHorizontalEdge(left, right, top, -amplitude, step)
    drawSawtoothHorizontalEdge(left, right, bottom, amplitude, step)
    drawSawtoothVerticalEdge(top, bottom, left, -amplitude, step)
    drawSawtoothVerticalEdge(top, bottom, right, amplitude, step)
}

private fun DrawScope.drawWavyBorder(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
) {
    val amplitude = 1.5.dp.toPx()
    val wavelength = 10.dp.toPx()
    drawWavyHorizontalEdge(left, right, top, -amplitude, wavelength)
    drawWavyHorizontalEdge(left, right, bottom, amplitude, wavelength)
    drawWavyVerticalEdge(top, bottom, left, -amplitude, wavelength)
    drawWavyVerticalEdge(top, bottom, right, amplitude, wavelength)
}

private fun DrawScope.drawSawtoothHorizontalEdge(
    startX: Float,
    endX: Float,
    y: Float,
    offsetY: Float,
    step: Float,
) {
    drawPath(
        path = Path().apply {
            moveTo(startX, y)
            var x = startX
            while (x < endX) {
                val mid = (x + step / 2f).coerceAtMost(endX)
                val next = (x + step).coerceAtMost(endX)
                lineTo(mid, y + offsetY)
                lineTo(next, y)
                x += step
            }
        },
        color = inkColor,
        style = Stroke(width = 2.dp.toPx()),
    )
}

private fun DrawScope.drawSawtoothVerticalEdge(
    startY: Float,
    endY: Float,
    x: Float,
    offsetX: Float,
    step: Float,
) {
    drawPath(
        path = Path().apply {
            moveTo(x, startY)
            var y = startY
            while (y < endY) {
                val mid = (y + step / 2f).coerceAtMost(endY)
                val next = (y + step).coerceAtMost(endY)
                lineTo(x + offsetX, mid)
                lineTo(x, next)
                y += step
            }
        },
        color = inkColor,
        style = Stroke(width = 2.dp.toPx()),
    )
}

private fun DrawScope.drawWavyHorizontalEdge(
    startX: Float,
    endX: Float,
    y: Float,
    offsetY: Float,
    wavelength: Float,
) {
    drawPath(
        path = Path().apply {
            moveTo(startX, y)
            var x = startX
            while (x < endX) {
                val mid = (x + wavelength / 2f).coerceAtMost(endX)
                val next = (x + wavelength).coerceAtMost(endX)
                quadraticBezierTo(mid, y + offsetY, next, y)
                x += wavelength
            }
        },
        color = inkColor,
        style = Stroke(width = 2.dp.toPx()),
    )
}

private fun DrawScope.drawWavyVerticalEdge(
    startY: Float,
    endY: Float,
    x: Float,
    offsetX: Float,
    wavelength: Float,
) {
    drawPath(
        path = Path().apply {
            moveTo(x, startY)
            var y = startY
            while (y < endY) {
                val mid = (y + wavelength / 2f).coerceAtMost(endY)
                val next = (y + wavelength).coerceAtMost(endY)
                quadraticBezierTo(x + offsetX, mid, x, next)
                y += wavelength
            }
        },
        color = inkColor,
        style = Stroke(width = 2.dp.toPx()),
    )
}

private fun DrawScope.drawRadialPatternBorder(
    center: Offset,
    baseRadius: Float,
    amplitude: Float,
    cycles: Int,
    angularSamples: Int,
    strokeWidth: Float,
    rounded: Boolean,
) {
    val totalSteps = cycles * angularSamples
    val path = Path()
    for (step in 0..totalSteps) {
        val angle = (2.0 * PI * step.toDouble() / totalSteps.toDouble()).toFloat()
        val phase = (2.0 * PI * cycles.toDouble() * step.toDouble() / totalSteps.toDouble()).toFloat()
        val radius = if (rounded) {
            baseRadius + amplitude * sin(phase)
        } else {
            val normalized = ((phase / PI.toFloat()) % 2f + 2f) % 2f
            val triangle = if (normalized < 1f) normalized else 2f - normalized
            baseRadius + amplitude * (triangle * 2f - 1f)
        }
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        if (step == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    drawPath(path = path, color = inkColor, style = Stroke(width = strokeWidth))
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
