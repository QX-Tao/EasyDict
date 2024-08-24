package com.qxtao.easydict.ui.activity.dict

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dict.DictSelectWordBookAdapter
import com.qxtao.easydict.database.DailySentenceData
import com.qxtao.easydict.database.SearchHistoryData
import com.qxtao.easydict.database.SimpleDictData
import com.qxtao.easydict.database.WordBookData
import com.qxtao.easydict.database.WordListData
import com.qxtao.easydict.databinding.ActivityDictBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.fragment.dict.DictDefinitionFragment
import com.qxtao.easydict.ui.fragment.dict.DictDetailFragment
import com.qxtao.easydict.ui.fragment.dict.DictExtraFragment
import com.qxtao.easydict.ui.fragment.dict.DictHasFragment
import com.qxtao.easydict.ui.fragment.dict.DictSearchFragment
import com.qxtao.easydict.ui.fragment.dict.DictWelcomeFragment
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.EasyPermissions
import com.qxtao.easydict.utils.common.AssetsUtils
import com.qxtao.easydict.utils.common.ClipboardUtils
import com.qxtao.easydict.utils.common.EncryptUtils
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.constant.ActionConstant.ACTION_CREATE_QUICK_SEARCH_NOTIFICATION
import com.qxtao.easydict.utils.constant.PermissionConstant
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_QUICK_SEARCH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.insets.windowInsetsHelper
import java.util.Locale
import kotlin.system.exitProcess

class DictActivity : BaseActivity<ActivityDictBinding>(ActivityDictBinding::inflate),
    BaseFragment.OnFragmentInteractionListener, ASRResultListener{
    // define variable
    private lateinit var dictViewModel: DictViewModel
    private lateinit var searchHistoryData: SearchHistoryData
    private lateinit var simpleDictData: SimpleDictData
    private lateinit var wordBookData: WordBookData
    private lateinit var dailySentenceData: DailySentenceData
    private lateinit var wordListData: WordListData
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var callback: OnBackPressedCallback
    private val recognitionHelper: RecognitionHelper by lazy { RecognitionHelper(this) }
    private val searchStr by lazy { intent?.getStringExtra("onSearch") }

    // define widget
    private lateinit var speechDialog: AlertDialog
    private lateinit var lvLoading: LoadingView
    private lateinit var clRoot: ConstraintLayout

    companion object {
        private var isReady = false
        fun onSearchStr(context: Context, str: String){
            val intent = Intent(context, DictActivity::class.java)
            intent.putExtra("onSearch", str)
            context.startActivity(intent)
        }
    }

    override fun onCreate1(savedInstanceState: Bundle?) {
        super.onCreate1(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) displaySplash()
        if (savedInstanceState == null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val shouldUnzip = searchStr.isNullOrBlank()
                if (shouldUnzip) {
                    val res = async { unzipData() }
                    if (withTimeoutOrNull(8000) { res.await() } == true) {
                        initializeData()
                        withContext(Dispatchers.Main) {
                            initializeViewModel()
                            toWelcomeFragment()
                            lvLoading.visibility = View.GONE
                            isReady = true
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showShortToast(getString(R.string.configure_simple_dict_error_desc))
                            finish()
                            exitProcess(0)
                        }
                    }
                } else {
                    initializeData(searchStr)
                    withContext(Dispatchers.Main) {
                        initializeViewModel(searchStr)
                        toDefinitionFragment(searchStr!!)
                        lvLoading.visibility = View.GONE
                    }
                }
            }
        } else {
            if (searchStr.isNullOrBlank()) {
                initializeData()
                initializeViewModel()
                lvLoading.visibility = View.GONE
            } else {
                initializeData(searchStr)
                initializeViewModel(searchStr)
                lvLoading.visibility = View.GONE
            }
        }
    }
    private fun initializeData(searchStr: String? = null) {
        simpleDictData = SimpleDictData(mContext)
        searchHistoryData = SearchHistoryData(mContext)
        wordBookData = WordBookData(mContext)
        if (searchStr == null) {
            dailySentenceData = DailySentenceData(mContext)
            wordListData = WordListData(mContext)
        }
    }
    private fun initializeViewModel(searchStr: String? = null) {
        dictViewModel = if (searchStr == null) {
            ViewModelProvider(this@DictActivity, DictViewModel.Factory(simpleDictData,
                searchHistoryData, wordBookData, dailySentenceData, wordListData))[DictViewModel::class.java]
        } else {
            ViewModelProvider(this@DictActivity, DictViewModel.Factory(simpleDictData,
                searchHistoryData, wordBookData))[DictViewModel::class.java]
        }
    }

    override fun onCreate() {
        dispatcher = onBackPressedDispatcher
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (photoView.imageViewer.handleBackPressed()) return
                val currentFragment = if (this@DictActivity::dictViewModel.isInitialized){
                        supportFragmentManager.findFragmentByTag(dictViewModel.currentFragmentTag)
                    } else null
                when(currentFragment){
                    is DictWelcomeFragment, is DictDefinitionFragment -> finish()
                    is DictSearchFragment, is DictExtraFragment -> backFragment()
                    null -> finish()
                }
            }
        }
        dispatcher.addCallback(this, callback)
    }

    override fun bindViews() {
        lvLoading = binding.lvLoading
        clRoot = binding.clRoot
    }
    override fun initViews() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
            clRoot.windowInsetsHelper?.fitSystemWindows = 0x50 or 0x00800003 or 0x00800005
        }
        initQuickSearch()
    }

    override fun onFragmentInteraction(vararg data: Any?) {
        if (data.isNotEmpty()) {
            when (data[0]) {
                "toDictSearchFragment" -> { toSearchFragment(DICT_SEARCH_HAS_FRAGMENT_TAG) }
                "toExtraFragment"-> { toExtraFragment(data[1] as String) }
                "toHasFragment" -> { toSearchFragment(DICT_SEARCH_HAS_FRAGMENT_TAG) }
                "toWelcomeFragment" -> { toWelcomeFragment() }
                "toDetailFragment" -> { toSearchFragment(DICT_SEARCH_DETAIL_FRAGMENT_TAG, data[1] as String) }
                "finishActivity" -> { finish() }
                "onBackPressed" -> { dispatcher.onBackPressed() }
                "voiceSearch" -> { checkAndVoiceSearch() }
                "editTextGetFocus" -> { etRequestFocus(data[1] as EditText) }
                "editTextClearFocus" -> { etUnRequestFocus(data[1] as EditText) }
                "showSelectWordBookDialog" -> { showSelectWordBookDialog() }
            }
        }
    }

    fun getDictViewModel(): DictViewModel = dictViewModel

    private fun checkAndVoiceSearch(){
        val easyPermissions = EasyPermissions(this@DictActivity)
        easyPermissions.need(Manifest.permission.RECORD_AUDIO)
            .subscribe(object : EasyPermissions.Subscribe {
                override fun onResult(requestCode: Int, allGranted: Boolean, permissions: Array<String>?) {
                    if (allGranted) {
                        if (recognitionHelper.prepareRecognition(this@DictActivity)) recognitionHelper.startRecognition()
                        else {
                            showShortToast(getString(R.string.voice_search_not_available))
                            return
                        }
                        speechDialog = showVoiceInputDialog()
                    }
                }
            }).request(PermissionConstant.REQUEST_CODE_RECORD_AUDIO)
    }

    private fun showVoiceInputDialog() : AlertDialog{
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_speech_input, null)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val tvInput = dialogView.findViewById<TextView>(R.id.tv_input)
        val tvConfirm =  dialogView.findViewById<TextView>(R.id.tv_confirm)
        val etInput = dialogView.findViewById<EditText>(R.id.et_input)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        tvCancel.setOnClickListener {
            recognitionHelper.releaseRecognition()
            dialog.dismiss()
        }
        tvInput.setOnClickListener {
            tvInput.visibility = View.GONE
            recognitionHelper.startRecognition()
        }
        tvConfirm.setOnClickListener {
            if (etInput.text.isNullOrBlank()) {
                etInput.error = getString(R.string.invalid_input)
                return@setOnClickListener
            }
            recognitionHelper.releaseRecognition()
            dialog.dismiss()
            toSearchFragment(DICT_SEARCH_DETAIL_FRAGMENT_TAG, etInput.text.toString())
        }
        etInput.postDelayed({ etInput.requestFocus() },200)
        dialog.show()
        return dialog
    }

    private fun toSettingsIntent(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", mContext.packageName, null)
        mContext.startActivity(intent)
    }

    private fun toWelcomeFragment() = toFragment(DICT_WELCOME_FRAGMENT_TAG)

    private fun toExtraFragment(extraStyle: String) {
        toFragment(DICT_EXTRA_FRAGMENT_TAG)
        dictViewModel.extraFragmentStyle.value = extraStyle
    }

    private fun toDefinitionFragment(searchStr: String) {
        toFragment(DICT_DEFINITION_FRAGMENT_TAG)
        dictViewModel.onlineSearch(searchStr)
        dictViewModel.dictDetailMode = DICT_DETAIL_MODE_DEFINITION
    }

    private fun toSearchFragment(childFragmentTag: String, searchStr: String? = null){
        val currentFragment = supportFragmentManager.findFragmentByTag(dictViewModel.currentFragmentTag)
        when (currentFragment){
            is DictSearchFragment -> {
                val toFragment = currentFragment.childFragmentManager.findFragmentByTag(childFragmentTag) ?: when(childFragmentTag){
                    DICT_SEARCH_HAS_FRAGMENT_TAG -> DictHasFragment()
                    DICT_SEARCH_DETAIL_FRAGMENT_TAG -> DictDetailFragment()
                    else -> return
                }
                val fromFragment = currentFragment.childFragmentManager.findFragmentByTag(dictViewModel.currentSearchChildFragmentTag)
                if (fromFragment != null){
                    if (dictViewModel.addedSearchChildFragment.contains(childFragmentTag)) {
                        // 使用hide, show方法
                        currentFragment.childFragmentManager.beginTransaction().apply {
                            hide(fromFragment)
                            show(toFragment)
                            try { commitNow() } catch (_: IllegalStateException) {
                                commitAllowingStateLoss() }
                        }
                    } else {
                        // 使用add, hide, show方法
                        currentFragment.childFragmentManager.beginTransaction().apply {
                            add(R.id.dict_search_content_fragment, toFragment, childFragmentTag)
                            hide(fromFragment)
                            show(toFragment)
                            try { commitNow() } catch (_: IllegalStateException) {
                                commitAllowingStateLoss() }
                        }
                    }
                } else {
                    currentFragment.childFragmentManager.beginTransaction().apply {
                        add(R.id.dict_search_content_fragment, toFragment, childFragmentTag)
                        show(toFragment)
                        try { commitNow() } catch (_: IllegalStateException) {
                            commitAllowingStateLoss() }
                    }
                }
                when(childFragmentTag){
                    DICT_SEARCH_HAS_FRAGMENT_TAG -> {
                        currentFragment.getEditTextFocus()
                        dictViewModel.getDictSearchHistory()
                    }
                    DICT_SEARCH_DETAIL_FRAGMENT_TAG -> {
                        if (searchStr.isNullOrBlank()) return else dictViewModel.onlineSearch(searchStr)
                    }
                }
                dictViewModel.addedSearchChildFragment.add(childFragmentTag)
                dictViewModel.currentSearchChildFragmentTag = childFragmentTag
            }
            else -> {
                toFragment(DICT_SEARCH_FRAGMENT_TAG)
                toSearchFragment(childFragmentTag, searchStr)
            }
        }
    }

    private fun showAddWordBookDialog(){
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_word_book_input, null)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val tvConfirm =  dialogView.findViewById<TextView>(R.id.tv_confirm)
        val etInput = dialogView.findViewById<EditText>(R.id.et_input)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        tvCancel.setOnClickListener { dialog.dismiss() }
        tvConfirm.setOnClickListener {
            val wordBookName = etInput.text.toString()
            if (wordBookName.isNotBlank()){
                val res = dictViewModel.addWordBook(wordBookName)
                if (res.first) {
                    dictViewModel.addSearchTextToWordBook(res.second)
                    showShortToast(getString(R.string.add_to_word_book, res.second))
                    dialog.dismiss()
                } else showShortToast(res.second)
            } else {
                etInput.error = getString(R.string.invalid_input)
            }
        }
        etRequestFocus(etInput)
        dialog.show()
    }

    private fun showSelectWordBookDialog(){
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_word_book_select, null)
        val rvSelectWordBook = dialogView.findViewById<RecyclerView>(R.id.rv_select_word_book)
        val tvAddWordBook =  dialogView.findViewById<TextView>(R.id.tv_add_word_book)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .create()
        tvAddWordBook.setOnClickListener {
            showAddWordBookDialog()
            dialog.dismiss()
        }
        rvSelectWordBook.layoutManager = LinearLayoutManager(mContext)
        rvSelectWordBook.adapter = DictSelectWordBookAdapter(dictViewModel.getWordBookList()).apply {
            setOnItemClickListener(object : DictSelectWordBookAdapter.OnItemClickListener{
                override fun onItemClick(position: Int) {
                    val res = dictViewModel.addSearchTextToWordBook(position)
                    dialog.dismiss()
                    if (res.first){
                        showShortToast(getString(R.string.add_to_word_book, res.second[res.third!!]))
                    } else {
                        if (res.second.size > 1 && res.third == null){
                            showSelectWordBookDialog()
                        } else showShortToast(getString(R.string.operation_failure))
                    }
                }
            })
        }
        dialog.show()
    }

    private fun etRequestFocus(editText: EditText){
        editText.requestFocus()
        // 延迟200ms 显示软键盘
        val showSoftInput = {
            if (editText.isFocused){
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler.createAsync(Looper.getMainLooper())
                .postDelayed(showSoftInput, 200)
        } else {
            Handler(Looper.getMainLooper())
                .postDelayed(showSoftInput, 200)
        }
    }

    private fun etUnRequestFocus(editText: EditText){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        editText.clearFocus()
    }


    // 重写方法 实现语音输入控制
    override fun onPartialResult(result: String) {
        val etInput = speechDialog.window?.findViewById<EditText>(R.id.et_input)
        val editableText = etInput?.editableText
        val index = etInput?.selectionStart ?: -1
        if (index < 0 || index >= editableText!!.length){
            editableText?.append(result)
        } else
            editableText.insert(index, result)
    }
    override fun onFinalResult(result: String) {
        val r = result.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(
            Locale.ROOT) else it.toString() }
        val etInput = speechDialog.window?.findViewById<EditText>(R.id.et_input)
        val tvInput =  speechDialog.window?.findViewById<TextView>(R.id.tv_input)
        val editableText = etInput?.editableText
        val index = etInput?.selectionStart ?: -1
        if (index < 0 || index >= editableText!!.length){
            editableText?.append(r)
        } else editableText.insert(index, r)
        tvInput?.visibility = View.VISIBLE
    }
    override fun onError(errCode: Int) {
        val tvInput =  speechDialog.window?.findViewById<TextView>(R.id.tv_input)
        tvInput?.visibility = View.VISIBLE
        showShortToast(getString(R.string.go_wrong_try_again))
    }

    // 快速查词功能
    private fun initQuickSearch() {
        val isNotificationsEnabled = NotificationManagerCompat.from(mContext).areNotificationsEnabled()
        val isQuickSearchEnabled = ShareUtils.getBoolean(mContext, IS_USE_QUICK_SEARCH, false)
        if (isNotificationsEnabled && isQuickSearchEnabled) sendBroadcast(Intent(ACTION_CREATE_QUICK_SEARCH_NOTIFICATION)
            .apply { setPackage(packageName)}) else {
            if (isQuickSearchEnabled) {
                showShortToast(getString(R.string.close_quick_search_desc))
                ShareUtils.delShare(mContext, IS_USE_QUICK_SEARCH)
            }
        }
    }

    // 重写函数dispatchTouchEvent，实现点击搜索框外时，收起软键盘
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 排除部分控件
        val currentFragment = if (this@DictActivity::dictViewModel.isInitialized){
            supportFragmentManager.findFragmentByTag(dictViewModel.currentFragmentTag)
        } else null
        if (currentFragment is DictSearchFragment){
            val clQuickSearchPanel = findViewById<ConstraintLayout>(R.id.cl_quick_search_panel)
            // 判断ev是否落在view上
            if (isTouchPointInView(clQuickSearchPanel, ev.x.toInt(), ev.y.toInt()))
                return super.dispatchTouchEvent(ev)
        }
        // 处理ACTION_DOWN事件
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (isHideInput(view, ev)) {
                hideSoftInput(view!!.windowToken)
                view.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    private fun isHideInput(v: View?, ev: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return !(ev.x > left && ev.x < right && ev.y > top && ev.y < bottom)
        }
        return false
    }
    private fun hideSoftInput(token: IBinder?) {
        if (token != null) {
            val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
    private fun isTouchPointInView(view: View?, x: Int, y: Int): Boolean {
        if (view == null) return false
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val left = location[0]
        val top = location[1]
        val right = left + view.measuredWidth
        val bottom = top + view.measuredHeight
        return view.isClickable && y in top..bottom && x in left .. right
    }


    // 程序初始化解压数据
    private suspend fun unzipData() : Boolean {
        return withContext(Dispatchers.IO) {
            val isFileValid = try {
                val filePath = getDatabasePath(SIMPLE_DICT_FILE_NAME)?.absolutePath
                filePath != null && EncryptUtils.encryptMD5FileToString(filePath).lowercase() == SIMPLE_DICT_MD5
            } catch (_: Exception) { false }
            if (isFileValid) { true } else {
                val unzipFile = async {
                    getDatabasePath(SIMPLE_DICT_FILE_NAME)?.parent?.let {
                        AssetsUtils.unZipAssetsFolder(applicationContext, SIMPLE_DICT_ZIP_FILE_PATH, it)
                    }
                }.await() == true
                val ready = try {
                    if (EncryptUtils.encryptMD5FileToString(getDatabasePath(SIMPLE_DICT_FILE_NAME).absolutePath).lowercase() == SIMPLE_DICT_MD5) {
                        true } else { unzipFile }
                } catch (_: Exception) { unzipFile }
                if (ready) true else unzipData()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (this::dictViewModel.isInitialized){
            dictViewModel.stopPlaySound()
            dictViewModel.deleteSearchRecord()
        }
    }

    private fun displaySplash(){
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isReady) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )
    }

    private fun toFragment(fragmentTag: String){
        val toFragment = supportFragmentManager.findFragmentByTag(fragmentTag) ?: when(fragmentTag){
            DICT_WELCOME_FRAGMENT_TAG -> DictWelcomeFragment()
            DICT_SEARCH_FRAGMENT_TAG -> DictSearchFragment()
            DICT_DEFINITION_FRAGMENT_TAG -> DictDefinitionFragment()
            DICT_EXTRA_FRAGMENT_TAG -> DictExtraFragment()
            else -> return
        }
        val fromFragment = supportFragmentManager.findFragmentByTag(dictViewModel.currentFragmentTag)
        if (fromFragment != null){
            if (dictViewModel.addedFragment.contains(fragmentTag)){
                // 使用hide, show方法
                supportFragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    hide(fromFragment)
                    show(toFragment)
                    try { commitNow() } catch (_: IllegalStateException) {
                        commitAllowingStateLoss() }
                }
            } else {
                // 使用add, hide, show方法
                supportFragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    add(R.id.dict_fragment, toFragment, fragmentTag)
                    hide(fromFragment)
                    show(toFragment)
                    try { commitNow() } catch (_: IllegalStateException) {
                        commitAllowingStateLoss() }
                }
            }
        } else {
            // 使用add, show方法，这时候就不要加动画了
            supportFragmentManager.beginTransaction().apply {
                add(R.id.dict_fragment, toFragment, fragmentTag)
                show(toFragment)
                try { commitNow() } catch (_: IllegalStateException) {
                    commitAllowingStateLoss() }
            }
        }
        dictViewModel.addedFragment.add(fragmentTag)
        dictViewModel.addedFragmentStack.add(fragmentTag)
        dictViewModel.currentFragmentTag = fragmentTag
    }
    private fun backFragment(){
        val cFragmentTag = dictViewModel.addedFragmentStack.pop()
        val tFragmentTag = dictViewModel.addedFragmentStack.peek()
        val toFragment = supportFragmentManager.findFragmentByTag(tFragmentTag) ?: return
        val fromFragment = supportFragmentManager.findFragmentByTag(cFragmentTag) ?: return
        // 使用hide, show方法
        supportFragmentManager.beginTransaction().apply {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            hide(fromFragment)
            show(toFragment)
            try { commitNow() } catch (_: IllegalStateException) {
                commitAllowingStateLoss() }
        }
        dictViewModel.currentFragmentTag = tFragmentTag
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val setPasteData = {
            if (this::dictViewModel.isInitialized){
                dictViewModel.setPasteData(
                    ClipboardUtils.hasClipboardText(mContext),
                    ClipboardUtils.getClipboardText(mContext)?.toString())
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler.createAsync(Looper.getMainLooper())
                .postDelayed(setPasteData, 300)
        } else {
            Handler(Looper.getMainLooper())
                .postDelayed(setPasteData, 300)
        }
    }
}