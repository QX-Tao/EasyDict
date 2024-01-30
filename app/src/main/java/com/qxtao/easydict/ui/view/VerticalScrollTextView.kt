package com.qxtao.easydict.ui.view

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

class VerticalScrollTextView : AppCompatTextView {

    private var mNeedScrollFlag = false
    private var mLastScrollFlag = false
    private var mLastY = 0F

    constructor(context : Context) : this(context, null)
    constructor(context : Context, attrs : AttributeSet?) : this(context, attrs, 0)
    constructor(context : Context, attrs : AttributeSet?, defStyleAttr : Int) : super(context, attrs, defStyleAttr) {
        isVerticalScrollBarEnabled = true
        movementMethod = ScrollingMovementMethod.getInstance()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isNeedScroll()) {
                        mNeedScrollFlag = true
                        mLastY = rawY
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mNeedScrollFlag and (rawY != mLastY)) {
                        val orientation = if (rawY - mLastY > 0) -1 else 1
                        val scrollFlag = canScrollVertically(orientation)
                        if (scrollFlag != mLastScrollFlag) {
                            parent.requestDisallowInterceptTouchEvent(scrollFlag)
                            mLastScrollFlag = scrollFlag
                            if (!scrollFlag) mNeedScrollFlag = false
                        }
                        mLastY = rawY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mNeedScrollFlag = false
                    mLastScrollFlag = false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isNeedScroll() : Boolean {
        return ((maxLines > 0) and (maxLines < lineCount)) or ((maxHeight > 0) and (maxHeight <= height))
    }

}
