package com.qxtao.easydict.ui.fragment.wordbook

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.wordbook.WordBookClassificationAdapter
import com.qxtao.easydict.databinding.FragmentWordBookClassificationBinding
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceActivity
import com.qxtao.easydict.ui.activity.wordbook.WORD_BOOK_MY_COLLECT
import com.qxtao.easydict.ui.activity.wordbook.WordBookActivity
import com.qxtao.easydict.ui.activity.wordbook.WordBookViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.factory.screenRotation

class WordBookClassificationFragment : BaseFragment<FragmentWordBookClassificationBinding>(FragmentWordBookClassificationBinding::inflate) {
    // define variable
    private lateinit var adapter: WordBookClassificationAdapter
    private lateinit var wordBookViewModel: WordBookViewModel
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
            if (position != 0){
                when (direction){
                    ItemTouchHelper.LEFT -> {
                        mListener.onFragmentInteraction("showDeleteWordBookDialog", position)
                    }
                    ItemTouchHelper.RIGHT -> {
                        mListener.onFragmentInteraction("showRenameWordBookDialog", position)
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
            if (viewHolder.absoluteAdapterPosition == 0) return
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // 绘制滑动背景
                val itemView = viewHolder.itemView
                val cornerRadius = SizeUtils.dp2px(15f).toFloat() // 圆角半径
                val background = ShapeDrawable(
                    RoundRectShape(
                        floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                            cornerRadius), null, null)
                )
                if (dX > 0) {
                    // 往右滑，黄色背景，重命名
                    background.setBounds(itemView.left, itemView.top,
                        minOf((itemView.left + dX + SizeUtils.dp2px(30f)).toInt(), itemView.right), itemView.bottom)
                    background.paint.color = requireActivity().getColor(R.color.colorBgLightYellow1)
                    background.draw(c)
                } else if (dX < 0f) {
                    // 往左滑，红色背景，删除
                    background.setBounds(
                        maxOf((itemView.right + dX - SizeUtils.dp2px(30f)).toInt(), itemView.left),
                        itemView.top, itemView.right, itemView.bottom)
                    background.paint.color = requireActivity().getColor(R.color.colorBgRed1)
                    background.draw(c)
                }

                // 绘制滑动图标说明
                val textPaint = Paint().apply {
                    color = requireActivity().getColor(R.color.secondTextColor)
                    textSize = SizeUtils.sp2px(13f).toFloat()
                }
                val iconMargin = SizeUtils.dp2px(20f)
                if (dX > 0 ){
                    // 往右滑，黄色背景，重命名
                    val infoText = getString(R.string.rename)
                    val infoIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_rename)!!
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
                } else if (dX < 0f) {
                    // 往左滑，红色背景，删除
                    val deleteText = getString(R.string.delete)
                    val deleteIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_ash4)!!
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
    private lateinit var ivMoreButton : ImageView
    private lateinit var ivBackButton : ImageView
    private lateinit var tvTitle : TextView
    private lateinit var vHolder: View
    private lateinit var mcvWordList: MaterialCardView
    private lateinit var rvWordBook: RecyclerView


    override fun bindViews() {
        tvTitle = binding.includeTitleBarSecond.tvTitle
        ivBackButton = binding.includeTitleBarSecond.ivBackButton
        ivMoreButton = binding.includeTitleBarSecond.ivMoreButton
        vHolder = binding.vHolder
        mcvWordList = binding.mcvWordList
        rvWordBook = binding.rvWordBook
    }

    override fun initViews() {
        wordBookViewModel = (activity as WordBookActivity).getWordBookViewModel()
        tvTitle.text = getString(R.string.my_word_book)
        ivMoreButton.setImageResource(R.drawable.ic_add_folder)
        ivMoreButton.tooltipText = getString(R.string.add_word_book)
        rvWordBook.layoutManager = LinearLayoutManager(requireActivity())
        adapter = WordBookClassificationAdapter(ArrayList())
        rvWordBook.adapter = adapter
        itemTouchHelper.attachToRecyclerView(rvWordBook)
        wordBookViewModel.wordBookList.observe(this){
            adapter.setData(it)
        }

    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(vHolder){ view, insets ->
            val displayCutout = insets.displayCutout
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            rvWordBook.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            params.topMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + SizeUtils.dp2px(56f)
            when (requireActivity().screenRotation){
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
        adapter.setOnItemClickListener(object : WordBookClassificationAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                mListener.onFragmentInteraction("toDetailFragment", adapter.getBookName(position))
            }
        })
        mcvWordList.setOnClickListener {
            mListener.onFragmentInteraction("toWordListActivity")
        }
        ivBackButton.setOnClickListener {
            mListener.onFragmentInteraction("finishActivity")
        }
        ivMoreButton.setOnClickListener {
            mListener.onFragmentInteraction("showAddWordBookDialog")
        }

    }

    override fun onResume() {
        super.onResume()
        wordBookViewModel.getWordBookList()
    }


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) { //表示是一个进入动作，比如add.show等
            return if (enter) { //普通的进入的动作
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_right)
            } else { //比如一个已经Fragment被另一个replace，是一个进入动作，被replace的那个就是false
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_out_left)
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) { //表示一个退出动作，比如出栈，hide，detach等
            return if (enter) { //之前被replace的重新进入到界面或者Fragment回到栈顶
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_left)
            } else { //Fragment退出，出栈
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_out_right)
            }
        }
        return null
    }
}