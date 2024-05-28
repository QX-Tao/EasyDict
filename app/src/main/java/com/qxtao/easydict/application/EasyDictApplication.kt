package com.qxtao.easydict.application

import android.app.Activity
import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.common.ThemeUtils
import com.qxtao.easydict.utils.constant.NetConstant
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.XHttpSDK


class EasyDictApplication : Application() {
    val activities = mutableListOf<Activity>()

    companion object {
        lateinit var instance: EasyDictApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        /**
         * 网络请求框架
         * XHttp: network request framework
         */
        XHttpSDK.init(this)
        XHttpSDK.setBaseUrl(NetConstant.baseService)
        XHttp.getInstance().setTimeout(120000)

        /**
         * 加载SQLCipher库
         * load lib 'SQLCipher'
         */
        System.loadLibrary("sqlcipher")

        /**
         * 深色模式
         * Dark mode
         */
        AppCompatDelegate.setDefaultNightMode(ThemeUtils.getDarkTheme(this))

    }

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }



}