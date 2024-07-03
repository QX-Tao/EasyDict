package com.qxtao.easydict.ui.view.imageviewer.photoview

internal interface OnGestureListener {
    fun onDrag(dx: Float, dy: Float)

    fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float)

    fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)
}