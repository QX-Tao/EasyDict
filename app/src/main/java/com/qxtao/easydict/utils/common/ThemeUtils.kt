package com.qxtao.easydict.utils.common

import android.content.Context
import androidx.annotation.StyleRes
import com.google.android.material.color.DynamicColors
import com.qxtao.easydict.R
import com.qxtao.easydict.utils.constant.ShareConstant.DARK_MODE
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_SYSTEM_THEME
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_AMBER_GOLD
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_HAWTHORN_RED
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_LIME_GREEN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_PERU_BROWN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_SAKURA_PINK
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_SKY_BLUE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_DODGER_BLUE
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_TEAL_GREEN
import com.qxtao.easydict.utils.constant.ShareConstant.MATERIAL_VIOLET_PURPLE
import com.qxtao.easydict.utils.constant.ShareConstant.MODE_NIGHT_FOLLOW_SYSTEM
import com.qxtao.easydict.utils.constant.ShareConstant.SYSTEM
import com.qxtao.easydict.utils.constant.ShareConstant.THEME_COLOR


object ThemeUtils {

    private val colorThemeMap: MutableMap<String, Int> = hashMapOf(
        MATERIAL_DODGER_BLUE to R.style.ThemeOverlay_MaterialDodgerBlue,
        MATERIAL_SAKURA_PINK to R.style.ThemeOverlay_MaterialSakuraPink,
        MATERIAL_AMBER_GOLD to R.style.ThemeOverlay_MaterialAmberGold,
        MATERIAL_PERU_BROWN to R.style.ThemeOverlay_MaterialPeruBrown,
        MATERIAL_VIOLET_PURPLE to R.style.ThemeOverlay_MaterialVioletPurple,
        MATERIAL_TEAL_GREEN to R.style.ThemeOverlay_MaterialTealGreen,
        MATERIAL_HAWTHORN_RED to R.style.ThemeOverlay_MaterialHawthornRed,
        MATERIAL_LIME_GREEN to R.style.ThemeOverlay_MaterialLimeGreen,
        MATERIAL_SKY_BLUE to R.style.ThemeOverlay_MaterialSkyBlue
    )

    fun isDynamicColorAvailable(): Boolean = DynamicColors.isDynamicColorAvailable()

    fun isSystemAccent(context: Context): Boolean =
        DynamicColors.isDynamicColorAvailable() && ShareUtils.getBoolean(context, IS_USE_SYSTEM_THEME, false)

    @StyleRes
    fun getColorThemeStyleRes(context: Context): Int =
        colorThemeMap[getColorTheme(context)] ?: R.style.ThemeOverlay_MaterialDodgerBlue
    fun getColorTheme(context: Context): String =
        if (isSystemAccent(context)) SYSTEM else ShareUtils.getString(context, THEME_COLOR, MATERIAL_DODGER_BLUE)
    fun getThemeColor(context: Context): String = ShareUtils.getString(context, THEME_COLOR, MATERIAL_DODGER_BLUE)

    fun getDarkTheme(context: Context): Int = ShareUtils.getInt(context, DARK_MODE, MODE_NIGHT_FOLLOW_SYSTEM)


    fun setDarkTheme(context: Context, mode: Int) = ShareUtils.putInt(context, DARK_MODE, mode)
    fun setThemeColor(context: Context, themeColor: String) = ShareUtils.putString(context, THEME_COLOR, themeColor)


}