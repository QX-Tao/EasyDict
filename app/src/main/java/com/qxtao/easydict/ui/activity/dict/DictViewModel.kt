package com.qxtao.easydict.ui.activity.dict

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.qxtao.easydict.adapter.dict.DictBlngClassificationAdapter
import com.qxtao.easydict.adapter.dict.TYPE_BLNG_CLASSIFICATION_NORMAL
import com.qxtao.easydict.adapter.dict.TYPE_BLNG_CLASSIFICATION_SELECT
import com.qxtao.easydict.database.SearchHistoryData
import com.qxtao.easydict.database.SimpleDictData
import com.qxtao.easydict.utils.common.HttpHelper
import com.qxtao.easydict.utils.constant.NetConstant
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.SimpleCallBack
import com.xuexiang.xhttp2.exception.ApiException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.util.LinkedList
import java.util.Queue


class DictViewModel(
    private val simpleDictData: SimpleDictData,
    private val searchHistoryData: SearchHistoryData
) : ViewModel() {
    private val _dictSearchHistory = MutableLiveData<List<SearchHistoryData.SearchHistory>?>()
    val dictSearchHistory: LiveData<List<SearchHistoryData.SearchHistory>?> = _dictSearchHistory
    private val _dictSearchSuggestion = MutableLiveData<List<SimpleDictData.SimpleDict>?>()
    val dictSearchSuggestion: LiveData<List<SimpleDictData.SimpleDict>?> = _dictSearchSuggestion
    private val _dictSearchSugWord = MutableLiveData<List<SimpleDictData.SimpleDict>?>()
    val dictSearchSugWord: LiveData<List<SimpleDictData.SimpleDict>?> = _dictSearchSugWord
    private val deleteQueue: Queue<Pair<SearchHistoryData.SearchHistory, Int>> = LinkedList() // 预删除的搜索记录
    var dataLoadInfo = MutableLiveData<Int>() // 初始化-1  加载中0 已加载1 加载失败2
    var dataMoreBlngLoadInfo = MutableLiveData<Int>() // 初始化-1  加载中0 已加载1 加载失败2
    var dataMoreAuthLoadInfo = MutableLiveData<Int>() // 初始化-1  加载中0 已加载1 加载失败2
    var dataSuggestionLoadInfo = MutableLiveData<Int>() // 初始化-1  加载中0 已加载1
    var hasShowRvInfo = MutableLiveData<Pair<Boolean, Boolean>>() //history-first  suggesting-second
    var hasShowDetailFragment = Triple(true, false, false) //JM-first  CO-second  EE-third
    var searchText = MutableLiveData<SearchText>()
    var detailFragmentAppBarExpanded = MutableLiveData<Int>() // appBarLayout状态
    var nsvOffset = mutableMapOf(JM_FRAGMENT to 0, CO_FRAGMENT to 0, EE_FRAGMENT to 0) // 页面滚动状态
    val playPosition = MutableLiveData<Map<Int, Int>>() // -1不播放
    private var _mediaPlayer: MediaPlayer?= null
    var hasSearchResult = MutableLiveData<Boolean>() // 初始化-1 无结果0 有结果1

    // 词典搜索信息
    var searchInfoResponse = MutableLiveData<SearchInfoResponse?>()
    var dictsResponse = MutableLiveData<DictsResponse?>()
    var ehResponse = MutableLiveData<EhResponse?>()
    var heResponse = MutableLiveData<HeResponse?>()
    var antoResponse = MutableLiveData<AntoResponse?>()
    var synoResponse = MutableLiveData<SynoResponse?>()
    var thesaurusResponse = MutableLiveData<ThesaurusResponse?>()
    var bilingualSentencesResponse = MutableLiveData<BilingualSentencesResponse?>()
    var examTypeResponse = MutableLiveData<ExamTypeResponse?>()
    var eeDictionaryResponse = MutableLiveData<EEDictionaryResponse?>()
    var authSentenceResponse = MutableLiveData<AuthSentencesResponse?>()
    var relWordResponse = MutableLiveData<RelWordResponse?>()
    var phrsResponse = MutableLiveData<PhrsResponse?>()
    var collinsResponse = MutableLiveData<CollinsResponse?>()
    var baikeDigest = MutableLiveData<BaikeDigest?>()
    var inflectionResponse = MutableLiveData<InflectionResponse?>()
    var topicResponse = MutableLiveData<TopicResponse?>()
    var picDictResponse = MutableLiveData<PicDictResponse?>()
    var typoResponse = MutableLiveData<TypoResponse?>()
    var webTransResponse = MutableLiveData<WebTransResponse?>()
    var etymResponse = MutableLiveData<EtymResponse?>()
    var specialResponse = MutableLiveData<SpecialResponse?>()
    // 双语例句
    var blngClassification = MutableLiveData<List<DictBlngClassificationAdapter.DictBlngClassificationItem>?>()
    var blngSents = MutableLiveData<Map<String, List<SentencePair>?>?>()
    var blngSelectedItem = MutableLiveData<String>()

    companion object{
        fun Factory(simpleDictData: SimpleDictData, searchHistoryData: SearchHistoryData): ViewModelProvider.Factory = viewModelFactory {
            initializer { DictViewModel(simpleDictData, searchHistoryData) }
        }
    }

    init {
        playPosition.value = mapOf(VOICE_AUTH_PART to -1, VOICE_BLNG_PART to -1, VOICE_EH_PART to -1, VOICE_NORMAL to -1)
        _dictSearchHistory.value = mutableListOf()
        _dictSearchSuggestion.value = mutableListOf()
        dataLoadInfo.value = -1
        detailFragmentAppBarExpanded.value = APPBAR_LAYOUT_EXPANDED
        dataMoreBlngLoadInfo.value = -1
        dataMoreAuthLoadInfo.value = -1
        dataSuggestionLoadInfo.value = -1
        hasShowRvInfo.value = Pair(true, false)
        searchText.value = SearchText(null, "",  0)
        getDictSearchSugWord()
    }

    // 设置搜索文本
    fun setSearchText(editSearchText: String, editCursor: Int){
        this.searchText.value = SearchText(this.searchText.value?.searchText, editSearchText, editCursor)
    }
    private fun setSearchText(searchText: String?, editSearchText: String = searchText ?: ""){
        this.searchText.value = SearchText(searchText, editSearchText, searchText?.length ?: editSearchText.length)
    }

    // 设置页面滚动状态
    fun setNsvScrollOffset(fragment: Int, offset: Int){
        nsvOffset[fragment] = offset
    }

    // 获取搜索记录
    fun getDictSearchHistory(){
        viewModelScope.launch(Dispatchers.IO) {
            val list = searchHistoryData.getSearchHistory()
            withContext(Dispatchers.Main) {
                _dictSearchHistory.value = list
            }
        }
    }

    // 获取推荐单词
    private fun getDictSearchSugWord(){
        viewModelScope.launch(Dispatchers.IO) {
            val list = simpleDictData.getRandomSimpleDictList()
            withContext(Dispatchers.Main) {
                _dictSearchSugWord.value = list
            }
        }
    }

    // 搜索提示
    fun searchInWordData(str: String){
        viewModelScope.launch(Dispatchers.IO) {
            dataSuggestionLoadInfo.postValue(0)
            val list = simpleDictData.searchDicts(str)
            setHasShowRvInfo(second = list?.isNotEmpty() == true)
            _dictSearchSuggestion.postValue(list)
            dataSuggestionLoadInfo.postValue(1)
        }
    }

    // 让检索结果为空
    fun setDictSearchSuggestionEmpty() {
        _dictSearchSuggestion.value = mutableListOf()
        dataSuggestionLoadInfo.value = -1
    }

    // hasFragment的列表显示控制
    fun setHasShowRvInfo(first: Boolean = hasShowRvInfo.value?.first ?: true, second: Boolean = hasShowRvInfo.value?.second ?: false) {
        viewModelScope.launch(Dispatchers.IO) {
            hasShowRvInfo.postValue(Pair(first, second))
        }
    }

    // 添加记录到表
    private fun setSearchHistoryItem(origin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var translation = ""
            if (ehResponse.value != null && heResponse.value == null){
                if (ehResponse.value?.isTran == true){
                    ehResponse.value?.trs?.let {
                        translation = it[0].tran ?: ""
                    }
                } else {
                    ehResponse.value?.trs?.let {
                        val l = mutableListOf<String>()
                        for (i in it.indices) {
                            l.add(((it[i].pos ?: "") + " " + it[i].tran).trim())
                        }
                        translation = l.joinToString(" ")
                    }
                }
            }
            if (heResponse.value != null && ehResponse.value == null){
                if (heResponse.value?.isTran == true){
                    heResponse.value?.trans?.let {
                        translation = it[0].w ?: ""
                    }
                } else {
                    heResponse.value?.trans?.let {
                        val l = mutableListOf<String>()
                        for (i in it.indices) {
                            l.add(((it[i].type ?: "") + " " + it[i].w).trim())
                        }
                        translation = l.joinToString(" ")
                    }
                }
            }
            if (ehResponse.value == null && heResponse.value == null){
                translation = simpleDictData.getTranslationByOrigin(origin) ?: ""
            }
            searchHistoryData.setSearchRecord( origin, translation)
            getDictSearchHistory()
        }
    }

    // 预删除记录
    fun deleteSearchHistoryItem(position: Int) {
        val deletedItem = _dictSearchHistory.value!![position]
        deleteQueue.offer(deletedItem to position)
        val currentList = _dictSearchHistory.value!!.toMutableList()
        currentList.removeAt(position)
        _dictSearchHistory.value = currentList
        if (deleteQueue.size > 1) deleteQueue.poll()?.let { deleteSearchRecord(it.first) }
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

    // 清除旧数据
    private fun clearResponseData(){
        playPosition.value = mapOf(VOICE_AUTH_PART to -1, VOICE_BLNG_PART to -1, VOICE_EH_PART to -1, VOICE_NORMAL to -1)
        detailFragmentAppBarExpanded.value = APPBAR_LAYOUT_EXPANDED
        nsvOffset = mutableMapOf(JM_FRAGMENT to 0, CO_FRAGMENT to 0, EE_FRAGMENT to 0)
        searchInfoResponse.value = null
        dictsResponse.value = null
        ehResponse.value = null
        heResponse.value = null
        antoResponse.value = null
        synoResponse.value = null
        thesaurusResponse.value = null
        bilingualSentencesResponse.value = null
        examTypeResponse.value = null
        eeDictionaryResponse.value = null
        authSentenceResponse.value = null
        relWordResponse.value = null
        phrsResponse.value = null
        collinsResponse.value = null
        typoResponse.value = null
        baikeDigest.value = null
        inflectionResponse.value = null
        topicResponse.value = null
        picDictResponse.value = null
        blngClassification.value = null
        blngSents.value = null
        webTransResponse.value = null
        etymResponse.value = null
        specialResponse.value = null
        dataMoreAuthLoadInfo.value = -1
        dataMoreBlngLoadInfo.value = -1
        hasSearchResult.value = true
    }

    // 离线查询
    fun offlineSearch(str: String) :String? {
        // 将确定进行搜索的文本保存一下
        setSearchText(searchText = null, editSearchText = str)
        // 每次搜索前，将之前的信息删除
        clearResponseData()
        setSearchHistoryItem(str)
        return simpleDictData.getTranslationByOrigin(str)
    }

    // 设置词典搜索详情数据
    private fun setDictDetailData() {
        viewModelScope.launch(Dispatchers.IO) {
            // 词典页面
            hasShowDetailFragment = Triple(true, collinsResponse.value != null && collinsResponse.value?.collins_entries?.isNotEmpty() == true,
                eeDictionaryResponse.value != null && eeDictionaryResponse.value?.word != null && eeDictionaryResponse.value?.source != null)

            // 双语例句分类
            val items = mutableListOf<DictBlngClassificationAdapter.DictBlngClassificationItem>()
            bilingualSentencesResponse.value?.`sentence-multi`.let {
                if (it != null){
                    items.add(
                        DictBlngClassificationAdapter.DictBlngClassificationItem(
                            TYPE_BLNG_CLASSIFICATION_SELECT,
                            "所有"
                        )
                    )
                    for (item in it){
                        items.add(
                            DictBlngClassificationAdapter.DictBlngClassificationItem(
                                TYPE_BLNG_CLASSIFICATION_NORMAL,
                                item.tran!!
                            )
                        )
                    }
                } else if (bilingualSentencesResponse.value?.`sentence-pair`!= null){
                    items.add(
                        DictBlngClassificationAdapter.DictBlngClassificationItem(
                            TYPE_BLNG_CLASSIFICATION_SELECT,
                            "所有"
                        )
                    )
                } else return@launch
            }
            blngClassification.postValue(items)
            val upd = blngSents.value?.toMutableMap() ?: mutableMapOf()
            for (item in items){
                if (item.text == "所有") {
                    upd[item.text] = bilingualSentencesResponse.value?.`sentence-pair`
                    continue
                }
                upd[item.text] = bilingualSentencesResponse.value?.`sentence-multi`?.find { it.tran == item.text }?.`sentence-pair`
            }
            blngSents.postValue(upd)
            blngSelectedItem.postValue("所有")
        }
    }

    // 设置双语例句选了啥
    fun setBlngClassification(text: String){
        val upd = blngClassification.value?.toMutableList()
        upd?.forEach {
            it.type = if (it.text == text) TYPE_BLNG_CLASSIFICATION_SELECT else TYPE_BLNG_CLASSIFICATION_NORMAL
        }
        blngClassification.value = upd
        blngSelectedItem.value = text
    }

    fun startPlaySound(type: Int, position: Int, audio: String, le: String): Boolean {
        try {
            if (playPosition.value?.get(type) == position) {
                stopPlaySound()
                return false
            } else {
                stopPlaySound()
                val upd = playPosition.value?.toMutableMap() ?: mutableMapOf(VOICE_AUTH_PART to -1, VOICE_BLNG_PART to -1, VOICE_NORMAL to -1)
                upd[type] = position
                playPosition.value = upd
                val data = "${NetConstant.dictVoice}?audio=${audio}&le=${le}"
                _mediaPlayer = MediaPlayer().apply {
                    setDataSource(data)
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener { stopPlaySound() }
                }
                return true
            }
        } catch (_: Exception){ }
        return false
    }

    fun stopPlaySound() {
        playPosition.value = mapOf(VOICE_AUTH_PART to -1, VOICE_BLNG_PART to -1, VOICE_EH_PART to -1, VOICE_NORMAL to -1)
        if (_mediaPlayer != null){
            _mediaPlayer?.stop()
            _mediaPlayer?.release()
            _mediaPlayer = null
        }
    }



    // 网络请求
    fun onlineSearch(s: String){ OnlineSearch(s).search() }
    fun onlineSearchSent(str: String? = searchText.value?.searchText){
        if (dataMoreAuthLoadInfo.value == 1) return // 数据已经加载 直接返回即可
        dataMoreAuthLoadInfo.value = 0
        if (str.isNullOrBlank()){
            dataMoreAuthLoadInfo.value = 2
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            XHttp.post(NetConstant.dictBase)
                .params("q", str)
                .params("jsonversion", 2)
                .params("tag", "sents-ee")
                .params("from", "en")
                .params("to", "zh-CN")
                .syncRequest(false)
                .execute(object : SimpleCallBack<Any>(){
                    override fun onSuccess(response: Any?) {
                        val gson = Gson()
                        val res = response as LinkedTreeMap<*, *>
                        authSentenceResponse.value = gson.fromJson(gson.toJson(res["auth_sents"]), AuthSentencesResponse::class.java)
                        dataMoreAuthLoadInfo.value = 1
                    }
                    override fun onError(e: ApiException?) {
                        dataMoreAuthLoadInfo.value = 2
                    }
                })
        }
    }
    fun onlineSearchBlng(str: String? = searchText.value?.searchText){
        if (dataMoreBlngLoadInfo.value == 1) return // 数据已经加载 直接返回即可
        dataMoreBlngLoadInfo.value = 0
        if (str.isNullOrBlank()){
            dataMoreBlngLoadInfo.value = 2
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            XHttp.post(NetConstant.dictBase)
                .params("q", str)
                .params("jsonversion", 2)
                .params("tag", "detail-sents-blng")
                .params("from", "en")
                .params("to", "zh-CN")
                .syncRequest(false)
                .execute(object : SimpleCallBack<Any>(){
                    override fun onSuccess(response: Any?) {
                        val gson = Gson()
                        val res = response as LinkedTreeMap<*, *>
                        bilingualSentencesResponse.value = gson.fromJson(gson.toJson(res["blng_sents"]), BilingualSentencesResponse::class.java)
                        setDictDetailData()
                        dataMoreBlngLoadInfo.value = 1
                    }
                    override fun onError(e: ApiException?) {
                        dataMoreBlngLoadInfo.value = 2
                    }
                })
        }
    }
    private inner class OnlineSearch(s: String){
        private val str = s.trim()
        fun search(){
            if (dataLoadInfo.value == 1 && str == searchText.value?.searchText) {
                setSearchText(searchText = str)
                return // 数据已经加载 直接返回即可
            }
            dataLoadInfo.value = 0
            if (str.isBlank()){
                dataLoadInfo.value = 2
                return
            }
            // 将确定进行搜索的文本保存一下
            setSearchText(searchText = str)
            // 每次搜索前，将之前的信息删除
            clearResponseData()
            viewModelScope.launch(Dispatchers.IO) {
                val request1 = async { onlineSearch() }
                val request2 =  async { onlineSearchMore() }
                dataLoadInfo.postValue(if (request1.await() && request2.await()) 1 else 2)
            }
        }
        private suspend fun onlineSearch(): Boolean {
            return withContext(Dispatchers.IO){
                try {
                    val result = CompletableDeferred<Boolean>()
                    XHttp.post(NetConstant.dictBase)
                        .params("q", str.trim())
                        .params("jsonversion", 2)
                        .params("tag", "detail-eh")
                        .params("from", "en")
                        .params("to", "zh-CN")
                        .syncRequest(false)
                        .execute(object : SimpleCallBack<Any>(){
                            override fun onSuccess(response: Any?) {
                                val gson = Gson()
                                val res = response as LinkedTreeMap<*, *>
                                searchInfoResponse.value = gson.fromJson(gson.toJson(res["search-info"]), SearchInfoResponse::class.java)
                                eeDictionaryResponse.value = gson.fromJson(gson.toJson(res["ee"]), EEDictionaryResponse::class.java)
                                collinsResponse.value = gson.fromJson(gson.toJson(res["collins"]), CollinsResponse::class.java)
                                ehResponse.value =  gson.fromJson(gson.toJson(res["eh"]), EhResponse::class.java)
                                heResponse.value =  gson.fromJson(gson.toJson(res["he"]), HeResponse::class.java)
                                typoResponse.value =  gson.fromJson(gson.toJson(res["typos"]), TypoResponse::class.java)
                                antoResponse.value = gson.fromJson(gson.toJson(res["anto"]), AntoResponse::class.java)
                                synoResponse.value = gson.fromJson(gson.toJson(res["syno"]), SynoResponse::class.java)
                                thesaurusResponse.value = gson.fromJson(gson.toJson(res["thesaurus"]), ThesaurusResponse::class.java)
                                bilingualSentencesResponse.value = gson.fromJson(gson.toJson(res["blng_sents_part"]), BilingualSentencesResponse::class.java)
                                examTypeResponse.value = gson.fromJson(gson.toJson(res["exam_type"]), ExamTypeResponse::class.java)
                                authSentenceResponse.value = gson.fromJson(gson.toJson(res["auth_sents_part"]), AuthSentencesResponse::class.java)
                                relWordResponse.value = gson.fromJson(gson.toJson(res["rel_word"]), RelWordResponse::class.java)
                                phrsResponse.value = gson.fromJson(gson.toJson(res["phrs"]), PhrsResponse::class.java)
                                picDictResponse.value = gson.fromJson(gson.toJson(res["pic_dict"]), PicDictResponse::class.java)
                                inflectionResponse.value = gson.fromJson(gson.toJson(res["inflection"]), InflectionResponse::class.java)
                                topicResponse.value = gson.fromJson(gson.toJson(res["topic"]), TopicResponse::class.java)
                                setDictDetailData()
                                setSearchHistoryItem(str)
                                result.complete(true)
                            }
                            override fun onError(e: ApiException?) {
                                result.complete(false)
                            }
                        })
                    result.await()
                } catch (e: Exception){
                    false
                }
            }
        }
        private suspend fun onlineSearchMore(): Boolean {
            return withContext(Dispatchers.IO){
                try {
                    val result = CompletableDeferred<Boolean>()
                    val onlineSearchMoreUrlBuilder : HttpUrl.Builder = NetConstant.dictMore.toHttpUrlOrNull()!!.newBuilder()
                    onlineSearchMoreUrlBuilder.addQueryParameter("q", str.trim())
                    onlineSearchMoreUrlBuilder.addQueryParameter("jsonversion", "2")
                    onlineSearchMoreUrlBuilder.addQueryParameter("client", "mobile")
                    val onlineSearchMoreJsonResponse = HttpHelper.requestResult(onlineSearchMoreUrlBuilder.build().toString())
                    val res = JSONObject(onlineSearchMoreJsonResponse)
                    val gson = Gson()
                    withContext(Dispatchers.Main){
                        dictsResponse.value = gson.fromJson(res.optString("meta"), DictsResponse::class.java)
                        webTransResponse.value = gson.fromJson(res.optString("web_trans"), WebTransResponse::class.java)
                        etymResponse.value = gson.fromJson(res.optString("etym"), EtymResponse::class.java)
                        specialResponse.value = gson.fromJson(res.optString("special"), SpecialResponse::class.java)
                        if (res.optString("wikipedia_digest").isNotBlank())
                            baikeDigest.value = gson.fromJson(res.optString("wikipedia_digest"), BaikeDigest::class.java)
                        if (res.optString("baike").isNotBlank())
                            baikeDigest.value = gson.fromJson(res.optString("baike"), BaikeDigest::class.java)
                    }
                    result.complete(true)
                    result.await()
                } catch (e: Exception){
                    false
                }
            }
        }
    }
}