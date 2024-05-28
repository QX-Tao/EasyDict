@file:Suppress("unused")

package com.qxtao.easydict.utils.common

import android.content.Context
import androidx.annotation.StyleRes
import com.google.android.material.color.DynamicColors
import com.qxtao.easydict.R
import com.qxtao.easydict.utils.constant.ShareConstant.DARK_MODE
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_SYSTEM_THEME
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
import com.qxtao.easydict.utils.constant.ShareConstant.SYSTEM
import com.qxtao.easydict.utils.constant.ShareConstant.THEME_COLOR


object ThemeUtils {

    private val colorThemeMap: MutableMap<String, Int> = hashMapOf(
        MATERIAL_AMBER to R.style.ThemeOverlay_MaterialAmber,
        MATERIAL_BLUE to R.style.ThemeOverlay_MaterialBlue,
        MATERIAL_BLUE_GREY to R.style.ThemeOverlay_MaterialBlueGrey,
        MATERIAL_BROWN to R.style.ThemeOverlay_MaterialBrown,
        MATERIAL_CYAN to R.style.ThemeOverlay_MaterialCyan,
        MATERIAL_DEEP_ORANGE to R.style.ThemeOverlay_MaterialDeepOrange,
        MATERIAL_DEEP_PURPLE to R.style.ThemeOverlay_MaterialDeepPurple,
        MATERIAL_GREEN to R.style.ThemeOverlay_MaterialGreen,
        MATERIAL_INDIGO to R.style.ThemeOverlay_MaterialIndigo,
        MATERIAL_LIGHT_BLUE to R.style.ThemeOverlay_MaterialLightBlue,
        MATERIAL_LIGHT_GREEN to R.style.ThemeOverlay_MaterialLightGreen,
        MATERIAL_LIME to R.style.ThemeOverlay_MaterialLime,
        MATERIAL_ORANGE to R.style.ThemeOverlay_MaterialOrange,
        MATERIAL_PINK to R.style.ThemeOverlay_MaterialPink,
        MATERIAL_PURPLE to R.style.ThemeOverlay_MaterialPurple,
        MATERIAL_RED to R.style.ThemeOverlay_MaterialRed,
        MATERIAL_SAKURA to R.style.ThemeOverlay_MaterialSakura,
        MATERIAL_TEAL to R.style.ThemeOverlay_MaterialTeal,
        MATERIAL_YELLOW to R.style.ThemeOverlay_MaterialYellow
    )

    fun isDynamicColorAvailable(): Boolean = DynamicColors.isDynamicColorAvailable()

    fun isSystemAccent(context: Context): Boolean =
        DynamicColors.isDynamicColorAvailable() && ShareUtils.getBoolean(context, IS_USE_SYSTEM_THEME, false)

    @StyleRes
    fun getColorThemeStyleRes(context: Context): Int =
        colorThemeMap[getColorTheme(context)] ?: R.style.ThemeOverlay_MaterialBlue
    private fun getColorTheme(context: Context): String? =
        if (isSystemAccent(context)) SYSTEM else ShareUtils.getString(context, THEME_COLOR, MATERIAL_BLUE)
    fun getThemeColor(context: Context): String? = ShareUtils.getString(context, THEME_COLOR, MATERIAL_BLUE)

    fun getDarkTheme(context: Context): Int = ShareUtils.getInt(context, DARK_MODE, MODE_NIGHT_FOLLOW_SYSTEM)


    fun setDarkTheme(context: Context, mode: Int) = ShareUtils.putInt(context, DARK_MODE, mode)
    fun setThemeColor(context: Context, themeColor: String) = ShareUtils.putString(context, THEME_COLOR, themeColor)


}