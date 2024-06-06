package com.qxtao.easydict.utils.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object TimeUtils {
    fun getTimeZone(): TimeZone = TimeZone.getDefault()
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

    fun getFormatDateByPattern(givenDate: String, pattern: String, format: String): String {
        val inputFormat =SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        val outputFormat = SimpleDateFormat(format, Locale.getDefault(Locale.Category.FORMAT))
        val date = inputFormat.parse(givenDate)
        return outputFormat.format(date!!)
    }


    fun getCurrentYear(): Int{
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

    fun getCurrentMonth(): Int{
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH) + 1
    }

    fun getCurrentDay(): Int{
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun getNowMills(): Long{
        return System.currentTimeMillis()
    }

    fun getCurrentDayOfWeek(): Int{
        // Calendar.SUNDAY：代表星期日，具有值1。
        // Calendar.MONDAY：代表星期一，具有值2。
        // Calendar.TUESDAY：代表星期二，具有值3。
        // Calendar.WEDNESDAY：代表星期三，具有值4。
        // Calendar.THURSDAY：代表星期四，具有值5。
        // Calendar.FRIDAY：代表星期五，具有值6。
        // Calendar.SATURDAY：代表星期六，具有值7。
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    fun getGivenDayOfWeek(year: Int, month: Int, day: Int): Int{
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    fun getFormerlyDateByPattern(pattern: String, startDate: String, num: Int): ArrayList<String> {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        val calendar = Calendar.getInstance()
        val dateList = ArrayList<String>()
        val startDateObj = dateFormat.parse(startDate)
        if (startDateObj != null) {
            calendar.time = startDateObj
        }
        for (i in 0 until num) {
            val formattedDate = dateFormat.format(calendar.time)
            dateList.add(formattedDate)
            calendar.add(Calendar.DATE, -1) // Subtract 1 day
        }
        return dateList
    }

    fun getTomorrowDateByPattern(pattern: String): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowDate = calendar.time
        val formatter = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        return formatter.format(tomorrowDate)
    }

    fun getYesterdayDateByPattern(pattern: String): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate = calendar.time
        val formatter = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        return formatter.format(yesterdayDate)
    }

    fun getDayBeforeGivenDateByPattern(pattern: String, givenDate: String): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        val calendar = Calendar.getInstance()
        calendar.time = formatter.parse(givenDate)!!
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate = calendar.time
        return formatter.format(yesterdayDate)
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

    fun getFormerlyDateByPatternDateAndD(givenDate: String, d:Int, pattern: String, format: String): String{
        val givenDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        val formattedGivenDate = givenDateFormat.parse(givenDate)
        val calendar = Calendar.getInstance()
        calendar.time = formattedGivenDate!!
        calendar.add(Calendar.DATE, -d)
        val outputDateFormat = SimpleDateFormat(format, Locale.getDefault())
        return outputDateFormat.format(calendar.time)
    }

    fun minutesToMilliseconds(minutes: Int): Long {
        val millisecondsPerMinute = 60 * 1000
        return minutes * millisecondsPerMinute.toLong()
    }

    fun minutesToSeconds(minutes: Int): Long {
        val secondsPerMinute = 60
        return minutes * secondsPerMinute.toLong()
    }

    fun millisecondsToHours(milliseconds: Long): Long {
        val millisecondsPerHour = 60 * 60 * 1000
        return milliseconds / millisecondsPerHour
    }

    fun millisecondsToMinutes(milliseconds: Long): Long {
        val millisecondsPerMinute = 60 * 1000
        return (milliseconds % (60 * 60 * 1000)) / millisecondsPerMinute
    }

    fun millisecondsToSeconds(milliseconds: Long): Long {
        val millisecondsPerSecond = 1000
        return (milliseconds % (60 * 1000)) / millisecondsPerSecond
    }

    fun secondsToHours(seconds: Long): Long {
        val secondsPerHour = 60 * 60
        return seconds / secondsPerHour
    }

    fun secondsToMinutes(seconds: Long): Long {
        val secondsPerMinute = 60
        return (seconds % (60 * 60)) / secondsPerMinute
    }

    fun secondsToSeconds(seconds: Long): Long {
        val secondsPerSecond = 1
        return (seconds % (60)) / secondsPerSecond
    }
}