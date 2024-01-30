package com.qxtao.easydict.ui.activity.web

import android.webkit.WebBackForwardList
import androidx.lifecycle.ViewModel
import java.util.Stack

class WebViewModel : ViewModel() {
    var currentUrl : String? = null
        set(url) {
            if (url.isNullOrBlank() || url == "about:blank") return
            else field = url
        }
}
