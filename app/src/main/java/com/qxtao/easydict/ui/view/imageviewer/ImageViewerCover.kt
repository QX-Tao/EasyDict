package com.qxtao.easydict.ui.view.imageviewer

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.android.material.appbar.MaterialToolbar
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.view.LoadFailedView
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.ClipboardUtils
import com.qxtao.easydict.utils.common.ColorUtils
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.DownloadProgressCallBack
import com.xuexiang.xhttp2.exception.ApiException
import java.io.File

class ImageViewerCover(private val activity: Activity) {
    private var coverView: View? = null

    fun getLoadingView(): View {
        return LoadingView(activity)
    }


    fun getLoadFailedView(): View {
        return LoadFailedView(activity).apply {
            setHintText(activity.getString(R.string.failed_to_load_click_the_screen_to_try_again))
        }
    }

    fun getCoverView(): View? {
        if (coverView == null) {
            coverView = LayoutInflater.from(activity).inflate(R.layout.layout_image_viewer_cover, null)
        }
        return coverView!!
    }
}