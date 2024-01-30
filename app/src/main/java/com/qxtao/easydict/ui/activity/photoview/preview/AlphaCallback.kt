package com.qxtao.easydict.ui.activity.photoview.preview

interface AlphaCallback {
    // 透明度改变回调
    fun onChangeAlphaCallback(alpha: Float)
    // 关闭预览回调
    fun onChangeClose()
}