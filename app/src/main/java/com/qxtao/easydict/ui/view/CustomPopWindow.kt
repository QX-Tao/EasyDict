package com.qxtao.easydict.ui.view


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow


/**
 * 自定义PopWindow类，封装了PopWindow的一些常用属性，用Builder模式支持链式调用
 */
class CustomPopWindow private constructor(private val mContext: Context) :
    PopupWindow.OnDismissListener {
    var width = 0
        private set
    var height = 0
        private set
    private var mIsFocusable = true
    private var mIsOutside = true
    private var mResLayoutId = -1
    private var mContentView: View? = null
    var popupWindow: PopupWindow? = null
        private set
    private var mAnimationStyle = -1
    private var mClippEnable = true //default is true
    private var mIgnoreCheekPress = false
    private var mInputMode = -1
    private var mOnDismissListener: PopupWindow.OnDismissListener? = null
    private var mSoftInputMode = -1
    private var mTouchable = true //default is ture
    private var mOnTouchListener: OnTouchListener? = null
    private var mWindow: Window? = null //当前Activity 的窗口

    /**
     * 弹出PopWindow 背景是否变暗，默认不会变暗。
     */
    private var mIsBackgroundDark = false
    private var mBackgroundDrakValue = 0f // 背景变暗的值，0 - 1

    /**
     * 设置是否允许点击 PopupWindow之外的地方，关闭PopupWindow
     */
    private var enableOutsideTouchDisMiss = true // 默认点击pop之外的地方可以关闭

    /**
     *
     * @param anchor
     * @param xOff
     * @param yOff
     * @return
     */
    fun showAsDropDown(anchor: View?, xOff: Int, yOff: Int): CustomPopWindow {
        if (popupWindow != null) {
            popupWindow!!.showAsDropDown(anchor, xOff, yOff)
        }
        return this
    }

    fun showAsDropDown(anchor: View?): CustomPopWindow {
        if (popupWindow != null) {
            popupWindow!!.showAsDropDown(anchor)
        }
        return this
    }

    fun showAsDropDown(anchor: View?, xOff: Int, yOff: Int, gravity: Int): CustomPopWindow {
        if (popupWindow != null) {
            popupWindow!!.showAsDropDown(anchor, xOff, yOff, gravity)
        }
        return this
    }

    /**
     * 相对于父控件的位置（通过设置Gravity.CENTER，下方Gravity.BOTTOM等 ），可以设置具体位置坐标
     * @param parent 父控件
     * @param gravity
     * @param x the popup's x location offset
     * @param y the popup's y location offset
     * @return
     */
    fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int): CustomPopWindow {
        if (popupWindow != null) {
            popupWindow!!.showAtLocation(parent, gravity, x, y)
        }
        return this
    }

    /**
     * 添加一些属性设置
     * @param popupWindow
     */
    private fun apply(popupWindow: PopupWindow) {
        popupWindow.isClippingEnabled = mClippEnable
        if (mIgnoreCheekPress) {
            popupWindow.setIgnoreCheekPress()
        }
        if (mInputMode != -1) {
            popupWindow.inputMethodMode = mInputMode
        }
        if (mSoftInputMode != -1) {
            popupWindow.softInputMode = mSoftInputMode
        }
        if (mOnDismissListener != null) {
            popupWindow.setOnDismissListener(mOnDismissListener)
        }
        if (mOnTouchListener != null) {
            popupWindow.setTouchInterceptor(mOnTouchListener)
        }
        popupWindow.isTouchable = mTouchable
    }

    private fun build(): PopupWindow {
        if (mContentView == null) {
            mContentView = LayoutInflater.from(mContext).inflate(mResLayoutId, null)
        }

        // 获取当前Activity的window
        val activity = mContentView!!.context as Activity
        if (mIsBackgroundDark) {
            //如果设置的值在0 - 1的范围内，则用设置的值，否则用默认值
            val alpha = if (mBackgroundDrakValue > 0 && mBackgroundDrakValue < 1) mBackgroundDrakValue else DEFAULT_ALPHA
            mWindow = activity.window
            val params = mWindow?.attributes as WindowManager.LayoutParams
            params.alpha = alpha
            mWindow?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            mWindow?.setDimAmount(1f)
            mWindow?.attributes = params
        }
        popupWindow = if (width != 0 && height != 0) {
            PopupWindow(mContentView, width, height)
        } else {
            PopupWindow(
                mContentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        if (mAnimationStyle != -1) {
            popupWindow!!.animationStyle = mAnimationStyle
        }
        apply(popupWindow!!) //设置一些属性
        if (width == 0 || height == 0) {
            popupWindow!!.contentView.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
            //如果外面没有设置宽高的情况下，计算宽高并赋值
            width = popupWindow!!.contentView.measuredWidth
            height = popupWindow!!.contentView.measuredHeight
        }

        // 添加dissmiss 监听
        popupWindow!!.setOnDismissListener(this)

        // 判断是否点击PopupWindow之外的地方关闭 popWindow
        if (!enableOutsideTouchDisMiss) {
            //注意这三个属性必须同时设置，不然不能disMiss，以下三行代码在Android 4.4 上是可以，然后在Android 6.0以上，下面的三行代码就不起作用了，就得用下面的方法
            popupWindow!!.isFocusable = true
            popupWindow!!.isOutsideTouchable = false
            popupWindow!!.setBackgroundDrawable(null)
            //注意下面这三个是contentView 不是PopupWindow
            popupWindow!!.contentView.isFocusable = true
            popupWindow!!.contentView.isFocusableInTouchMode = true
            popupWindow!!.contentView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    popupWindow!!.dismiss()
                    return@OnKeyListener true
                }
                false
            })
            //在Android 6.0以上 ，只能通过拦截事件来解决
            popupWindow!!.setTouchInterceptor(object : OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    if (event.action == MotionEvent.ACTION_DOWN
                        && (x < 0 || x >= width || y < 0 || y >= height)
                    ) {
                        Log.e(TAG, "out side ")
                        Log.e(
                            TAG,
                            "width:" + popupWindow!!.width + "height:" + popupWindow!!.height + " x:" + x + " y  :" + y
                        )
                        return true
                    } else if (event.action == MotionEvent.ACTION_OUTSIDE) {
                        Log.e(TAG, "out side ...")
                        return true
                    }
                    return false
                }
            })
        } else {
            popupWindow!!.isFocusable = mIsFocusable
            popupWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            popupWindow!!.isOutsideTouchable = mIsOutside
        }
        // update
        popupWindow!!.update()
        return popupWindow!!
    }

    override fun onDismiss() {
        dissmiss()
    }

    /**
     * 关闭popWindow
     */
    fun dissmiss() {
        if (mOnDismissListener != null) {
            mOnDismissListener!!.onDismiss()
        }
        //如果设置了背景变暗，那么在dissmiss的时候需要还原
        if (mWindow != null) {
            val params = mWindow!!.attributes
            params.alpha = 1.0f
            mWindow!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            mWindow!!.setDimAmount(0f)
            mWindow!!.attributes = params
        }
        if (popupWindow != null && popupWindow!!.isShowing) {
            popupWindow!!.dismiss()
        }
    }

    class PopupWindowBuilder(context: Context) {
        private val mCustomPopWindow: CustomPopWindow

        init {
            mCustomPopWindow = CustomPopWindow(context)
        }

        fun size(width: Int, height: Int): PopupWindowBuilder {
            mCustomPopWindow.width = width
            mCustomPopWindow.height = height
            return this
        }

        fun setFocusable(focusable: Boolean): PopupWindowBuilder {
            mCustomPopWindow.mIsFocusable = focusable
            return this
        }

        fun setView(resLayoutId: Int): PopupWindowBuilder {
            mCustomPopWindow.mResLayoutId = resLayoutId
            mCustomPopWindow.mContentView = null
            return this
        }

        fun setView(view: View?): PopupWindowBuilder {
            mCustomPopWindow.mContentView = view
            mCustomPopWindow.mResLayoutId = -1
            return this
        }

        fun setOutsideTouchable(outsideTouchable: Boolean): PopupWindowBuilder {
            mCustomPopWindow.mIsOutside = outsideTouchable
            return this
        }

        /**
         * 设置弹窗动画
         * @param animationStyle
         * @return
         */
        fun setAnimationStyle(animationStyle: Int): PopupWindowBuilder {
            mCustomPopWindow.mAnimationStyle = animationStyle
            return this
        }

        fun setClippingEnable(enable: Boolean): PopupWindowBuilder {
            mCustomPopWindow.mClippEnable = enable
            return this
        }

        fun setIgnoreCheekPress(ignoreCheekPress: Boolean): PopupWindowBuilder {
            mCustomPopWindow.mIgnoreCheekPress = ignoreCheekPress
            return this
        }

        fun setInputMethodMode(mode: Int): PopupWindowBuilder {
            mCustomPopWindow.mInputMode = mode
            return this
        }

        fun setOnDissmissListener(onDissmissListener: PopupWindow.OnDismissListener?): PopupWindowBuilder {
            mCustomPopWindow.mOnDismissListener = onDissmissListener
            return this
        }

        fun setSoftInputMode(softInputMode: Int): PopupWindowBuilder {
            mCustomPopWindow.mSoftInputMode = softInputMode
            return this
        }

        fun setTouchable(touchable: Boolean): PopupWindowBuilder {
            mCustomPopWindow.mTouchable = touchable
            return this
        }

        fun setTouchIntercepter(touchIntercepter: OnTouchListener?): PopupWindowBuilder {
            mCustomPopWindow.mOnTouchListener = touchIntercepter
            return this
        }

        /**
         * 设置背景变暗是否可用
         * @param isDark
         * @return
         */
        fun enableBackgroundDark(isDark: Boolean): PopupWindowBuilder {
            mCustomPopWindow.mIsBackgroundDark = isDark
            return this
        }

        /**
         * 设置背景变暗的值
         * @param darkValue
         * @return
         */
        fun setBgDarkAlpha(darkValue: Float): PopupWindowBuilder {
            mCustomPopWindow.mBackgroundDrakValue = darkValue
            return this
        }

        /**
         * 设置是否允许点击 PopupWindow之外的地方，关闭PopupWindow
         * @param disMiss
         * @return
         */
        fun enableOutsideTouchableDissmiss(disMiss: Boolean): PopupWindowBuilder {
            mCustomPopWindow.enableOutsideTouchDisMiss = disMiss
            return this
        }

        fun create(): CustomPopWindow {
            //构建PopWindow
            mCustomPopWindow.build()
            return mCustomPopWindow
        }
    }

    companion object {
        private const val TAG = "CustomPopWindow"
        private const val DEFAULT_ALPHA = 0.7f
    }
}