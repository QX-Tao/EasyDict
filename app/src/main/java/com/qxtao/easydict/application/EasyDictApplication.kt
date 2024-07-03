package com.qxtao.easydict.application

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.qxtao.easydict.ui.activity.bughandler.BugHandlerActivity
import com.qxtao.easydict.utils.common.ThemeUtils
import com.qxtao.easydict.utils.constant.NetConstant
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.XHttpSDK
import kotlin.system.exitProcess


class EasyDictApplication : Application() {
    val activities = mutableListOf<Activity>()

    companion object {
        lateinit var instance: EasyDictApplication
        const val TAG = "EasyDictApplication"
    }

    override fun onCreate() {
        Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
            val exceptionMessage = Log.getStackTraceString(paramThrowable)
            val threadName = Thread.currentThread().name
            Log.e(TAG, "Error on thread $threadName:\n $exceptionMessage")
            activities.forEach { it.finish() }
            val intent = Intent(this, BugHandlerActivity::class.java)
            intent.putExtra("exception_message", exceptionMessage)
            intent.putExtra("thread", threadName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            exitProcess(10)
        }
        super.onCreate()
        instance = this

        /**
         * 网络请求框架
         * XHttp: network request framework
         */
        XHttpSDK.init(this)
        XHttpSDK.setBaseUrl(NetConstant.baseService)
        XHttp.getInstance().setTimeout(60000)

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