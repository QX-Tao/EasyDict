package com.qxtao.easydict.utils.common

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object EncryptUtils {
    fun encryptMD5ToString(str: String): String {
        val md5Digest = MessageDigest.getInstance("MD5")
        val byteArray = md5Digest.digest(str.toByteArray())
        return byteArrayToHexString(byteArray)
    }

    fun encryptMD5FileToString(filePath: String): String {
        val file = File(filePath)
        val md5Digest = MessageDigest.getInstance("MD5")
        val byteArray = ByteArray(8192)
        val inputStream = FileInputStream(file)
        var bytesRead: Int
        while (inputStream.read(byteArray).also { bytesRead = it } != -1) {
            md5Digest.update(byteArray, 0, bytesRead)
        }
        inputStream.close()
        return byteArrayToHexString(md5Digest.digest())
    }



    private fun byteArrayToHexString(byteArray: ByteArray): String {
        val hexString = StringBuilder()
        for (byte in byteArray) {
            val hex = Integer.toHexString(0xFF and byte.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}