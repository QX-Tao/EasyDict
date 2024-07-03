package com.qxtao.easydict.ui.activity.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.qxtao.easydict.adapter.popupmenu.PopupMenuItem
import com.qxtao.easydict.adapter.settings.AppUpdateDataItem
import com.qxtao.easydict.adapter.settings.OpenSourceItem
import com.qxtao.easydict.ui.fragment.settings.SettingsAboutFragment
import com.qxtao.easydict.ui.fragment.settings.SettingsAppUpdateFragment
import com.qxtao.easydict.ui.fragment.settings.SettingsDictCardFragment
import com.qxtao.easydict.ui.fragment.settings.SettingsWelcomeScreenFragment
import com.qxtao.easydict.utils.common.HttpUtils
import com.qxtao.easydict.utils.constant.NetConstant
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_AMBER_GOLD
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_DODGER_BLUE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_HAWTHORN_RED
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_LIME_GREEN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_PERU_BROWN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_SAKURA_PINK
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_SKY_BLUE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_TEAL_GREEN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_VIOLET_PURPLE
import com.qxtao.easydict.utils.constant.ShareConstant.MEI
import com.qxtao.easydict.utils.constant.ShareConstant.MODE_NIGHT_FOLLOW_SYSTEM
import com.qxtao.easydict.utils.constant.ShareConstant.MODE_NIGHT_NO
import com.qxtao.easydict.utils.constant.ShareConstant.MODE_NIGHT_YES
import com.qxtao.easydict.utils.constant.ShareConstant.YING
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class SettingsViewModel : ViewModel() {
    val detailFragmentMap = mapOf(SETTINGS_ABOUT to SettingsAboutFragment::class.java,
        SETTINGS_DICT_CARD to SettingsDictCardFragment::class.java,
        SETTINGS_WELCOME_SCREEN to SettingsWelcomeScreenFragment::class.java,
        SETTINGS_APP_UPDATE to SettingsAppUpdateFragment::class.java)
    val defVoiceMap = linkedMapOf(MEI to "美", YING to "英")
    var defVoiceList = mutableListOf(
        PopupMenuItem(0, defVoiceMap[MEI]!!, true),
        PopupMenuItem(1, defVoiceMap[YING]!!, false)
    )
    val darkModeMap = linkedMapOf(MODE_NIGHT_FOLLOW_SYSTEM to "跟随系统", MODE_NIGHT_YES to "始终开启", MODE_NIGHT_NO to "始终关闭")
    var darkModeList = mutableListOf(
        PopupMenuItem(0, darkModeMap[MODE_NIGHT_FOLLOW_SYSTEM]!!, true),
        PopupMenuItem(1, darkModeMap[MODE_NIGHT_YES]!!, false),
        PopupMenuItem(2, darkModeMap[MODE_NIGHT_NO]!!, false)
    )
    val themeColorMap = linkedMapOf(MATERIAL_DODGER_BLUE to "道奇蓝", MATERIAL_SAKURA_PINK to "樱花粉", MATERIAL_AMBER_GOLD to "琥珀金", MATERIAL_PERU_BROWN to "秘鲁棕",
        MATERIAL_VIOLET_PURPLE to "罗兰紫", MATERIAL_TEAL_GREEN to "水鸭绿", MATERIAL_HAWTHORN_RED to "山楂红", MATERIAL_LIME_GREEN to "青柠绿", MATERIAL_SKY_BLUE to "天空蓝"
    )
    var themeColorList = mutableListOf(
        PopupMenuItem(0, themeColorMap[MATERIAL_DODGER_BLUE]!!, true),
        PopupMenuItem(1, themeColorMap[MATERIAL_SAKURA_PINK]!!, false),
        PopupMenuItem(2, themeColorMap[MATERIAL_AMBER_GOLD]!!, false),
        PopupMenuItem(3, themeColorMap[MATERIAL_PERU_BROWN]!!, false),
        PopupMenuItem(4, themeColorMap[MATERIAL_VIOLET_PURPLE]!!, false),
        PopupMenuItem(5, themeColorMap[MATERIAL_TEAL_GREEN]!!, false),
        PopupMenuItem(6, themeColorMap[MATERIAL_HAWTHORN_RED]!!, false),
        PopupMenuItem(7, themeColorMap[MATERIAL_LIME_GREEN]!!, false),
        PopupMenuItem(8, themeColorMap[MATERIAL_SKY_BLUE]!!, false)
    )

    private val _openSourceItems = MutableLiveData<List<OpenSourceItem>>()
    val openSourceItems: LiveData<List<OpenSourceItem>> = _openSourceItems
    private val _appUpdateDataItems = MutableLiveData<List<AppUpdateDataItem>>()
    val appUpdateDataItems: LiveData<List<AppUpdateDataItem>> = _appUpdateDataItems

    var appUpdateDataLoadInfo = MutableLiveData<Int>() //初始化-1 加载中0 已加载1 加载失败2

    init {
        appUpdateDataLoadInfo.value = -1
    }

    fun getOpenSourceItems() {
        val openSourceItems = mutableListOf<OpenSourceItem>()
        openSourceItems.add(
            OpenSourceItem("Xhttp2 - xuexiangjys",
                "A powerful network request library, encapsulated using the RxJava2 + Retrofit2 + OKHttp combination.",
                "https://github.com/xuexiangjys/XHttp2",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "Retrofit - square",
                "Type-safe HTTP client for Android and Java by Square, Inc.",
                "https://github.com/square/retrofit",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "OkHttp - square",
                "An HTTP & HTTP/2 client for Android and Java applications.",
                "https://github.com/square/okhttp",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "Gson - Google",
                "A Java serialization library that can convert Java Objects into JSON and back.",
                "https://github.com/google/gson",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "flexbox-layout - Google",
                "FlexboxLayout is a library project which brings the similar capabilities of CSS Flexible Box Layout Module to Android.",
                "https://github.com/google/flexbox-layout",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "RxJava - ReactiveX",
                "A library for composing asynchronous and event-based programs using observable sequences for the Java VM.",
                "https://github.com/ReactiveX/RxJava",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "RxAndroid - ReactiveX",
                "RxJava bindings for Android, make writing reactive components in Android applications easy and hassle-free.",
                "https://github.com/ReactiveX/RxAndroid",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "PhotoView - Baseflow",
                "PhotoView aims to help produce an easily usable implementation of a zooming Android ImageView.",
                "https://github.com/Baseflow/PhotoView",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "coil - coil-kt",
                "Image loading for Android backed by Kotlin Coroutines.",
                "https://github.com/coil-kt/coil",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "PermissionsDispatcher - Google",
                "A declarative API to handle Android runtime permissions.",
                "https://github.com/permissions-dispatcher/PermissionsDispatcher",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "AndRatingBar - wdsqjq",
                "A RatingBar library for android, you can customize size, color, spacing and image easily! Support right to left.",
                "https://github.com/wdsqjq/AndRatingBar",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "WebProgress - youlookwhat",
                "An Android WebView progress bar display control, so that its loading progress smooth transition.",
                "https://github.com/youlookwhat/WebProgress",
                "Apache-2.0 License"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "RikkaX - RikkaApps",
                "Libraries used in Rikka apps.",
                "https://github.com/RikkaApps/RikkaX",
                "MIT License"
            )
        )
        _openSourceItems.value = openSourceItems
    }

    fun getAppUpdateData(reload: Boolean = false) {
        if (appUpdateDataLoadInfo.value == 1 && !reload) return
        appUpdateDataLoadInfo.value = 0
        viewModelScope.launch(Dispatchers.IO) {
            val res = async {
                try {
                    val result = CompletableDeferred<Boolean>()
                    val urlBuilder : HttpUrl.Builder = NetConstant.appUpdate.toHttpUrlOrNull()!!.newBuilder()
                    val jsonResponse = HttpUtils.requestResult(urlBuilder.build().toString())
                    val responseJonArray = Gson().fromJson(jsonResponse, JsonArray::class.java)
                    val itemList = mutableListOf<AppUpdateDataItem>()
                    for (responseJsonElement in responseJonArray){
                        val publishedAt = responseJsonElement.asJsonObject.get("published_at").asString
                        val htmlUrl = responseJsonElement.asJsonObject.get("html_url").asString
                        val name = responseJsonElement.asJsonObject.get("name").asString
                        val assets = responseJsonElement.asJsonObject.get("assets").asJsonArray.get(0)
                        val browserDownloadUrl = assets.asJsonObject.get("browser_download_url").asString
                        val body = responseJsonElement.asJsonObject.get("body").asString
                        itemList.add(AppUpdateDataItem(publishedAt, htmlUrl, name, browserDownloadUrl, body))
                    }
                    _appUpdateDataItems.postValue(itemList)
                    result.complete(true)
                    result.await()
                } catch (e: Exception){
                    false
                }
            }
            appUpdateDataLoadInfo.postValue(if (res.await()) 1 else 2)
        }
    }

    fun selectPopupMenuList(list: List<PopupMenuItem>, position: Int): Boolean {
        if (list.find { it.isMenuItemSelected }!!.position == position) return false
        for (element in list) element.isMenuItemSelected = false
        list[position].isMenuItemSelected = true
        return true
    }

    fun selectPopupMenuList(list: List<PopupMenuItem>, menuItemText: String): Boolean {
        if (list.find { it.isMenuItemSelected }!!.menuItemText == menuItemText) return false
        for (element in list) element.isMenuItemSelected = false
        list.find { it.menuItemText == menuItemText }!!.isMenuItemSelected = true
        return true
    }

}
