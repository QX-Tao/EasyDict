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
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dict.DictBlngClassificationAdapter
import com.qxtao.easydict.adapter.dict.DictExternalDictTransAdapter
import com.qxtao.easydict.adapter.dict.TYPE_BLNG_CLASSIFICATION_NORMAL
import com.qxtao.easydict.adapter.dict.TYPE_BLNG_CLASSIFICATION_SELECT
import com.qxtao.easydict.database.DailySentenceData
import com.qxtao.easydict.database.SearchHistoryData
import com.qxtao.easydict.database.SimpleDictData
import com.qxtao.easydict.database.WordBookData
import com.qxtao.easydict.database.WordListData
import com.qxtao.easydict.utils.common.HttpUtils
import com.qxtao.easydict.utils.common.TimeUtils
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
import java.util.Stack


class DictViewModel(
    private val simpleDictData: SimpleDictData,
    private val searchHistoryData: SearchHistoryData,
    private val wordBookData: WordBookData,
    private val dailySentenceData: DailySentenceData?,
    private val wordListData: WordListData?
) : ViewModel() {
    val wordNameMap = mapOf("cet4" to "四级大纲", "cet6" to "六级大纲", "kaoyan" to "考研词汇", "ielts" to "雅思词汇",
        "toefl" to "托福词汇", "xiaoxue" to "小学词汇", "chuzhong" to "初中大纲", "gaokao" to "高考大纲", "tem4" to "专四大纲", "tem8" to "专八大纲")
    private val voiceSoundMap = mapOf(-1 to NetConstant.dictVoice, 0 to NetConstant.voiceUs, 1 to NetConstant.voiceUk)
    var currentFragmentTag: String? = null
    var addedFragment = mutableSetOf<String>()
    var addedFragmentStack = Stack<String>()
    var extraFragmentStyle = MutableLiveData<String>()

    private val _dictSearchHistory = MutableLiveData<List<SearchHistoryData.SearchHistory>?>()
    val dictSearchHistory: LiveData<List<SearchHistoryData.SearchHistory>?> = _dictSearchHistory
    private val _dictSearchSuggestion = MutableLiveData<List<SimpleDictData.SimpleDict>?>()
    val dictSearchSuggestion: LiveData<List<SimpleDictData.SimpleDict>?> = _dictSearchSuggestion
    private val _dictExternalDict = MutableLiveData<List<DictExternalDictTransAdapter.ExternalDictTransItem>?>()
    val dictExternalDict: LiveData<List<DictExternalDictTransAdapter.ExternalDictTransItem>?> = _dictExternalDict
    private val _dictExternalTrans = MutableLiveData<List<DictExternalDictTransAdapter.ExternalDictTransItem>?>()
    val dictExternalTrans: LiveData<List<DictExternalDictTransAdapter.ExternalDictTransItem>?> = _dictExternalTrans
    private val _dictSearchSugWord = MutableLiveData<List<SimpleDictData.SimpleDict>?>()
    val dictSearchSugWord: LiveData<List<SimpleDictData.SimpleDict>?> = _dictSearchSugWord
    private val deleteQueue: Queue<Pair<SearchHistoryData.SearchHistory, Int>> = LinkedList() // 预删除的搜索记录
    var dataLoadInfo = MutableLiveData<Int>() // 初始化-1  加载中0 已加载1 加载失败2
    var dataMoreBlngLoadInfo = MutableLiveData<Int>() // 初始化-1  加载中0 已加载1 加载失败2
    var dataMoreAuthLoadInfo = MutableLiveData<Int>() // 初始化-1  加载中0 已加载1 加载失败2
    private var dataSuggestionLoadInfo = MutableLiveData<Int>() // 初始化-1  加载中0 已加载1
    var hasShowRvInfo = MutableLiveData<Int>() //history or suggesting
    var playSound = 0
    var hasShowDetailFragment = Triple(true, false, false) //JM-first  CO-second  EE-third
    var dailySentenceItem = MutableLiveData<Pair<Boolean, DailySentenceData.DailySentence?>?>()
    var wordListInfo = MutableLiveData<Pair<Boolean, Triple<String, Int, Int>?>?>()
    var searchText = MutableLiveData<SearchText>()
    val playPosition = MutableLiveData<Map<Int, Int>>() // -1不播放
    private var _mediaPlayer: MediaPlayer?= null
    var hasSearchResult = MutableLiveData<Boolean>() // 初始化-1 无结果0 有结果1
    var isSearchTextFavorite = MutableLiveData<Boolean>() // 搜索文本是否已收藏
    var dictDetailMode = DICT_DETAIL_MODE_NORMAL

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
        fun Factory(simpleDictData: SimpleDictData, searchHistoryData: SearchHistoryData, wordBookData: WordBookData,
                    dailySentenceData: DailySentenceData, wordListData: WordListData
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { DictViewModel(simpleDictData, searchHistoryData, wordBookData, dailySentenceData, wordListData) }
        }
        fun Factory(simpleDictData: SimpleDictData, searchHistoryData: SearchHistoryData, wordBookData: WordBookData
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { DictViewModel(simpleDictData, searchHistoryData, wordBookData, null, null) }
        }
    }

    init {
        playPosition.value = mapOf(VOICE_AUTH_PART to -1, VOICE_BLNG_PART to -1, VOICE_EH_PART to -1, VOICE_NORMAL to -1)
        _dictSearchHistory.value = mutableListOf()
        _dictSearchSuggestion.value = mutableListOf()
        _dictExternalDict.value = mutableListOf()
        _dictExternalTrans.value = mutableListOf()
        dataLoadInfo.value = -1
        dataMoreBlngLoadInfo.value = -1
        dataMoreAuthLoadInfo.value = -1
        dataSuggestionLoadInfo.value = -1
        isSearchTextFavorite.value = false
        hasShowRvInfo.value = DICT_RV_HISTORY
        searchText.value = SearchText(null, "",  0)
        dailySentenceItem.value = null
        wordListInfo.value = null
    }

    // 设置搜索文本
    fun setSearchText(editSearchText: String, editCursor: Int){
        this.searchText.value = SearchText(this.searchText.value?.searchText, editSearchText, editCursor)
    }
    private fun setSearchText(searchText: String?, editSearchText: String = searchText ?: ""){
        this.searchText.value = SearchText(searchText, editSearchText, searchText?.length ?: editSearchText.length)
    }

    // 获取搜索记录
    fun getDictSearchHistory(){
        viewModelScope.launch(Dispatchers.IO) {
            val list = searchHistoryData.getSearchHistory()
            _dictSearchHistory.postValue(list)
        }
    }

    // 搜索文本是否已收藏
    private fun isSearchTextFavorite(searchText: String): Boolean
        = wordBookData.isWordInWordBooks(searchText)
    // 收藏搜索文本
    fun addSearchTextToWordBook(sIndex: Int? = null): Triple<Boolean, List<String>, Int?>{
        val wordBookList = wordBookData.getWordBookList()
        return if (wordBookList.size == 1) {
            addSearchTextToWordBook(wordBookList[0])
            Triple(true, wordBookList, 0)
        } else {
            if (sIndex == null){
                Triple(false, wordBookList, null)
            } else {
                addSearchTextToWordBook(wordBookList[sIndex])
                Triple(true, wordBookList, sIndex)
            }
        }
    }
    fun addSearchTextToWordBook(bookName: String){
        val word = searchText.value?.searchText!!
        val translation = getSearchTranslation(word)
        wordBookData.addWordToBook(word, translation, bookName)
        isSearchTextFavorite.value = isSearchTextFavorite(word)
    }
    // 取消收藏搜索文本
    fun removeSearchTextFromWordBook(){
        val word = searchText.value?.searchText!!
        wordBookData.deleteWordFromBook(word)
        isSearchTextFavorite.value = isSearchTextFavorite(word)
    }

    // 获取推荐单词
    fun getDictSearchSugWord(){
        viewModelScope.launch(Dispatchers.IO) {
            val list = simpleDictData.getRandomSimpleDictList()
            withContext(Dispatchers.Main) {
                _dictSearchSugWord.value = list
            }
        }
    }

    // 获取单词列表
    fun getWordListInfo(){
        wordListInfo.value = null
        viewModelScope.launch(Dispatchers.IO){
            wordListInfo.postValue(wordListData?.getSelectBookInfo())
        }
    }

    // 获取每日一句
    fun getDailySentence() {
        dailySentenceItem.value = null
        viewModelScope.launch(Dispatchers.IO){
            dailySentenceItem.postValue(getDailySentenceItem(TimeUtils.getCurrentDateByPattern("yyyy-MM-dd")))
        }
    }
    private suspend fun getDailySentenceItem(date: String): Pair<Boolean, DailySentenceData.DailySentence?> {
        return withContext(Dispatchers.IO){
            val tmp = dailySentenceData?.getDataByDate(date)
            if (tmp != null){
                Pair(true, DailySentenceData.DailySentence(tmp.date, tmp.enSentence, tmp.cnSentence, tmp.imageUrl, tmp.ttsUrl))
            } else {
                try {
                    val imgResponse = HttpUtils.requestDisableCertificateValidationResponse(
                        NetConstant.imgApi + TimeUtils.getFormatDateByPattern(date, "yyyy-MM-dd", "yyyyMMdd"))
                    val imageUrl = imgResponse.request.url.toString()

                    val dailySentenceResponseJson = HttpUtils.requestResult(NetConstant.dailySentenceApi + date)
                    val dailySentenceJsonObject = JSONObject(dailySentenceResponseJson)
                    val enSentence: String = dailySentenceJsonObject.getString("content").trim()
                    val chSentence: String = dailySentenceJsonObject.getString("note").trim()
                    val ttsUrl: String = dailySentenceJsonObject.getString("tts")

                    if (imageUrl.isNotEmpty() && enSentence.isNotEmpty() && chSentence.isNotEmpty() && ttsUrl.isNotEmpty()) {
                        dailySentenceData?.insertData(date, enSentence, chSentence, imageUrl, ttsUrl)
                        Pair(true, DailySentenceData.DailySentence(date, enSentence, chSentence, imageUrl, ttsUrl))
                    } else {
                        Pair(false, null)
                    }

                }catch (e: Exception) {
                    Pair(false, null)
                }
            }
        }
    }

    // 搜索提示
    fun searchInWordData(str: String){
        viewModelScope.launch(Dispatchers.IO) {
            dataSuggestionLoadInfo.postValue(0)
            val list = simpleDictData.searchDicts(str)
            withContext(Dispatchers.Main){
                _dictSearchSuggestion.value = list
                dataSuggestionLoadInfo.value = 1
                hasShowRvInfo.value = if (list?.isNotEmpty() == true) DICT_RV_SUGGESTION else DICT_RV_SUGGESTION_LM
            }
        }
    }

    // hasFragment的列表显示控制
    fun setHasShowRvInfo(mode : Int = DICT_RV_HISTORY){
        hasShowRvInfo.value = mode
    }

    // 添加记录到表
    private fun setSearchHistoryItem(origin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val translation = getSearchTranslation(origin)
            searchHistoryData.setSearchRecord(origin, translation)
            getDictSearchHistory()
        }
    }

    private fun getSearchTranslation(origin: String): String{
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
            translation = simpleDictData.getTranslationByOrigin(origin) ?: searchHistoryData.getTranslationByOrigin(origin) ?: ""
        }
        return translation
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

    // 清除旧数据
    private fun clearResponseData(){
        playPosition.value = mapOf(VOICE_AUTH_PART to -1, VOICE_BLNG_PART to -1, VOICE_EH_PART to -1, VOICE_NORMAL to -1)
        _dictExternalDict.value = mutableListOf()
        _dictExternalTrans.value = mutableListOf()
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
        isSearchTextFavorite.value = false
    }

    // 离线查询
    fun offlineSearch(str: String) :String? {
        // 将确定进行搜索的文本保存一下
        setSearchText(searchText = null, editSearchText = str)
        // 每次搜索前，将之前的信息删除
        clearResponseData()
        setSearchHistoryItem(str)
        return simpleDictData.getTranslationByOrigin(str) ?: searchHistoryData.getTranslationByOrigin(str)
    }

    // 设置词典搜索详情数据
    private fun setDictDetailData() {
        viewModelScope.launch(Dispatchers.IO) {
            // 词典页面
            hasShowDetailFragment = Triple(true, collinsResponse.value != null && collinsResponse.value?.collins_entries?.isNotEmpty() == true,
                eeDictionaryResponse.value != null && eeDictionaryResponse.value?.word != null && eeDictionaryResponse.value?.source != null)

            // 外部词典/翻译
            if (ehResponse.value != null && heResponse.value == null){
                if (ehResponse.value?.isTran == true){
                    val externalDictTransItems = mutableListOf<DictExternalDictTransAdapter.ExternalDictTransItem>()
                    externalDictTransItems.addAll(
                        listOf(
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_biying, R.string.bing,
                                "https://cn.bing.com/translator/?h_text=msn_ctxt&setlang=zh-cn"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_jinshan, R.string.jinshan,
                                "https://m.iciba.com/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_youdao, R.string.youdao,
                                "https://www.youdao.com/result?word=${searchText.value?.searchText}&lang=en"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_deepl, R.string.deepl,
                                "https://www.deepl.com/zh/translator#en/zh/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_google, R.string.google,
                                "https://translate.google.com/?hl=zh-CN&sl=auto&tl=zh-CN&text=${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_baidu, R.string.baidu,
                                "https://fanyi.baidu.com/#en/zh/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_tencent, R.string.tencent,
                                "https://fanyi.qq.com/"
                            )
                        )
                    )
                    _dictExternalTrans.postValue(externalDictTransItems)
                } else {
                    val externalDictTransItems = mutableListOf<DictExternalDictTransAdapter.ExternalDictTransItem>()
                    externalDictTransItems.addAll(
                        listOf(
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_momo, R.string.momo,
                                "https://lookup.maimemo.com/?limit=10&offset=0&keyword=${searchText.value?.searchText}&paper_type=ALL"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_biying, R.string.bing,
                                "https://cn.bing.com/dict/search?q=${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_kelinsi, R.string.collins,
                                "https://www.collinsdictionary.com/dictionary/english/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_niujin, R.string.oxford,
                                "https://www.oxfordlearnersdictionaries.com/definition/english/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_weishi, R.string.webster,
                                "https://www.merriam-webster.com/dictionary/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_jianqiao_he, R.string.cambridge_he,
                                "https://dictionary.cambridge.org/dictionary/english-chinese-simplified/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_jianqiao_ee, R.string.cambridge_ee,
                                "https://dictionary.cambridge.org/dictionary/english/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_jinshan, R.string.jinshan,
                                "https://m.iciba.com/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_youdao, R.string.youdao,
                                "https://www.youdao.com/result?word=${searchText.value?.searchText}&lang=en"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_haici, R.string.haici,
                                "https://m.dict.cn/msearch.php?q=${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_langwen, R.string.langwen,
                                "https://www.ldoceonline.com/dictionary/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_ciyuan, R.string.ciyuan,
                                "https://www.etymonline.com/search?q=${searchText.value?.searchText}"
                            )
                        )
                    )
                    _dictExternalDict.postValue(externalDictTransItems)
                }
            } else if (heResponse.value != null && ehResponse.value == null){
                if (heResponse.value?.isTran == true){
                    val externalDictTransItems = mutableListOf<DictExternalDictTransAdapter.ExternalDictTransItem>()
                    externalDictTransItems.addAll(
                        listOf(
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_biying, R.string.bing,
                                "https://cn.bing.com/translator/?h_text=msn_ctxt&setlang=zh-cn"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_jinshan, R.string.jinshan,
                                "https://m.iciba.com/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_youdao, R.string.youdao,
                                "https://www.youdao.com/result?word=${searchText.value?.searchText}&lang=en"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_deepl, R.string.deepl,
                                "https://www.deepl.com/zh/translator#zh/en/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_google, R.string.google,
                                "https://translate.google.com/?hl=zh-CN&sl=auto&tl=en&text=${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_baidu, R.string.baidu,
                                "https://fanyi.baidu.com/#zh/en/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_tencent, R.string.tencent,
                                "https://fanyi.qq.com/"
                            )
                        )
                    )
                    _dictExternalTrans.postValue(externalDictTransItems)
                } else {
                    val externalDictTransItems = mutableListOf<DictExternalDictTransAdapter.ExternalDictTransItem>()
                    externalDictTransItems.addAll(
                        listOf(
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_biying, R.string.bing,
                                "https://cn.bing.com/dict/search?q=${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_jinshan, R.string.jinshan,
                                "https://m.iciba.com/${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_youdao, R.string.youdao,
                                "https://www.youdao.com/result?word=${searchText.value?.searchText}&lang=en"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_haici, R.string.haici,
                                "https://m.dict.cn/msearch.php?q=${searchText.value?.searchText}"
                            ),
                            DictExternalDictTransAdapter.ExternalDictTransItem(
                                R.drawable.ic_dict_handian, R.string.handian,
                                "https://www.zdic.net/hans/${searchText.value?.searchText}"
                            )
                        )
                    )
                    _dictExternalDict.postValue(externalDictTransItems)
                }
            }

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

    fun startPlaySound(type: Int, position: Int, audio: String, le: String, playSound: Int = this.playSound): Boolean {
        try {
            if (playPosition.value?.get(type) == position) {
                stopPlaySound()
                return false
            } else {
                stopPlaySound()
                val upd = playPosition.value?.toMutableMap() ?: mutableMapOf(VOICE_AUTH_PART to -1, VOICE_BLNG_PART to -1, VOICE_NORMAL to -1)
                upd[type] = position
                playPosition.value = upd
                val data = if (playSound == -1) "${NetConstant.dictVoice}?audio=${audio}&le=${le}" else voiceSoundMap[playSound] + "${audio}&le=${le}"
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

    fun addWordBook(bookName: String) = wordBookData.addWordBook(bookName)
    fun getWordBookList() = wordBookData.getWordBookList()

    private inner class OnlineSearch(s: String){
        private val str = s.trim()
        fun search(){
            if (dataLoadInfo.value == 1 && str == searchText.value?.searchText) {
                setSearchText(searchText = str)
                isSearchTextFavorite.value = isSearchTextFavorite(str)
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
                isSearchTextFavorite.postValue(isSearchTextFavorite(str))
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
                    val onlineSearchMoreJsonResponse = HttpUtils.requestResult(onlineSearchMoreUrlBuilder.build().toString())
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