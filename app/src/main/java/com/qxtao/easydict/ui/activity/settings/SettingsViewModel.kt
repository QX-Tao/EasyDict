package com.qxtao.easydict.ui.activity.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.qxtao.easydict.adapter.popupmenu.PopupMenuItem
import com.qxtao.easydict.adapter.settings.OpenSourceItem
import com.qxtao.easydict.ui.fragment.settings.SettingsAboutFragment
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_AMBER
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_BLUE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_BLUE_GREY
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_BROWN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_CYAN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_DEEP_ORANGE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_DEEP_PURPLE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_GREEN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_INDIGO
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_LIGHT_BLUE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_LIGHT_GREEN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_LIME
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_ORANGE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_PINK
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_PURPLE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_RED
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_SAKURA
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_TEAL
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_YELLOW
import com.qxtao.easydict.utils.constant.ShareConstant.MODE_NIGHT_FOLLOW_SYSTEM
import com.qxtao.easydict.utils.constant.ShareConstant.MODE_NIGHT_NO
import com.qxtao.easydict.utils.constant.ShareConstant.MODE_NIGHT_YES

class SettingsViewModel : ViewModel() {
    val detailFragmentMap = mapOf(SETTINGS_KEY_ABOUT to SettingsAboutFragment::class.java)
    val darkModeMap = linkedMapOf(MODE_NIGHT_FOLLOW_SYSTEM to "跟随系统", MODE_NIGHT_YES to "始终开启", MODE_NIGHT_NO to "始终关闭")
    var darkModeList = mutableListOf(
        PopupMenuItem(0, darkModeMap[MODE_NIGHT_FOLLOW_SYSTEM]!!, true),
        PopupMenuItem(1, darkModeMap[MODE_NIGHT_YES]!!, false),
        PopupMenuItem(2, darkModeMap[MODE_NIGHT_NO]!!, false)
    )
    val themeColorMap = linkedMapOf(MATERIAL_SAKURA to "樱花粉", MATERIAL_RED to "山楂红", MATERIAL_PINK to "热情粉", MATERIAL_PURPLE to "兰花紫",
        MATERIAL_DEEP_PURPLE to "罗兰紫", MATERIAL_BLUE to "板岩蓝", MATERIAL_INDIGO to "海洋蓝", MATERIAL_LIGHT_BLUE to "天空蓝", MATERIAL_CYAN to "蜜瓜青",
        MATERIAL_TEAL to "水鸭绿", MATERIAL_GREEN to "胡杨绿", MATERIAL_LIGHT_GREEN to "薄荷绿", MATERIAL_LIME to "柠檬青", MATERIAL_YELLOW to "金桔黄",
        MATERIAL_AMBER to "琥珀金", MATERIAL_ORANGE to "太阳橙", MATERIAL_DEEP_ORANGE to "晚霞橙", MATERIAL_BROWN to "秘鲁棕", MATERIAL_BLUE_GREY to "长城灰"
    )
    var themeColorList = mutableListOf(
        PopupMenuItem(0, themeColorMap[MATERIAL_SAKURA]!!, false),
        PopupMenuItem(1, themeColorMap[MATERIAL_RED]!!, false),
        PopupMenuItem(2, themeColorMap[MATERIAL_PINK]!!, false),
        PopupMenuItem(3, themeColorMap[MATERIAL_PURPLE]!!, false),
        PopupMenuItem(4, themeColorMap[MATERIAL_DEEP_PURPLE]!!, false),
        PopupMenuItem(5, themeColorMap[MATERIAL_BLUE]!!, true),
        PopupMenuItem(6, themeColorMap[MATERIAL_INDIGO]!!, false),
        PopupMenuItem(7, themeColorMap[MATERIAL_LIGHT_BLUE]!!, false),
        PopupMenuItem(8, themeColorMap[MATERIAL_CYAN]!!, false),
        PopupMenuItem(9, themeColorMap[MATERIAL_TEAL]!!, false),
        PopupMenuItem(10, themeColorMap[MATERIAL_GREEN]!!, false),
        PopupMenuItem(11, themeColorMap[MATERIAL_LIGHT_GREEN]!!, false),
        PopupMenuItem(12, themeColorMap[MATERIAL_LIME]!!, false),
        PopupMenuItem(13, themeColorMap[MATERIAL_YELLOW]!!, false),
        PopupMenuItem(14, themeColorMap[MATERIAL_AMBER]!!, false),
        PopupMenuItem(15, themeColorMap[MATERIAL_ORANGE]!!, false),
        PopupMenuItem(16, themeColorMap[MATERIAL_DEEP_ORANGE]!!, false),
        PopupMenuItem(17, themeColorMap[MATERIAL_BROWN]!!, false),
        PopupMenuItem(18, themeColorMap[MATERIAL_BLUE_GREY]!!, false)
    )

    private val _openSourceItems = MutableLiveData<List<OpenSourceItem>>()
    val openSourceItems: LiveData<List<OpenSourceItem>> = _openSourceItems


    fun getOpenSourceItems() {
        val openSourceItems = mutableListOf<OpenSourceItem>()
        openSourceItems.add(
            OpenSourceItem("Xhttp2 - xuexiangjys",
                "A powerful network request library, encapsulated using the RxJava2 + Retrofit2 + OKHttp combination.",
                "https://github.com/xuexiangjys/XHttp2",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "Retrofit - square",
                "Type-safe HTTP client for Android and Java by Square, Inc.",
                "https://github.com/square/retrofit",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "OkHttp - square",
                "An HTTP & HTTP/2 client for Android and Java applications.",
                "https://github.com/square/okhttp",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "Gson - Google",
                "A Java serialization library that can convert Java Objects into JSON and back.",
                "https://github.com/google/gson",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "flexbox-layout - Google",
                "FlexboxLayout is a library project which brings the similar capabilities of CSS Flexible Box Layout Module to Android.",
                "https://github.com/google/flexbox-layout",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "RxJava - ReactiveX",
                "RxJava - Reactive Extensions for the JVM - a library for composing asynchronous and event-based programs using observable sequences for the Java VM.",
                "https://github.com/ReactiveX/RxJava",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "RxAndroid - ReactiveX",
                "RxJava bindings for Android.",
                "https://github.com/ReactiveX/RxAndroid",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "PhotoView - Baseflow",
                "PhotoView aims to help produce an easily usable implementation of a zooming Android ImageView.",
                "https://github.com/Baseflow/PhotoView",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "coil - coil-kt",
                "Image loading for Android backed by Kotlin Coroutines.",
                "https://github.com/coil-kt/coil",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "autofittextview - grantland",
                "A TextView that automatically resizes text to fit perfectly within its bounds.",
                "https://github.com/grantland/android-autofittextview",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "sqlcipher - sqlcipher",
                "SQLCipher is a standalone fork of SQLite that adds 256 bit AES encryption of database files and other security features.",
                "https://github.com/sqlcipher/sqlcipher",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "PermissionsDispatcher - Google",
                "A declarative API to handle Android runtime permissions.",
                "https://github.com/permissions-dispatcher/PermissionsDispatcher",
                "Apache License 2.0"
            )
        )
        openSourceItems.add(
            OpenSourceItem(
                "AndRatingBar - wdsqjq",
                "A RatingBar library for android, you can customize size ,color ,spacing and image easily!Support right to left.",
                "https://github.com/wdsqjq/AndRatingBar",
                "Apache License 2.0"
            )
        )
        _openSourceItems.value = openSourceItems
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
