package org.zotero.android.pdf.reader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathDashPathEffect
import android.graphics.RectF
import android.view.View
import androidx.core.graphics.PathParser
import com.pspdfkit.annotations.HighlightAnnotation
import com.pspdfkit.annotations.TextMarkupAnnotation
import com.pspdfkit.annotations.UnderlineAnnotation
import com.pspdfkit.annotations.getAnnotationsBlocking
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.ui.overlay.OverlayLayoutParams
import com.pspdfkit.ui.overlay.OverlayViewProvider
import org.zotero.android.ktx.baseColor
import org.zotero.android.pdf.annotationstyle.AnnotationGlyph
import org.zotero.android.pdf.annotationstyle.AnnotationColorStyles
import org.zotero.android.pdf.annotationstyle.quoteLeftGlyphOffsetXFactor
import org.zotero.android.pdf.annotationstyle.quoteLeftSvgPathData
import org.zotero.android.pdf.annotationstyle.quoteLeftSvgViewBoxSize
import kotlin.math.max
import kotlin.math.min

internal class GrayscaleAnnotationOverlayViewProvider(
    private val isEnabled: () -> Boolean,
) : OverlayViewProvider() {

    override fun getViewsForPage(
        context: Context,
        document: PdfDocument,
        pageIndex: Int,
    ): List<View> {
        if (!isEnabled()) {
            return emptyList()
        }

        return document.annotationProvider
            .getAnnotationsBlocking(pageIndex)
            .mapNotNull { annotation ->
                val markup = annotation as? TextMarkupAnnotation ?: return@mapNotNull null
                if (markup !is HighlightAnnotation && markup !is UnderlineAnnotation) {
                    return@mapNotNull null
                }
                GrayscaleMarkupOverlayView(context, markup)
            }
    }
}

private class GrayscaleMarkupOverlayView(
    context: Context,
    private val annotation: TextMarkupAnnotation,
) : View(context) {

    private val baseBounds = normalizedRect(annotation.boundingBox)
    private val overlayBounds = expandedRect(baseBounds)
    private val style = AnnotationColorStyles.styleFor(annotation.baseColor)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(255, 32, 32, 32)
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 32, 32, 32)
        style = Paint.Style.STROKE
    }
    private val highlightOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(210, 32, 32, 32)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val borderRectPath = Path()
    private val sawtoothStamp = Path().apply {
        moveTo(0f, 0f)
        lineTo(6f, -7f)
        lineTo(12f, 0f)
    }
    private val horizontalStamp = Path().apply {
        addRect(0f, -0.9f, 8f, 0.9f, Path.Direction.CW)
    }
    private val dotStamp = Path().apply {
        addCircle(0f, 0f, 1.5f, Path.Direction.CW)
    }

    init {
        layoutParams = OverlayLayoutParams(overlayBounds, OverlayLayoutParams.SizingMode.SCALING)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) {
            return
        }

        val rects = annotation.rects
            ?.takeIf { it.isNotEmpty() }
            ?.map(::normalizedRect)
            ?: listOf(baseBounds)
        val localRects = rects.map { it.toLocalRect() }

        if (annotation is HighlightAnnotation) {
            localRects.forEach { drawHighlightBorder(canvas, it) }
        }

        drawBadge(canvas, localRects.first())
    }

    private fun drawHighlightBorder(canvas: Canvas, rect: RectF) {
        val borderRect = RectF(
            rect.left + 1f,
            rect.top + 1f,
            rect.right - 1f,
            rect.bottom - 1f,
        )
        if (borderRect.width() <= 0f || borderRect.height() <= 0f) {
            return
        }
        when (style.glyph) {
            AnnotationGlyph.Marker,
            AnnotationGlyph.Unknown,
            -> return

            AnnotationGlyph.Info -> drawSawtoothBorder(canvas, borderRect)

            AnnotationGlyph.Question -> drawSolidBorder(canvas, borderRect)

            AnnotationGlyph.Exclamation -> drawPatternBorder(
                canvas = canvas,
                rect = borderRect,
                pathEffect = PathDashPathEffect(dotStamp, 7f, 0f, PathDashPathEffect.Style.TRANSLATE),
            )

            AnnotationGlyph.Quotes -> drawWavyBorder(canvas, borderRect)

            AnnotationGlyph.Language -> drawPatternBorder(
                canvas = canvas,
                rect = borderRect,
                pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f),
            )
        }
    }

    private fun drawPatternBorder(
        canvas: Canvas,
        rect: RectF,
        pathEffect: android.graphics.PathEffect,
    ) {
        borderRectPath.reset()
        borderRectPath.addRect(rect, Path.Direction.CW)
        highlightOutlinePaint.pathEffect = pathEffect
        canvas.drawPath(borderRectPath, highlightOutlinePaint)
        highlightOutlinePaint.pathEffect = null
    }

    private fun drawSolidBorder(canvas: Canvas, rect: RectF) {
        canvas.drawRect(rect, highlightOutlinePaint)
    }

    private fun drawSawtoothBorder(canvas: Canvas, rect: RectF) {
        highlightOutlinePaint.pathEffect = PathDashPathEffect(sawtoothStamp, 16f, 0f, PathDashPathEffect.Style.ROTATE)
        canvas.drawPath(
            Path().apply {
                moveTo(rect.left, rect.top - 1f)
                lineTo(rect.right, rect.top - 1f)
            },
            highlightOutlinePaint,
        )
        canvas.drawPath(
            Path().apply {
                moveTo(rect.left, rect.bottom + 1f)
                lineTo(rect.right, rect.bottom + 1f)
            },
            highlightOutlinePaint,
        )
        canvas.drawPath(
            Path().apply {
                moveTo(rect.left, rect.top)
                lineTo(rect.left, rect.bottom)
            },
            highlightOutlinePaint,
        )
        canvas.drawPath(
            Path().apply {
                moveTo(rect.right, rect.top)
                lineTo(rect.right, rect.bottom)
            },
            highlightOutlinePaint,
        )
        highlightOutlinePaint.pathEffect = null
    }

    private fun drawWavyBorder(canvas: Canvas, rect: RectF) {
        val amplitude = 4.5f
        val wavelength = 14f
        drawWavyHorizontalEdge(canvas, rect.left, rect.right, rect.top, amplitude, wavelength)
        drawWavyHorizontalEdge(canvas, rect.left, rect.right, rect.bottom, amplitude, wavelength)
        drawWavyVerticalEdge(canvas, rect.top, rect.bottom, rect.left, amplitude, wavelength)
        drawWavyVerticalEdge(canvas, rect.top, rect.bottom, rect.right, amplitude, wavelength)
    }

    private fun drawWavyHorizontalEdge(
        canvas: Canvas,
        startX: Float,
        endX: Float,
        y: Float,
        amplitude: Float,
        wavelength: Float,
    ) {
        val path = Path().apply {
            moveTo(startX, y)
            var x = startX
            while (x < endX) {
                val mid = (x + wavelength / 2f).coerceAtMost(endX)
                val next = (x + wavelength).coerceAtMost(endX)
                quadTo(mid, y - amplitude, next, y)
                x += wavelength
            }
        }
        canvas.drawPath(path, highlightOutlinePaint)
    }

    private fun drawWavyVerticalEdge(
        canvas: Canvas,
        startY: Float,
        endY: Float,
        x: Float,
        amplitude: Float,
        wavelength: Float,
    ) {
        val path = Path().apply {
            moveTo(x, startY)
            var y = startY
            while (y < endY) {
                val mid = (y + wavelength / 2f).coerceAtMost(endY)
                val next = (y + wavelength).coerceAtMost(endY)
                quadTo(x - amplitude, mid, x, next)
                y += wavelength
            }
        }
        canvas.drawPath(path, highlightOutlinePaint)
    }

    private fun drawBadge(
        canvas: Canvas,
        firstRect: RectF,
    ) {
        val badgeSize = firstRect.height().coerceIn(12f, 18f)
        val badgeLeft = (firstRect.left - badgeSize * 0.5f)
            .coerceAtMost((width - badgeSize).coerceAtLeast(0f))
        val badgeTop = (firstRect.top - badgeSize * 1.0f)
            .coerceIn(0f, (height - badgeSize).coerceAtLeast(0f))
        val badgeRect = RectF(
            badgeLeft,
            badgeTop,
            badgeLeft + badgeSize,
            badgeTop + badgeSize,
        )
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(245, 255, 255, 255)
            style = Paint.Style.FILL
        }
        borderPaint.strokeWidth = max(1.2f, badgeSize * 0.06f)
        canvas.drawRoundRect(badgeRect, badgeSize * 0.24f, badgeSize * 0.24f, fillPaint)
        canvas.drawRoundRect(badgeRect, badgeSize * 0.24f, badgeSize * 0.24f, borderPaint)
        drawGlyph(canvas, badgeRect)
    }

    private fun drawGlyph(
        canvas: Canvas,
        rect: RectF,
    ) {
        when (style.glyph) {
            AnnotationGlyph.Unknown -> drawCenteredText(canvas, rect, "?", 0.76f)
            AnnotationGlyph.Marker -> drawMarker(canvas, rect)
            AnnotationGlyph.Info -> drawInfo(canvas, rect)
            AnnotationGlyph.Question -> drawCenteredText(canvas, rect, "?", 0.8f)
            AnnotationGlyph.Exclamation -> drawCenteredText(canvas, rect, "!", 0.82f)
            AnnotationGlyph.Quotes -> drawQuote(canvas, rect)
            AnnotationGlyph.Language -> drawLanguage(canvas, rect)
        }
    }

    private fun drawCenteredText(canvas: Canvas, rect: RectF, text: String, scale: Float) {
        iconPaint.textSize = min(rect.width(), rect.height()) * scale
        val baseline = rect.centerY() - (iconPaint.descent() + iconPaint.ascent()) / 2f
        canvas.drawText(text, rect.centerX(), baseline, iconPaint)
    }

    private fun drawMarker(canvas: Canvas, rect: RectF) {
        val width = rect.width()
        val height = rect.height()
        val body = RectF(
            rect.left + width * 0.14f,
            rect.top + height * 0.3f,
            rect.left + width * 0.78f,
            rect.top + height * 0.54f,
        )
        canvas.save()
        canvas.rotate(-32f, rect.centerX(), rect.centerY())
        canvas.drawRoundRect(body, width * 0.08f, width * 0.08f, iconPaint)
        val tip = Path().apply {
            moveTo(rect.left + width * 0.78f, rect.top + height * 0.3f)
            lineTo(rect.left + width * 0.96f, rect.top + height * 0.42f)
            lineTo(rect.left + width * 0.78f, rect.top + height * 0.54f)
            close()
        }
        canvas.drawPath(tip, iconPaint)
        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(235, 255, 255, 255)
            strokeWidth = max(1.5f, width * 0.055f)
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(
            rect.left + width * 0.24f,
            rect.top + height * 0.37f,
            rect.left + width * 0.65f,
            rect.top + height * 0.37f,
            stripePaint,
        )
        canvas.restore()
    }

    private fun drawInfo(canvas: Canvas, rect: RectF) {
        val width = rect.width()
        val height = rect.height()
        canvas.drawCircle(rect.left + width * 0.5f, rect.top + height * 0.22f, min(width, height) * 0.11f, iconPaint)
        canvas.drawRoundRect(
            RectF(rect.left + width * 0.36f, rect.top + height * 0.36f, rect.left + width * 0.64f, rect.top + height * 0.74f),
            width * 0.05f,
            width * 0.05f,
            iconPaint,
        )
        canvas.drawRoundRect(
            RectF(rect.left + width * 0.28f, rect.top + height * 0.73f, rect.left + width * 0.72f, rect.top + height * 0.84f),
            width * 0.05f,
            width * 0.05f,
            iconPaint,
        )
    }

    private fun drawQuote(canvas: Canvas, rect: RectF) {
        val path = PathParser.createPathFromPathData(quoteLeftSvgPathData) ?: return
        val margin = min(rect.width(), rect.height()) * 0.08f
        val scale = (min(rect.width(), rect.height()) - margin * 2f) / quoteLeftSvgViewBoxSize
        path.transform(
            Matrix().apply {
                setScale(scale, scale)
                postTranslate(
                    rect.left + (rect.width() - quoteLeftSvgViewBoxSize * scale) / 2f + rect.width() * quoteLeftGlyphOffsetXFactor,
                    rect.top + (rect.height() - quoteLeftSvgViewBoxSize * scale) / 2f,
                )
            }
        )
        canvas.drawPath(path, iconPaint)
    }

    private fun drawLanguage(canvas: Canvas, rect: RectF) {
        val width = rect.width()
        val height = rect.height()
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 32, 32, 32)
            style = Paint.Style.STROKE
            strokeWidth = max(1.4f, width * 0.06f)
            strokeCap = Paint.Cap.ROUND
        }
        val lightFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val leftPanel = Path().apply {
            moveTo(rect.left + width * 0.14f, rect.top + height * 0.26f)
            lineTo(rect.left + width * 0.45f, rect.top + height * 0.16f)
            lineTo(rect.left + width * 0.45f, rect.top + height * 0.72f)
            lineTo(rect.left + width * 0.14f, rect.top + height * 0.82f)
            close()
        }
        val rightPanel = Path().apply {
            moveTo(rect.left + width * 0.55f, rect.top + height * 0.16f)
            lineTo(rect.left + width * 0.86f, rect.top + height * 0.26f)
            lineTo(rect.left + width * 0.86f, rect.top + height * 0.82f)
            lineTo(rect.left + width * 0.55f, rect.top + height * 0.72f)
            close()
        }
        val backFrame = Path().apply {
            moveTo(rect.left + width * 0.24f, rect.top + height * 0.18f)
            lineTo(rect.left + width * 0.5f, rect.top + height * 0.1f)
            lineTo(rect.left + width * 0.76f, rect.top + height * 0.18f)
            lineTo(rect.left + width * 0.76f, rect.top + height * 0.3f)
        }
        canvas.drawPath(backFrame, strokePaint)
        canvas.drawPath(leftPanel, lightFillPaint)
        canvas.drawPath(leftPanel, strokePaint)
        canvas.drawPath(rightPanel, iconPaint)
        canvas.drawPath(rightPanel, strokePaint)
        canvas.drawLine(rect.left + width * 0.28f, rect.top + height * 0.36f, rect.left + width * 0.34f, rect.top + height * 0.36f, strokePaint)
        val glyphPath = Path().apply {
            moveTo(rect.left + width * 0.26f, rect.top + height * 0.44f)
            quadTo(rect.left + width * 0.33f, rect.top + height * 0.52f, rect.left + width * 0.18f, rect.top + height * 0.62f)
            moveTo(rect.left + width * 0.33f, rect.top + height * 0.46f)
            quadTo(rect.left + width * 0.31f, rect.top + height * 0.56f, rect.left + width * 0.22f, rect.top + height * 0.68f)
        }
        canvas.drawPath(glyphPath, strokePaint)
        val aPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = strokePaint.strokeWidth
            strokeCap = Paint.Cap.ROUND
        }
        val aPath = Path().apply {
            moveTo(rect.left + width * 0.62f, rect.top + height * 0.67f)
            lineTo(rect.left + width * 0.7f, rect.top + height * 0.34f)
            lineTo(rect.left + width * 0.78f, rect.top + height * 0.67f)
            moveTo(rect.left + width * 0.65f, rect.top + height * 0.55f)
            lineTo(rect.left + width * 0.75f, rect.top + height * 0.55f)
        }
        canvas.drawPath(aPath, aPaint)
    }

    private fun RectF.toLocalRect(): RectF {
        return RectF(
            width * ((left - overlayBounds.left) / overlayBounds.width()),
            height * ((overlayBounds.bottom - bottom) / overlayBounds.height()),
            width * ((right - overlayBounds.left) / overlayBounds.width()),
            height * ((overlayBounds.bottom - top) / overlayBounds.height()),
        )
    }

    private fun expandedRect(rect: RectF): RectF {
        val padding = rect.height() * 0.3f
        return RectF(
            rect.left - padding,
            rect.top - padding,
            rect.right + padding,
            rect.bottom + padding,
        )
    }

    private fun normalizedRect(rect: RectF): RectF {
        return RectF(
            min(rect.left, rect.right),
            min(rect.top, rect.bottom),
            max(rect.left, rect.right),
            max(rect.top, rect.bottom),
        )
    }
}
