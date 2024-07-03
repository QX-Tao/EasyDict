package com.qxtao.easydict.ui.activity.wordbook

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dict.DictSelectWordBookAdapter
import com.qxtao.easydict.database.WordBookData
import com.qxtao.easydict.databinding.ActivityWordBookBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.fragment.wordbook.WordBookClassificationFragment
import com.qxtao.easydict.ui.fragment.wordbook.WordBookDetailFragment

class WordBookActivity : BaseActivity<ActivityWordBookBinding>(ActivityWordBookBinding::inflate),
    BaseFragment.OnFragmentInteractionListener {
    // define variable
    private lateinit var wordBookViewModel: WordBookViewModel
    private lateinit var wordBookData: WordBookData
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var callback: OnBackPressedCallback

    override fun onCreate1(savedInstanceState: Bundle?) {
        super.onCreate1(savedInstanceState)
        wordBookData = WordBookData(mContext)
        wordBookViewModel = ViewModelProvider(this@WordBookActivity, WordBookViewModel.Factory(wordBookData))[WordBookViewModel::class.java]
        if (savedInstanceState == null){ toClassificationFragment() }
    }

    override fun onCreate() {
        dispatcher = onBackPressedDispatcher
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 判断是否只有一个fragment 如果是则finish 不是则pop
                if (supportFragmentManager.backStackEntryCount == 1) {
                    finish()
                } else {
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.word_book_fragment)
                    if (currentFragment is WordBookDetailFragment && wordBookViewModel.detailMode.value == BOOK_WORD_MODE_CONTROL){
                        wordBookViewModel.detailMode.value = BOOK_WORD_MODE_NORMAL
                    } else supportFragmentManager.popBackStack(null, 0)
                }
            }
        }
        dispatcher.addCallback(this, callback)
    }

    override fun initViews() {}
    override fun addListener() {}

    @Suppress("UNCHECKED_CAST")
    override fun onFragmentInteraction(vararg data: Any?) {
        if (data.isNotEmpty()) {
            when(data[0]){
                "finishActivity" -> { finish() }
                "onBackPressed" -> dispatcher.onBackPressed()
                "toDetailFragment" -> toDetailFragment(data[1] as String)
                "showAddWordBookDialog" -> showAddWordBookDialog()
                "showRenameWordBookDialog" -> showRenameWordBookDialog(data[1] as Int)
                "showDeleteWordBookDialog" -> showDeleteWordBookDialog(data[1] as Int)
                "showMoveWordBookDialog" -> showMoveWordBookDialog(data[1] as List<String>, data[2] as String)
                "showDeleteWordsDialog" -> showDeleteWordsDialog(data[1] as List<String>, data[2] as String)
            }
        }
    }

    fun getWordBookViewModel(): WordBookViewModel = wordBookViewModel

    private fun toClassificationFragment(){
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.word_book_fragment, WordBookClassificationFragment::class.java, null)
            .addToBackStack(null)
            .commit()
    }

    private fun toDetailFragment(bookName: String){
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            replace(R.id.word_book_fragment, WordBookDetailFragment().newInstance(bookName))
            addToBackStack(null)
        }
    }


    private fun showAddWordBookDialog(){
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_word_book_input, null)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val tvConfirm =  dialogView.findViewById<TextView>(R.id.tv_confirm)
        val etInput = dialogView.findViewById<EditText>(R.id.et_input)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        tvCancel.setOnClickListener { dialog.dismiss() }
        tvConfirm.setOnClickListener {
            val wordBookName = etInput.text.toString()
            if (wordBookName.isNotBlank()){
                val res = wordBookViewModel.addWordBook(wordBookName)
                if (res.first) {
                    showShortToast(getString(R.string.add_word_book_success_eee, res.second))
                    wordBookViewModel.getWordBookList()
                    dialog.dismiss()
                } else showShortToast(res.second)
            } else {
                etInput.error = getString(R.string.invalid_input)
            }
        }
        etRequestFocus(etInput)
        dialog.show()
    }

    private fun showAddWordBookDialog(words: List<String>, oldName: String){
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_word_book_input, null)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val tvConfirm =  dialogView.findViewById<TextView>(R.id.tv_confirm)
        val etInput = dialogView.findViewById<EditText>(R.id.et_input)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        tvCancel.setOnClickListener { dialog.dismiss() }
        tvConfirm.setOnClickListener {
            val wordBookName = etInput.text.toString()
            if (wordBookName.isNotBlank()){
                val res = wordBookViewModel.addWordBook(wordBookName)
                if (res.first) {
                    val ress = wordBookViewModel.moveWordBook(words, oldName, wordBookName)
                    if (ress.first){
                        showShortToast(getString(R.string.move_word_book, res.second))
                        wordBookViewModel.getWordList(oldName)
                        wordBookViewModel.detailMode.value = BOOK_WORD_MODE_NORMAL
                    } else showShortToast(getString(R.string.operation_failure))
                    wordBookViewModel.getWordBookList()
                    dialog.dismiss()
                } else showShortToast(res.second)
            } else {
                etInput.error = getString(R.string.invalid_input)
            }
        }
        etRequestFocus(etInput)
        dialog.show()
    }

    private fun showRenameWordBookDialog(position: Int){
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_word_book_input, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val tvConfirm =  dialogView.findViewById<TextView>(R.id.tv_confirm)
        val etInput = dialogView.findViewById<EditText>(R.id.et_input)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        val oldName = wordBookViewModel.wordBookList.value?.get(position) ?: return
        tvTitle.text = getString(R.string.rename)
        etInput.setText(oldName)
        etInput.setSelection(oldName.length)
        tvCancel.setOnClickListener { dialog.dismiss() }
        tvConfirm.setOnClickListener {
            val wordBookName = etInput.text.toString()
            if (wordBookName.isNotBlank()){
                if (wordBookName == oldName) {
                    dialog.dismiss()
                    return@setOnClickListener
                }
                val res = wordBookViewModel.renameWordBook(oldName, wordBookName)
                if (res.first) {
                    showShortToast(getString(R.string.rename_word_book_success_eee, res.second))
                    wordBookViewModel.getWordBookList()
                    dialog.dismiss()
                } else showShortToast(res.second)
            } else {
                etInput.error = getString(R.string.invalid_input)
            }
        }
        etRequestFocus(etInput)
        dialog.show()
    }

    private fun showDeleteWordBookDialog(position: Int){
        val dialog = AlertDialog.Builder(mContext)
            .setTitle(mContext.getString(R.string.hint))
            .setMessage(mContext.getString(R.string.confirm_delete_word_book_desc))
            .setCancelable(true)
            .setPositiveButton(mContext.getString(R.string.confirm)){ _, _ ->
                val bookName = wordBookViewModel.wordBookList.value?.get(position) ?: return@setPositiveButton
                wordBookViewModel.deleteWordBook(bookName)
                wordBookViewModel.getWordBookList()
            }
            .setNegativeButton(mContext.getString(R.string.cancel), null)
            .create()
        dialog.show()
    }

    private fun showDeleteWordsDialog(wordList: List<String>, bookName: String){
        val dialog = AlertDialog.Builder(mContext)
            .setTitle(mContext.getString(R.string.hint))
            .setMessage(mContext.getString(R.string.confirm_delete_word_desc))
            .setCancelable(true)
            .setPositiveButton(mContext.getString(R.string.confirm)){ _, _ ->
                wordBookViewModel.deleteWordsFromBook(wordList)
                wordBookViewModel.getWordList(bookName)
                wordBookViewModel.detailMode.value = BOOK_WORD_MODE_NORMAL
            }
            .setNegativeButton(mContext.getString(R.string.cancel), null)
            .create()
        dialog.show()
    }

    private fun showMoveWordBookDialog(wordList: List<String>, oldName: String){
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_word_book_select, null)
        val rvSelectWordBook = dialogView.findViewById<RecyclerView>(R.id.rv_select_word_book)
        val tvAddWordBook =  dialogView.findViewById<TextView>(R.id.tv_add_word_book)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .create()
        tvAddWordBook.setOnClickListener {
            showAddWordBookDialog(wordList, oldName)
            dialog.dismiss()
        }
        rvSelectWordBook.layoutManager = LinearLayoutManager(mContext)
        rvSelectWordBook.adapter = DictSelectWordBookAdapter(wordBookViewModel.getWordBookList()).apply {
            setOnItemClickListener(object : DictSelectWordBookAdapter.OnItemClickListener{
                override fun onItemClick(position: Int) {
                    val res = wordBookViewModel.moveWordBook(wordList, oldName, getBookName(position))
                    dialog.dismiss()
                    if (res.first){
                        showShortToast(getString(R.string.move_word_book, res.second))
                        wordBookViewModel.getWordList(oldName)
                        wordBookViewModel.detailMode.value = BOOK_WORD_MODE_NORMAL
                    } else showShortToast(getString(R.string.operation_failure))
                }
            })
        }
        dialog.show()
    }

    private fun etRequestFocus(editText: EditText){
        editText.requestFocus()
        // 延迟200ms 显示软键盘
        Handler.createAsync(Looper.getMainLooper())
            .postDelayed({
                if (editText.isFocused){
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                }
            }, 200)
    }
}