package com.qxtao.easydict.ui.activity.wordlist

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.popupmenu.PopupMenuAdapter
import com.qxtao.easydict.adapter.popupmenu.PopupMenuItem
import com.qxtao.easydict.adapter.wordlist.WordListAdapter
import com.qxtao.easydict.database.WordListData
import com.qxtao.easydict.databinding.ActivityWordListBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.view.CustomPopWindow
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.factory.screenRotation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WordListActivity : BaseActivity<ActivityWordListBinding>(ActivityWordListBinding::inflate){
    // define weight
    private lateinit var ivMoreButton : ImageView
    private lateinit var ivBackButton : ImageView
    private lateinit var tvTitle : TextView
    private lateinit var rvListWord: RecyclerView
    private lateinit var lvLoading : LoadingView
    private lateinit var llLoadingFail : LinearLayout
    private lateinit var llListEmpty : LinearLayout
    private lateinit var tvListInfo : TextView
    private lateinit var tvListCategory : TextView
    private lateinit var mcvListTop : MaterialCardView
    private lateinit var mcvVoice: MaterialCardView
    private lateinit var btSwitchVoice: Button
    private lateinit var vHolder: View
    private lateinit var snackBar: Snackbar

    // define variable
    private var isInitView = false
    private lateinit var wordListData: WordListData
    private lateinit var wordListViewModel: WordListViewModel
    private lateinit var adapter: WordListAdapter
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
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
            when (direction){
                ItemTouchHelper.LEFT -> {
                    when (wordListViewModel.getClasPopupMenuListSelectedPosition()) {
                        1, 2, 3 -> {
                            // 往左滑，删除
                            wordListViewModel.deleteWord(position)
                            if (wordListViewModel.getDeleteQueue().isNotEmpty()){
                                snackBar = Snackbar.make(viewHolder.itemView,
                                    when (wordListViewModel.getClasPopupMenuListSelectedPosition()){
                                        1 -> getString(R.string.word_mark_learned)
                                        2 -> getString(R.string.word_mark_learning)
                                        else -> getString(R.string.word_mark_collect)
                                    },
                                    Snackbar.LENGTH_LONG)
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_FADE) // 设置SnackBar动画的模式
                                val layout = snackBar.view as Snackbar.SnackbarLayout
                                if (layout.getChildAt(0) != null && layout.getChildAt(0) is SnackbarContentLayout) {
                                    val contentLayout = layout.getChildAt(0) as SnackbarContentLayout
                                    layout.setPadding(SizeUtils.dp2px(12f), 0,
                                        SizeUtils.dp2px(12f),
                                        SizeUtils.dp2px(16f))
                                    layout.setBackgroundColor(Color.parseColor("#00000000"))
                                    contentLayout.setPadding(SizeUtils.dp2px(12f), 0, SizeUtils.dp2px(12f),0)
                                    contentLayout.background = ContextCompat.getDrawable(mContext, R.drawable.sp_radius_r15)
                                    contentLayout.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CC000000"))
                                }
                                snackBar.setAction(getString(R.string.undo_operate)) { wordListViewModel.undoDeleteWord() }
                                    .setTextColor(Color.parseColor("#FFEFEFEF"))
                                    .setActionTextColor(Color.parseColor("#FFF9CD16"))
                                    .show()
                                snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                        when (event){
                                            DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE, DISMISS_EVENT_SWIPE,
                                            DISMISS_EVENT_TIMEOUT -> { wordListViewModel.deleteWordRecord() }
                                        }
                                        super.onDismissed(transientBottomBar, event)
                                    }
                                })
                            }
                        }
                    }
                }
                ItemTouchHelper.RIGHT -> {
                    when (wordListViewModel.getClasPopupMenuListSelectedPosition()) {
                        0,1,2 -> {
                            showShortToast(getString(R.string.collect_success))
                            wordListViewModel.collectWord(position)
                        }
                    }
                }
            }
            adapter.notifyItemChanged(position)
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
                val cornerRadius = SizeUtils.dp2px(15f).toFloat() // 圆角半径
                val background = ShapeDrawable(RoundRectShape(
                    floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                        cornerRadius), null, null))
                if (dX > 0 && wordListViewModel.getClasPopupMenuListSelectedPosition() != 3) {
                    // 往右滑，黄色背景，收藏
                    background.setBounds(itemView.left, itemView.top,
                        minOf((itemView.left + dX + SizeUtils.dp2px(30f)).toInt(), itemView.right), itemView.bottom)
                    background.paint.color = getColor(R.color.colorBgLightYellow1)
                    background.draw(c)
                } else if (dX < 0f && wordListViewModel.getClasPopupMenuListSelectedPosition() != 0) {
                    // 往左滑，红色背景，删除
                    background.setBounds(
                        maxOf((itemView.right + dX - SizeUtils.dp2px(30f)).toInt(), itemView.left),
                        itemView.top, itemView.right, itemView.bottom)
                    background.paint.color = getColor(R.color.colorBgRed1)
                    background.draw(c)
                }

                // 绘制滑动图标说明
                val textPaint = Paint().apply {
                    color = getColor(R.color.secondTextColor)
                    textSize = SizeUtils.sp2px(13f).toFloat()
                }
                val iconMargin = SizeUtils.dp2px(20f)
                if (dX > 0 && wordListViewModel.getClasPopupMenuListSelectedPosition() != 3){
                    // 往右滑，黄色背景，收藏
                    val infoText = getString(R.string.collect)
                    val infoIcon = ContextCompat.getDrawable(this@WordListActivity, R.drawable.ic_collect)!!
                    val iconTop1 = itemView.top + (itemView.height - infoIcon.intrinsicHeight) / 2
                    val iconBottom1 = iconTop1 + infoIcon.intrinsicHeight
                    val iconLeft1 = itemView.left + iconMargin
                    val iconRight1 = iconLeft1 + infoIcon.intrinsicWidth
                    infoIcon.setBounds(iconLeft1, iconTop1, iconRight1, iconBottom1)
                    infoIcon.draw(c)
                    val textBounds1 = Rect()
                    textPaint.getTextBounds(infoText, 0, infoText.length, textBounds1)
                    val textX1 = iconRight1 + SizeUtils.dp2px(5f)
                    val textY1 = itemView.top + (itemView.height + textBounds1.height()) / 2 - 2
                    c.drawText(infoText, textX1.toFloat(), textY1.toFloat(), textPaint)
                    // 绘制滑动动画
                    getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.itemView, minOf(dX, 0.3f * itemView.width), dY, actionState, isCurrentlyActive)
                } else if (dX < 0f && wordListViewModel.getClasPopupMenuListSelectedPosition() != 0) {
                    // 往左滑，红色背景，删除
                    val deleteText = getString(R.string.delete)
                    val deleteIcon = ContextCompat.getDrawable(this@WordListActivity, R.drawable.ic_ash4)!!
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

    override fun onCreate() {
        wordListData = WordListData(mContext)
        wordListViewModel = ViewModelProvider(this, WordListViewModel.Factory(wordListData))[WordListViewModel::class.java]
    }

    override fun initViews() {
        tvTitle.text = getString(R.string.word_list)
        ivMoreButton.setImageResource(R.drawable.ic_switch)
        ivMoreButton.tooltipText = getString(R.string.switch_list)
        tvListCategory.text = wordListViewModel.clasPopWindowList.find {
            it.isMenuItemSelected }?.menuItemText ?: getString(R.string.all_words)
        isInitView = true
        wordListViewModel.initData()
        rvListWord.layoutManager = LinearLayoutManager(this)
        adapter = WordListAdapter(ArrayList())
        rvListWord.adapter = adapter
        itemTouchHelper.attachToRecyclerView(rvListWord)
        wordListViewModel.wordListItems.observe(this){ itemList ->
            adapter.setData(itemList)
            tvListInfo.text = String.format(getString(R.string.word_num_eee), itemList?.size)
        }
        wordListViewModel.wordSelected.observe(this){
            tvTitle.text = it
        }
        wordListViewModel.clasSelected.observe(this){
            tvListCategory.text = it ?: getString(R.string.all_words)
        }
        wordListViewModel.dataLoadInfo.observe(this){
            when (it) {
                0 -> lvLoading.visibility = View.VISIBLE
                1 -> {
                    llListEmpty.visibility = View.GONE
                    lvLoading.visibility = View.GONE
                }
                2 -> llLoadingFail.visibility = View.VISIBLE
                3 -> {
                    lvLoading.visibility = View.GONE
                    llListEmpty.visibility = View.VISIBLE
                }
            }
        }
        wordListViewModel.isPlaying.observe(this){
            mcvVoice.visibility = if (it) View.VISIBLE else View.GONE
        }
        wordListViewModel.playSound.observe(this){
            when (it) {
                0 -> btSwitchVoice.text = getString(R.string.mei)
                1 -> btSwitchVoice.text = getString(R.string.ying)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            binding.appBarLayout.setExpanded(wordListViewModel.appBarExpanded, false)
            val lm = rvListWord.layoutManager as? LinearLayoutManager ?: return@launch
            lm.scrollToPositionWithOffset(wordListViewModel.firstVisibleItemPosition, wordListViewModel.topOffset)
            isInitView = false
        }
    }

    override fun bindViews() {
        tvTitle = binding.includeTitleBarSecond.tvTitle
        ivBackButton = binding.includeTitleBarSecond.ivBackButton
        ivMoreButton = binding.includeTitleBarSecond.ivMoreButton
        rvListWord = binding.rvListWord
        lvLoading = binding.lvLoading
        llLoadingFail = binding.llLoadingFail
        llListEmpty = binding.llListEmpty
        tvListInfo = binding.tvListInfo
        tvListCategory = binding.tvListCategory
        mcvListTop = binding.mcvListTop
        mcvVoice = binding.mcvVoice
        btSwitchVoice = binding.btSwitchVoice
        vHolder = binding.vHolder
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(vHolder){ view, insets ->
            val displayCutout = insets.displayCutout
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            rvListWord.setPadding(SizeUtils.dp2px(16f), 0, SizeUtils.dp2px(16f), insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            params.topMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + SizeUtils.dp2px(56f)
            when (screenRotation){
                90 -> {
                    params.leftMargin = displayCutout?.safeInsetLeft ?: insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()).left
                    params.rightMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).right
                }
                270 -> {
                    params.rightMargin = displayCutout?.safeInsetRight ?: insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()).right
                    params.leftMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).left
                }
            }
            insets
        }
        ivBackButton.setOnClickListener { finish() }
        binding.appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (!isInitView) {
                wordListViewModel.appBarExpanded = verticalOffset == 0
            }
        }
        mcvListTop.setOnClickListener{
            showPopupMenu(mcvListTop, wordListViewModel.clasPopWindowList).also {
                it.second.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
                    override fun onMenuItemClick(position: Int) {
                        wordListViewModel.deleteWordRecord()
                        if (this@WordListActivity::snackBar.isInitialized) if (snackBar.isShown) snackBar.dismiss()
                        tvListCategory.text = wordListViewModel.clasPopWindowList[position].menuItemText
                        wordListViewModel.setClasSelected(position)
                        it.first.dissmiss()
                    }
                })
            }
        }
        ivMoreButton.setOnClickListener {
            showPopupMenu(ivMoreButton, wordListViewModel.wordPopWindowList).also {
                it.second.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
                    override fun onMenuItemClick(position: Int) {
                        wordListViewModel.deleteWordRecord()
                        if (this@WordListActivity::snackBar.isInitialized) if (snackBar.isShown) snackBar.dismiss()
                        wordListViewModel.setWordSelected(position)
                        wordListViewModel.setClasSelected(0)
                        it.first.dissmiss()
                    }
                })
            }
        }
        llLoadingFail.setOnClickListener {
            wordListViewModel.initData()
            llLoadingFail.visibility = View.GONE
        }
        btSwitchVoice.setOnClickListener {
            val map = mapOf(0 to getString(R.string.ying), 1 to getString(R.string.mei))
            showShortToast(String.format(getString(R.string.switch_to_eee_voice), map[wordListViewModel.playSound.value]))
            wordListViewModel.switchVoice()
        }
        adapter.setOnTextMeanClickListener(object : WordListAdapter.OnTextMeanClickListener{
            override fun onTextMeanClick(position: Int) {
                wordListViewModel.setWordOnclick(position)
            }
        })
        adapter.setOnTextWordClickListener(object : WordListAdapter.OnTextWordClickListener{
            override fun onTextWordClick(position: Int) {
                wordListViewModel.playWordVoice(position)
            }
        })
        rvListWord.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (!isInitView) {
                    wordListViewModel.firstVisibleItemPosition =
                        lm.findFirstVisibleItemPosition()
                    wordListViewModel.topOffset =
                        lm.findViewByPosition(wordListViewModel.firstVisibleItemPosition)?.top
                            ?: 0
                }
            }
        })

    }

    private fun showPopupMenu(view: View, popWindowList: List<PopupMenuItem>): Pair<CustomPopWindow, PopupMenuAdapter>{
        val contentView = LayoutInflater.from(this).inflate(R.layout.pop_menu_recycleview, null)
        val popWindow = CustomPopWindow.PopupWindowBuilder(this)
            .setView(contentView)
            .enableBackgroundDark(true)
            .create()
            .showAsDropDown(view,view.right - SizeUtils.dp2px(196f), view.top - view.bottom)
        val recycleView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = PopupMenuAdapter(popWindowList)
        recycleView.adapter = adapter
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter.selectItem(popWindowList.find { it.isMenuItemSelected }?.position ?: 0)
        adapter.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
            override fun onMenuItemClick(position: Int) {
                popWindow.dissmiss()
            }
        })
        return Pair(popWindow, adapter)
    }

    override fun onDestroy1() {
        super.onDestroy1()
        if (this::snackBar.isInitialized) if (snackBar.isShown) snackBar.dismiss()
        if (this::wordListViewModel.isInitialized) {
            wordListViewModel.deleteWordRecord()
            wordListViewModel.stopPlaySound()
        }
    }
}