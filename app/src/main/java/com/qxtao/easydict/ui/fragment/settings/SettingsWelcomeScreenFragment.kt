package com.qxtao.easydict.ui.fragment.settings

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.FragmentSettingsWelcomeScreenBinding
import com.qxtao.easydict.ui.activity.settings.SettingsActivity
import com.qxtao.easydict.ui.activity.settings.SettingsViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.constant.ShareConstant.COUNT_DOWN_NAME
import com.qxtao.easydict.utils.constant.ShareConstant.COUNT_DOWN_NAME_OFF
import com.qxtao.easydict.utils.constant.ShareConstant.COUNT_DOWN_TIME
import com.qxtao.easydict.utils.constant.ShareConstant.COUNT_DOWN_TIME_OFF
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_COUNT_DOWN
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_DAILY_SENTENCE
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_GRAMMAR_CHECK
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_SUGGEST_SEARCH
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_WORD_BOOK
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_WORD_LIST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsWelcomeScreenFragment : BaseFragment<FragmentSettingsWelcomeScreenBinding>(FragmentSettingsWelcomeScreenBinding::inflate) {
    // define variable
    private lateinit var settingsViewModel: SettingsViewModel
    // define widget
    private lateinit var mtTitle: MaterialToolbar
    private lateinit var clCountDown: ConstraintLayout
    private lateinit var clSuggestSearch: ConstraintLayout
    private lateinit var clWordList: ConstraintLayout
    private lateinit var clDailySentence: ConstraintLayout
    private lateinit var clWordBook: ConstraintLayout
    private lateinit var clGrammarCheck: ConstraintLayout
    private lateinit var swCountDown: MaterialSwitch
    private lateinit var swSuggestSearch: MaterialSwitch
    private lateinit var swWordList: MaterialSwitch
    private lateinit var swDailySentence: MaterialSwitch
    private lateinit var swWordBook: MaterialSwitch
    private lateinit var swGrammarCheck: MaterialSwitch

    override fun bindViews() {
        mtTitle = binding.mtTitle
        clCountDown = binding.clCountDown
        clSuggestSearch = binding.clSuggestSearch
        clWordList = binding.clWordList
        clDailySentence = binding.clDailySentence
        clWordBook = binding.clWordBook
        clGrammarCheck = binding.clGrammarCheck
        swCountDown = binding.swCountDown
        swSuggestSearch = binding.swSuggestSearch
        swWordList = binding.swWordList
        swDailySentence = binding.swDailySentence
        swWordBook = binding.swWordBook
        swGrammarCheck = binding.swGrammarCheck
    }

    override fun initViews() {
        settingsViewModel = (activity as SettingsActivity).getSettingsViewModel()
        swCountDown.isChecked = ShareUtils.getBoolean(mContext, IS_USE_COUNT_DOWN, false) &&
                ShareUtils.getString(mContext, COUNT_DOWN_NAME, COUNT_DOWN_NAME_OFF).isNotEmpty() &&
                ShareUtils.getLong(mContext, COUNT_DOWN_TIME, COUNT_DOWN_TIME_OFF) > 0
        swSuggestSearch.isChecked = ShareUtils.getBoolean(mContext, IS_USE_SUGGEST_SEARCH, true)
        swWordList.isChecked = ShareUtils.getBoolean(mContext, IS_USE_WORD_LIST, true)
        swDailySentence.isChecked = ShareUtils.getBoolean(mContext, IS_USE_DAILY_SENTENCE, true)
        swWordBook.isChecked = ShareUtils.getBoolean(mContext, IS_USE_WORD_BOOK, true)
        swGrammarCheck.isChecked = ShareUtils.getBoolean(mContext, IS_USE_GRAMMAR_CHECK, true)
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener{
            mListener.onFragmentInteraction("onBackPressed")
        }
        clCountDown.setOnClickListener { swCountDown.isChecked = !swCountDown.isChecked }
        clSuggestSearch.setOnClickListener { swSuggestSearch.isChecked = !swSuggestSearch.isChecked }
        clWordList.setOnClickListener { swWordList.isChecked = !swWordList.isChecked }
        clDailySentence.setOnClickListener { swDailySentence.isChecked = !swDailySentence.isChecked }
        clWordBook.setOnClickListener { swWordBook.isChecked = !swWordBook.isChecked }
        clGrammarCheck.setOnClickListener { swGrammarCheck.isChecked = !swGrammarCheck.isChecked }

        swCountDown.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                showCountDownSettingDialog()
            } else {
                ShareUtils.putBoolean(mContext, IS_USE_COUNT_DOWN, false)
                ShareUtils.delShare(mContext, COUNT_DOWN_NAME)
                ShareUtils.delShare(mContext, COUNT_DOWN_TIME)
            }
        }
        swSuggestSearch.setOnCheckedChangeListener { _, isChecked ->
            ShareUtils.putBoolean(mContext, IS_USE_SUGGEST_SEARCH, isChecked)
        }
        swWordList.setOnCheckedChangeListener { _, isChecked ->
            ShareUtils.putBoolean(mContext, IS_USE_WORD_LIST, isChecked)
        }
        swDailySentence.setOnCheckedChangeListener { _, isChecked ->
            ShareUtils.putBoolean(mContext, IS_USE_DAILY_SENTENCE, isChecked)
        }
        swWordBook.setOnCheckedChangeListener { _, isChecked ->
            ShareUtils.putBoolean(mContext, IS_USE_WORD_BOOK, isChecked)
        }
        swGrammarCheck.setOnCheckedChangeListener { _, isChecked ->
            ShareUtils.putBoolean(mContext, IS_USE_GRAMMAR_CHECK, isChecked)
        }
    }

    private fun showCountDownSettingDialog(){
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_count_down_input, null)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val tvConfirm =  dialogView.findViewById<TextView>(R.id.tv_confirm)
        val etInput = dialogView.findViewById<EditText>(R.id.et_input)
        val dpDate = dialogView.findViewById<DatePicker>(R.id.dp_date)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dpDate.minDate = System.currentTimeMillis()
        tvCancel.setOnClickListener { dialog.dismiss() }
        tvConfirm.setOnClickListener {
            val countDownName = etInput.text.toString()
            val countDownTime = TimeUtils.getTimestampByYMD(dpDate.year, dpDate.month + 1, dpDate.dayOfMonth)
            if (countDownName.isNotBlank()){
                ShareUtils.putBoolean(mContext, IS_USE_COUNT_DOWN, true)
                ShareUtils.putString(mContext, COUNT_DOWN_NAME, countDownName)
                ShareUtils.putLong(mContext, COUNT_DOWN_TIME, countDownTime)
                dialog.dismiss()
                showShortToast(getString(R.string.count_down_set_success))
            } else {
                etInput.error = getString(R.string.invalid_input)
            }
        }
        etRequestFocus(etInput)
        dialog.show()
        dialog.setOnDismissListener {
            lifecycleScope.launch(Dispatchers.IO) {
                delay(400)
                withContext(Dispatchers.Main){
                    swCountDown.isChecked = ShareUtils.getBoolean(mContext, IS_USE_COUNT_DOWN, false) &&
                            ShareUtils.getString(mContext, COUNT_DOWN_NAME, COUNT_DOWN_NAME_OFF).isNotEmpty() &&
                            ShareUtils.getLong(mContext, COUNT_DOWN_TIME, COUNT_DOWN_TIME_OFF) > 0
                }
            }
        }
    }

    private fun etRequestFocus(editText: EditText){
        editText.requestFocus()
        // 延迟200ms 显示软键盘
        val showSoftInput = {
            if (editText.isFocused){
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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