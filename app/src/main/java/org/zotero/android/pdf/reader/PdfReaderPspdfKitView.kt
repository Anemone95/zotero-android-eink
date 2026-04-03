package org.zotero.android.pdf.reader

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import org.zotero.android.R
import org.zotero.android.architecture.ui.CustomLayoutSize
import timber.log.Timber

@Composable
fun PdfReaderPspdfKitView(
    vMInterface: PdfReaderVMInterface,
    isFixedCropModeEnabled: Boolean,
) {
    val activity = LocalActivity.current as? AppCompatActivity ?: return
    val annotationMaxSideSize = annotationMaxSideSize()
    val fragmentManager = activity.supportFragmentManager
    val layoutType = CustomLayoutSize.calculateLayoutType()
    vMInterface.annotationMaxSideSize = annotationMaxSideSize
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val frameLayout = SingleFingerVerticalOnlyFrameLayout(context).apply {
                lockHorizontalSingleFingerPan = isFixedCropModeEnabled
                onDoubleTap = vMInterface::onPdfDoubleTap
                hasActiveAnnotationTool = { vMInterface.activeAnnotationTool != null }
            }

            val containerId = R.id.container
            val fragmentContainerView = FragmentContainerView(context).apply {
                id = containerId
            }
            frameLayout.addView(fragmentContainerView)

            vMInterface.init(
                isTablet = layoutType.isTablet(),
                backgroundColor = backgroundColor,
                containerId = fragmentContainerView.id,
                fragmentManager = fragmentManager,
                annotationMaxSideSize = annotationMaxSideSize
            )
            frameLayout
        },
        update = { frameLayout ->
            (frameLayout as? SingleFingerVerticalOnlyFrameLayout)?.apply {
                lockHorizontalSingleFingerPan = isFixedCropModeEnabled
                onDoubleTap = vMInterface::onPdfDoubleTap
                hasActiveAnnotationTool = { vMInterface.activeAnnotationTool != null }
            }
        }
    )
}

private class SingleFingerVerticalOnlyFrameLayout(
    context: Context,
) : FrameLayout(context) {
    var lockHorizontalSingleFingerPan: Boolean = false
    var onDoubleTap: (() -> Unit)? = null
    var hasActiveAnnotationTool: () -> Boolean = { false }
    private var lockedX: Float? = null
    private var lockedNonFingerX: Float? = null
    private var lockedNonFingerY: Float? = null
    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTap?.invoke()
                return false
            }
        }
    )

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        logInputToolEvent(event)
        if (isFingerEvent(event)) {
            gestureDetector.onTouchEvent(event)
        }

        if (shouldFreezeNonFingerPan(event)) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lockedNonFingerX = event.x
                    lockedNonFingerY = event.y
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    lockedNonFingerX = null
                    lockedNonFingerY = null
                }
            }

            if (event.actionMasked == MotionEvent.ACTION_MOVE && event.pointerCount == 1) {
                val fixedX = lockedNonFingerX ?: event.x
                val fixedY = lockedNonFingerY ?: event.y
                val adjustedEvent = MotionEvent.obtain(event)
                adjustedEvent.setLocation(fixedX, fixedY)
                return try {
                    super.dispatchTouchEvent(adjustedEvent)
                } finally {
                    adjustedEvent.recycle()
                }
            }
        } else if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            lockedNonFingerX = null
            lockedNonFingerY = null
        }

        if (!lockHorizontalSingleFingerPan) {
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                lockedX = null
            }
            return super.dispatchTouchEvent(event)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lockedX = event.x
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount > 1) {
                    lockedX = null
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                lockedX = null
            }
        }

        if (event.actionMasked == MotionEvent.ACTION_MOVE && event.pointerCount == 1) {
            val fixedX = lockedX ?: event.x
            val adjustedEvent = MotionEvent.obtain(event)
            adjustedEvent.setLocation(fixedX, event.y)
            return try {
                super.dispatchTouchEvent(adjustedEvent)
            } finally {
                adjustedEvent.recycle()
            }
        }

        return super.dispatchTouchEvent(event)
    }

    private fun isFingerEvent(event: MotionEvent): Boolean {
        return event.pointerCount > 0 && event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER
    }

    private fun shouldFreezeNonFingerPan(event: MotionEvent): Boolean {
        if (event.pointerCount != 1) {
            return false
        }
        if (hasActiveAnnotationTool()) {
            return false
        }
        return when (event.getToolType(0)) {
            MotionEvent.TOOL_TYPE_STYLUS,
            MotionEvent.TOOL_TYPE_ERASER -> true
            else -> false
        }
    }

    private fun logInputToolEvent(event: MotionEvent) {
        val shouldLog = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_POINTER_UP -> true
            else -> false
        }
        if (!shouldLog) {
            return
        }

        val tools = buildString {
            for (index in 0 until event.pointerCount) {
                if (index > 0) append(", ")
                append(index)
                append("=")
                append(toolTypeName(event.getToolType(index)))
            }
        }

        Timber.d(
            "PDF input: action=%s pointers=%s tools=[%s] source=0x%s buttonState=0x%s",
            actionName(event.actionMasked),
            event.pointerCount,
            tools,
            event.source.toString(16),
            event.buttonState.toString(16),
        )
    }

    private fun actionName(action: Int): String {
        return when (action) {
            MotionEvent.ACTION_DOWN -> "DOWN"
            MotionEvent.ACTION_UP -> "UP"
            MotionEvent.ACTION_CANCEL -> "CANCEL"
            MotionEvent.ACTION_POINTER_DOWN -> "POINTER_DOWN"
            MotionEvent.ACTION_POINTER_UP -> "POINTER_UP"
            else -> action.toString()
        }
    }

    private fun toolTypeName(toolType: Int): String {
        return when (toolType) {
            MotionEvent.TOOL_TYPE_FINGER -> "FINGER"
            MotionEvent.TOOL_TYPE_STYLUS -> "STYLUS"
            MotionEvent.TOOL_TYPE_ERASER -> "ERASER"
            MotionEvent.TOOL_TYPE_MOUSE -> "MOUSE"
            MotionEvent.TOOL_TYPE_UNKNOWN -> "UNKNOWN"
            else -> toolType.toString()
        }
    }
}

@Composable
private fun annotationMaxSideSize(): Int {
    val layoutType = CustomLayoutSize.calculateLayoutType()
    val context = LocalContext.current
    val outValue = TypedValue()
    context.resources.getValue(R.dimen.pdf_sidebar_width_percent, outValue, true)
    val sidebarWidthPercentage = outValue.float
    val metricsWidthPixels = Resources.getSystem().displayMetrics.widthPixels
    val annotationSize = metricsWidthPixels * sidebarWidthPercentage
    val result = annotationSize.toInt()
    if (result <= 0) {
        val errorMessage = "PdfReaderPspdfKitView annotationMaxSideSize is $result" +
                ".sidebarWidthPercentage = $sidebarWidthPercentage" +
                ".metricsWidthPixels = $metricsWidthPixels"
        Timber.e(errorMessage)
        return if (layoutType.isTablet()) {
            480
        } else {
            1080
        }
    }
    return result
}
