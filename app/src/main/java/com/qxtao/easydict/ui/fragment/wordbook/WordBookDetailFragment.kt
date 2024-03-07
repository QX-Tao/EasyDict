package com.qxtao.easydict.ui.fragment.wordbook

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.wordbook.WordBookDetailAdapter
import com.qxtao.easydict.databinding.FragmentWordBookDetailBinding
import com.qxtao.easydict.ui.activity.wordbook.BOOK_WORD_MODE_CONTROL
import com.qxtao.easydict.ui.activity.wordbook.BOOK_WORD_MODE_NORMAL
import com.qxtao.easydict.ui.activity.wordbook.WordBookActivity
import com.qxtao.easydict.ui.activity.wordbook.WordBookViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.factory.screenRotation

class WordBookDetailFragment : BaseFragment<FragmentWordBookDetailBinding>(FragmentWordBookDetailBinding::inflate) {
    // define variable
    private lateinit var adapter: WordBookDetailAdapter
    private lateinit var wordBookViewModel: WordBookViewModel
    // define widget
    private lateinit var ivMoreButton : ImageView
    private lateinit var ivBackButton : ImageView
    private lateinit var tvTitle : TextView
    private lateinit var vHolder: View
    private lateinit var lvLoading : LoadingView
    private lateinit var llLoadingFail : LinearLayout
    private lateinit var llListEmpty : LinearLayout
    private lateinit var rvBookWord: RecyclerView
    private lateinit var cvDelete: CardView
    private lateinit var cvMove: CardView
    private lateinit var ivDelete: ImageView
    private lateinit var ivMove: ImageView

    fun newInstance(bookName: String): WordBookDetailFragment {
        val args = Bundle()
        args.putString("bookName", bookName)
        val fragment = WordBookDetailFragment()
        fragment.arguments = args
        return fragment
    }

    private fun getBookName(): String? {
        return arguments?.getString("bookName")
    }

    override fun bindViews() {
        tvTitle = binding.includeTitleBarSecond.tvTitle
        ivBackButton = binding.includeTitleBarSecond.ivBackButton
        ivMoreButton = binding.includeTitleBarSecond.ivMoreButton
        vHolder = binding.vHolder
        lvLoading = binding.lvLoading
        llLoadingFail = binding.llLoadingFail
        llListEmpty = binding.llListEmpty
        rvBookWord = binding.rvBookWord
        cvDelete = binding.cvDelete
        cvMove = binding.cvMove
        ivDelete = binding.ivDelete
        ivMove = binding.ivMove
    }

    override fun initViews() {
        wordBookViewModel = (activity as WordBookActivity).getWordBookViewModel()
        wordBookViewModel.getWordList(getBookName() ?: getString(R.string.empty_string))
        tvTitle.text = getBookName()
        ivMoreButton.setImageResource(R.drawable.ic_edit)
        ivMoreButton.tooltipText = getString(R.string.edit_word_book)
        rvBookWord.layoutManager = LinearLayoutManager(requireActivity())
        adapter = WordBookDetailAdapter(ArrayList())
        rvBookWord.adapter = adapter
        wordBookViewModel.dataLoadInfo.observe(this){
            when (it) {
                0 -> {
                    ivMoreButton.visibility = View.GONE
                    lvLoading.visibility = View.VISIBLE
                }
                1 -> {
                    ivMoreButton.visibility = View.VISIBLE
                    llListEmpty.visibility = View.GONE
                    lvLoading.visibility = View.GONE
                }
                2 -> {
                    ivMoreButton.visibility = View.GONE
                    llLoadingFail.visibility = View.VISIBLE
                }
                3 -> {
                    lvLoading.visibility = View.GONE
                    ivMoreButton.visibility = View.GONE
                    llListEmpty.visibility = View.VISIBLE
                }
            }
        }
        wordBookViewModel.detailMode.observe(this){
            when(it){
                0 -> {
                    tvTitle.text = getBookName()
                    ivMoreButton.setImageResource(R.drawable.ic_edit)
                    ivMoreButton.tooltipText = getString(R.string.edit_word_book)
                    cvDelete.visibility = View.GONE
                    cvMove.visibility = View.GONE
                    adapter.exitMultiSelectMode()
                }
                1 -> {
                    ivMoreButton.setImageResource(R.drawable.ic_select_all)
                    ivMoreButton.tooltipText = getString(R.string.select_all)
                    tvTitle.text = getString(R.string.please_select_item)
                }
            }
        }
        wordBookViewModel.bookWordList.observe(this){
            adapter.setData(it)
        }
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(vHolder){ view, insets ->
            val displayCutout = insets.displayCutout
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            rvBookWord.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
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
        ivBackButton.setOnClickListener {
            if (wordBookViewModel.detailMode.value == BOOK_WORD_MODE_CONTROL){
                adapter.exitMultiSelectMode()
            }
            mListener.onFragmentInteraction("onBackPressed")
        }
        ivMoreButton.setOnClickListener {
            if (wordBookViewModel.detailMode.value == BOOK_WORD_MODE_NORMAL) {
                wordBookViewModel.detailMode.value = BOOK_WORD_MODE_CONTROL
                adapter.joinMultiSelectMode()
                return@setOnClickListener
            }
            if (wordBookViewModel.detailMode.value == BOOK_WORD_MODE_CONTROL){
                adapter.selectOrUnselectAll()
                tvTitle.text = if (adapter.multiSelectedList.isEmpty()) getString(R.string.please_select_item) else String.format(getString(R.string.select_item_eee), adapter.getSelectedCount())
                ivMoreButton.tooltipText = if (adapter.isSelectedAll()) getString(R.string.unselect_all) else getString(R.string.select_all)
                cvDelete.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
                cvMove.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
                return@setOnClickListener
            }
        }
        adapter.setOnItemClickListener(object : WordBookDetailAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                tvTitle.text = if (adapter.multiSelectedList.isEmpty()) getString(R.string.please_select_item) else String.format(getString(R.string.select_item_eee), adapter.getSelectedCount())
                ivMoreButton.tooltipText = if (adapter.isSelectedAll()) getString(R.string.unselect_all) else getString(R.string.select_all)
                cvDelete.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
                cvMove.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
            }
        })
        adapter.setOnItemLongClickListener(object : WordBookDetailAdapter.OnItemLongClickListener{
            override fun onItemLongClick(position: Int) {
                if (wordBookViewModel.detailMode.value == BOOK_WORD_MODE_NORMAL) {
                    wordBookViewModel.detailMode.value = BOOK_WORD_MODE_CONTROL
                    adapter.joinMultiSelectMode()
                }
                tvTitle.text = if (adapter.multiSelectedList.isEmpty()) getString(R.string.please_select_item) else String.format(getString(R.string.select_item_eee), adapter.getSelectedCount())
                ivMoreButton.tooltipText = if (adapter.isSelectedAll()) getString(R.string.unselect_all) else getString(R.string.select_all)
                cvDelete.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
                cvMove.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
            }
        })
        ivMove.setOnClickListener {
            mListener.onFragmentInteraction("showMoveWordBookDialog", adapter.getSelectedItems(), getBookName())
        }
        ivDelete.setOnClickListener {
            mListener.onFragmentInteraction("showDeleteWordsDialog", adapter.getSelectedItems(), getBookName())
        }
    }

    override fun onResume() {
        super.onResume()
        wordBookViewModel.getWordList(getBookName() ?: getString(R.string.empty_string))
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