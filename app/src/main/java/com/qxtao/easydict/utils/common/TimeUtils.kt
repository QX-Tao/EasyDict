package com.qxtao.easydict.utils.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object TimeUtils {
    private fun getTimeZone(): TimeZone = TimeZone.getDefault()
    fun getTimeZoneOffset(): Int = getTimeZone().getOffset(System.currentTimeMillis())

    fun getCurrentDateByPattern(pattern: String): String {
        val currentDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        return formatter.format(currentDate)
    }

    fun getFormatDateByTimeStamp(timeStamp: Long, pattern: String): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(timeStamp)
    }

    fun getTimestampByFormatDate(date: String, pattern: String, timeZone: TimeZone = getTimeZone()): Long {
        val inputFormat = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        inputFormat.timeZone = timeZone
        val calendar = Calendar.getInstance()
        calendar.time = inputFormat.parse(date)!!
        return calendar.timeInMillis
    }

    fun getTimestampByYMD(year: Int, month: Int, day: Int, timeZone: TimeZone = getTimeZone()): Long =
        getTimestampByYMDHMS(year, month, day, 0, 0, 0, timeZone)

    fun getTimestampByYMDHMS(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, timeZone: TimeZone = getTimeZone()): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute, second)
        calendar.timeZone = timeZone
        return calendar.timeInMillis
    }

    fun getFormatDateTimeByPattern(givenDateTime: String, pattern: String, format: String, timeZone: TimeZone = getTimeZone()): String{
        val inputFormat = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        inputFormat.timeZone = timeZone
        val outputFormat = SimpleDateFormat(format, Locale.getDefault(Locale.Category.FORMAT))
        val date = inputFormat.parse(givenDateTime)
        return outputFormat.format(date!!)
    }

    fun calculateDateDifference(givenDate: String, pattern: String, abs: Boolean = true): Long {
        val dateFormat =  SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        val currentDate = getCurrentDateByPattern(pattern)
        val currentDateTime = dateFormat.parse(currentDate)!!
        val givenDateTime = dateFormat.parse(givenDate)!!
        val differenceInMillis = if (abs) abs(givenDateTime.time - currentDateTime.time) else
            givenDateTime.time - currentDateTime.time
        return TimeUnit.MILLISECONDS.toDays(differenceInMillis)
    }

    fun calculateDateDifference(timeStamp: Long, abs: Boolean = true): Long {
        val currentTime = System.currentTimeMillis()
        val differenceInMillis = if (abs) abs(timeStamp - currentTime) else timeStamp - currentTime
        return TimeUnit.MILLISECONDS.toDays(differenceInMillis)
    }

    // 现在时间是否是给定时间戳的未来24小时内
    fun isGivenTimeIn24Hours(timeStamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val differenceInMillis = currentTime - timeStamp
        return differenceInMillis in 0..(24 * 60 * 60 * 1000)
    }

    fun getDateByPatternAndD(d:Int, pattern: String): String{
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -d)
        val yesterdayDate = calendar.time
        val formatter = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        return formatter.format(yesterdayDate)
    }

}