package org.zotero.android.pdf.reader

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
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
                onScaleEnd = vMInterface::onPdfScaleEnd
                isTextSelectionModeActive = { vMInterface.isTextSelectionModeActive }
                onTextSelectionMove = vMInterface::onTextSelectionMove
                onTextSelectionEnd = vMInterface::onTextSelectionEnd
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
                onScaleEnd = vMInterface::onPdfScaleEnd
                isTextSelectionModeActive = { vMInterface.isTextSelectionModeActive }
                onTextSelectionMove = vMInterface::onTextSelectionMove
                onTextSelectionEnd = vMInterface::onTextSelectionEnd
            }
        }
    )
}

private class SingleFingerVerticalOnlyFrameLayout(
    context: Context,
) : FrameLayout(context) {
    var lockHorizontalSingleFingerPan: Boolean = false
    var onDoubleTap: (() -> Boolean)? = null
    var onScaleEnd: (() -> Unit)? = null
    var isTextSelectionModeActive: () -> Boolean = { false }
    var onTextSelectionMove: ((Float, Float) -> Unit)? = null
    var onTextSelectionEnd: (() -> Unit)? = null
    private var lockedX: Float? = null
    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                return onDoubleTap?.invoke() == true
            }
        }
    )
    private val scaleGestureDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var inScaleGesture = false

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                inScaleGesture = true
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                if (inScaleGesture) {
                    this@SingleFingerVerticalOnlyFrameLayout.onScaleEnd?.invoke()
                }
                inScaleGesture = false
            }
        }
    )

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }

        if (event.pointerCount == 1 && isTextSelectionModeActive()) {
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    onTextSelectionMove?.invoke(event.x, event.y)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onTextSelectionEnd?.invoke()
                }
            }
        }

        if (!lockHorizontalSingleFingerPan) {
            val handled = super.dispatchTouchEvent(event)
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                lockedX = null
            }
            scaleGestureDetector.onTouchEvent(event)
            return handled
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
                scaleGestureDetector.onTouchEvent(event)
                adjustedEvent.recycle()
            }
        }

        val handled = super.dispatchTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return handled
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
        return if (layoutType.isTablet()) {
            480
        } else {
            1080
        }
    }
    return result
}
