package com.qxtao.easydict.ui.activity.quicksearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qxtao.easydict.database.SearchHistoryData
import com.qxtao.easydict.database.SimpleDictData
import com.qxtao.easydict.ui.activity.dict.DICT_RV_HISTORY
import com.qxtao.easydict.ui.activity.dict.DICT_RV_SUGGESTION
import com.qxtao.easydict.ui.activity.dict.DICT_RV_SUGGESTION_LM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

class QuickSearchViewModel(
    private val simpleDictData: SimpleDictData,
    private val searchHistoryData: SearchHistoryData,
) : ViewModel() {
    private val _dictSearchHistory = MutableLiveData<List<SearchHistoryData.SearchHistory>?>()
    val dictSearchHistory: LiveData<List<SearchHistoryData.SearchHistory>?> = _dictSearchHistory
    private val _dictSearchSuggestion = MutableLiveData<List<SimpleDictData.SimpleDict>?>()
    val dictSearchSuggestion: LiveData<List<SimpleDictData.SimpleDict>?> = _dictSearchSuggestion
    var hasShowRvInfo = MutableLiveData<Int>() //history or suggesting
    private val deleteQueue: Queue<Pair<SearchHistoryData.SearchHistory, Int>> = LinkedList() // 预删除的搜索记录
    var pasteData = MutableLiveData<Pair<Boolean, String?>>() // 粘贴板数据 <是否显示，内容>

    companion object{
        fun Factory(simpleDictData: SimpleDictData, searchHistoryData: SearchHistoryData): ViewModelProvider.Factory = viewModelFactory{
            initializer { QuickSearchViewModel(simpleDictData, searchHistoryData) }
        }
    }

    init {
        _dictSearchHistory.value = mutableListOf()
        _dictSearchSuggestion.value = mutableListOf()
        hasShowRvInfo.value = DICT_RV_HISTORY
    }


    // 获取搜索记录
    fun getDictSearchHistory(){
        viewModelScope.launch(Dispatchers.IO) {
            val list = searchHistoryData.getSearchHistory()
            _dictSearchHistory.postValue(list)
        }
    }

    // 搜索提示
    fun searchInWordData(str: String){
        viewModelScope.launch(Dispatchers.IO) {
            val list = simpleDictData.searchDicts(str)
            _dictSearchSuggestion.postValue(list)
            hasShowRvInfo.postValue(if (list?.isNotEmpty() == true) DICT_RV_SUGGESTION else DICT_RV_SUGGESTION_LM)
        }
    }

    // 显示哪一个RecycleView
    fun setHasShowRvInfo(mode : Int = DICT_RV_HISTORY){
        hasShowRvInfo.value = mode
    }

    // 预删除记录
    fun deleteSearchHistoryItem(position: Int) {
        val deletedItem = _dictSearchHistory.value!![position]
        deleteQueue.offer(deletedItem to position)
        val currentList = _dictSearchHistory.value!!.toMutableList()
        currentList.removeAt(position)
        _dictSearchHistory.value = currentList
    }

    // 撤销删除记录
    fun undoDeleteWord() {
        val (deletedItem, position) = deleteQueue.poll() ?: return
        val currentList = _dictSearchHistory.value!!.toMutableList()
        currentList.add(position, deletedItem)
        _dictSearchHistory.value = currentList
    }

    fun getDeleteQueue() = deleteQueue

    // 真正的删除记录
    fun deleteSearchRecord(){
        val (deletedItem, _) = deleteQueue.poll() ?: return
        deleteSearchRecord(deletedItem)
    }
    private fun deleteSearchRecord(deletedItem: SearchHistoryData.SearchHistory){
        searchHistoryData.deleteSearchRecord(deletedItem.origin)
    }

    fun setPasteData(isShow: Boolean, data: String? = pasteData.value?.second){
        pasteData.value = Pair(isShow, data)
    }

}
