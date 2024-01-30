package com.qxtao.easydict.ui.activity.photoview.preview

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.github.chrisbanes.photoview.PhotoView
import kotlin.math.abs

class PhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PhotoView(context, attrs, defStyleAttr) {

    // 手指按下的点为(x1, y1) 手指离开屏幕的点为(x2, y2)
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f

    // 拦截向上手势
    private var interceptUpGesture = false

    // 拦截双指手势
    private var interceptPointerCount2Gesture = false

    // 是否拦截事件
    private var intercept = false

    // 透明度回调
    private var mAlphaCallback: AlphaCallback? = null

    // 缩放回调
    private var mScaleCallback: ScaleCallback? = null

    // 用于记录当前图片所在位置
    private var locationX = 0
    private var locationY = 0

    // 用于记录透明度
    private var mAlpha = 1.0f

    // 是否单机关闭
    private var isClickClose = true

    // 最小关闭大小，用于图片缩小到多少后关闭
    private var minSize = 0.5f


    init {
        // 设置最小比例
        minimumScale = 0.3f
        setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (isClickClose && scale >= 1.0f) {
                    if (!interceptUpGesture) {
                        mAlphaCallback?.onChangeClose()
                    }
                }
                return false
            }

            override fun onDoubleTap(ev: MotionEvent): Boolean {
                try {
                    val scale = scale
                    val x = ev.x
                    val y = ev.y
                    if (scale < mediumScale) {
                        setScale(mediumScale, x, y, true)
                    } else if (scale >= mediumScale && scale < maximumScale) {
                        setScale(maximumScale, x, y, true)
                    } else {
                        setScale(1f, x, y, true)
                    }
                } catch (_: ArrayIndexOutOfBoundsException) { return false }
                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                return false
            }

        })
        setOnScaleChangeListener { _, _, _ ->
            mScaleCallback?.onScaleCallback(scale)
        }
    }


    // 这里重写onTouchEvent没有效果，因为PhotoView需要设置onTouchEvent，这样会造成冲突
    @SuppressLint("ObjectAnimatorBinding")
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            // 按下
            MotionEvent.ACTION_DOWN -> {
                interceptUpGesture = false
                // 拦截viewpager2的事件
                parent?.requestDisallowInterceptTouchEvent(true)
                y1 = event.y
                x1 = event.x
                attacher.onTouch(this, event)
            }
            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(true)
                y2 = event.y
                x2 = event.x

                ObjectAnimator.ofInt(this, "locationX", locationX, 0).start()
                ObjectAnimator.ofInt(this, "locationY", locationY, 0).start()

                // 如果比例小于5，并且只有一个触控得情况下，关闭预览
                if (scale < minSize && event.pointerCount == 1) {
                    mAlphaCallback?.onChangeClose()
                    return true
                }

                // 如果是手动放大，这里不做复原
                if (scale < 1.0f) {
                    ObjectAnimator.ofFloat(this,"scale", scale, 1.0f).start()
                }
                ObjectAnimator.ofFloat(this, "malpha", mAlpha, 1.0f).start()
                attacher.onTouch(this, event)
                intercept = false
                interceptPointerCount2Gesture = false

            }
            MotionEvent.ACTION_MOVE -> {
                y2 = event.y
                x2 = event.x
                // 判断为滑动事件 并且需要在1个手指头的时候触发
                if (abs(y1 - y2) > ViewConfiguration.get(context).scaledTouchSlop && event.pointerCount == 1 && scale <= 1.0f && !interceptPointerCount2Gesture) {
                    intercept = true
                    moveAndScale(event, x1 - x2, y1 - y2)
                    return true
                } else if (event.pointerCount > 1 || abs(x1 - x2) > ViewConfiguration.get(context).scaledTouchSlop) {
                    if (!intercept) interceptPointerCount2Gesture = true
                    attacher.onTouch(this, event)
                } else if( scale > 1.0f){
                    attacher.onTouch(this, event)
                }
            }
        }
        return true
    }


    private fun setMalpha(alpha: Float) {
        mAlpha = alpha
        mAlphaCallback?.onChangeAlphaCallback(alpha)
    }

    // 用于动画设置的属性
    private fun setLocationX(x: Int) {
        locationX = x
        scrollTo(locationX, locationY)
    }

    // 用于动画设置的属性
    private fun setLocationY(y: Int) {
        locationY = y
        scrollTo(locationX, locationY)
    }


    // 这里是处理移动、放大缩小、透明度的关键代码
    private fun moveAndScale(event: MotionEvent, fx: Float, fy: Float): Boolean {
        // fx : x1 - x2    fy : y1 - y2
        if (fy > 0){
            interceptUpGesture = true
            return false
        }
        locationX = fx.toInt()
        locationY = fy.toInt()
        scrollTo(locationX, locationY)
        // 滑动比例 计算出大小 越往下越小
        var cale = 1 - abs(fy / height) * 2
        if (cale <= 0.3f) cale = 0.3f
        scale = cale
        setMalpha(cale)
        return false
    }

    /**
     * 设置透明度回调
     * @param mAlphaCallback
     */
    fun setAlphaCallback(mAlphaCallback: AlphaCallback) {
        this.mAlphaCallback = mAlphaCallback
    }

    /**
     * 设置缩放回调
     * @param mScaleCallback
     */
    fun setScaleCallback(mScaleCallback: ScaleCallback) {
        this.mScaleCallback = mScaleCallback
    }

}

