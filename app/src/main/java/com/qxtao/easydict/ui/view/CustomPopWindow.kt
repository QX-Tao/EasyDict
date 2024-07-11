package com.qxtao.easydict.ui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow
import com.qxtao.easydict.utils.common.SizeUtils

class CustomPopWindow private constructor(private val mContext: Context) : PopupWindow.OnDismissListener {

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
    private var mClipEnable = true
    private var mIgnoreCheekPress = false
    private var mInputMode = -1
    private var mOnDismissListener: PopupWindow.OnDismissListener? = null
    private var mSoftInputMode = -1
    private var mTouchable = true
    private var mOnTouchListener: OnTouchListener? = null
    private var mWindow: Window? = null

    private var mIsBackgroundDark = false
    private var mBackgroundDarkValue = 0f

    private var enableOutsideTouchDismiss = true

    fun showAsDropDown(anchor: View?, xOff: Int = 0, yOff: Int = 0, gravity: Int? = null): CustomPopWindow {
        popupWindow?.apply {
            if (gravity != null) {
                showAsDropDown(anchor, xOff, yOff, gravity)
            } else {
                showAsDropDown(anchor, xOff, yOff)
            }
        }
        return this
    }

    fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int): CustomPopWindow {
        popupWindow?.showAtLocation(parent, gravity, x, y)
        return this
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    override fun onDismiss() {
        if (mOnDismissListener != null) {
            mOnDismissListener!!.onDismiss()
        }
        if (mIsBackgroundDark && mWindow != null) {
            val params = mWindow!!.attributes
            params.alpha = 1.0f
            mWindow!!.attributes = params
        }
    }

    private fun build() {
        if (mContentView == null) {
            mContentView = LayoutInflater.from(mContext).inflate(mResLayoutId, null)
        }

        popupWindow = if (width != 0 && height != 0) {
            PopupWindow(mContentView, width, height)
        } else {
            PopupWindow(mContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        popupWindow?.apply {
            isFocusable = mIsFocusable
            isOutsideTouchable = mIsOutside
            isClippingEnabled = mClipEnable
            isTouchable = mTouchable
            elevation = SizeUtils.dp2px(3f).toFloat()

            if (mAnimationStyle != -1) {
                animationStyle = mAnimationStyle
            }

            if (mIgnoreCheekPress) {
                setIgnoreCheekPress()
            }

            if (mInputMode != -1) {
                inputMethodMode = mInputMode
            }

            if (mSoftInputMode != -1) {
                softInputMode = mSoftInputMode
            }

            mOnTouchListener?.let {
                contentView.setOnTouchListener(it)
            }

            setOnDismissListener(this@CustomPopWindow)

            if (!enableOutsideTouchDismiss) {
                setTouchInterceptor { view, event ->
                    if (event.action == MotionEvent.ACTION_OUTSIDE) {
                        view.performClick()
                        return@setTouchInterceptor true
                    }
                    false
                }
            }

            if (mIsBackgroundDark) {
                applyBackgroundDark(mBackgroundDarkValue)
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun applyBackgroundDark(bgDarkAlpha: Float) {
        if (mContext is Activity) {
            mWindow = mContext.window
            val params = mWindow!!.attributes
            params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            params.alpha = bgDarkAlpha
            mWindow!!.attributes = params
        } else {
            Log.e(TAG, "Unable to apply background dark. Context is not an activity.")
        }
    }

    class PopupWindowBuilder(val mContext: Context) {
        private val mCustomPopWindow = CustomPopWindow(mContext)

        fun setSize(width: Int, height: Int) = apply {
            mCustomPopWindow.width = width
            mCustomPopWindow.height = height
        }

        fun setFocusable(focusable: Boolean) = apply {
            mCustomPopWindow.mIsFocusable = focusable
        }

        fun setView(resLayoutId: Int) = apply {
            mCustomPopWindow.mResLayoutId = resLayoutId
            mCustomPopWindow.mContentView = null
        }

        fun setView(view: View?) = apply {
            mCustomPopWindow.mContentView = view
            mCustomPopWindow.mResLayoutId = -1
        }

        fun setOutsideTouchable(outsideTouchable: Boolean) = apply {
            mCustomPopWindow.mIsOutside = outsideTouchable
        }

        fun setAnimationStyle(animationStyle: Int) = apply {
            mCustomPopWindow.mAnimationStyle = animationStyle
        }

        fun setClippingEnable(enable: Boolean) = apply {
            mCustomPopWindow.mClipEnable = enable
        }

        fun setIgnoreCheekPress(ignoreCheekPress: Boolean) = apply {
            mCustomPopWindow.mIgnoreCheekPress = ignoreCheekPress
        }

        fun setInputMethodMode(mode: Int) = apply {
            mCustomPopWindow.mInputMode = mode
        }

        fun setOnDismissListener(onDismissListener: PopupWindow.OnDismissListener?) = apply {
            mCustomPopWindow.mOnDismissListener = onDismissListener
        }

        fun setSoftInputMode(softInputMode: Int) = apply {
            mCustomPopWindow.mSoftInputMode = softInputMode
        }

        fun setTouchable(touchable: Boolean) = apply {
            mCustomPopWindow.mTouchable = touchable
        }

        fun setTouchIntercepter(touchIntercepter: OnTouchListener?) = apply {
            mCustomPopWindow.mOnTouchListener = touchIntercepter
        }

        fun enableBackgroundDark(isDark: Boolean) = apply {
            mCustomPopWindow.mIsBackgroundDark = isDark
        }

        fun setBgDarkAlpha(darkValue: Float) = apply {
            mCustomPopWindow.mBackgroundDarkValue = darkValue
        }

        fun enableOutsideTouchableDismiss(dismiss: Boolean) = apply {
            mCustomPopWindow.enableOutsideTouchDismiss = dismiss
        }

        fun create(): CustomPopWindow {
            mCustomPopWindow.build()
            return mCustomPopWindow
        }
    }

    companion object {
        private const val TAG = "CustomPopWindow"
    }
}
