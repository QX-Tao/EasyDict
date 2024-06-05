@file:Suppress("unused")

package com.qxtao.easydict.utils.common

import android.content.Context

/**
 * @author: PanYiTao
 * @date: 2022/08/16 22:27
 * @description: SharedPreferences Tool.
 */
object ShareUtils {
    private const val PREFERENCE_FILE_NAME = "DefaultPrefs"

    fun putString(mContext: Context?, key: String?, value: String?) {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        sp?.edit()?.putString(key, value)?.apply()
    }

    fun getString(mContext: Context?, key: String?, defValue: String): String {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        return sp?.getString(key, defValue) ?: defValue
    }

    fun putInt(mContext: Context?, key: String?, value: Int) {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        sp?.edit()?.putInt(key, value)?.apply()
    }

    fun getInt(mContext: Context?, key: String?, defValue: Int): Int {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        return sp?.getInt(key, defValue) ?: defValue
    }

    fun putLong(mContext: Context?, key: String?, value: Long) {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        sp?.edit()?.putLong(key, value)?.apply()
    }

    fun getLong(mContext: Context?, key: String?, defValue: Long): Long {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        return sp?.getLong(key, defValue) ?: defValue
    }

    fun putBoolean(mContext: Context?, key: String?, value: Boolean) {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        sp?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(mContext: Context?, key: String?, defValue: Boolean): Boolean {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        return sp?.getBoolean(key, defValue) ?: defValue
    }

    fun delShare(mContext: Context?, key: String?) {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        sp?.edit()?.remove(key)?.apply()
    }

    fun delAll(mContext: Context?) {
        val sp = mContext?.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        sp?.edit()?.clear()?.apply()
    }
}