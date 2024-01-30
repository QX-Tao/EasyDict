package com.qxtao.easydict.ui.activity.web

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewFeature.isFeatureSupported
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.ActivityWebBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.common.NetworkUtils
import com.qxtao.easydict.utils.factory.isAppearanceLight
import com.qxtao.easydict.utils.factory.isLandscape
import com.qxtao.easydict.utils.factory.screenRotation
import me.jingbin.progress.WebProgress


class WebActivity : BaseActivity<ActivityWebBinding>(ActivityWebBinding::inflate){
    // define variable
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var callback: OnBackPressedCallback
    private lateinit var webViewModel: WebViewModel
    private var isRestoreWebView = false
    private val initUrl by lazy { intent?.getStringExtra("extra_url") }
    private val title by lazy { intent?.getStringExtra("extra_title") }
    private val webBgColor by lazy { intent?.getIntExtra("extra_webBgColor", this.getColor(R.color.colorBgWhite10)) }
    private val foreColor by lazy { getForeColorForBackground(webBgColor!!) }
    private val isUseTitleBarSpace by lazy { intent?.getBooleanExtra("extra_isUseTitleBarSpace", true) }
    private val allowOtherUrls by lazy { intent?.getBooleanExtra("extra_allowOtherUrls", false) }
    private val useWebTitle by lazy { intent?.getBooleanExtra("extra_useWebTitle", false) }
    private val showOpenInBrowserButton by lazy { intent?.getBooleanExtra("extra_showOpenInBrowserButton", false) }
    private val useCache by lazy { intent?.getBooleanExtra("extra_useCache", true) }
    // define widget
    private lateinit var ivMoreButton : ImageView
    private lateinit var ivBackButton : ImageView
    private lateinit var tvTitle : TextView
    private lateinit var webView: WebView
    private lateinit var llLoadingFail: LinearLayout
    private lateinit var lvLoading: LoadingView
    private lateinit var tvLoadingFail: TextView
    private lateinit var clWvRoot: ConstraintLayout
    private lateinit var cvTitle: CardView
    private lateinit var wpProgress: WebProgress
    private lateinit var vHolder: View

    companion object{
        fun start(
            activity: Activity,
            url: String,
            title: String? = null,
            webBgColor: Int? = null,
            isUseTitleBarSpace: Boolean = true,
            allowOtherUrls: Boolean = false,
            useWebTitle: Boolean = false,
            showOpenInBrowserButton: Boolean = false,
            useCache: Boolean =  true
        ){
            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra("extra_url", url)
            intent.putExtra("extra_title", title)
            intent.putExtra("extra_webBgColor", webBgColor)
            intent.putExtra("extra_isUseTitleBarSpace", isUseTitleBarSpace)
            intent.putExtra("extra_allowOtherUrls", allowOtherUrls)
            intent.putExtra("extra_useWebTitle", useWebTitle)
            intent.putExtra("extra_showOpenInBrowserButton", showOpenInBrowserButton)
            intent.putExtra("extra_useCache", useCache)
            activity.startActivity(intent)
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
        isAppearanceLight = foreColor == Color.BLACK
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
        tvTitle = binding.includeTitleBarSecond.tvTitle
        ivBackButton = binding.includeTitleBarSecond.ivBackButton
        ivMoreButton = binding.includeTitleBarSecond.ivMoreButton
        webView = binding.webView
        llLoadingFail = binding.llLoadingFail
        lvLoading = binding.lvLoading
        tvLoadingFail = binding.tvLoadingFail
        clWvRoot = binding.clWvRoot
        cvTitle = binding.cvTitle
        wpProgress = binding.wpProgress
    }

    override fun initViews() {
        ivBackButton.setColorFilter(foreColor)
        tvTitle.setTextColor(foreColor)
        wpProgress.setColor(getColor(R.color.themeMainColor))
        ivMoreButton.setColorFilter(foreColor)
        ivMoreButton.setImageResource(R.drawable.ic_browser)
        ivMoreButton.tooltipText = getString(R.string.open_in_browser)
        ivMoreButton.visibility = if (showOpenInBrowserButton == true) View.VISIBLE else View.GONE
        if (isUseTitleBarSpace == false) {
            cvTitle.cardElevation = SizeUtils.dp2px(4f).toFloat()
            cvTitle.setCardBackgroundColor(webBgColor!!)
        }
        tvLoadingFail.setTextColor(foreColor)
        tvLoadingFail.setCompoundDrawables(null, ContextCompat.getDrawable(mContext, R.drawable.ic_loading_fail2)!!
            .also {
                it.colorFilter = PorterDuffColorFilter(foreColor, PorterDuff.Mode.SRC_IN)
                it.setBounds(0,0,it.minimumWidth,it.minimumHeight)
            },null,null)
        clWvRoot.setBackgroundColor(webBgColor!!)
        lvLoading.setBackgroundColor(webBgColor!!)
        llLoadingFail.setBackgroundColor(webBgColor!!)
        initWebView()
        loadUrl()
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(vHolder){ view, insets ->
            val displayCutout = insets.displayCutout
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = if (isUseTitleBarSpace == false) insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + SizeUtils.dp2px(56f) else 0
            when (screenRotation){
                90 -> {
                    params.leftMargin = displayCutout?.safeInsetLeft ?: insets.getInsets(WindowInsetsCompat.Type.systemBars()).left
                    params.rightMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).right
                }
                270 -> {
                    params.rightMargin = displayCutout?.safeInsetRight ?: insets.getInsets(WindowInsetsCompat.Type.systemBars()).right
                    params.leftMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).left
                }
            }
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(wpProgress){ view, insets ->
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = if (isUseTitleBarSpace == true) insets.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0
            insets
        }
        ivBackButton.setOnClickListener { dispatcher.onBackPressed() }
        ivMoreButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webViewModel.currentUrl))
            mContext.startActivity(intent)
        }
        ivBackButton.setOnLongClickListener {
            finish()
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
        tvTitle.text =
            if (useWebTitle == false && title.isNullOrEmpty()) getString(R.string.empty_string)
            else if (useWebTitle == true && title.isNullOrEmpty())
                if (webView.title == "about:blank") getString(R.string.web_page_cannot_be_opened)
                else webView.title
            else title.toString()
    }
    private fun setTitle(title: String){
        tvTitle.text =
            if (useWebTitle == false || title.isBlank()) getString(R.string.empty_string)
            else title
    }

    private fun getForeColorForBackground(backgroundColor: Int): Int {
        val red = Color.red(backgroundColor)
        val green = Color.green(backgroundColor)
        val blue = Color.blue(backgroundColor)
        val brightness = (red * 299 + green * 587 + blue * 114) / 1000.0
        // 180，则使用白色作为文字颜色；否则使用黑色
        return if (brightness <= 180) Color.WHITE else Color.BLACK
    }

    override fun onDestroy1() {
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        webView.clearHistory()
        webView.removeAllViews()
        val parent = webView.parent
        if (parent is ViewGroup) {
            parent.removeView(webView)
        }
        webView.destroy()
        super.onDestroy1()
    }

}