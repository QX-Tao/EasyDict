package com.qxtao.easydict.ui.view.imageviewer

interface OnDragChangeListener {
    fun onRelease()
    fun onDragChange(dy: Int, scale: Float, fraction: Float)
}