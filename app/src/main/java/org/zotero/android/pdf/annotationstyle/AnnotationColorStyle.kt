package org.zotero.android.pdf.annotationstyle

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

internal enum class AnnotationGlyph {
    Unknown,
    Marker,
    Info,
    Question,
    Exclamation,
    Quotes,
    Language,
}

internal const val quoteLeftSvgViewBoxSize = 640f
internal const val quoteLeftGlyphOffsetXFactor = 0.045f
internal const val quoteLeftSvgPathData =
    "M96 280C96 213.7 149.7 160 216 160L224 160C241.7 160 256 174.3 256 192C256 209.7 241.7 224 224 224L216 224C185.1 224 160 249.1 160 280L160 288L224 288C259.3 288 288 316.7 288 352L288 416C288 451.3 259.3 480 224 480L160 480C124.7 480 96 451.3 96 416L96 280zM352 280C352 213.7 405.7 160 472 160L480 160C497.7 160 512 174.3 512 192C512 209.7 497.7 224 480 224L472 224C441.1 224 416 249.1 416 280L416 288L480 288C515.3 288 544 316.7 544 352L544 416C544 451.3 515.3 480 480 480L416 480C380.7 480 352 451.3 352 416L352 280z"

internal enum class AnnotationTexture {
    None,
    Horizontal,
    RisingDiagonal,
    FallingDiagonal,
    Dots,
    Crosshatch,
    Grid,
}

internal data class AnnotationColorStyle(
    val hex: String,
    val glyph: AnnotationGlyph,
    val texture: AnnotationTexture,
)

internal object AnnotationColorStyles {
    private val unknownStyle = AnnotationColorStyle(
        hex = "#ffffff",
        glyph = AnnotationGlyph.Unknown,
        texture = AnnotationTexture.None,
    )

    private val styles = listOf(
        AnnotationColorStyle(
            hex = "#ffd400",
            glyph = AnnotationGlyph.Marker,
            texture = AnnotationTexture.Horizontal,
        ),
        AnnotationColorStyle(
            hex = "#5fb236",
            glyph = AnnotationGlyph.Info,
            texture = AnnotationTexture.RisingDiagonal,
        ),
        AnnotationColorStyle(
            hex = "#ff6666",
            glyph = AnnotationGlyph.Question,
            texture = AnnotationTexture.FallingDiagonal,
        ),
        AnnotationColorStyle(
            hex = "#f19837",
            glyph = AnnotationGlyph.Exclamation,
            texture = AnnotationTexture.Dots,
        ),
        AnnotationColorStyle(
            hex = "#a28ae5",
            glyph = AnnotationGlyph.Quotes,
            texture = AnnotationTexture.Crosshatch,
        ),
        AnnotationColorStyle(
            hex = "#aaaaaa",
            glyph = AnnotationGlyph.Language,
            texture = AnnotationTexture.Grid,
        ),
    )

    val pickerColors: List<String> = styles.map { it.hex }

    fun styleFor(hex: String): AnnotationColorStyle {
        val normalized = normalize(hex)
        return styles.firstOrNull { it.hex == normalized } ?: unknownStyle
    }

    fun isSupported(hex: String): Boolean {
        val normalized = normalize(hex)
        return styles.any { it.hex == normalized }
    }

    fun subtleFill(hex: String): Color {
        val style = styleFor(hex)
        if (style.glyph == AnnotationGlyph.Unknown) {
            return Color.White
        }
        val color = Color(style.hex.toColorInt())
        return color.copy(alpha = 0.16f)
    }

    fun closestSupportedHexOrNull(colorInt: Int, maxDistance: Int = 72): String? {
        val target = colorInt and 0xFFFFFF
        val nearest = styles.minByOrNull { distanceSquared(it.hex.toColorInt() and 0xFFFFFF, target) } ?: return null
        val distance = kotlin.math.sqrt(distanceSquared(nearest.hex.toColorInt() and 0xFFFFFF, target).toDouble())
        return nearest.hex.takeIf { distance <= maxDistance }
    }

    private fun normalize(hex: String): String {
        val trimmed = hex.trim().lowercase()
        return if (trimmed.startsWith("#")) trimmed else "#$trimmed"
    }

    private fun distanceSquared(a: Int, b: Int): Int {
        val ar = (a shr 16) and 0xFF
        val ag = (a shr 8) and 0xFF
        val ab = a and 0xFF
        val br = (b shr 16) and 0xFF
        val bg = (b shr 8) and 0xFF
        val bb = b and 0xFF
        val dr = ar - br
        val dg = ag - bg
        val db = ab - bb
        return dr * dr + dg * dg + db * db
    }
}
