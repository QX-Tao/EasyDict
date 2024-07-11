@file:Suppress("unused")

package com.qxtao.easydict.utils.factory

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Surface
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat


/**
 * 系统深色模式是否开启
 */
val Context.isSystemInDarkMode
    get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

/**
 * 系统深色模式是否没开启
 */
val Context.isNotSystemInDarkMode
    get() = isSystemInDarkMode.not()

/**
 * 获取屏幕宽度
 */
val Context.screenWidth: Int
    get() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            true -> wm.defaultDisplay.getRealSize(point)
            false -> wm.defaultDisplay.getSize(point)
        }
        return point.x
    }

/**
 * 获取屏幕高度
 */
val Context.screenHeight: Int
    get() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            true -> wm.defaultDisplay.getRealSize(point)
            false -> wm.defaultDisplay.getSize(point)
        }
        return point.y
    }

/**
 * 获取应用宽度
 */
val Activity.appWidth: Int
    get() {
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

/**
 * 获取应用高度
 */
val Activity.appHeight: Int
    get() {
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }

/**
 * 判断和设置是否全屏，赋值为true设置成全屏
 */
var Activity.isFullScreen: Boolean
    get() {
        val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        return (window.attributes.flags and flag) == flag
    }
    set(value) {
        if (value) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

/**
 * 主题外观，赋值为true则为黑色
 */
var Activity.isAppearanceLight: Boolean
    get() {
        return WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars
    }
    set(value) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = value
            isAppearanceLightNavigationBars = value
        }
    }

@RequiresApi(Build.VERSION_CODES.P)
fun Activity.edgeToEdge() {
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    window.attributes.layoutInDisplayCutoutMode = WindowManager
        .LayoutParams
        .LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    setWindowEdgeToEdge(this.window)
}

private fun setWindowEdgeToEdge(window: Window) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT
}

fun Activity.hideSystemBars(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        val controller = window.decorView.windowInsetsController
        controller?.hide(WindowInsets.Type.systemBars())
    } else {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}

fun Activity.showSystemBars(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val controller = window.decorView.windowInsetsController
        controller?.show(WindowInsets.Type.systemBars())
    } else {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }
}


/**
 * 是否是竖屏
 */
val Activity.isPortrait: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT


/**
 * 是否是横屏
 */
val Activity.isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * 屏幕旋转角度
 */
val Activity.screenRotation: Int
    get() = when(windowManager.defaultDisplay.rotation){
        Surface.ROTATION_0 -> 0
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }

/**
 * 文本可选择
 */
fun TextView.fixTextSelection() {
    setTextIsSelectable(false)
    post { setTextIsSelectable(true) }
}