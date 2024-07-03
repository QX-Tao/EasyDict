package com.qxtao.easydict.utils.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.FileProvider
import com.qxtao.easydict.R
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStream

object ClipboardUtils {

    fun getClipboardText(context: Context): CharSequence? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        return clip?.getItemAt(0)?.text
    }

    fun hasClipboardText(context: Context): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        return clip != null && clip.itemCount > 0
    }

    fun copyTextToClipboard(context: Context, text: String?, successToast: String? = null) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(null, text)
        clipboard.setPrimaryClip(clip)
        successToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    fun copyTextToClipboard(context: Context, text: CharSequence?, successToast: String? = null) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(null, text)
        clipboard.setPrimaryClip(clip)
        successToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    fun copyImageToClipboard(context: Context, imageFilePath: String, successToast: String? = null) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copyUri = FileProvider.getUriForFile(context, "com.qxtao.easydict.fileprovider", File(imageFilePath))
        val clip = ClipData.newUri(context.contentResolver, null, copyUri)
        clipboard.setPrimaryClip(clip)
        successToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

}