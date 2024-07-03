package com.qxtao.easydict.utils.common

import android.content.res.Resources
import android.util.TypedValue

object SizeUtils {
    /**
     * dp转px
     */
    fun dp2px(dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    /**
     * sp转px
     */
    fun sp2px(spVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    /**
     * px转dp
     */
    fun px2dp(pxVal: Float): Float {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxVal / scale)
    }

    /**
     * px转sp
     */
    fun px2sp(pxVal: Float): Float {
        return (pxVal / Resources.getSystem().displayMetrics.density)
    }
}