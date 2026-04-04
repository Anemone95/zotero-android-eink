package org.zotero.android.ktx

import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.getAnnotationBlocking
import com.pspdfkit.document.PdfDocument

fun PdfDocument.annotation(page: Int, key: String): Annotation? {
    return annotationProvider.getAnnotationBlocking(page, key)
}
