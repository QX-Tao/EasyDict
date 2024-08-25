package com.qxtao.easydict.ui.activity.dict

/**
 * 单词基础信息
 */
data class EhResponse(
    val usphone: String?,
    val ukphone: String?,
    val ukspeech: String?,
    val trs: List<Trs>?,
    val `return-phrase`: String?,
    val `query-roman`: String?,
    val from: String?,
    val to: String?,
    val isTran: Boolean?,
    val usspeech: String?
)
data class Trs(
    val pos: String?,
    val tran: String?,
    val `tran-roman`: String?
)
data class HeResponse(
    val from: String?,
    val to: String?,
    val word: String?,
    val isTran: Boolean?,
    val trans: List<Trans>?,
    val `return-phrase`: ReturnPhrase?
)
data class Trans(
    val w: String?,
    val type: String?,
    val trans: String?,
    val `tran-roman`: String?
)
data class ReturnPhrase(
    val `query-roman`: String?,
    val word: String?
)


/**
 * 图片词典
 */
data class PicDictResponse(
    val pic: List<Pic>?
)
data class Pic(
    val image: String?,
    val host: String?,
    val url: String?
)



/**
 * 反义词、同义词、同反义词
 */
data class Word(
    val w: String?
)
data class AntoResponse(
    val antos: List<Antonym>?,
    val word: String?
)
data class Antonym(
    val pos: String?,
    val ws: List<String>?,
    val tran: String?
)
data class SynoResponse(
    val synos: List<Synonym>?,
    val word: String?
)
data class Synonym(
    val pos: String?,
    val ws: List<String>?,
    val tran: String?
)
data class ThesaurusResponse(
    val word: String?,
    val thesauruses: List<Thesaurus>?
)
data class Thesaurus(
    val pos: String?,
    val thesaurus: List<ThesaurusEntry>?
)
data class ThesaurusEntry(
    val syno: List<Word>?,
    val anto: List<Word>?,
    val tran: String?
)



/**
 * 双语例句
 */
data class SentencePair(
    val sentence: String?,
    val `taged-sentence`: String?,
    val `taged-translation`: String?,
    val speech: String?,
    val translation: String?,
    val `sentence-score`: Double?,
    val `speech-size`: String?,
    val id: String?,
    val source: String?,
    val `speech-text`: String?,
    val url: String?
)
data class SentenceMulti(
    val `sentence-count`: Double?,
    val `sentence-pair`: List<SentencePair>?,
    val tran: String?
)
data class BilingualSentencesResponse(
    val `sentence-count`: Double?,
    val `sentence-pair`: List<SentencePair>?,
    val `sentence-multi`: List<SentenceMulti>?
)



/**
 * 考试分类
 */
data class ExamTypeResponse(
    val types: List<String>?
)



/**
 * 英英词典
 */
data class EEDictionaryResponse(
    val word: EEWord?,
    val source: Source?
)
data class EEWord(
    val trs: List<EETranslation>?,
    val phone: String?,
    val speech: String?,
    val `return-phrase`: String?
)
data class EETranslation(
    val pos: String?,
    val tr: List<EETranslationItem>?
)
data class EETranslationItem(
    val examples: List<String>?,
    val tran: String?,
    val `similar-words`: List<String>?
)



/**
 * 权威例句
 */
data class AuthSentencesResponse(
    val `sentence-count`: Double?,
    val more: String?,
    val sent: List<AuthSentence>?
)
data class AuthSentence(
    val score: Double?,
    val speech: String?,
    val id: String?,
    val source: String?,
    val url: String?,
    val foreign: String?
)




/**
 * 同根词
 */
data class RelWordResponse(
    val word: String?,
    val rels: List<Rel>?,
    val stem: String?
)
data class Rel(
    val rel: RelInfo?
)
data class RelInfo(
    val pos: String?,
    val words: List<RelWord>?
)
data class RelWord(
    val word: String?
)



/**
 * 短语
 */
data class PhrsResponse(
    val phrs: List<Phr>?,
    val word: String?
)
data class Phr(
    val headword: String?,
    val source: String?
)



/**
 * 科林斯
 */
data class CollinsResponse(
    val collins_entries: List<CollinsEntry>?,
    val super_headwords: SuperHeadWords
)
data class SuperHeadWords(
    val super_headword: List<String>?
)
data class CollinsEntry(
    val hwas: String?,
    val entries: Entries?,
    val basic_entries: BasicEntries?,
    val phonetic: String?,
    val headword: String?,
    val super_headword: String?,
    val star: String?
)
data class Entries(
    val entry: List<Entry>?
)
data class Entry(
    val tran_entry: List<TranEntry>?
)
data class TranEntry(
    val headword: String?,
    val sees: Sees?,
    val pos_entry: PosEntry?,
    val exam_sents: ExamSents?,
    val seeAlsos: SeeAlsos?,
    val tran: String?
)
data class SeeAlsos(
    val seealso: String?,
    val seeAlso: List<See>?
)
data class See(
    val seeword: String?
)
data class Sees(
    val see: List<See>?
)
data class PosEntry(
    val pos: String?
)
data class ExamSents(
    val sent: List<Sentence>?
)
data class Sentence(
    val eng_sent: String?
)
data class BasicEntries(
    val basic_entry: List<BasicEntry>?
)
data class BasicEntry(
    val headword: String?,
    val sees: Sees?,
    val wordforms: WordForms?,
    val pos_entry: PosEntry?,
    val exam_sents: ExamSents?,
    val seeAlsos: SeeAlsos?,
    val tran: String?
)
data class WordForms(
    val wordform: List<WordForm>?
)
data class WordForm(
    val word: String?
)



/**
 * 百科
 */
data class BaikeDigest(
    val summarys: List<Summary>?,
    val source: Source?
)
data class Summary(
    val summary: String?,
    val key: String?
)
data class Source(
    val site: String?,
    val name: String?,
    val url: String?
)



/**
 * 词性变化
 */
data class InflectionResponse(
    val inflections: List<Inflection>?
)
data class Inflection(
    val name: String?,
    val value: String?
)


/**
 * 拼写错误
 */
data class TypoResponse(
    val typo: List<Typo>?
)
data class Typo(
    val word: String?,
    val lang: String?
)


/**
 * 话题
 */
data class TopicResponse(
    val topics: List<Topic>?
)
data class Topic(
    val topic: String?,
    val index: Double?
)

/**
 * 网络释义
 */
data class WebTransResponse(
    val `web-translation`: List<WebTranslation>?
)
data class WebTranslation(
    val key : String?,
    val `key-speech`: String?,
    val trans: List<WebTrans>?
)
data class WebTrans(
    val value: String?,
    val url: String?,
    val summary: Line?
)
data class Line(
    val line: List<String>?
)
/**
 * 专业释义
 */
data class SpecialResponse(
    val `co-add`: String?,
    val total: String?,
    val summary: SpecialSummary?,
    val entries: List<SpecialEntries>?
)
data class SpecialSummary(
    val text: String?,
    val sources: Sources?,
)
data class Sources(
    val source: Source?
)
data class SpecialEntries(
    val entry: SpecialEntry?
)
data class SpecialEntry(
    val major: String?,
    val num: Int?,
    val trs: List<SpecialTrs>?
)
data class SpecialTrs(
    val tr: SpecialTr?
)
data class SpecialTr(
    val nat: String?,
    val chnSent: String?,
    val engSent: String?,
    val cite: String?,
    val docTitle: String?,
    val url: String?
)

/**
 * 词源
 */
data class EtymResponse(
    val etyms: Etyms?,
    val word: String?
)
data class Etyms(
    val zh: List<Etym>?,
    val en: List<Etym>?
)
data class Etym(
    val source: String?,
    val word: String?,
    val value: String?,
    val url: String?,
    val desc: String?
)

/**
 * Lan match
 */
data class LanMatchResponse(
    val match: Boolean?
)


/**
 * 搜索信息
 */
data class SearchInfoResponse(
    val external_data: Boolean?,
    val truncated: Boolean?,
    val from: String?,
    val to: String?,
    val lang_detected: String?,
)
data class DictsResponse(
    val dicts: List<String>?
)

/**
 * 文本编辑
 */
data class SearchText(
    val searchText: String?, // 确定搜索的文本
    val editSearchText: String, // 编辑搜索的文本
    val editSelectionStart: Int, // 光标位置
    val editSelectionEnd: Int // 光标位置
)

