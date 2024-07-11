package com.qxtao.easydict.ui.fragment.dict

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
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
import com.qxtao.easydict.ui.view.PerformEdit
import rikka.insets.windowInsetsHelper


class DictSearchFragment : BaseFragment<FragmentDictSearchBinding>(FragmentDictSearchBinding::inflate) {
    // define variable
    private lateinit var dictViewModel: DictViewModel
    private lateinit var performEdit: PerformEdit
    // define widget
    private lateinit var mcvSearchBox: MaterialCardView
    private lateinit var ivBack : ImageView
    private lateinit var ivClear : ImageView
    private lateinit var ivSearch : ImageView
    private lateinit var etSearchBox : EditText
    private lateinit var clQuickSearchPanel : ConstraintLayout
    private lateinit var ivRedo: ImageView
    private lateinit var ivBackspace: ImageView
    private lateinit var ivUndo: ImageView
    private lateinit var tvPaste: TextView
    private lateinit var tvClear: TextView
    private lateinit var tvSearch: TextView
    private lateinit var cvPaste: CardView
    private lateinit var cvClear: CardView

    override fun bindViews() {
        ivBack = binding.ivBack
        ivClear = binding.ivClear
        ivSearch = binding.ivSearch
        etSearchBox = binding.etSearchBox
        mcvSearchBox = binding.mcvSearchBox
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R){ clQuickSearchPanel.windowInsetsHelper?.fitSystemWindows = 0 }

        dictViewModel = (activity as DictActivity).getDictViewModel()
        ivSearch.visibility = if (dictViewModel.searchText.value?.editSearchText.isNullOrEmpty()) View.GONE else View.VISIBLE
        ivClear.visibility = if (dictViewModel.searchText.value?.editSearchText.isNullOrEmpty()) View.GONE else View.VISIBLE
        dictViewModel.searchText.observe(this){
            etSearchBox.setText(it.editSearchText)
            etSearchBox.setSelection(if (it.editSearchText.length >= it.editSelectionStart) it.editSelectionStart else it.editSearchText.length,
                if (it.editSearchText.length >= it.editSelectionEnd) it.editSelectionEnd else it.editSearchText.length)
        }
        dictViewModel.pasteData.observe(this){
            cvPaste.visibility = if (it.first && !it.second.isNullOrBlank() && etSearchBox.text.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun addListener() {
        ivBack.setOnClickListener {
            mListener.onFragmentInteraction("onBackPressed")
        }
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
        etSearchBox.doAfterTextChanged {
            if (!etSearchBox.text.isNullOrEmpty()){
                ivSearch.visibility = View.VISIBLE
                ivClear.visibility = View.VISIBLE
                cvClear.visibility = View.VISIBLE
                dictViewModel.searchInWordData(it.toString())
            } else {
                ivSearch.visibility = View.GONE
                ivClear.visibility = View.GONE
                cvClear.visibility = View.GONE
                dictViewModel.setHasShowRvInfo(DICT_RV_HISTORY)
                val currentFragment = childFragmentManager.findFragmentById(R.id.dict_search_content_fragment)
                if (currentFragment !is DictHasFragment && etSearchBox.isFocused) mListener.onFragmentInteraction("toHasFragment")
            }
            dictViewModel.setPasteData(etSearchBox.text.isNullOrEmpty())
            ivRedo.isSelected = performEdit.isHasRedos()
            ivUndo.isSelected = performEdit.isHasUndos()
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
        etSearchBox.setOnFocusChangeListener { _, hasFocus ->
            clQuickSearchPanel.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
        tvPaste.setOnClickListener {
            dictViewModel.pasteData.value?.second?.let {
                etSearchBox.setText(it)
                etSearchBox.setSelection(etSearchBox.text?.length ?: 0)
            }
        }
        ivClear.setOnClickListener {
            mListener.onFragmentInteraction("editTextGetFocus", etSearchBox)
            dictViewModel.setSearchText(getString(R.string.empty_string),  0, 0)
        }
        tvClear.setOnClickListener {
            mListener.onFragmentInteraction("editTextGetFocus", etSearchBox)
            dictViewModel.setSearchText(getString(R.string.empty_string),  0, 0)
        }
        ivSearch.setOnClickListener {
            if (etSearchBox.text.isNullOrBlank()){
                showShortToast(getString(R.string.invalid_input))
                return@setOnClickListener
            }
            mListener.onFragmentInteraction("toDetailFragment", etSearchBox.text.toString())
            mListener.onFragmentInteraction("editTextClearFocus", etSearchBox)
        }
        tvSearch.setOnClickListener {
            if (etSearchBox.text.isNullOrBlank()){
                showShortToast(getString(R.string.invalid_input))
                return@setOnClickListener
            }
            mListener.onFragmentInteraction("toDetailFragment", etSearchBox.text.toString())
            mListener.onFragmentInteraction("editTextClearFocus", etSearchBox)
        }
    }

    fun getSearchBoxText(): String = etSearchBox.text.toString()

    fun getEditText(): EditText = etSearchBox

    fun getEditTextFocus() = mListener.onFragmentInteraction("editTextGetFocus", etSearchBox)

    fun exitEditTextFocus() = mListener.onFragmentInteraction("editTextClearFocus", etSearchBox)

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

    override fun onPause() {
        super.onPause()
        if (this::dictViewModel.isInitialized){
            dictViewModel.setSearchText(etSearchBox.text.toString(), etSearchBox.selectionStart, etSearchBox.selectionEnd)
        }
    }

}