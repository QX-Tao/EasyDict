package com.qxtao.easydict.ui.activity.daysentence

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qxtao.easydict.adapter.dailysentence.DailySentenceItem
import com.qxtao.easydict.adapter.dailysentence.TYPE_FOOTER
import com.qxtao.easydict.adapter.dailysentence.TYPE_NORMAL
import com.qxtao.easydict.database.DailySentenceData
import com.qxtao.easydict.utils.common.HttpHelper
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.constant.NetConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class DaySentenceViewModel(private val dailySentenceData: DailySentenceData) : ViewModel() {
    private val _dataList = MutableLiveData<ArrayList<DailySentenceItem>>()
    val dataList: LiveData<ArrayList<DailySentenceItem>> = _dataList
    private var _mediaPlayer: MediaPlayer ?= null
    private var _requestDate: String = TimeUtils.getCurrentDateByPattern("yyyy-MM-dd")
    var isLoadingNextPage = false
    var isPullingData = false
    val playPosition = MutableLiveData<Int>()
    var firstVisibleItemPosition = -1
    var topOffset = -1
    var isDoubleClickHeader =  MutableLiveData<Boolean>()
    var dataLoadInfo = MutableLiveData<Int>() // 初始化-1 加载中0 已加载1 加载失败2 到底了3

    companion object{
        fun Factory(dailySentenceData: DailySentenceData): ViewModelProvider.Factory = viewModelFactory {
            initializer { DaySentenceViewModel(dailySentenceData) }
        }
    }

    init {
        dataLoadInfo.value = -1
        playPosition.value = -1
        isDoubleClickHeader.value = false
        addData(DailySentenceItem(TYPE_FOOTER, FOOTER_DATE, "","","",""))
    }

    fun getData(requestDate: String = _requestDate, dataSize: Int = 1) {
        viewModelScope.launch(Dispatchers.Main){ dataLoadInfo.value = 0 }
        val items = ArrayList<DailySentenceItem>()
        for (i in 0 until dataSize) {
            if (_requestDate == TimeUtils.getDayBeforeGivenDateByPattern("yyyy-MM-dd", ORIGIN_DATE)) {
                viewModelScope.launch(Dispatchers.Main){ dataLoadInfo.value = 3 }
                break
            }
            val item = getData(_requestDate)
            if (item == null) {
                // 某一项加载失败
                viewModelScope.launch(Dispatchers.Main){ dataLoadInfo.value = 2 }
                break
            } else {
                _requestDate = TimeUtils.getFormerlyDateByPatternDateAndD(
                    requestDate,
                    i + 1,
                    "yyyy-MM-dd",
                    "yyyy-MM-dd"
                )
                items.add(item)
            }
        }
        viewModelScope.launch(Dispatchers.Main){
            when (dataLoadInfo.value) {
                2 -> {}
                3 -> { removeFooterData() }
                else -> { dataLoadInfo.value = 1 }
            }
        }
        addData(items)
    }


    fun getData(date: String): DailySentenceItem? {
        val tmp = dailySentenceData.getDataByDate(date)
        if (tmp != null)
            return DailySentenceItem(TYPE_NORMAL, tmp.date, tmp.enSentence, tmp.cnSentence, tmp.imageUrl, tmp.ttsUrl)
        else {
            try {
                val imgResponse = HttpHelper.requestDisableCertificateValidationResponse(
                    NetConstant.imgApi + TimeUtils.getFormatDateByPattern(date, "yyyy-MM-dd", "yyyyMMdd"))
                val imageUrl = imgResponse.request.url.toString()

                val dailySentenceResponseJson = HttpHelper.requestResult(NetConstant.dailySentenceApi + date)
                val dailySentenceJsonObject = JSONObject(dailySentenceResponseJson)
                val enSentence: String = dailySentenceJsonObject.getString("content").trim()
                val chSentence: String = dailySentenceJsonObject.getString("note").trim()
                val ttsUrl: String = dailySentenceJsonObject.getString("tts")

                if (imageUrl.isNotEmpty() && enSentence.isNotEmpty() && chSentence.isNotEmpty() && ttsUrl.isNotEmpty()) {
                    dailySentenceData.insertData(date, enSentence, chSentence, imageUrl, ttsUrl)
                    return DailySentenceItem(TYPE_NORMAL, date, enSentence, chSentence, imageUrl, ttsUrl)
                }
                return null
            } catch (e: Exception) {
                return null
            }
        }
    }

    fun startPlaySound(position: Int, soundPath: String) {
        try {
            if (playPosition.value == position) {
                stopPlaySound()
            } else {
                stopPlaySound()
                _mediaPlayer = MediaPlayer().apply {
                    setDataSource(soundPath)
                    prepareAsync()
                    playPosition.value = position
                    setOnPreparedListener { start() }
                    setOnCompletionListener { stopPlaySound() }
                }
            }
        } catch (_: Exception){ }
    }

    fun stopPlaySound() {
        playPosition.value = -1
        if (_mediaPlayer != null){
            _mediaPlayer?.stop()
            _mediaPlayer?.release()
            _mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean = _mediaPlayer?.isPlaying == true

    private fun addData(data: ArrayList<DailySentenceItem>) {
        val currentDataList = _dataList.value ?: ArrayList()
        for (item in data) {
            if (currentDataList.any { it.date == item.date }) { continue }
            currentDataList.add(item)
        }
        _dataList.postValue(currentDataList)
        _dataList.value?.sortByDescending { it.date }
    }

    private fun addData(data: DailySentenceItem){
        val currentDataList = _dataList.value ?: ArrayList()
        if (currentDataList.any { it.date == data.date }) {
            return
        }
        currentDataList.add(data)
        _dataList.postValue(currentDataList)
        _dataList.value?.sortByDescending { it.date }
    }

    private fun removeFooterData(){
        val currentDataList = _dataList.value ?: ArrayList()
        val footerItem = currentDataList.find { it.date == FOOTER_DATE } ?: return
        currentDataList.remove(footerItem)
        _dataList.postValue(currentDataList)
    }

    fun getData(): ArrayList<DailySentenceItem> = _dataList.value ?: ArrayList()


}