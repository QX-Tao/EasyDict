package com.qxtao.easydict.utils.common

import android.icu.text.DecimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object FileUtils {
    suspend fun getFileSizes(file: File): Long {
       return withContext(Dispatchers.IO) {
            file.listFiles()?.sumOf { if (it.isDirectory)
                getFileSizes(it) else getFileSize(it) } ?: 0L
        }
    }


    private fun getFileSize(file: File): Long {
        return if (file.exists()) {
            file.length() } else {
            file.createNewFile()
            0L
        }
    }

    fun formatFileSize(fileSize: Long): String {
        val df = DecimalFormat("#.00")
        return when {
            fileSize == 0L -> "0B"
            fileSize < 1024 -> df.format(fileSize.toDouble()) + "B"
            fileSize < 1048576 -> df.format(fileSize.toDouble() / 1024) + "KB"
            fileSize < 1073741824 -> df.format(fileSize.toDouble() / 1048576) + "MB"
            else -> df.format(fileSize.toDouble() / 1073741824) + "GB"
        }
    }


    suspend fun clearFile(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            if (file.exists()) {
                file.listFiles()?.forEach {
                    if (it.isDirectory) {
                        deleteDir(it)
                    } else {
                        it.delete()
                    }
                }
            }
            true
        }
    }

    private fun deleteDir(dir: File): Boolean {
        return if (dir.isDirectory) {
            dir.listFiles()?.all { deleteDir(it) } == true && dir.delete()
        } else {
            dir.delete()
        }
    }

}