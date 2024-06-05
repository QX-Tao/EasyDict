@file:Suppress("MemberVisibilityCanBePrivate")

package com.qxtao.easydict.ui.base

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
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
        setContentView(binding.root)
        bindViews()
        onCreate1(savedInstanceState)
        EasyDictApplication.instance.addActivity(this)
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.navigationBarDividerColor = Color.TRANSPARENT
        /**
         * Init children
         * 装载子类
         */
        onCreate()
        initViews()
        addListener()
    }


    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        if (!ThemeUtils.isSystemAccent(this)) {
            theme.applyStyle(ThemeUtils.getColorThemeStyleRes(this), true)
        }
    }
    override fun computeUserThemeKey(): String = ThemeUtils.getColorTheme(this)


    /**
     * Callback [onCreate] method
     * 回调 [onCreate] 方法
     */
    abstract fun onCreate()

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
