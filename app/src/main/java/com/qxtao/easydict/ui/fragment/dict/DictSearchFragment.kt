package com.qxtao.easydict.ui.fragment.dict

import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.card.MaterialCardView
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.FragmentDictSearchBinding
import com.qxtao.easydict.ui.activity.dict.DICT_RV_HISTORY
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.base.BaseFragment


class DictSearchFragment : BaseFragment<FragmentDictSearchBinding>(FragmentDictSearchBinding::inflate) {
    // define variable
    private lateinit var dictViewModel: DictViewModel

    // define widget
    private lateinit var clRoot: ConstraintLayout
    private lateinit var mcvSearchBox: MaterialCardView
    private lateinit var ivBack : ImageView
    private lateinit var ivClear : ImageView
    private lateinit var ivSearch : ImageView
    private lateinit var etSearchBox : EditText

    override fun bindViews() {
        ivBack = binding.ivBack
        ivClear = binding.ivClear
        ivSearch = binding.ivSearch
        etSearchBox = binding.etSearchBox
        mcvSearchBox = binding.mcvSearchBox
        clRoot = binding.clRoot
    }

    override fun initViews() {
        dictViewModel = (activity as DictActivity).getDictViewModel()
        ivSearch.visibility = if (dictViewModel.searchText.value?.editSearchText.isNullOrEmpty()) View.GONE else View.VISIBLE
        ivClear.visibility = if (dictViewModel.searchText.value?.editSearchText.isNullOrEmpty()) View.GONE else View.VISIBLE
        dictViewModel.searchText.observe(this){
            etSearchBox.setText(it.editSearchText)
            etSearchBox.setSelection(if (it.editSearchText.length >= it.editCursor) it.editCursor else it.editSearchText.length)
        }
    }

    override fun addListener() {
        ivBack.setOnClickListener {
            mListener.onFragmentInteraction("onBackPressed")
        }
        etSearchBox.doAfterTextChanged {
            if (!etSearchBox.text.isNullOrEmpty()){
                ivSearch.visibility = View.VISIBLE
                ivClear.visibility = View.VISIBLE
                dictViewModel.searchInWordData(it.toString())
            } else {
                ivSearch.visibility = View.GONE
                ivClear.visibility = View.GONE
                dictViewModel.setHasShowRvInfo(DICT_RV_HISTORY)
                val currentFragment = childFragmentManager.findFragmentById(R.id.dict_search_content_fragment)
                if (currentFragment !is DictHasFragment && etSearchBox.isFocused) mListener.onFragmentInteraction("toHasFragment")
            }
        }
        etSearchBox.setOnEditorActionListener { _, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH || (keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)){
                if (etSearchBox.text.isNullOrBlank()){
                    showShortToast(getString(R.string.invalid_input))
                    return@setOnEditorActionListener true
                }
                mListener.onFragmentInteraction("toDetailFragment", etSearchBox.text.toString())
                mListener.onFragmentInteraction("editTextClearFocus", etSearchBox)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        ivClear.setOnClickListener {
            mListener.onFragmentInteraction("editTextGetFocus", etSearchBox)
            dictViewModel.setSearchText(getString(R.string.empty_string),  0)
        }
        ivSearch.setOnClickListener {
            if (etSearchBox.text.isNullOrBlank()){
                showShortToast(getString(R.string.invalid_input))
                return@setOnClickListener
            }
            mListener.onFragmentInteraction("toDetailFragment", etSearchBox.text.toString())
        }
    }

    fun getSearchBoxText(): String = etSearchBox.text.toString()

    fun getEditText(): EditText = etSearchBox

    fun getEditTextFocus(){
        val currentFragment = childFragmentManager.findFragmentById(R.id.dict_search_content_fragment)
        if (currentFragment is DictHasFragment){
            mListener.onFragmentInteraction("editTextGetFocus", etSearchBox)
        }
    }

    fun exitEditTextFocus(){
        val currentFragment = childFragmentManager.findFragmentById(R.id.dict_search_content_fragment)
        if (currentFragment is DictDetailFragment){
            mListener.onFragmentInteraction("editTextClearFocus", etSearchBox)
        }
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