package com.qxtao.easydict.utils.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {
    fun getDayOfMonth(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun getCurrentDateByPattern(pattern: String): String {
        val currentDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        return formatter.format(currentDate)
    }

    fun getFormatDateByYMD(year: Int, month: Int, day: Int, pattern: String): String {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun getSecondTimestampByYMD(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, 0, 0, 0)
        return calendar.timeInMillis / 1000 // 将毫秒级时间戳转换为秒级时间戳
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

    fun calculateDateDifference(givenDate: String, pattern: String): Long {
        val dateFormat =  SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))
        val currentDate = getCurrentDateByPattern(pattern)
        val currentDateTime = dateFormat.parse(currentDate)!!
        val givenDateTime = dateFormat.parse(givenDate)!!
        val differenceInMillis = currentDateTime.time - givenDateTime.time
        return TimeUnit.MILLISECONDS.toDays(differenceInMillis)
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