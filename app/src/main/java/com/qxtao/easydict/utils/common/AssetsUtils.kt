package com.qxtao.easydict.utils.common

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


object AssetsUtils {
    /**
     * 解压assets目录下的zip到指定的路径
     * @param zipFileString ZIP的名称，压缩包的名称：xxx.zip
     * @param outPathString 要解压缩路径
     * @throws Exception
     */
    suspend fun unZipAssetsFolder(context: Context, zipFileString: String, outPathString: String): Boolean {
        return withContext(Dispatchers.IO){
            try {
                val inPutZip = ZipInputStream(context.assets.open(zipFileString))
                var zipEntry: ZipEntry
                var szName = ""
                while (inPutZip.nextEntry.also { zipEntry = it } != null) {
                    szName = zipEntry.name
                    if (zipEntry.isDirectory) {
                        szName = szName.substring(0, szName.length - 1)
                        val folder = File(outPathString + File.separator + szName)
                        // 目前判断条件，如果包含解压过的文件就不再解压
                        if (!folder.exists()) {
                            folder.mkdirs()
                        } else {
                            return@withContext true
                        }
                    } else {
                        val file = File(outPathString + File.separator + szName)
                        if (!file.exists()) {
                            file.parentFile?.mkdirs()
                            file.createNewFile()
                        }
                        // 获取文件的输出流
                        val out = FileOutputStream(file)
                        var len: Int
                        val buffer = ByteArray(1024)
                        // 读取（字节）字节到缓冲区
                        while (inPutZip.read(buffer).also { len = it } != -1) {
                            // 从缓冲区（0）位置写入（字节）字节
                            out.write(buffer, 0, len)
                            out.flush()
                        }
                        out.close()
                    }
                }
                inPutZip.close()
                true
            } catch (e: Exception){
                false
            }
        }
    }

    fun getFilesAllName(path: String): List<String>? {
        val file = File(path)
        val files = file.listFiles() ?: return null
        val s: MutableList<String> = ArrayList()
        for (i in files.indices) {
            s.add(files[i].absolutePath)
        }
        return s
    }
}