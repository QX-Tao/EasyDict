package com.qxtao.easydict.utils.common

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStream

object FileIOUtils {
    fun readFile2String(path: String): String {
        val file = File(path)
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(FileReader(file))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    fun readFile2String(inputStream: InputStream) : String {
        val stringBuilder = StringBuilder()
        val bufferedInputStream = BufferedInputStream(inputStream)
        val buffer = ByteArray(1024)
        var len: Int
        while (bufferedInputStream.read(buffer).also { len = it } != -1) {
            stringBuilder.append(String(buffer, 0, len))
        }
        bufferedInputStream.close()
        return stringBuilder.toString()
    }
}