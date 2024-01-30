package com.qxtao.easydict.ui.view

import android.view.MotionEvent

import android.view.View
import android.view.View.OnTouchListener


class OnDoubleClickListener(
    /**
     * 自定义回调接口
     */
    private val mCallback: DoubleClickCallback?
) :
    OnTouchListener {
    private var count = 0 //点击次数
    private var firstClick: Long = 0 //第一次点击时间
    private var secondClick: Long = 0 //第二次点击时间

    /**
     * 两次点击时间间隔，单位毫秒
     */
    private val totalTime = 1000

    interface DoubleClickCallback {
        fun onDoubleClick()
    }

    /**
     * 触摸事件处理
     *
     * @param v
     * @param event
     * @return
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (MotionEvent.ACTION_DOWN == event.action) { //按下
            count++
            if (1 == count) {
                firstClick = System.currentTimeMillis() //记录第一次点击时间
            } else if (2 == count) {
                secondClick = System.currentTimeMillis() //记录第二次点击时间
                if (secondClick - firstClick < totalTime) { //判断二次点击时间间隔是否在设定的间隔时间之内
                    mCallback?.onDoubleClick()
                    count = 0
                    firstClick = 0
                } else {
                    firstClick = secondClick
                    count = 1
                }
                secondClick = 0
            }
        }
        return true
    }
}