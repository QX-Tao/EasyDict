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
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
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
import com.qxtao.easydict.ui.view.LoadFailedView
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.constant.ShareConstant.DEF_VOICE
import com.qxtao.easydict.utils.constant.ShareConstant.MEI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordListActivity : BaseActivity<ActivityWordListBinding>(ActivityWordListBinding::inflate){
    // define weight
    private lateinit var rvListWord: RecyclerView
    private lateinit var lvLoading : LoadingView
    private lateinit var lvLoadFailed : LoadFailedView
    private lateinit var llListEmpty : LinearLayout
    private lateinit var snackBar: Snackbar
    private lateinit var mtTitle: MaterialToolbar
    private lateinit var swList: View

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
                                    layout.setPadding(SizeUtils.dp2px(16f), 0,
                                        SizeUtils.dp2px(16f),
                                        SizeUtils.dp2px(24f))
                                    layout.setBackgroundColor(Color.TRANSPARENT)
                                    contentLayout.setPadding(SizeUtils.dp2px(12f), SizeUtils.dp2px(2f), SizeUtils.dp2px(12f),SizeUtils.dp2px(2f))
                                    contentLayout.background = ContextCompat.getDrawable(mContext, R.drawable.bg_shape_r16)
                                    contentLayout.backgroundTintList = ColorStateList.valueOf(ColorUtils.colorSurfaceInverse(mContext))
                                }
                                snackBar.setAction(getString(R.string.undo_operate)) { wordListViewModel.undoDeleteWord() }
                                    .setTextColor(ColorUtils.colorOnSurfaceInverse(mContext))
                                    .setActionTextColor(ColorUtils.colorPrimaryInverse(mContext))
                                    .show()
                                snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                        wordListViewModel.deleteWordRecord()
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
                            snackBar = Snackbar.make(viewHolder.itemView, getString(R.string.collect_success), Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                            val layout = snackBar.view as Snackbar.SnackbarLayout
                            if (layout.getChildAt(0) != null && layout.getChildAt(0) is SnackbarContentLayout) {
                                val contentLayout = layout.getChildAt(0) as SnackbarContentLayout
                                layout.setPadding(SizeUtils.dp2px(16f), 0,
                                    SizeUtils.dp2px(16f),
                                    SizeUtils.dp2px(24f))
                                layout.setBackgroundColor(Color.TRANSPARENT)
                                contentLayout.setPadding(SizeUtils.dp2px(12f), SizeUtils.dp2px(2f), SizeUtils.dp2px(12f),SizeUtils.dp2px(2f))
                                contentLayout.background = ContextCompat.getDrawable(mContext, R.drawable.bg_shape_r16)
                                contentLayout.backgroundTintList = ColorStateList.valueOf(ColorUtils.colorSurfaceInverse(mContext))
                            }
                            snackBar.setTextColor(ColorUtils.colorOnSurfaceInverse(mContext))
                                .setActionTextColor(ColorUtils.colorPrimaryInverse(mContext))
                                .show()
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
                    background.paint.color = ColorUtils.colorPrimary(mContext)
                    background.draw(c)
                } else if (dX < 0f && wordListViewModel.getClasPopupMenuListSelectedPosition() != 0) {
                    // 往左滑，红色背景，删除
                    background.setBounds(
                        maxOf((itemView.right + dX - SizeUtils.dp2px(30f)).toInt(), itemView.left),
                        itemView.top, itemView.right, itemView.bottom)
                    background.paint.color = ColorUtils.colorError(mContext)
                    background.draw(c)
                }

                // 绘制滑动图标说明
                val textPaint1 = Paint().apply {
                    color = ColorUtils.colorOnPrimary(mContext)
                    textSize = resources.getDimension(R.dimen.text_size_medium)
                }
                val textPaint2 = Paint().apply {
                    color = ColorUtils.colorOnError(mContext)
                    textSize = resources.getDimension(R.dimen.text_size_medium)
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
                    textPaint1.getTextBounds(infoText, 0, infoText.length, textBounds1)
                    val textX1 = iconRight1 + SizeUtils.dp2px(5f)
                    val textY1 = itemView.top + (itemView.height + textBounds1.height()) / 2 - 2
                    c.drawText(infoText, textX1.toFloat(), textY1.toFloat(), textPaint1)
                    // 绘制滑动动画
                    getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.itemView, minOf(dX, 0.3f * itemView.width), dY, actionState, isCurrentlyActive)
                } else if (dX < 0f && wordListViewModel.getClasPopupMenuListSelectedPosition() != 0) {
                    // 往左滑，红色背景，删除
                    val deleteText = getString(R.string.delete)
                    val deleteIcon = ContextCompat.getDrawable(this@WordListActivity, R.drawable.ic_ash)!!
                    val textBounds2 = Rect()
                    textPaint2.getTextBounds(deleteText, 0, deleteText.length, textBounds2)
                    val textX2 = itemView.right - iconMargin - textBounds2.width()
                    val textY2 = itemView.top + (itemView.height + textBounds2.height()) / 2 - 2
                    c.drawText(deleteText, textX2.toFloat(), textY2.toFloat(), textPaint2)
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
        isInitView = true
        mtTitle.title = getString(R.string.word_list)
        wordListViewModel.initData()
        wordListViewModel.playSound.value = if (ShareUtils.getString(mContext, DEF_VOICE, MEI) == MEI) 0 else 1
        rvListWord.layoutManager = LinearLayoutManager(this)
        adapter = WordListAdapter(ArrayList())
        rvListWord.adapter = adapter
        itemTouchHelper.attachToRecyclerView(rvListWord)
        wordListViewModel.wordListItems.observe(this){ itemList ->
            adapter.setData(itemList)
            mtTitle.subtitle = getString(R.string.word_clas_num_eee,
                wordListViewModel.clasPopWindowList.find { it.isMenuItemSelected }?.menuItemText ?: getString(R.string.learning_words),
                itemList?.size)
        }
        wordListViewModel.wordSelected.observe(this){
            mtTitle.title = it
            rvListWord.scrollToPosition(0)
        }
        wordListViewModel.clasSelected.observe(this){
            mtTitle.subtitle = getString(R.string.word_clas_num_eee,
                wordListViewModel.clasPopWindowList.find { it.isMenuItemSelected }?.menuItemText ?: getString(R.string.learning_words),
                wordListViewModel.wordListItems.value?.size)
            rvListWord.scrollToPosition(0)
        }
        wordListViewModel.dataLoadInfo.observe(this){
            when (it) {
                0 -> {
                    swList.visibility = View.GONE
                    lvLoading.visibility = View.VISIBLE
                }
                1 -> {
                    swList.visibility = View.VISIBLE
                    llListEmpty.visibility = View.GONE
                    lvLoading.visibility = View.GONE
                }
                2 -> {
                    swList.visibility = View.GONE
                    lvLoadFailed.visibility = View.VISIBLE
                }
                3 -> {
                    lvLoading.visibility = View.GONE
                    swList.visibility = View.VISIBLE
                    llListEmpty.visibility = View.VISIBLE
                }
            }
        }
        val lm = rvListWord.layoutManager as? LinearLayoutManager ?: return
        CoroutineScope(Dispatchers.Main).launch {
            lm.scrollToPositionWithOffset(wordListViewModel.firstVisibleItemPosition, wordListViewModel.topOffset)
            isInitView = false
        }
    }

    override fun bindViews() {
        mtTitle = binding.mtTitle
        rvListWord = binding.rvListWord
        lvLoading = binding.lvLoading
        lvLoadFailed = binding.lvLoadFailed
        llListEmpty = binding.llListEmpty
        swList = findViewById(R.id.switch_list)
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener{ finish() }
        swList.setOnClickListener { v ->
            when(wordListViewModel.dataLoadInfo.value){
                1, 3 -> {
                    showPopupMenu(v, wordListViewModel.clasPopWindowList).also {
                        it.second.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
                            override fun onMenuItemClick(position: Int) {
                                wordListViewModel.deleteWordRecord()
                                if (this@WordListActivity::snackBar.isInitialized) if (snackBar.isShown) snackBar.dismiss()
                                mtTitle.subtitle = getString(R.string.word_clas_num_eee,
                                    wordListViewModel.clasPopWindowList[position].menuItemText,
                                    wordListViewModel.wordListItems.value?.size)
                                wordListViewModel.setClasSelected(position)
                                it.first.dismiss()
                            }
                        })
                    }
                }
                else -> {
                    showShortToast(getString(R.string.data_not_load))
                    return@setOnClickListener
                }
            }
        }
        swList.setOnLongClickListener { v ->
            when(wordListViewModel.dataLoadInfo.value){
                1, 3 -> {
                    showPopupMenu(v, wordListViewModel.wordPopWindowList).also {
                        it.second.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
                            override fun onMenuItemClick(position: Int) {
                                wordListViewModel.deleteWordRecord()
                                if (this@WordListActivity::snackBar.isInitialized) if (snackBar.isShown) snackBar.dismiss()
                                wordListViewModel.setWordSelected(position)
                                wordListViewModel.setClasSelected(1)
                                it.first.dismiss()
                            }
                        })
                    }
                }
                else -> showShortToast(getString(R.string.data_not_load))
            }
            true
        }
        lvLoadFailed.setOnClickListener {
            wordListViewModel.initData()
            lvLoadFailed.visibility = View.GONE
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
        val parentView = findViewById<ViewGroup>(android.R.id.content)
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.pop_menu_recycleview, parentView, false)
        val popWindow = CustomPopWindow.PopupWindowBuilder(mContext)
            .setView(contentView)
            .create()
        val recycleView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = PopupMenuAdapter(popWindowList)
        recycleView.adapter = adapter
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter.selectItem(popWindowList.find { it.isMenuItemSelected }?.position ?: 0)
        adapter.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
            override fun onMenuItemClick(position: Int) {
                popWindow.dismiss()
            }
        })
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        popWindow.popupWindow?.contentView?.measure(widthMeasureSpec, heightMeasureSpec)
        val xOff = view.right - (popWindow.popupWindow?.contentView?.measuredWidth ?: 0) - SizeUtils.dp2px(4f)
        val yOff = view.top - view.bottom
        popWindow.showAsDropDown(view, xOff, yOff)
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