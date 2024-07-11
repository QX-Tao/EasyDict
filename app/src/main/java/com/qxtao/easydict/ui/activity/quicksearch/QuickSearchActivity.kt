package com.qxtao.easydict.ui.activity.quicksearch

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.quicksearch.QuickSearchWordLineAdapter
import com.qxtao.easydict.database.SearchHistoryData
import com.qxtao.easydict.database.SimpleDictData
import com.qxtao.easydict.databinding.ActivityQuickSearchBinding
import com.qxtao.easydict.ui.activity.dict.DICT_RV_HISTORY
import com.qxtao.easydict.ui.activity.dict.DICT_RV_SUGGESTION
import com.qxtao.easydict.ui.activity.dict.DICT_RV_SUGGESTION_LM
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.view.PerformEdit
import com.qxtao.easydict.utils.common.ClipboardUtils
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.SizeUtils

class QuickSearchActivity: BaseActivity<ActivityQuickSearchBinding>(ActivityQuickSearchBinding::inflate) {
    // define variable
    private lateinit var simpleDictData: SimpleDictData
    private lateinit var searchHistoryData: SearchHistoryData
    private lateinit var quickSearchViewModel: QuickSearchViewModel
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var performEdit: PerformEdit
    private lateinit var callback: OnBackPressedCallback
    private lateinit var rvSearchHistoryAdapter: QuickSearchWordLineAdapter<SearchHistoryData.SearchHistory>
    private lateinit var rvSearchSuggestionAdapter: QuickSearchWordLineAdapter<SimpleDictData.SimpleDict>
    private lateinit var snackBar: Snackbar
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean { return false }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return 0.3f
        }

        @SuppressLint("RestrictedApi")
        override fun onSwiped(
            viewHolder: RecyclerView.ViewHolder,
            direction: Int
        ) {
            val position = viewHolder.absoluteAdapterPosition
            if (direction == ItemTouchHelper.LEFT){
                // 往左滑，删除
                quickSearchViewModel.deleteSearchHistoryItem(position)
                if (quickSearchViewModel.getDeleteQueue().isNotEmpty()){
                    snackBar = Snackbar.make(viewHolder.itemView, getString(R.string.record_deleted), Snackbar.LENGTH_LONG)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_FADE) // 设置SnackBar动画的模式
                    val layout = snackBar.view as Snackbar.SnackbarLayout
                    if (layout.getChildAt(0) != null && layout.getChildAt(0) is SnackbarContentLayout) {
                        val contentLayout = layout.getChildAt(0) as SnackbarContentLayout
                        layout.setPadding(
                            SizeUtils.dp2px(16f), 0,
                            SizeUtils.dp2px(16f),
                            SizeUtils.dp2px(24f))
                        layout.setBackgroundColor(Color.TRANSPARENT)
                        contentLayout.setPadding(
                            SizeUtils.dp2px(12f), SizeUtils.dp2px(2f), SizeUtils.dp2px(12f),
                            SizeUtils.dp2px(2f))
                        contentLayout.background = ContextCompat.getDrawable(mContext, R.drawable.bg_shape_r16)
                        contentLayout.backgroundTintList = ColorStateList.valueOf(ColorUtils.colorSurfaceInverse(mContext))
                    }
                    snackBar.setAction(getString(R.string.undo_delete)) { quickSearchViewModel.undoDeleteWord() }
                        .setTextColor(ColorUtils.colorOnSurfaceInverse(mContext))
                        .setActionTextColor(ColorUtils.colorPrimaryInverse(mContext))
                        .show()
                    snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            quickSearchViewModel.deleteSearchRecord()
                            super.onDismissed(transientBottomBar, event)
                        }
                    })
                }
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE){
                viewHolder?.itemView?.setBackgroundColor(0)
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // 绘制滑动背景
                val itemView = viewHolder.itemView
                val background: ShapeDrawable
                if (dX < 0) {
                    // 往左滑，红色背景，删除
                    background = ShapeDrawable(RectShape())
                    background.setBounds(
                        maxOf(itemView.right - 0.3f * itemView.width, itemView.right + dX).toInt(),
                        itemView.top, itemView.right, itemView.bottom)
                    background.paint.color = ColorUtils.colorError(mContext)
                    background.draw(c)
                }

                // 绘制滑动图标说明
                val textPaint = Paint().apply {
                    color = ColorUtils.colorOnError(mContext)
                    textSize = resources.getDimension(R.dimen.text_size_medium)
                }
                val iconMargin = SizeUtils.dp2px(20f)
                if (dX < 0){
                    // 往左滑，红色背景，删除
                    val deleteText = getString(R.string.delete)
                    val deleteIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_ash)!!
                    val textBounds2 = Rect()
                    textPaint.getTextBounds(deleteText, 0, deleteText.length, textBounds2)
                    val textX2 = itemView.right - iconMargin - textBounds2.width()
                    val textY2 = itemView.top + (itemView.height + textBounds2.height()) / 2 - 2
                    c.drawText(deleteText, textX2.toFloat(), textY2.toFloat(), textPaint)
                    val iconTop2 = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconBottom2 = iconTop2 + deleteIcon.intrinsicHeight
                    val iconRight2 = textX2 - SizeUtils.dp2px(5f)
                    val iconLeft2 = iconRight2 - deleteIcon.intrinsicWidth
                    deleteIcon.setBounds(iconLeft2, iconTop2, iconRight2, iconBottom2)
                    deleteIcon.draw(c)

                    // 绘制滑动动画
                    getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.itemView, maxOf(dX, -0.3f * itemView.width), dY, actionState, isCurrentlyActive)
                }
            } else {
                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                )
            }
        }
    })
    // define widget
    private lateinit var mcvSearchBox: MaterialCardView
    private lateinit var ivBack : ImageView
    private lateinit var ivClear : ImageView
    private lateinit var ivSearch : ImageView
    private lateinit var etSearchBox : EditText
    private lateinit var rvSearchHistory : RecyclerView
    private lateinit var rvSearchSuggestion : RecyclerView
    private lateinit var flQuickSearchHas : FrameLayout
    private lateinit var clQuickSearchPanel : ConstraintLayout
    private lateinit var ivRedo: ImageView
    private lateinit var ivBackspace: ImageView
    private lateinit var ivUndo: ImageView
    private lateinit var tvPaste: TextView
    private lateinit var tvClear: TextView
    private lateinit var tvSearch: TextView
    private lateinit var cvPaste: CardView
    private lateinit var cvClear: CardView

    override fun onCreate1(savedInstanceState: Bundle?) {
        super.onCreate1(savedInstanceState)
        simpleDictData = SimpleDictData(mContext)
        searchHistoryData = SearchHistoryData(mContext)
        quickSearchViewModel = ViewModelProvider(this@QuickSearchActivity,
            QuickSearchViewModel.Factory(simpleDictData, searchHistoryData))[QuickSearchViewModel::class.java]
    }

    override fun onCreate() {
        super.onCreate()
        dispatcher = onBackPressedDispatcher
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        dispatcher.addCallback(this, callback)
    }

    override fun bindViews() {
        ivBack = binding.ivBack
        ivClear = binding.ivClear
        ivSearch = binding.ivSearch
        etSearchBox = binding.etSearchBox
        mcvSearchBox = binding.mcvSearchBox
        rvSearchHistory = binding.rvSearchHistory
        rvSearchSuggestion = binding.rvSearchSuggestion
        flQuickSearchHas = binding.flQuickSearchHas
        clQuickSearchPanel = binding.clQuickSearchPanel
        ivRedo = binding.ivRedo
        ivUndo = binding.ivUndo
        ivBackspace = binding.ivBackspace
        tvPaste = binding.tvPaste
        tvClear = binding.tvClear
        tvSearch = binding.tvSearch
        cvPaste = binding.cvPaste
        cvClear = binding.cvClear
    }

    override fun initViews() {
        performEdit = PerformEdit(etSearchBox)
        rvSearchHistoryAdapter = QuickSearchWordLineAdapter(ArrayList())
        rvSearchSuggestionAdapter = QuickSearchWordLineAdapter(ArrayList())
        rvSearchHistory.adapter = rvSearchHistoryAdapter
        rvSearchSuggestion.adapter = rvSearchSuggestionAdapter
        rvSearchHistory.layoutManager = LinearLayoutManager(mContext)
        rvSearchSuggestion.layoutManager = LinearLayoutManager(mContext)
        quickSearchViewModel.setHasShowRvInfo(if(etSearchBox.text.isEmpty()) DICT_RV_HISTORY else DICT_RV_SUGGESTION)
        etRequestFocus(etSearchBox)
        itemTouchHelper.attachToRecyclerView(rvSearchHistory)
        quickSearchViewModel.getDictSearchHistory()

        quickSearchViewModel.hasShowRvInfo.observe(this){
            when(it){
                DICT_RV_HISTORY -> {
                    rvSearchHistory.visibility = View.VISIBLE
                    rvSearchSuggestion.visibility = View.GONE
                }
                DICT_RV_SUGGESTION -> {
                    if (etSearchBox.text.isEmpty()){
                        quickSearchViewModel.setHasShowRvInfo(DICT_RV_HISTORY)
                    } else {
                        rvSearchHistory.visibility = View.GONE
                        rvSearchSuggestion.visibility = View.VISIBLE
                    }
                }
                DICT_RV_SUGGESTION_LM -> {
                    if (etSearchBox.text.isEmpty()){
                        quickSearchViewModel.setHasShowRvInfo(DICT_RV_HISTORY)
                    } else {
                        rvSearchHistory.visibility = View.GONE
                        rvSearchSuggestion.visibility = View.GONE
                    }
                }
            }
        }
        quickSearchViewModel.dictSearchHistory.observe(this){
            rvSearchHistoryAdapter.setData(it)
        }
        quickSearchViewModel.dictSearchSuggestion.observe(this){
            rvSearchSuggestionAdapter.setData(it)
        }
        quickSearchViewModel.pasteData.observe(this){
            cvPaste.visibility = if (it.first && !it.second.isNullOrBlank() && etSearchBox.text.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun addListener() {
        ivBack.setOnClickListener { dispatcher.onBackPressed() }
        flQuickSearchHas.setOnClickListener { dispatcher.onBackPressed() }
        ivRedo.setOnClickListener { performEdit.redo() }
        ivUndo.setOnClickListener { performEdit.undo() }
        ivBackspace.apply {
            val handler = Handler(Looper.getMainLooper())
            var isDeleting = false
            val deleteRunnable = object : Runnable {
                override fun run() {
                    if (isDeleting) {
                        etSearchBox.text?.let {
                            if (it.isNotEmpty()) {
                                val start = etSearchBox.selectionStart
                                val end = etSearchBox.selectionEnd
                                if (start != end) {
                                    // 有选中文本时，删除选中的文本
                                    it.delete(start, end)
                                } else if (start > 0) {
                                    // 没有选中文本时，删除光标前一个字符
                                    it.delete(start - 1, start)
                                }
                            }
                        }
                        handler.postDelayed(this, 50) // 间隔 50 毫秒执行下一次删除
                    }
                }
            }
            setOnClickListener {
                etSearchBox.text?.let {
                    if (it.isNotEmpty()) {
                        val start = etSearchBox.selectionStart
                        val end = etSearchBox.selectionEnd
                        if (start != end) {
                            // 有选中文本时，删除选中的文本
                            it.delete(start, end)
                        } else if (start > 0) {
                            // 没有选中文本时，删除光标前一个字符
                            it.delete(start - 1, start)
                        }
                    }
                }
            }
            setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                    // 停止连续删除
                    isDeleting = false
                    handler.removeCallbacks(deleteRunnable)
                }
                false
            }
            setOnLongClickListener {
                // 长按快速删除
                isDeleting = true
                handler.postDelayed(deleteRunnable, 100) // 间隔 100 毫秒执行第一次删除
                true
            }
        }
        tvPaste.setOnClickListener {
            quickSearchViewModel.pasteData.value?.second?.let {
                etSearchBox.setText(it)
                etSearchBox.setSelection(etSearchBox.text?.length ?: 0)
            }
        }
        ivClear.setOnClickListener {
            etSearchBox.setText(getString(R.string.empty_string))
            etRequestFocus(etSearchBox)
        }
        tvClear.setOnClickListener {
            etSearchBox.setText(getString(R.string.empty_string))
            etRequestFocus(etSearchBox)
        }
        ivSearch.setOnClickListener {
            if (etSearchBox.text.isNullOrBlank()){
                showShortToast(getString(R.string.invalid_input))
                return@setOnClickListener
            }
            toDictSearch(etSearchBox.text.toString())
        }
        tvSearch.setOnClickListener {
            if (etSearchBox.text.isNullOrBlank()){
                showShortToast(getString(R.string.invalid_input))
                return@setOnClickListener
            }
            toDictSearch(etSearchBox.text.toString())
        }
        etSearchBox.doAfterTextChanged {
            if (!etSearchBox.text.isNullOrEmpty()){
                ivSearch.visibility = View.VISIBLE
                ivClear.visibility = View.VISIBLE
                cvClear.visibility = View.VISIBLE
                quickSearchViewModel.searchInWordData(it.toString())
            } else {
                quickSearchViewModel.setHasShowRvInfo(DICT_RV_HISTORY)
                ivSearch.visibility = View.GONE
                ivClear.visibility = View.GONE
                cvClear.visibility = View.GONE
            }
            quickSearchViewModel.setPasteData(etSearchBox.text.isNullOrEmpty())
            ivRedo.isSelected = performEdit.isHasRedos()
            ivUndo.isSelected = performEdit.isHasUndos()
        }
        etSearchBox.setOnFocusChangeListener { _, hasFocus ->
            clQuickSearchPanel.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
        etSearchBox.setOnEditorActionListener { _, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH || (keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)){
                if (etSearchBox.text.isNullOrBlank()){
                    showShortToast(getString(R.string.invalid_input))
                    return@setOnEditorActionListener true
                }
                toDictSearch(etSearchBox.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        rvSearchHistoryAdapter.setOnItemClickListener(object : QuickSearchWordLineAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                toDictSearch(quickSearchViewModel.dictSearchHistory.value!![position].origin)
            }
        })
        rvSearchSuggestionAdapter.setOnItemClickListener(object : QuickSearchWordLineAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                toDictSearch(quickSearchViewModel.dictSearchSuggestion.value!![position].origin)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::snackBar.isInitialized) if (snackBar.isShown) snackBar.dismiss()
        if (this::quickSearchViewModel.isInitialized) quickSearchViewModel.deleteSearchRecord()
    }

    private fun toDictSearch(str: String){
        DictActivity.onSearchStr(mContext, str)
        finish()
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

    // 重写函数dispatchTouchEvent，实现点击搜索框外时，收起软键盘
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 排除部分控件
        if (isTouchPointInView(clQuickSearchPanel, ev.x.toInt(), ev.y.toInt()))
            return super.dispatchTouchEvent(ev)
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val setPasteData = {
            if (this::quickSearchViewModel.isInitialized){
                quickSearchViewModel.setPasteData(
                    ClipboardUtils.hasClipboardText(mContext),
                    ClipboardUtils.getClipboardText(mContext)?.toString())
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler.createAsync(Looper.getMainLooper())
                .postDelayed(setPasteData, 200)
        } else {
            Handler(Looper.getMainLooper())
                .postDelayed(setPasteData, 200)
        }
    }

}