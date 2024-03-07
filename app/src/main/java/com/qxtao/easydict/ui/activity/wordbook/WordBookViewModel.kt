package com.qxtao.easydict.ui.activity.wordbook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qxtao.easydict.database.WordBookData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class WordBookViewModel(
    private val wordBookData: WordBookData
) : ViewModel() {
    companion object{
        fun Factory(wordBookData: WordBookData): ViewModelProvider.Factory = viewModelFactory {
            initializer { WordBookViewModel(wordBookData) }
        }
    }

    private val _wordBookList = MutableLiveData<List<String>?>()
    val wordBookList: LiveData<List<String>?> = _wordBookList
    private val _bookWordList = MutableLiveData<List<WordBookData.Word>?>()
    val bookWordList: LiveData<List<WordBookData.Word>?> = _bookWordList
    var dataLoadInfo =  MutableLiveData<Int>() // 初始化-1 加载中0 已加载1 加载失败2 列表为空3
    var detailMode = MutableLiveData<Int>() //  0默认 1多选控制


    init {
        getWordBookList()
        detailMode.value = BOOK_WORD_MODE_NORMAL
    }


    fun getWordList(bookName: String) {
        dataLoadInfo.value = -1
        _bookWordList.value = null
        detailMode.value = BOOK_WORD_MODE_NORMAL
        viewModelScope.launch(Dispatchers.IO) {
            dataLoadInfo.postValue(0)
            try {
                val result = async{
                    wordBookData.getWordList(bookName)
                }.await()
                _bookWordList.postValue(result)
                dataLoadInfo.postValue(if (result.isEmpty()) 3 else 1)
            } catch (e: Exception){
                dataLoadInfo.postValue(2)
            }
        }
    }
    fun getWordBookList(): List<String> {
        _wordBookList.value = wordBookData.getWordBookList()
        return _wordBookList.value!!
    }
    fun moveWordBook(wordList: List<String>, bookName: String, newBookName: String): Pair<Boolean, String> = wordBookData.moveWordsToBook(wordList, bookName, newBookName)
    fun deleteWordBook(bookName: String) = wordBookData.deleteWordBook(bookName)
    fun deleteWordsFromBook(wordList: List<String>) = wordBookData.deleteWordsFromBook(wordList)
    fun addWordBook(bookName: String): Pair<Boolean, String> = wordBookData.addWordBook(bookName)
    fun renameWordBook(bookName: String, newBookName: String): Pair<Boolean, String> = wordBookData.renameWordBook(bookName, newBookName)
}
