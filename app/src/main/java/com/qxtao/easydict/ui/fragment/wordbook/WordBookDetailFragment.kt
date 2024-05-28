package com.qxtao.easydict.ui.fragment.wordbook

import android.os.Bundle
import android.view.MenuItem
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var vHolder: View
    private lateinit var lvLoading : LoadingView
    private lateinit var llLoadingFail : LinearLayout
    private lateinit var llListEmpty : LinearLayout
    private lateinit var rvBookWord: RecyclerView
    private lateinit var fabDelete: FloatingActionButton
    private lateinit var fabMove: FloatingActionButton
    private lateinit var mtTitle: MaterialToolbar

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
        mtTitle = binding.mtTitle
        vHolder = binding.vHolder
        lvLoading = binding.lvLoading
        llLoadingFail = binding.llLoadingFail
        llListEmpty = binding.llListEmpty
        rvBookWord = binding.rvBookWord
        fabDelete = binding.fabDelete
        fabMove = binding.fabMove
    }

    override fun initViews() {
        wordBookViewModel = (activity as WordBookActivity).getWordBookViewModel()
        wordBookViewModel.getWordList(getBookName() ?: getString(R.string.empty_string))
        mtTitle.title = getBookName()
        rvBookWord.layoutManager = LinearLayoutManager(requireActivity())
        adapter = WordBookDetailAdapter(ArrayList())
        rvBookWord.adapter = adapter
        wordBookViewModel.dataLoadInfo.observe(this){
            when (it) {
                0 -> {
                    lvLoading.visibility = View.VISIBLE
                }
                1 -> {
                    llListEmpty.visibility = View.GONE
                    lvLoading.visibility = View.GONE
                }
                2 -> {
                    llLoadingFail.visibility = View.VISIBLE
                }
                3 -> {
                    lvLoading.visibility = View.GONE
                    llListEmpty.visibility = View.VISIBLE
                }
            }
        }
        wordBookViewModel.detailMode.observe(this){
            when(it){
                0 -> {
                    mtTitle.title = getBookName()
                    displayMenuItem(R.id.edit_word_book)
                    fabDelete.visibility = View.GONE
                    fabMove.visibility = View.GONE
                    adapter.exitMultiSelectMode()
                }
                1 -> {
                    displayMenuItem(R.id.select_all)
                    mtTitle.title = getString(R.string.please_select_item)
                }
            }
        }
        wordBookViewModel.bookWordList.observe(this){
            adapter.setData(it)
        }
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(vHolder){ view, insets ->
            rvBookWord.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            val displayCutout = insets.displayCutout
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            when (requireActivity().screenRotation){
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
        mtTitle.setNavigationOnClickListener{
            if (wordBookViewModel.detailMode.value == BOOK_WORD_MODE_CONTROL){
                adapter.exitMultiSelectMode()
            }
            mListener.onFragmentInteraction("onBackPressed")
        }
        mtTitle.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_word_book -> {
                    if (wordBookViewModel.detailMode.value == BOOK_WORD_MODE_NORMAL) {
                        wordBookViewModel.detailMode.value = BOOK_WORD_MODE_CONTROL
                        adapter.joinMultiSelectMode()
                    }
                }
                R.id.select_all, R.id.unselect_all -> {
                    displayMenuItem(if (it.itemId == R.id.select_all) R.id.unselect_all else R.id.select_all)
                    adapter.selectOrUnselectAll()
                    mtTitle.title = if (adapter.multiSelectedList.isEmpty()) getString(R.string.please_select_item) else String.format(getString(R.string.select_item_eee), adapter.getSelectedCount())
                    fabDelete.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
                    fabMove.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
                }
            }
            true
        }
        adapter.setOnItemClickListener(object : WordBookDetailAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                mtTitle.title = if (adapter.multiSelectedList.isEmpty()) getString(R.string.please_select_item) else String.format(getString(R.string.select_item_eee), adapter.getSelectedCount())
                displayMenuItem(if (adapter.isSelectedAll()) R.id.unselect_all else R.id.select_all)
                fabDelete.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
                fabMove.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
            }
        })
        adapter.setOnItemLongClickListener(object : WordBookDetailAdapter.OnItemLongClickListener{
            override fun onItemLongClick(position: Int) {
                if (wordBookViewModel.detailMode.value == BOOK_WORD_MODE_NORMAL) {
                    wordBookViewModel.detailMode.value = BOOK_WORD_MODE_CONTROL
                    adapter.joinMultiSelectMode()
                }
                mtTitle.title = if (adapter.multiSelectedList.isEmpty()) getString(R.string.please_select_item) else String.format(getString(R.string.select_item_eee), adapter.getSelectedCount())
                displayMenuItem(if (adapter.isSelectedAll()) R.id.unselect_all else R.id.select_all)
                fabDelete.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
                fabMove.visibility = if (adapter.multiSelectedList.isEmpty()) View.GONE else View.VISIBLE
            }
        })
        fabMove.setOnClickListener {
            mListener.onFragmentInteraction("showMoveWordBookDialog", adapter.getSelectedItems(), getBookName())
        }
        fabDelete.setOnClickListener {
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

    private fun displayMenuItem(menuItemId: Int){
        val menuItemList = listOf(R.id.add_word_book, R.id.edit_word_book, R.id.select_all, R.id.unselect_all)
        menuItemList.forEach { mtTitle.menu.findItem(it).isVisible = false }
        mtTitle.menu.findItem(menuItemId).isVisible = true
    }
}