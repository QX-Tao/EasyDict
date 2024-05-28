@file:Suppress("MemberVisibilityCanBePrivate")

package com.qxtao.easydict.ui.base

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.DynamicColors
import com.qxtao.easydict.R
import com.qxtao.easydict.application.EasyDictApplication
import com.qxtao.easydict.utils.common.ThemeUtils
import com.qxtao.easydict.utils.factory.isNotSystemInDarkMode
import rikka.material.app.MaterialActivity


abstract class BaseActivity<VB : ViewBinding>(open val block:(LayoutInflater)->VB) : MaterialActivity() {

    protected val binding by lazy{ block(layoutInflater) }
    private lateinit var _context: Context
    protected val mContext get() = _context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _context = this
        onCreate1(savedInstanceState)
        EasyDictApplication.instance.addActivity(this)

        /**
         * Display SplashScreen
         * 展示开屏动画
         */
        if (isDisplaySplashScreen()) { installSplashScreen() }
        setContentView(binding.root)
        /**
         * Hide Activity title bar
         * 隐藏系统的标题栏
         */
        supportActionBar?.hide()
        /**
         * Init immersive status bar
         * 初始化沉浸状态栏
         */
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = isNotSystemInDarkMode
            isAppearanceLightNavigationBars = isNotSystemInDarkMode
        }
        /**
         * set Status color
         * 设置状态颜色
         */
        window.setDecorFitsSystemWindows(false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        setStatusColor(
            ResourcesCompat.getColor(resources, R.color.trans, null),
            ResourcesCompat.getColor(resources, R.color.trans, null),
            ResourcesCompat.getColor(resources, R.color.trans, null)
        )
        /**
         * Init children
         * 装载子类
         */
        onCreate()
        bindViews()
        initViews()
        addListener()
    }


    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        if (!ThemeUtils.isSystemAccent(this)) {
            theme.applyStyle(ThemeUtils.getColorThemeStyleRes(this), true)
        }
        theme.applyStyle(rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true)
    }


    /**
     * Callback [onCreate] method
     * 回调 [onCreate] 方法
     */
    abstract fun onCreate()

    /**
     * isDisplaySplashScreen
     * SplashScreen 开屏动画
     */
    protected open fun isDisplaySplashScreen(): Boolean { return false }

    /**
     * setStatusColor
     * 设置状态颜色
     */
    protected open fun setStatusColor(statusBarColor: Int, navigationBarColor: Int, navigationBarDividerColor: Int) {
        setStatusBarColor(statusBarColor)
        setNavigationBarColor(navigationBarColor)
        setNavigationBarDividerColor(navigationBarDividerColor)
    }
    protected open fun setStatusBarColor(statusBarColor: Int) { window?.statusBarColor = statusBarColor }
    protected open fun setNavigationBarColor(navigationBarColor: Int) { window?.navigationBarColor = navigationBarColor }
    protected open fun setNavigationBarDividerColor(navigationBarDividerColor: Int) { window?.navigationBarDividerColor = navigationBarDividerColor }

    /**
     * Callback [bindViews] method
     * 回调 [bindViews] 方法
     */
    protected open fun bindViews(){}

    /**
     * Callback [initViews] method
     * 回调 [initViews] 方法
     */
    protected abstract fun initViews()

    /**
     * Callback [addListener] method
     * 回调 [addListener] 方法
     */
    protected abstract fun addListener()

    /**
     * show short toast
     * Toast通知
     */
    protected fun showShortToast(str: String) {
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show()
    }

    /**
     * show long toast
     * Toast通知
     */
    protected fun showLongToast(str: String) {
        Toast.makeText(mContext, str, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        onDestroy1()
        super.onDestroy()
        EasyDictApplication.instance.removeActivity(this)
    }

    /**
     * Callback [onDestroy] method
     * 回调 [onDestroy] 方法
     */
    protected open fun onDestroy1() {}

    /**
     * Callback [onCreate] method
     * 回调 [onCreate] 方法
     */
    protected open fun onCreate1(savedInstanceState: Bundle?){}
}
