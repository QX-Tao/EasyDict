package com.qxtao.easydict.ui.view.imageviewer

import android.graphics.drawable.Drawable
import android.widget.ImageView

interface ImageLoader {
    fun load(imageView: ImageView, url: String?, loaderListener: ImageLoaderListener?)

    fun stopLoad(imageView: ImageView)

    interface ImageLoaderListener {
        fun onLoadFailed(errorDrawable: Drawable?)

        fun onLoadSuccess(drawable: Drawable?)
    }
}