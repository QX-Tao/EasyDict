package com.qxtao.easydict.ui.view.imageviewer

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.android.material.appbar.MaterialToolbar
import com.qxtao.easydict.R
import com.qxtao.easydict.utils.common.ClipboardUtils
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.EncryptUtils
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.DownloadProgressCallBack
import com.xuexiang.xhttp2.exception.ApiException
import java.io.File

class PhotoView(private val activity: Activity) {
    val imageViewer = imageViewer {
        context = activity
        imageLoader = ImageViewLoader
        bgColor = ColorUtils.colorSurface(activity)
        imageViewerCover = ImageViewerCover(activity)
    }
    val mtTitle = imageViewer.imageViewerCover?.getCoverView()?.findViewById<MaterialToolbar>(R.id.mt_title)

    fun show(imageDataList: List<String>, imageViewList: List<ImageView>, index: Int){
        imageViewer.srcImageViewFetcher = { imageViewList[it] }
        imageViewer.onPageSelected = {
            mtTitle?.title = getString(R.string.photo_current_of_all, it + 1, imageDataList.size)
            mtTitle?.setOnMenuItemClickListener{ item ->
                when (item.itemId) {
                    R.id.copy_to_clipboard -> {
                        val imgUrl = imageDataList[it]
                        val savePath = activity.cacheDir.path + "/Image/"
                        val fileName = EncryptUtils.encryptMD5ToString(imgUrl)
                        DataCache(activity).copyImage(imgUrl, fileName, savePath)
                    }
                    R.id.save_image -> {
                        val imgUrl = imageDataList[it]
                        val savePath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).absolutePath + "/EasyDict/"
                        val fileName = EncryptUtils.encryptMD5ToString(imgUrl)
                        DataCache(activity).cacheImage(imgUrl, fileName, savePath)
                    }
                    R.id.share_image -> {
                        val imgUrl = imageDataList[it]
                        val savePath = activity.cacheDir.path + "/Image/"
                        val fileName = EncryptUtils.encryptMD5ToString(imgUrl)
                        DataCache(activity).shareImage(imgUrl, fileName, savePath)
                    }
                }
                true
            }
            mtTitle?.setNavigationOnClickListener {
                imageViewer.handleBackPressed()
            }
        }
        imageViewer.show(activity.window.decorView as ViewGroup, imageDataList, index)
    }
    fun show(imageData: String, imageView: ImageView){
        val imageDataList = listOf(imageData)
        val imageViewList = listOf(imageView)
        show(imageDataList, imageViewList, 0)
    }

    private fun showShortToast(text: String){
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }
    private fun getString(id: Int, vararg args: Any?): String {
        return activity.getString(id, *args)
    }

    inner class DataCache(private val activity: Activity){
        private val alertDialog = showLoadingDialog()
        private fun showLoadingDialog() : AlertDialog {
            val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_progress_bar, null)
            val alertDialog = AlertDialog.Builder(activity)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            alertDialog.show()
            return alertDialog
        }
        fun cacheImage(imgUrl: String, fileName: String, savePath: String){
            cacheImage(imgUrl, fileName, savePath, object : ImageCacheCallback {
                override fun onCached(path: String) {
                    showShortToast(getString(R.string.save_image_success_desc, path))
                }
                override fun onError() {
                    showShortToast(getString(R.string.save_failure))
                }
            })
        }
        fun shareImage(imgUrl: String, fileName: String, savePath: String){
            cacheImage(imgUrl, fileName, savePath, object : ImageCacheCallback{
                override fun onCached(path: String) {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    val shareUri = FileProvider.getUriForFile(activity, "com.qxtao.easydict.fileprovider", File(path))
                    shareIntent.type = "image/*"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri)
                    val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.share_image))
                    if (shareIntent.resolveActivity(activity.packageManager) != null) {
                        activity.startActivity(chooserIntent)
                    } else {
                        showShortToast(getString(R.string.no_share_app_available))
                    }
                }
                override fun onError() {
                    showShortToast(getString(R.string.share_failure))
                }
            })
        }
        fun copyImage(imgUrl: String, fileName: String, savePath: String){
            cacheImage(imgUrl, fileName, savePath, object : ImageCacheCallback{
                override fun onCached(path: String) {
                    ClipboardUtils.copyImageToClipboard(activity, path, getString(R.string.copy_success))
                }
                override fun onError() {
                    showShortToast(getString(R.string.copy_failure))
                }
            })
        }

        private fun cacheImage(imgUrl: String, fileName: String, savePath: String, callback: ImageCacheCallback){
            val saveDir = File(savePath)
            if (!saveDir.exists()) { saveDir.mkdirs() }
            XHttp.downLoad(imgUrl)
                .savePath(savePath)
                .saveName(fileName)
                .execute<String>(object : DownloadProgressCallBack<String?>() {
                    override fun onStart() {}
                    override fun onError(e: ApiException) {
                        callback.onError()
                        alertDialog.dismiss()
                    }
                    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {}
                    override fun onComplete(path: String) {
                        callback.onCached(path)
                        alertDialog.dismiss()
                    }
                })
        }
    }

    interface ImageCacheCallback {
        fun onCached(path: String)
        fun onError()
    }

}