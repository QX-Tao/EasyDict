package com.qxtao.easydict.utils.common

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color

object ColorUtils {
    fun colorOnSecondaryFixedVariant(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorOnSecondaryFixed)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorPrimary(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorPrimary)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorOnSurface(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorOnSurface)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorSurfaceContainer(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorSurfaceContainer)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorPopupMenuBackground(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.popupMenuBackground)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorOnSurfaceVariant(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorOnSurfaceVariant)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorTertiary(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorTertiary)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorSurface(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorSurface)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorError(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorError)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorOnPrimary(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorOnPrimary)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorSecondary(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorSecondary)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorSecondaryContainer(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorSecondaryContainer)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorOnError(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorOnError)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorSurfaceInverse(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorSurfaceInverse)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorOnSurfaceInverse(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorOnSurfaceInverse)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
    fun colorPrimaryInverse(context: Context): Int {
        val attribute1 = intArrayOf(com.google.android.material.R.attr.colorPrimaryInverse)
        val array1: TypedArray = context.theme.obtainStyledAttributes(attribute1)
        val color1 = array1.getColor(0, Color.TRANSPARENT)
        array1.recycle()
        return color1
    }
}