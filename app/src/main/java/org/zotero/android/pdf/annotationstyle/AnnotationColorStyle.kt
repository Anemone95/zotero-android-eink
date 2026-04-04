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

    private fun normalize(hex: String): String {
        val trimmed = hex.trim().lowercase()
        return if (trimmed.startsWith("#")) trimmed else "#$trimmed"
    }
}
