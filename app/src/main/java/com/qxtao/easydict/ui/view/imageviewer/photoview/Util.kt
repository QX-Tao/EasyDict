package com.qxtao.easydict.ui.view.imageviewer.photoview

import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ImageView.ScaleType

internal object Util {
    fun checkZoomLevels(minZoom: Float, midZoom: Float, maxZoom: Float) {
        require(!(minZoom >= midZoom) || !(midZoom >= maxZoom)) {
            "Minimum zoom has to be less than Medium zoom. Call setMinimumZoom() with a more appropriate value"
        }
    }

    fun hasDrawable(imageView: ImageView): Boolean {
        return imageView.drawable != null
    }

    fun isSupportedScaleType(scaleType: ScaleType): Boolean {
        return scaleType != ScaleType.MATRIX
    }

    fun getPointerIndex(action: Int): Int {
        return (action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
    }
}