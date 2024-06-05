package com.qxtao.easydict.ui.activity.daysentence

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qxtao.easydict.database.DailySentenceData
import com.qxtao.easydict.utils.common.HttpHelper
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.constant.NetConstant
import org.json.JSONObject


class DaySentenceViewModel(private val dailySentenceData: DailySentenceData) : ViewModel() {
    private var _mediaPlayer: MediaPlayer ?= null
    val playPosition = MutableLiveData<Int>()

    companion object{
        fun Factory(dailySentenceData: DailySentenceData): ViewModelProvider.Factory = viewModelFactory {
            initializer { DaySentenceViewModel(dailySentenceData) }
        }
    }

    init {
        playPosition.value = -1
    }

    fun getData(date: String, reload: Boolean = false): DailySentenceData.DailySentence? {
        val tmp = dailySentenceData.getDataByDate(date)
        if (tmp != null && !reload){
            return DailySentenceData.DailySentence(tmp.date, tmp.enSentence, tmp.cnSentence, tmp.imageUrl, tmp.ttsUrl)
        } else {
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
                    return DailySentenceData.DailySentence(date, enSentence, chSentence, imageUrl, ttsUrl)
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

}