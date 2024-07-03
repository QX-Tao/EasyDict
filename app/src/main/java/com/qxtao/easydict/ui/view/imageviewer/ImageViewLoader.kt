package com.qxtao.easydict.ui.view.imageviewer

import android.graphics.drawable.Animatable
import android.widget.ImageView
import coil.dispose
import coil.load

object ImageViewLoader : ImageLoader {
    override fun load(imageView: ImageView, url: String?, loaderListener: ImageLoader.ImageLoaderListener?) {
        imageView.load(url){
            target(
                onSuccess = { drawable ->
                    loaderListener?.onLoadSuccess(drawable)
                    imageView.post {
                        imageView.setImageDrawable(drawable)
                        if (drawable is Animatable && !drawable.isRunning) {
                            drawable.start()
                        }
                    }
                },
                onError = { drawable ->
                    loaderListener?.onLoadFailed(drawable)
                }
            )
        }
    }

    override fun stopLoad(imageView: ImageView) {
        imageView.dispose()
    }

}
