package com.qxtao.easydict.ui.activity.web

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewFeature.isFeatureSupported
import com.google.android.material.appbar.MaterialToolbar
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.ActivityWebBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.NetworkUtils
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_WEB_VIEW
import me.jingbin.progress.WebProgress
import rikka.core.util.ClipboardUtils


class WebActivity : BaseActivity<ActivityWebBinding>(ActivityWebBinding::inflate){
    // define variable
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var callback: OnBackPressedCallback
    private lateinit var webViewModel: WebViewModel
    private var isRestoreWebView = false
    private val initUrl by lazy { intent?.getStringExtra("extra_url") }
    private val title by lazy { intent?.getStringExtra("extra_title") }
    private val allowOtherUrls by lazy { intent?.getBooleanExtra("extra_allowOtherUrls", false) }
    private val useWebTitle by lazy { intent?.getBooleanExtra("extra_useWebTitle", false) }
    private val useCache by lazy { intent?.getBooleanExtra("extra_useCache", true) }
    // define widget
    private lateinit var webView: WebView
    private lateinit var llLoadingFail: LinearLayout
    private lateinit var lvLoading: LoadingView
    private lateinit var tvLoadingFail: TextView
    private lateinit var mtTitle: MaterialToolbar
    private lateinit var wpProgress: WebProgress
    private lateinit var vHolder: View

    companion object{
        fun start(
            activity: Activity,
            url: String,
            title: String? = null,
            allowOtherUrls: Boolean = false,
            useWebTitle: Boolean = false,
            useCache: Boolean =  true
        ){
            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra("extra_url", url)
            intent.putExtra("extra_title", title)
            intent.putExtra("extra_allowOtherUrls", allowOtherUrls)
            intent.putExtra("extra_useWebTitle", useWebTitle)
            intent.putExtra("extra_useCache", useCache)
            activity.startActivity(intent)
        }
        fun start(
            context: Context,
            url: String,
            title: String? = null,
            allowOtherUrls: Boolean = false,
            useWebTitle: Boolean = false,
            useCache: Boolean =  true
        ){
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra("extra_url", url)
            intent.putExtra("extra_title", title)
            intent.putExtra("extra_allowOtherUrls", allowOtherUrls)
            intent.putExtra("extra_useWebTitle", useWebTitle)
            intent.putExtra("extra_useCache", useCache)
            context.startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
        isRestoreWebView = true
    }

    override fun onCreate() {
        if (!ShareUtils.getBoolean(this, IS_USE_WEB_VIEW, true)) {
            if (initUrl.isNullOrEmpty()) finish()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(initUrl))
            mContext.startActivity(intent)
            finish()
        }
        webViewModel = ViewModelProvider(this)[WebViewModel::class.java]
        dispatcher = onBackPressedDispatcher
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val history = webView.copyBackForwardList()
                var index = -1
                var url: String? = null
                val curUrl = webView.url
                if (curUrl.equals("about:blank")){ // 当前页面加载不出来
                    while (webView.canGoBackOrForward(index)){
                        val curIndex = history.currentIndex
                        val lastUrl = history.getItemAtIndex( curIndex + index ).url
                        if (!lastUrl.equals("about:blank") && index != -1){
                            webView.goBackOrForward(index)
                            url = lastUrl
                            break
                        }
                        index--
                    }
                } else { // 当前页面加载得出来
                    while (webView.canGoBackOrForward(index)){
                        val curIndex = history.currentIndex
                        val lastUrl = history.getItemAtIndex( curIndex + index ).url
                        if (lastUrl != "about:blank" && !curUrl.equals(lastUrl)){
                            webView.goBackOrForward(index)
                            url = lastUrl
                            break
                        }
                        index--
                    }
                }
                if (url == null) finish()
                if (history.size == 2 && curUrl.equals("about:blank")) finish()
            }
        }
        dispatcher.addCallback(callback)
    }

    override fun bindViews() {
        vHolder = binding.vHolder
        webView = binding.webView
        llLoadingFail = binding.llLoadingFail
        lvLoading = binding.lvLoading
        tvLoadingFail = binding.tvLoadingFail
        mtTitle = binding.mtTitle
        wpProgress = binding.wpProgress
    }

    override fun initViews() {
        wpProgress.setColor(ColorUtils.colorPrimary(mContext))
        initWebView()
        loadUrl()
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener { dispatcher.onBackPressed() }
        mtTitle.setOnMenuItemClickListener { 
            when(it.itemId){
                R.id.refresh -> { webView.reload() }
                R.id.copy_url -> {
                    val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(null, webViewModel.currentUrl)
                    clipboard.setPrimaryClip(clip)
                    showShortToast(getString(R.string.copied))
                }
                R.id.open_in_browser -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webViewModel.currentUrl))
                    mContext.startActivity(intent)
                }
                R.id.clear_cache -> {
                    webView.clearCache(true)
                    showShortToast(getString(R.string.clear_cache_success))
                }
                R.id.exit -> { finish() }
            }
            true
        }
        llLoadingFail.setOnClickListener {
            webView.loadUrl(webViewModel.currentUrl!!)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            blockNetworkImage = false
            mixedContentMode =  WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = if (useCache == true) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_NO_CACHE
            loadsImagesAutomatically = true
            builtInZoomControls = false
            setSupportZoom(false)
            if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                if (isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, true)
                } else if (isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
                }
            }
        }
        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                val hitTestResult = view?.hitTestResult
                if (hitTestResult == null || hitTestResult.extra == null){
                    return false
                }
                if (allowOtherUrls == false && request.url.toString() != initUrl){
                    val intent = Intent(Intent.ACTION_VIEW, request.url)
                    view.context.startActivity(intent)
                    return true
                } else{
                    if (request.url.toString().startsWith("http://")){
                        showShortToast(getString(R.string.cleartext_not_permitted))
                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                        view.context.startActivity(intent)
                        return true
                    } else {
                        webViewModel.currentUrl = request.url.toString()
                        view.loadUrl(request.url.toString())
                    }
                }
                return false
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                webViewModel.currentUrl = url
                setTitle(getString(R.string.loading_3))
                lvLoading.visibility = View.VISIBLE
                llLoadingFail.visibility = View.GONE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                if (NetworkUtils.isConnected(mContext)){
                    webViewModel.currentUrl = url
                    wpProgress.hide()
                    setTitle()
                } else {
                    if (url != "about:blank") {
                        webView.loadUrl("about:blank")
                    }
                }
                if (url == "about:blank"){
                    llLoadingFail.visibility = View.VISIBLE
                }
                lvLoading.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                Log.d("WebView", "onReceivedError: ${error.errorCode}")
                when (error.errorCode){
                    ERROR_TIMEOUT -> { }
                    ERROR_HOST_LOOKUP -> { }
                    ERROR_UNKNOWN -> { }
                    ERROR_CONNECT -> { }
                    else -> {
                        webView.loadUrl("about:blank")
                        llLoadingFail.visibility = View.VISIBLE
                    }
                }
            }

            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString().lowercase()
                return if (url.contains("google.com") || url.contains("googleads.g")) {
                    WebResourceResponse(null, null, null)
                } else {
                    super.shouldInterceptRequest(view, request)
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                wpProgress.setWebProgress(newProgress)
                when (newProgress){
                    in 0.. 94 ->{
                        setTitle(getString(R.string.loading_3))
                        lvLoading.visibility = View.VISIBLE
                        llLoadingFail.visibility = View.GONE
                    }
                    else -> {
                        setTitle()
                        lvLoading.visibility = View.GONE
                    }
                }
            }
        }
        webView.setBackgroundColor(0)
        webView.background.alpha = 0
    }

    private fun loadUrl(){
        if (isRestoreWebView) return
        val url = webViewModel.currentUrl ?: initUrl
        if (url.isNullOrEmpty()) finish()
        else {
            if (url.startsWith("http://")){
                showShortToast(getString(R.string.cleartext_not_permitted))
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                mContext.startActivity(intent)
                finish()
            } else {
                webViewModel.currentUrl = url
                webView.loadUrl(url)
            }
        }
    }

    private fun setTitle(){
        mtTitle.title =
            if (useWebTitle == false && title.isNullOrEmpty()) getString(R.string.empty_string)
            else if (useWebTitle == true && title.isNullOrEmpty())
                if (webView.title == "about:blank") getString(R.string.web_page_cannot_be_opened)
                else webView.title
            else title.toString()
    }
    private fun setTitle(title: String){
        mtTitle.title =
            if (useWebTitle == false || title.isBlank()) getString(R.string.empty_string)
            else title
    }

    override fun onDestroy1() {
        if (this::webView.isInitialized){
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            webView.clearHistory()
            webView.removeAllViews()
            val parent = webView.parent
            if (parent is ViewGroup) {
                parent.removeView(webView)
            }
            webView.destroy()
        }
        super.onDestroy1()
    }

}