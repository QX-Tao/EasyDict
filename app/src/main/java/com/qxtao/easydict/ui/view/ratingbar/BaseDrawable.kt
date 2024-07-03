package com.qxtao.easydict.ui.view.ratingbar

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.PorterDuff.Mode
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

abstract class BaseDrawable : Drawable() {
    var mAlpha: Int = 255
    private var mColorFilter: ColorFilter? = null
    private var mTintList: ColorStateList? = null
    private var mTintMode: Mode? = Mode.SRC_IN
    private var mTintFilter: PorterDuffColorFilter? = null
    private val mConstantState: CustomConstantState = CustomConstantState()

    override fun getAlpha(): Int = mAlpha

    override fun setAlpha(alpha: Int) {
        if (mAlpha != alpha) {
            mAlpha = alpha
            invalidateSelf()
        }
    }

    override fun getColorFilter(): ColorFilter? = mColorFilter

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mColorFilter = colorFilter
        invalidateSelf()
    }

    override fun setTint(@ColorInt tintColor: Int) {
        setTintList(ColorStateList.valueOf(tintColor))
    }

    override fun setTintList(tint: ColorStateList?) {
        mTintList = tint
        if (updateTintFilter()) {
            invalidateSelf()
        }
    }

    override fun setTintMode(tintMode: Mode?) {
        mTintMode = tintMode
        if (updateTintFilter()) {
            invalidateSelf()
        }
    }

    override fun isStateful(): Boolean = mTintList?.isStateful ?: false

    override fun onStateChange(state: IntArray): Boolean = updateTintFilter()

    private fun updateTintFilter(): Boolean {
        mTintFilter = if (mTintList != null && mTintMode != null) {
            PorterDuffColorFilter(mTintList!!.getColorForState(state, 0), mTintMode!!)
        } else {
            null
        }
        return mTintFilter != null
    }

    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        if (bounds.width() != 0 && bounds.height() != 0) {
            val saveCount = canvas.save()
            canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
            onDraw(canvas, bounds.width(), bounds.height())
            canvas.restoreToCount(saveCount)
        }
    }

    protected val colorFilterForDrawing: ColorFilter?
        get() = mColorFilter ?: mTintFilter

    protected abstract fun onDraw(canvas: Canvas, width: Int, height: Int)

    override fun getConstantState(): ConstantState = mConstantState

    private inner class CustomConstantState : ConstantState() {
        override fun getChangingConfigurations(): Int = 0

        override fun newDrawable(): Drawable = this@BaseDrawable
    }
}
