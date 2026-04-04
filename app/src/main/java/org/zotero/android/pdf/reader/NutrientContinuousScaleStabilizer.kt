package org.zotero.android.pdf.reader

import android.view.View
import android.view.ViewGroup
import android.widget.Scroller
import com.pspdfkit.ui.PdfFragment
import java.lang.reflect.Field
import java.lang.reflect.Method

internal object NutrientContinuousScaleStabilizer {

    private val documentViewClassNames = listOf(
        "com.pspdfkit.internal.views.document.DocumentView",
        "androidx.compose.runtime.views.document.DocumentView",
    )
    private val pageViewClassNames = listOf(
        "com.pspdfkit.internal.vn",
        "androidx.compose.runtime.vn",
    )
    private val continuousLayoutManagerClassNames = listOf(
        "com.pspdfkit.internal.f8",
        "androidx.compose.runtime.f8",
    )

    private val reflection by lazy(LazyThreadSafetyMode.NONE) { ReflectionCache.create() }

    fun stabilize(pdfFragment: PdfFragment): Boolean {
        val documentView = findDocumentView(pdfFragment.view) ?: return false
        val layoutManager = reflection.documentViewLayoutManagerField.get(documentView) ?: return false
        if (layoutManager.javaClass != reflection.continuousLayoutManagerClass) {
            return false
        }

        val scroller = reflection.layoutManagerScrollerField.get(layoutManager) as? Scroller ?: return false
        val clampedX = clamp(
            scroller.currX,
            reflection.layoutManagerMinXMethod.invoke(layoutManager) as Int,
            reflection.layoutManagerMaxXMethod.invoke(layoutManager) as Int,
        )
        val clampedY = clamp(
            scroller.currY,
            reflection.layoutManagerMinYMethod.invoke(layoutManager) as Int,
            reflection.layoutManagerMaxYMethod.invoke(layoutManager) as Int,
        )

        scroller.forceFinished(true)
        scroller.startScroll(clampedX, clampedY, 0, 0, 0)
        scroller.forceFinished(true)
        reflection.layoutManagerLayoutXField.setInt(layoutManager, clampedX)
        reflection.layoutManagerLayoutYField.setInt(layoutManager, clampedY)

        val currentPage = reflection.layoutManagerCurrentPageMethod.invoke(layoutManager, 0, 0) as Int
        reflection.layoutManagerSetCurrentPageMethod.invoke(layoutManager, currentPage)
        reflection.documentViewNotifyPageChangedMethod.invoke(documentView, currentPage)

        repeat(documentView.childCount) { index ->
            val child = documentView.getChildAt(index)
            reflection.layoutManagerMeasureChildMethod.invoke(
                layoutManager,
                child,
                View.MeasureSpec.EXACTLY,
                View.MeasureSpec.EXACTLY,
            )
            reflection.layoutManagerLayoutChildMethod.invoke(layoutManager, child)
            reflection.pageViewUpdateVisibilityMethod.invoke(child)
        }

        documentView.invalidate()
        documentView.postInvalidateOnAnimation()
        return true
    }

    private fun findDocumentView(view: View?): ViewGroup? {
        if (view == null) {
            return null
        }
        if (view.javaClass == reflection.documentViewClass && view is ViewGroup) {
            return view
        }
        if (view is ViewGroup) {
            repeat(view.childCount) { index ->
                val match = findDocumentView(view.getChildAt(index))
                if (match != null) {
                    return match
                }
            }
        }
        return null
    }

    private fun clamp(value: Int, boundA: Int, boundB: Int): Int {
        val min = minOf(boundA, boundB)
        val max = maxOf(boundA, boundB)
        return value.coerceIn(min, max)
    }

    private data class ReflectionCache(
        val documentViewClass: Class<*>,
        val continuousLayoutManagerClass: Class<*>,
        val documentViewLayoutManagerField: Field,
        val documentViewNotifyPageChangedMethod: Method,
        val layoutManagerScrollerField: Field,
        val layoutManagerLayoutXField: Field,
        val layoutManagerLayoutYField: Field,
        val layoutManagerMinXMethod: Method,
        val layoutManagerMaxXMethod: Method,
        val layoutManagerMinYMethod: Method,
        val layoutManagerMaxYMethod: Method,
        val layoutManagerCurrentPageMethod: Method,
        val layoutManagerSetCurrentPageMethod: Method,
        val layoutManagerMeasureChildMethod: Method,
        val layoutManagerLayoutChildMethod: Method,
        val pageViewUpdateVisibilityMethod: Method,
    ) {
        companion object {
            fun create(): ReflectionCache {
                val documentViewClass = findFirstClass(documentViewClassNames)
                val pageViewClass = findFirstClass(pageViewClassNames)
                val layoutManagerClass = findFirstClass(continuousLayoutManagerClassNames)
                return ReflectionCache(
                    documentViewClass = documentViewClass,
                    continuousLayoutManagerClass = layoutManagerClass,
                    documentViewLayoutManagerField = findField(documentViewClass, "D"),
                    documentViewNotifyPageChangedMethod = findMethod(documentViewClass, "g", Int::class.javaPrimitiveType!!),
                    layoutManagerScrollerField = findField(layoutManagerClass, "H"),
                    layoutManagerLayoutXField = findField(layoutManagerClass, "O"),
                    layoutManagerLayoutYField = findField(layoutManagerClass, "P"),
                    layoutManagerMinXMethod = findMethod(layoutManagerClass, "Q"),
                    layoutManagerMaxXMethod = findMethod(layoutManagerClass, "O"),
                    layoutManagerMinYMethod = findMethod(layoutManagerClass, "R"),
                    layoutManagerMaxYMethod = findMethod(layoutManagerClass, "P"),
                    layoutManagerCurrentPageMethod = findMethod(
                        layoutManagerClass,
                        "b",
                        Int::class.javaPrimitiveType!!,
                        Int::class.javaPrimitiveType!!,
                    ),
                    layoutManagerSetCurrentPageMethod = findMethod(
                        layoutManagerClass.superclass,
                        "j",
                        Int::class.javaPrimitiveType!!,
                    ),
                    layoutManagerMeasureChildMethod = findMethod(
                        layoutManagerClass,
                        "a",
                        pageViewClass,
                        Int::class.javaPrimitiveType!!,
                        Int::class.javaPrimitiveType!!,
                    ),
                    layoutManagerLayoutChildMethod = findMethod(
                        layoutManagerClass,
                        "a",
                        pageViewClass,
                    ),
                    pageViewUpdateVisibilityMethod = findMethod(pageViewClass, "p"),
                )
            }

            private fun findFirstClass(classNames: List<String>): Class<*> {
                var lastError: Throwable? = null
                classNames.forEach { className ->
                    try {
                        return Class.forName(className)
                    } catch (t: Throwable) {
                        lastError = t
                    }
                }
                throw ClassNotFoundException(classNames.joinToString(), lastError)
            }

            private fun findField(clazz: Class<*>, name: String): Field {
                var current: Class<*>? = clazz
                while (current != null) {
                    current.declaredFields.firstOrNull { it.name == name }?.let {
                        it.isAccessible = true
                        return it
                    }
                    current = current.superclass
                }
                throw NoSuchFieldException(name)
            }

            private fun findMethod(clazz: Class<*>?, name: String, vararg parameterTypes: Class<*>): Method {
                var current = clazz
                while (current != null) {
                    current.declaredMethods.firstOrNull { candidate ->
                        candidate.name == name &&
                            candidate.parameterTypes.contentEquals(parameterTypes)
                    }?.let {
                        it.isAccessible = true
                        return it
                    }
                    current = current.superclass
                }
                throw NoSuchMethodException(name)
            }

            private fun findMethod(clazz: Class<*>, name: String, parameterCount: Int): Method {
                var current: Class<*>? = clazz
                while (current != null) {
                    current.declaredMethods.firstOrNull { candidate ->
                        candidate.name == name && candidate.parameterTypes.size == parameterCount
                    }?.let {
                        it.isAccessible = true
                        return it
                    }
                    current = current.superclass
                }
                throw NoSuchMethodException(name)
            }
        }
    }
}
