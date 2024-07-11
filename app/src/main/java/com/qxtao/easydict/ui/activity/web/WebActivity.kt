package com.qxtao.easydict.ui.activity.web

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewFeature.isFeatureSupported
import com.google.android.material.appbar.MaterialToolbar
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.ActivityWebBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.view.ProgressView
import com.qxtao.easydict.utils.common.ClipboardUtils
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_WEB_VIEW


class WebActivity : BaseActivity<ActivityWebBinding>(ActivityWebBinding::inflate){
    // define variable
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var callback: OnBackPressedCallback
    private lateinit var webViewModel: WebViewModel
    private var needLoadUrl = true
    private val initUrl by lazy { intent.getStringExtra("extra_initUrl") }
    private val fixedTitle by lazy { intent.getStringExtra("extra_fixedTitle") }
    private val allowOtherUrls by lazy { intent.getBooleanExtra("extra_allowOtherUrls", true) }
    private val useCache by lazy { intent.getBooleanExtra("extra_useCache", true) }
    // define widget
    private lateinit var webView: WebView
    private lateinit var mtTitle: MaterialToolbar
    private lateinit var pvProgress: ProgressView

    companion object{
        fun start(activity: Activity, initUrl: String, fixedTitle: String? = null,
            allowOtherUrls: Boolean = true, useCache: Boolean = true){
            start(context = activity, initUrl, fixedTitle, allowOtherUrls, useCache)
        }
        fun start(context: Context, initUrl: String, fixedTitle: String? = null,
            allowOtherUrls: Boolean = true, useCache: Boolean = true){
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra("extra_initUrl", initUrl)
            intent.putExtra("extra_fixedTitle", fixedTitle)
            intent.putExtra("extra_allowOtherUrls", allowOtherUrls)
            intent.putExtra("extra_useCache", useCache)
            context.startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    override fun onCreate1(savedInstanceState: Bundle?) {
        super.onCreate1(savedInstanceState)
        webViewModel = ViewModelProvider(this)[WebViewModel::class.java]
        needLoadUrl = savedInstanceState == null || webViewModel.currentUrl.isNullOrBlank()
    }

    override fun onCreate() {
        if (!ShareUtils.getBoolean(this, IS_USE_WEB_VIEW, true)) {
            if (initUrl.isNullOrBlank()) finish()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(initUrl))
            mContext.startActivity(intent)
            finish()
        }
        dispatcher = onBackPressedDispatcher
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        }
        dispatcher.addCallback(callback)
    }

    override fun bindViews() {
        webView = binding.webView
        mtTitle = binding.mtTitle
        pvProgress = binding.pvProgress
    }

    override fun initViews() {
        initWebView()
        loadUrl()
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener { dispatcher.onBackPressed() }
        mtTitle.setOnMenuItemClickListener { 
            when(it.itemId){
                R.id.refresh -> { webView.reload() }
                R.id.copy_url -> {
                    val copyText = webViewModel.currentUrl ?: initUrl
                    if (copyText.isNullOrBlank()){
                        showShortToast(getString(R.string.web_request_url_empty))
                        return@setOnMenuItemClickListener true
                    }
                    ClipboardUtils.copyTextToClipboard(mContext, copyText, getString(R.string.copied))
                }
                R.id.open_in_browser -> {
                    val uriString = webViewModel.currentUrl ?: initUrl ?: return@setOnMenuItemClickListener true
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
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
        webView.setDownloadListener { url, _, _, _, _ ->
            AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.hint))
                .setMessage(getString(R.string.web_request_download_file_desc))
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        mContext.startActivity(intent)
                    } catch (_: Exception) { }
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show()
        }
    }

    @Suppress("SetJavaScriptEnabled", "DEPRECATION")
    private fun initWebView() {
        webView.setBackgroundColor(0)
        webView.background.alpha = 0
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            blockNetworkImage = false
            loadWithOverviewMode = true
            mixedContentMode =  WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = if (useCache) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_NO_CACHE
            loadsImagesAutomatically = true
            displayZoomControls = false
            builtInZoomControls = true
            setSupportZoom(true)
            if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                if (isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, true)
                } else if (isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
                }
            }
        }
        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.toString().startsWith("http")){
                    if (!allowOtherUrls && request.url.toString() != initUrl){
                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                        view.context.startActivity(intent)
                    } else{
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                } else {
                    AlertDialog.Builder(mContext)
                        .setTitle(getString(R.string.hint))
                        .setMessage(getString(R.string.web_request_open_app))
                        .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, request.url)
                                view.context.startActivity(intent)
                            } catch (_: Exception) { }
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .create()
                        .show()
                }
                return true
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                webViewModel.currentUrl = url
                setTitle(getString(R.string.page_loading))
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                pvProgress.hide()
                setTitle()
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                Log.d("WebView", "onReceivedError: ${error.errorCode}")
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                pvProgress.setWebProgress(newProgress)
                when (newProgress){
                    in 0.. 94 ->{
                        setTitle(getString(R.string.page_loading))
                    }
                    else -> {
                        setTitle()
                    }
                }
            }
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(view.context)
                    .setTitle(getString(R.string.hint))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                        result.confirm()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setOnDismissListener {
                        result.cancel()
                    }
                    .create()
                    .show()
                return true
            }
            override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(view.context)
                    .setTitle(getString(R.string.hint))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                        result.confirm()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setOnDismissListener {
                        result.cancel()
                    }
                    .create()
                    .show()
                return true
            }
        }
    }

    private fun loadUrl(){
        if (!needLoadUrl) return
        val url = webViewModel.currentUrl ?: initUrl
        if (url.isNullOrBlank()) showLoadBingDialog() else webView.loadUrl(url)
    }

    private fun showLoadBingDialog(){
        AlertDialog.Builder(mContext)
            .setTitle(getString(R.string.hint))
            .setMessage(getString(R.string.load_bing_hint))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.load_bing)) { _, _ ->
                webView.loadUrl("https://cn.bing.com/")
            }
            .setNegativeButton(getString(R.string.exit)) { _, _ -> finish() }
            .create()
            .show()
    }

    private fun setTitle(title: String? = webView.title){
        mtTitle.title = fixedTitle ?: title
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