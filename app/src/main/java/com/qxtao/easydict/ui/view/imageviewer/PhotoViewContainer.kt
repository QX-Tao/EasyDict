package com.qxtao.easydict.ui.view.imageviewer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.customview.widget.ViewDragHelper
import androidx.viewpager.widget.ViewPager
import com.qxtao.easydict.ui.view.imageviewer.photoview.PhotoView
import kotlin.math.abs
import kotlin.math.min

class PhotoViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cb: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(view: View, i: Int): Boolean {
            return !isAnimating
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return 1
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val t = viewPager!!.top + dy / 2
            return if (t >= 0) { min(t, maxOffset) } else { -min(-t, maxOffset) }
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            if (changedView != viewPager) {
                viewPager!!.offsetTopAndBottom(dy)
            }
            val fraction = abs(top) * 1f / maxOffset
            val pageScale = 1 - fraction * .2f
            viewPager!!.scaleX = pageScale
            viewPager!!.scaleY = pageScale
            changedView.scaleX = pageScale
            changedView.scaleY = pageScale
            dragChangeListener?.onDragChange(dy, pageScale, fraction)
        }

        override fun onViewReleased(
            releasedChild: View,
            xvel: Float,
            yvel: Float
        ) {
            super.onViewReleased(releasedChild, xvel, yvel)
            if (abs(releasedChild.top) > hideTopThreshold) {
                dragChangeListener?.onRelease()
            } else {
                dragHelper.smoothSlideViewTo(viewPager!!, 0, 0)
                dragHelper.smoothSlideViewTo(releasedChild, 0, 0)
                this@PhotoViewContainer.postInvalidateOnAnimation()
            }
        }
    }

    private val dragHelper: ViewDragHelper = ViewDragHelper.create(this, cb)
    var viewPager: ViewPager? = null
    private val hideTopThreshold = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics)
    private var maxOffset = 0
    var dragChangeListener: OnDragChangeListener? = null
    /**该参数表示是否正在被拖拽或是否正处于拖拽返回的动画之中*/
    var isAnimating = false
    private var isVertical = false
    private var touchX = 0f
    private  var touchY = 0f

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxOffset = height / 3
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                touchX = ev.x
                touchY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - touchX
                val dy = ev.y - touchY
                viewPager!!.dispatchTouchEvent(ev)
                isVertical = abs(dy) > abs(dx)
                touchX = ev.x
                touchY = ev.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchX = 0f
                touchY = 0f
                isVertical = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isTopOrBottomEnd(): Boolean {
        val photoView: PhotoView? = getCurrentPhotoView()
        return photoView != null && (photoView.attacher.isTopEnd || photoView.attacher.isBottomEnd)
    }

    private fun getCurrentPhotoView(): PhotoView? {
        return (viewPager?.adapter as? ImageViewer.ImageViewerAdapter)?.currentPhotoView
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val result = dragHelper.shouldInterceptTouchEvent(ev)
        if (ev.pointerCount > 1 && ev.action == MotionEvent.ACTION_MOVE) return false
        return if (isTopOrBottomEnd() && isVertical) true else result && isVertical
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount > 1) return false
        try {
            dragHelper.processTouchEvent(ev)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (dragHelper.continueSettling(false)) {
            this@PhotoViewContainer.postInvalidateOnAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAnimating = false
    }
}