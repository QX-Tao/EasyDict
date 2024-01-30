package com.qxtao.easydict.application

import android.app.Application
import com.qxtao.easydict.utils.constant.NetConstant
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.XHttpSDK


class EasyDictApplication : Application() {

    override fun onCreate() {
        super.onCreate()

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
    }

}