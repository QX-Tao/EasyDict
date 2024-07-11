package com.qxtao.easydict.ui.view.imageviewer

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.view.LoadFailedView
import com.qxtao.easydict.ui.view.LoadingView

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
            val parentView = activity.findViewById<ViewGroup>(android.R.id.content)
            coverView = LayoutInflater.from(activity).inflate(R.layout.layout_image_viewer_cover, parentView, false)
        }
        return coverView!!
    }
}