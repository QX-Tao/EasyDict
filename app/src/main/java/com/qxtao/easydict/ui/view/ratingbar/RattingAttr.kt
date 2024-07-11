package com.qxtao.easydict.ui.view.ratingbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources

class RattingAttr(
    private val context: Context,
    var starCount: Int,
    var bgDrawable: Int,
    var starDrawable: Int,
    var bgColor: ColorStateList?,
    var secondaryStarColor: ColorStateList?,
    var starColor: ColorStateList?,
    var isKeepOriginColor: Boolean
) {
    val layerList: Array<Drawable>
        get() = arrayOf(
            createLayerDrawableWithTint(bgDrawable, android.R.attr.colorControlHighlight),
            createClippedLayerDrawableWithTint(starDrawable, 0),
            createClippedLayerDrawableWithTint(starDrawable, android.R.attr.colorControlActivated)
        )

    private fun createLayerDrawableWithTint(tileRes: Int, tintAttrRes: Int): Drawable {
        val tintColor = if (isKeepOriginColor) -1 else getColorFromAttrRes(tintAttrRes)
        return createLayerDrawableWithTintColor(tileRes, tintColor)
    }

    private fun createClippedLayerDrawableWithTint(tileResId: Int, tintAttrRes: Int): Drawable {
        val tintColor = if (tintAttrRes == 0) 0 else getColorFromAttrRes(tintAttrRes)
        return ClipDrawable(createLayerDrawableWithTintColor(tileResId, tintColor), Gravity.START, ClipDrawable.HORIZONTAL)
    }

    private fun createLayerDrawableWithTintColor(tileRes: Int, tintColor: Int): Drawable {
        val drawable = TileDrawable(AppCompatResources.getDrawable(context, tileRes)!!)
        drawable.mutate()
        if (tintColor != -1) {
            drawable.setTint(tintColor)
        }
        return drawable
    }

    private fun getColorFromAttrRes(attrRes: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrRes))
        return try {
            typedArray.getColor(0, 0)
        } finally {
            typedArray.recycle()
        }
    }
}

