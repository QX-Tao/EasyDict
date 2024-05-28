package com.qxtao.easydict.ui.activity.wordlist

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qxtao.easydict.adapter.popupmenu.PopupMenuItem
import com.qxtao.easydict.adapter.wordlist.WordListItem
import com.qxtao.easydict.database.WordListData
import com.qxtao.easydict.utils.constant.NetConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue


class WordListViewModel(private val wordListData: WordListData) : ViewModel() {
    private val wordMap = mapOf(0 to "cet4", 1 to "cet6", 2 to "kaoyan", 3 to "ielts",
        4 to "toefl", 5 to "xiaoxue", 6 to "chuzhong", 7 to "gaokao", 8 to "tem4", 9 to "tem8")
    private val reverseWordMap = mapOf("cet4" to 0, "cet6" to 1, "kaoyan" to 2, "ielts" to 3,
        "toefl" to 4, "xiaoxue" to 5, "chuzhong" to 6, "gaokao" to 7, "tem4" to 8, "tem8" to 9)
    private val wordNameMap = mapOf("cet4" to "四级大纲", "cet6" to "六级大纲", "kaoyan" to "考研词汇", "ielts" to "雅思词汇",
        "toefl" to "托福词汇", "xiaoxue" to "小学词汇", "chuzhong" to "初中大纲", "gaokao" to "高考大纲", "tem4" to "专四大纲", "tem8" to "专八大纲")
    private val clasNameMap = mapOf(0 to "全部单词", 1 to "在学单词", 2 to "已学单词", 3 to "收藏单词")
    private val voiceSoundMap = mapOf(0 to NetConstant.voiceUs, 1 to NetConstant.voiceUk)
    private val _wordListItems = MutableLiveData<List<WordListItem>?>()
    val wordListItems: LiveData<List<WordListItem>?> = _wordListItems
    private val deleteQueue: Queue<DeleteTuple> = LinkedList() // 哪一个量 position是多少 哪一个列表
    var firstVisibleItemPosition = -1
    private var _mediaPlayer: MediaPlayer?= null
    var topOffset = -1
    var appBarExpanded = true
    var dataLoadInfo =  MutableLiveData<Int>() // 初始化-1 加载中0 已加载1 加载失败2 列表为空3
    var isPlaying = MutableLiveData<Boolean>()
    var playSound = MutableLiveData<Int>()
    var wordSelected = MutableLiveData<String?>()
    var clasSelected = MutableLiveData<String?>()
    private var playPosition = -1
    var clasPopWindowList = mutableListOf(
        PopupMenuItem(0, "全部单词", false),
        PopupMenuItem(1, "在学单词", true),
        PopupMenuItem(2, "已学单词", false),
        PopupMenuItem(3, "收藏单词", false)
    )
    var wordPopWindowList = mutableListOf(
        PopupMenuItem(0, "四级大纲", true),
        PopupMenuItem(1, "六级大纲", false),
        PopupMenuItem(2, "考研词汇", false),
        PopupMenuItem(3, "雅思词汇", false),
        PopupMenuItem(4, "托福词汇", false),
        PopupMenuItem(5, "小学词汇", false),
        PopupMenuItem(6, "初中大纲", false),
        PopupMenuItem(7, "高考大纲", false),
        PopupMenuItem(8, "专四大纲", false),
        PopupMenuItem(9, "专八大纲", false)
    )

    companion object{
        fun Factory(wordListData: WordListData): ViewModelProvider.Factory = viewModelFactory {
            initializer { WordListViewModel(wordListData) }
        }
    }

    init {
        playSound.value = 0
        dataLoadInfo.value = -1
        isPlaying.value = false
    }

    fun initData(){
        if (dataLoadInfo.value == 1 || dataLoadInfo.value == 3) return
        dataLoadInfo.value = 0
        viewModelScope.launch(Dispatchers.IO){
            try {
                val result = async { wordListData.getAllWordListItems() }.await()
                _wordListItems.postValue(result.first)
                dataLoadInfo.postValue(if (result.first.isEmpty()) 3 else 1)
                wordSelected.postValue(wordNameMap[result.second])
                selectPopupMenuList(wordPopWindowList, reverseWordMap[result.second]!!)
            } catch (e: Exception){
                dataLoadInfo.postValue(2)
            }
        }
    }

    fun setWordSelected(position: Int){
        if (!selectPopupMenuList(wordPopWindowList, position)) return
        dataLoadInfo.value = -1
        viewModelScope.launch(Dispatchers.IO){
            async{ wordListData.updateConfig(wordMap[position]!!) }.await()
            withContext(Dispatchers.Main){ initData() }
        }
    }

    fun setClasSelected(position: Int){
        if (!selectPopupMenuList(clasPopWindowList, position)) return
        dataLoadInfo.value = -1
        viewModelScope.launch(Dispatchers.IO){
            dataLoadInfo.postValue(0)
            try {
                val result = async {
                    when (position){
                        0 -> { wordListData.getAllWordListItem() }
                        1 -> { wordListData.getLearningWordListItem() }
                        2 -> { wordListData.getLearnedWordListItem() }
                        3 -> { wordListData.getCollectedWordListItem() }
                        else -> wordListData.getAllWordListItem()
                    }
                }.await()
                clasSelected.postValue(clasNameMap[position])
                _wordListItems.postValue(result)
                dataLoadInfo.postValue(if (result.isEmpty()) 3 else 1)
            } catch (e: Exception){
                dataLoadInfo.postValue(2)
            }
        }
    }

    fun deleteWord(listPosition: Int,
                   clasPosition: Int = getClasPopupMenuListSelectedPosition(),
                   wordPosition: Int = getWordPopupMenuListSelectedPosition()
    ) {
        val deletedItem = _wordListItems.value!![listPosition]
        deleteQueue.offer(DeleteTuple(deletedItem, listPosition, clasPosition, wordPosition))
        val currentList = _wordListItems.value!!.toMutableList()
        currentList.removeAt(listPosition)
        _wordListItems.value = currentList
        dataLoadInfo.value = if (currentList.isEmpty()) 3 else 1
    }
    fun undoDeleteWord() {
        val (deletedItem, listPosition, _, _) = deleteQueue.poll() ?: return
        val currentList = _wordListItems.value!!.toMutableList()
        currentList.add(listPosition, deletedItem)
        _wordListItems.value = currentList
        dataLoadInfo.value = if (currentList.isEmpty()) 3 else 1
    }
    fun deleteWordRecord(){
        val deleteTuple = deleteQueue.poll() ?: return
        deleteWordRecord(deleteTuple)
    }
    private fun deleteWordRecord(deleteTuple: DeleteTuple){
        val deletedItem = deleteTuple.wordListItem
        val wordPosition = deleteTuple.wordPosition
        when(deleteTuple.clasPosition){
            1 -> wordListData.setIsLearned(deletedItem.wordName, wordPosition, true)
            2 -> wordListData.setIsLearned(deletedItem.wordName, wordPosition, false)
            3 -> wordListData.setIsCollected(deletedItem.wordName, wordPosition, false)
        }
    }

    fun collectWord(position: Int, wordPosition: Int = getWordPopupMenuListSelectedPosition()){
        wordListData.setIsCollected(_wordListItems.value!![position].wordName, wordPosition, true)
    }

    fun setWordOnclick(position: Int) {
        val currentList = _wordListItems.value!!.toMutableList()
        currentList[position].isOnClick = !currentList[position].isOnClick
        _wordListItems.value = currentList
    }

    fun playWordVoice(position: Int){
        val itemWord = _wordListItems.value!![position]
        try {
            stopPlaySound()
            isPlaying.value = true
            playPosition = position
            _mediaPlayer = MediaPlayer().apply {
                setDataSource(voiceSoundMap[playSound.value] + itemWord.wordName)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener { stopPlaySound() }
            }
        } catch (_: Exception){ }
    }

    fun stopPlaySound() {
        isPlaying.value = false
        if (_mediaPlayer != null){
            _mediaPlayer?.stop()
            _mediaPlayer?.release()
            _mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean = isPlaying.value == true

    fun getDeleteQueue() = deleteQueue

    fun switchVoice(){
        playSound.value = if (playSound.value == 0) 1 else 0
        stopPlaySound()
        playWordVoice(playPosition)
    }

    private fun selectPopupMenuList(list: List<PopupMenuItem>, position: Int): Boolean {
        if (list.find { it.isMenuItemSelected }!!.position == position) return false
        for (element in list) element.isMenuItemSelected = false
        list[position].isMenuItemSelected = true
        return true
    }

    fun getClasPopupMenuListSelectedPosition(): Int{
        return clasPopWindowList.find { it.isMenuItemSelected }!!.position
    }
    fun getWordPopupMenuListSelectedPosition(): Int{
        return wordPopWindowList.find { it.isMenuItemSelected }!!.position
    }

    data class DeleteTuple(
        val wordListItem: WordListItem,
        val listPosition: Int,
        val clasPosition: Int,
        val wordPosition: Int
    )

}