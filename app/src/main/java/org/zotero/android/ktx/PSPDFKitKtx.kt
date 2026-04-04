package org.zotero.android.ktx

import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.FreeTextAnnotation
import com.pspdfkit.annotations.InkAnnotation
import com.pspdfkit.annotations.SquareAnnotation
import org.json.JSONObject
import org.zotero.android.database.objects.AnnotationsConfig
import org.zotero.android.pdf.annotationstyle.AnnotationColorStyles
import java.util.Locale

private val zoteroAnnotationNameRegex =
    Regex("Zotero-([23456789ABCDEFGHIJKLMNPQRSTUVWXYZ]{8})")

var Annotation.key: String?
    get() {
        val customDataKey = this.customData?.opt(AnnotationsConfig.keyKey)?.toString()
        if (!customDataKey.isNullOrBlank()) {
            return customDataKey
        }
        val name = this.name ?: return null
        return zoteroAnnotationNameRegex.find(name)?.groupValues?.getOrNull(1)
    }
    set(newValue) {
        if (this.customData == null) {
            val key = newValue
            if (key != null) {
                this.customData =
                    JSONObject().put(AnnotationsConfig.keyKey, key)
            }
        } else {
            this.customData?.put(AnnotationsConfig.keyKey, newValue)
        }
    }

val Annotation.isZoteroAnnotation: Boolean
    get() {
        return this.key != null
    }

val Annotation.baseColor: String get() {
    val customBaseColor = this.customData?.optString(AnnotationsConfig.baseColorKey)?.takeIf { !it.isNullOrBlank() }
    if (customBaseColor != null) {
        return customBaseColor
    }
    return this.color.let { AnnotationsConfig.colorVariationMap[it] }
        ?: this.color.let { AnnotationsConfig.colorVariationRgbMap[it and 0xFFFFFF] }
        ?: this.color.let { AnnotationColorStyles.closestSupportedHexOrNull(it) }
        ?: String.format(Locale.US, "#%06x", this.color and 0xFFFFFF)
}

val Annotation.shouldRenderPreview: Boolean
    get() {
        return (this is SquareAnnotation) || (this is InkAnnotation) || (this is FreeTextAnnotation)
    }

val Annotation.previewId: String get() {
    return this.key ?: this.uuid
}
