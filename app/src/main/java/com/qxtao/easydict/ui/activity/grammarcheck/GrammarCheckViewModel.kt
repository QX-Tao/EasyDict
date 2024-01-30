package com.qxtao.easydict.ui.activity.grammarcheck

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.qxtao.easydict.utils.common.EncryptUtils
import com.qxtao.easydict.utils.constant.NetConstant
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.SimpleCallBack
import com.xuexiang.xhttp2.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GrammarCheckViewModel : ViewModel() {
    var pageType = MutableLiveData<Int>() //页面状态 初始界面0 加载中1 已加载2 查询失败3
    var correctedEssay = MutableLiveData<String>() // 正确结果
    var errorSentFeedback = MutableLiveData<List<SentFeedback>>() // 错误语法
    var correctSentFeedback = MutableLiveData<List<SentFeedback>>() // 正确语法
    var pasteData = MutableLiveData<Pair<Boolean, String?>>() // 粘贴板数据 <是否显示，内容>
    var originText = MutableLiveData<String>() // 原始文本

    init {
        errorSentFeedback.value = mutableListOf()
        correctSentFeedback.value = mutableListOf()
        pageType.value = 0
        originText.value = ""
    }

    fun check(str: String) {
        pageType.value = 1
        originText.value = str
        val comContext = str.replace("\n", " ")
        val sign = EncryptUtils.encryptMD5ToString((if (comContext.length > 30) comContext.substring(0, 30) else comContext) + "l9CYii83SlZsEwqW#W6r")
        viewModelScope.launch(Dispatchers.IO) {
            XHttp.post(NetConstant.grammarCheck)
                .params("keyid", 23)
                .params("comcontext", comContext)
                .params("sign", sign)
                .syncRequest(false)
                .execute(object : SimpleCallBack<Any>(){
                    override fun onSuccess(response: Any?) {
                        val gson = Gson()
                        val essayFeedback = gson.fromJson(gson.toJson(((response as LinkedTreeMap<*, *>)["essayFeedback"] as LinkedTreeMap<*, *>)), EssayFeedback::class.java)
                        errorSentFeedback.value = essayFeedback.sentsFeedback.filter { it.isContainGrammarError || it.isContainTypoError }
                        correctSentFeedback.value = essayFeedback.sentsFeedback.filter { !it.isContainGrammarError && !it.isContainTypoError }
                        correctedEssay.value = response["correctedEssay"].toString()
                        viewModelScope.launch(Dispatchers.Main) {
                            pageType.value = 2
                        }
                    }
                    override fun onError(e: ApiException?) {
                        viewModelScope.launch(Dispatchers.Main) {
                            pageType.value = 3
                        }
                    }
                })
        }
    }

    fun setPasteData(isShow: Boolean, data: String? = pasteData.value?.second){
        pasteData.value = Pair(isShow, data)
    }

    data class ErrorPosInfo(
        val reason: String,
        val isValidLangChunk: Boolean,
        val orgChunk: String,
        val new_error_type: Int,
        val error_type: Int,
        val new_sub_error_type: Int,
        val type: String,
        val analysis: String,
        val startPos: Int,
        val correctChunk: String,
        val endPos: Int
    )

    data class SentFeedback(
        val rawSent: String,
        val paraId: Int,
        val sentId: Int,
        val errorPosInfos: List<ErrorPosInfo>,
        val sentFeedback: String,
        val sentStartPos: Int,
        val correctedSent: String,
        val rawSegSent: String,
        val isContainGrammarError: Boolean,
        val isContainTypoError: Boolean,
        val sentScore: Int,
        val isValidLangSent: Boolean
    )

    data class EssayFeedback(
        val sentsFeedback: List<SentFeedback>
    )
}