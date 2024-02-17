package com.qxtao.easydict.ui.fragment.dict

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dict.DictWordLineAdapter
import com.qxtao.easydict.database.SearchHistoryData
import com.qxtao.easydict.database.SimpleDictData
import com.qxtao.easydict.databinding.FragmentDictHasBinding
import com.qxtao.easydict.ui.activity.dict.DICT_RV_HISTORY
import com.qxtao.easydict.ui.activity.dict.DICT_RV_SUGGESTION
import com.qxtao.easydict.ui.activity.dict.DICT_RV_SUGGESTION_LM
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.factory.isLandscape
import com.qxtao.easydict.utils.factory.screenRotation


class DictHasFragment : BaseFragment<FragmentDictHasBinding>(FragmentDictHasBinding::inflate) {
    // define variable
    private lateinit var dictViewModel: DictViewModel
    private lateinit var rvSearchHistoryAdapter: DictWordLineAdapter<SearchHistoryData.SearchHistory>
    private lateinit var rvSearchSuggestionAdapter: DictWordLineAdapter<SimpleDictData.SimpleDict>
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
                dictViewModel.deleteSearchHistoryItem(position)
                if (dictViewModel.getDeleteQueue().isNotEmpty()){
                    snackBar = Snackbar.make(viewHolder.itemView, getString(R.string.record_deleted), Snackbar.LENGTH_LONG)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_FADE) // 设置SnackBar动画的模式
                    val layout = snackBar.view as Snackbar.SnackbarLayout
                    if (layout.getChildAt(0) != null && layout.getChildAt(0) is SnackbarContentLayout) {
                        val contentLayout = layout.getChildAt(0) as SnackbarContentLayout
                        layout.setPadding(
                            SizeUtils.dp2px(12f), 0,
                            SizeUtils.dp2px(12f),
                            SizeUtils.dp2px(16f))
                        layout.setBackgroundColor(Color.parseColor("#00000000"))
                        contentLayout.setPadding(SizeUtils.dp2px(12f), 0, SizeUtils.dp2px(12f),0)
                        contentLayout.background = ContextCompat.getDrawable(mContext, R.drawable.sp_radius_r15)
                        contentLayout.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CC000000"))
                    }
                    snackBar.setAction(getString(R.string.undo_delete)) { dictViewModel.undoDeleteWord() }
                        .setTextColor(Color.parseColor("#FFEFEFEF"))
                        .setActionTextColor(Color.parseColor("#FFF9CD16"))
                        .show()
                    snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            dictViewModel.deleteSearchRecord()
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
                val cornerRadius = SizeUtils.dp2px(15f).toFloat() // 圆角半径
                val background: ShapeDrawable
                if (dX < 0) {
                    // 往左滑，红色背景，删除
                    background = ShapeDrawable(
                        RoundRectShape(
                            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius),
                            null,
                            null
                        )
                    )
                    background.setBounds(
                        maxOf((itemView.right + dX - SizeUtils.dp2px(30f)).toInt(), itemView.left),
                        itemView.top, itemView.right, itemView.bottom)
                    background.paint.color = ContextCompat.getColor(mContext, R.color.colorBgRed1)
                    background.draw(c)
                }

                // 绘制滑动图标说明
                val textPaint = Paint().apply {
                    color = ContextCompat.getColor(mContext, R.color.secondTextColor)
                    textSize = SizeUtils.sp2px(13f).toFloat()
                }
                val iconMargin = SizeUtils.dp2px(20f)
                if (dX < 0){
                    // 往左滑，红色背景，删除
                    val deleteText = getString(R.string.delete)
                    val deleteIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_ash4)!!
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
    private lateinit var rvSearchHistory : RecyclerView
    private lateinit var rvSearchSuggestion : RecyclerView

    override fun bindViews() {
        rvSearchHistory = binding.rvSearchHistory
        rvSearchSuggestion = binding.rvSearchSuggestion
    }

    override fun initViews() {
        mListener.onFragmentInteraction("changeSearchFragmentBackgroundColor")
        dictViewModel = (activity as DictActivity).getDictViewModel()
        (parentFragment as DictSearchFragment).getEditTextFocus()
        rvSearchHistoryAdapter = DictWordLineAdapter(ArrayList())
        rvSearchSuggestionAdapter = DictWordLineAdapter(ArrayList())
        rvSearchHistory.adapter = rvSearchHistoryAdapter
        rvSearchSuggestion.adapter = rvSearchSuggestionAdapter
        rvSearchHistory.layoutManager = LinearLayoutManager(activity)
        rvSearchSuggestion.layoutManager = LinearLayoutManager(activity)
        dictViewModel.dictSearchHistory.observe(this){
            rvSearchHistoryAdapter.setData(it)
        }
        dictViewModel.dictSearchSuggestion.observe(this){
            rvSearchSuggestionAdapter.setData(it)
        }
        dictViewModel.setHasShowRvInfo(if (dictViewModel.searchText.value?.editSearchText.isNullOrEmpty()) DICT_RV_HISTORY else DICT_RV_SUGGESTION)
        rvSearchHistory.visibility = if (dictViewModel.searchText.value?.editSearchText.isNullOrEmpty()) View.GONE else View.VISIBLE
        dictViewModel.hasShowRvInfo.observe(this){
            when(it){
                DICT_RV_HISTORY -> {
                        rvSearchHistory.visibility = View.VISIBLE
                        rvSearchSuggestion.visibility = View.GONE
                }
                DICT_RV_SUGGESTION -> {
                    if ((parentFragment as DictSearchFragment).getSearchBoxText().isEmpty()){
                        dictViewModel.setHasShowRvInfo(DICT_RV_HISTORY)
                    } else {
                        rvSearchHistory.visibility = View.GONE
                        rvSearchSuggestion.visibility = View.VISIBLE
                    }
                }
                DICT_RV_SUGGESTION_LM -> {
                    if ((parentFragment as DictSearchFragment).getSearchBoxText().isEmpty()){
                        dictViewModel.setHasShowRvInfo(DICT_RV_HISTORY)
                    } else {
                        rvSearchHistory.visibility = View.GONE
                        rvSearchSuggestion.visibility = View.GONE
                    }
                }
            }
        }
        itemTouchHelper.attachToRecyclerView(rvSearchHistory)
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(rvSearchHistory){ view, insets ->
            view.setPadding(SizeUtils.dp2px(16f), 0, SizeUtils.dp2px(16f), insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(rvSearchSuggestion){ view, insets ->
            view.setPadding(SizeUtils.dp2px(16f), 0, SizeUtils.dp2px(16f), insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }
        rvSearchHistoryAdapter.setOnItemClickListener(object : DictWordLineAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                mListener.onFragmentInteraction("toDetailFragment", dictViewModel.dictSearchHistory.value!![position].origin)
            }
        })
        rvSearchSuggestionAdapter.setOnItemClickListener(object : DictWordLineAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                mListener.onFragmentInteraction("toDetailFragment", dictViewModel.dictSearchSuggestion.value!![position].origin)
            }
        })
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) { //表示是一个进入动作，比如add.show等
            return if (enter) { //普通的进入的动作
                AnimationUtils.loadAnimation(mContext, R.anim.anim_romove_anim_in)
            } else { //比如一个已经Fragment被另一个replace，是一个进入动作，被replace的那个就是false
                AnimationUtils.loadAnimation(mContext, R.anim.anim_romove_anim_out)
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) { //表示一个退出动作，比如出栈，hide，detach等
            return if (enter) { //之前被replace的重新进入到界面或者Fragment回到栈顶
                AnimationUtils.loadAnimation(mContext, R.anim.anim_romove_anim_in)
            } else { //Fragment退出，出栈
                AnimationUtils.loadAnimation(mContext, R.anim.anim_romove_anim_out)
            }
        }
        return null
    }
    override fun onDestroy() {
        super.onDestroy()
        if (this::snackBar.isInitialized) if (snackBar.isShown) snackBar.dismiss()
        if (this::dictViewModel.isInitialized) dictViewModel.deleteSearchRecord()
    }

    override fun onResume() {
        dictViewModel.getDictSearchHistory()
        super.onResume()
    }


}