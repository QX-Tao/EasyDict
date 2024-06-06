package com.qxtao.easydict.ui.activity.grammarcheck

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.grammarcheck.GrammarCheckAdapter
import com.qxtao.easydict.databinding.ActivityGrammarCheckBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.view.LimitEditText
import com.qxtao.easydict.ui.view.PerformEdit
import com.qxtao.easydict.utils.common.ClipboardUtils
import com.qxtao.easydict.utils.common.ColorUtils
import java.util.regex.Pattern

class GrammarCheckActivity : BaseActivity<ActivityGrammarCheckBinding>(ActivityGrammarCheckBinding::inflate){
    // define variable
    private lateinit var grammarCheckViewModel: GrammarCheckViewModel
    private lateinit var performEdit: PerformEdit
    private lateinit var grammarCheckAdapter: GrammarCheckAdapter

    // define widget
    private lateinit var mtTitle: MaterialToolbar
    private lateinit var tvCheck: TextView
    private lateinit var cvCheck: CardView
    private lateinit var ivRedo: ImageView
    private lateinit var ivUndo: ImageView
    private lateinit var etGrammarCheck: LimitEditText
    private lateinit var cvGrammarCheck: CardView
    private lateinit var nsvCheckResult: NestedScrollView
    private lateinit var clLoading: ConstraintLayout
    private lateinit var clGrammarCheck: ConstraintLayout
    private lateinit var mcvRetype: MaterialCardView
    private lateinit var tvRetype: TextView
    private lateinit var tvRightText: TextView
    private lateinit var tvOriginText: TextView
    private lateinit var rvCheckResult: RecyclerView
    private lateinit var cvNoError: CardView
    private lateinit var cvPaste: CardView
    private lateinit var ivPaste: ImageView


    override fun onCreate() {
        grammarCheckViewModel = ViewModelProvider(this)[GrammarCheckViewModel::class.java]
    }

    override fun bindViews() {
        mtTitle = binding.mtTitle
        tvCheck = binding.tvCheck
        cvCheck = binding.cvCheck
        ivRedo = binding.ivRedo
        ivUndo = binding.ivUndo
        etGrammarCheck = binding.etGrammarCheck
        cvGrammarCheck = binding.cvGrammarCheck
        nsvCheckResult = binding.nsvCheckResult
        clLoading = binding.clLoading
        mcvRetype = binding.mcvRetype
        tvRetype = binding.tvRetype
        tvRightText = binding.tvRightText
        tvOriginText = binding.tvOriginText
        rvCheckResult = binding.rvCheckResult
        cvNoError = binding.cvNoError
        cvPaste = binding.cvPaste
        ivPaste = binding.ivPaste
        clGrammarCheck = binding.clGrammarCheck
    }

    override fun initViews() {
        performEdit = PerformEdit(etGrammarCheck)
        grammarCheckAdapter = GrammarCheckAdapter(ArrayList())
        rvCheckResult.adapter = grammarCheckAdapter
        rvCheckResult.layoutManager = LinearLayoutManager(this)
        grammarCheckViewModel.pageType.observe(this){
            when(it) {
                0 -> {
                    clLoading.visibility = View.GONE
                    cvGrammarCheck.visibility = View.VISIBLE
                    nsvCheckResult.visibility = View.GONE
                    mcvRetype.visibility = View.GONE
                }
                1 -> {
                    clLoading.visibility = View.VISIBLE
                }
                2 -> {
                    clLoading.visibility = View.GONE
                    cvGrammarCheck.visibility = View.GONE
                    nsvCheckResult.visibility = View.VISIBLE
                    mcvRetype.visibility = View.VISIBLE
                }
                3 -> {
                    showShortToast(getString(R.string.loading_failure))
                    grammarCheckViewModel.pageType.value = 0
                }
            }
        }
        grammarCheckViewModel.correctedEssay.observe(this){
            tvRightText.text = it
        }
        grammarCheckViewModel.errorSentFeedback.observe(this){
            if (it.isNotEmpty()) {
                rvCheckResult.visibility = View.VISIBLE
                cvNoError.visibility = View.GONE
                grammarCheckAdapter.setData(it)
            } else {
                rvCheckResult.visibility = View.GONE
                cvNoError.visibility = View.VISIBLE
            }
        }
        grammarCheckViewModel.originText.observe(this){
            etGrammarCheck.setText(it)
            etGrammarCheck.setSelection(it.length)
            tvOriginText.text = it
        }
        grammarCheckViewModel.pasteData.observe(this){
            cvPaste.visibility = if (it.first && !it.second.isNullOrBlank() && etGrammarCheck.text.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener { finish() }
        ivRedo.setOnClickListener { performEdit.redo() }
        ivUndo.setOnClickListener { performEdit.undo() }
        etGrammarCheck.doAfterTextChanged {
            if(etGrammarCheck.text.isNullOrBlank()) {
                cvCheck.setCardBackgroundColor(ColorUtils.colorSecondary(mContext))
            } else {
                cvCheck.setCardBackgroundColor(ColorUtils.colorPrimary(mContext))
            }
            grammarCheckViewModel.setPasteData(etGrammarCheck.text.isNullOrEmpty())
            ivRedo.isSelected = performEdit.isHasRedos()
            ivUndo.isSelected = performEdit.isHasUndos()
        }
        etGrammarCheck.commitTextListener(object : LimitEditText.ICommitTextListener{
            override fun onCommitText(text: CharSequence): CharSequence {
                return textualize(text)
            }
        })
        tvCheck.setOnClickListener {
            val confirmText = textualize(etGrammarCheck.text.toString()).trim()
            if (confirmText.isNotBlank()) {
                // 关闭软键盘
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etGrammarCheck.windowToken, 0)
                grammarCheckViewModel.check(confirmText.toString())
            }
        }
        tvRetype.setOnClickListener {
            grammarCheckViewModel.pageType.value = 0
            // 弹出软键盘
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etGrammarCheck, InputMethodManager.SHOW_IMPLICIT)
            etGrammarCheck.requestFocus()
        }
        ivPaste.setOnClickListener {
            grammarCheckViewModel.pasteData.value?.second.let {
                if (it != null){
                    etGrammarCheck.setText(textualize(it))
                    etGrammarCheck.setSelection(etGrammarCheck.text?.length ?: 0)
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            grammarCheckViewModel.setPasteData(ClipboardUtils.hasClipboardText(mContext),
                ClipboardUtils.getClipboardText(mContext)?.let { textualize(it).toString() })
            if (grammarCheckViewModel.pageType.value == 0){
                // 弹出软键盘
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etGrammarCheck, InputMethodManager.SHOW_IMPLICIT)
                etGrammarCheck.requestFocus()
            }
        }
    }

    private fun textualize(text: CharSequence): CharSequence{
        // 限制输入中文字符
        val pattern = Pattern.compile("[\u4e00-\u9fa5、，。！？：；‘’“”『』【】〖〗《》‖]")
        val input = text.toString()
        val filteredInput = StringBuilder()
        for (i in text.indices) {
            val c = input[i]
            if (!pattern.matcher(c.toString()).matches()) {
                filteredInput.append(c)
            }
        }
        return filteredInput
    }

}