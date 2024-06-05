package com.qxtao.easydict.utils.common

import android.icu.text.DecimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream



object FileUtils {
    suspend fun getFileSizes(f: File): Long {
        return withContext(Dispatchers.IO){
            var size: Long = 0
            val flist = f.listFiles()
            if (flist != null) {
                for (i in flist.indices) {
                    size += if (flist[i].isDirectory) {
                        getFileSizes(flist[i])
                    } else {
                        getFileSize(flist[i])
                    }
                }
            }
            size
        }
    }

    private fun getFileSize(file: File): Long {
        var size: Long = 0
        if (file.exists()) {
            var fis: FileInputStream? = null
            fis = FileInputStream(file)
            size = fis.available().toLong()
        } else {
            file.createNewFile()
            return 0
        }
        return size
    }

    fun formatFileSize(fileS: Long): String {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        val wrongSize = "0B"
        if (fileS == 0L) {
            return wrongSize
        }
        fileSizeString = if (fileS < 1024) {
            df.format(fileS.toDouble()) + "B"
        } else if (fileS < 1048576) {
            df.format(fileS.toDouble() / 1024) + "KB"
        } else if (fileS < 1073741824) {
            df.format(fileS.toDouble() / 1048576) + "MB"
        } else {
            df.format(fileS.toDouble() / 1073741824) + "GB"
        }
        return fileSizeString
    }


    suspend fun clearFile(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            if (file.exists()) {
                val files = file.listFiles()
                if (files != null) {
                    for (f in files) {
                        if (f.isDirectory) {
                            deleteDir(f)
                        } else {
                            f.delete()
                        }
                    }
                }
            }
            true
        }
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return dir.delete()
    }

}