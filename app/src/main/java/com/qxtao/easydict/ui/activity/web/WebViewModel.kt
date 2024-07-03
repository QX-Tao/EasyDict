package com.qxtao.easydict.ui.activity.web

import androidx.lifecycle.ViewModel
import java.util.Stack

class WebViewModel: ViewModel() {
    var currentUrl: String? = null
        set(url) {
            if (url.isNullOrBlank()) return
            else field = url
        }
}
