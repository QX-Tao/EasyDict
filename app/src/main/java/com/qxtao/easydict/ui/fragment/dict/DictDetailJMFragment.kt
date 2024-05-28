package com.qxtao.easydict.ui.fragment.dict

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dict.DictAuthSentsAdapter
import com.qxtao.easydict.adapter.dict.DictBlngClassificationAdapter
import com.qxtao.easydict.adapter.dict.DictBlngSentsAdapter
import com.qxtao.easydict.adapter.dict.DictEhHeaderExplainAdapter
import com.qxtao.easydict.adapter.dict.DictEhHeaderInflectionAdapter
import com.qxtao.easydict.adapter.dict.DictEtymAdapter
import com.qxtao.easydict.adapter.dict.DictExternalDictTransAdapter
import com.qxtao.easydict.adapter.dict.DictHeHeaderExplainAdapter
import com.qxtao.easydict.adapter.dict.DictPhraseAdapter
import com.qxtao.easydict.adapter.dict.DictRelWordAdapter
import com.qxtao.easydict.adapter.dict.DictSpecialOuterAdapter
import com.qxtao.easydict.adapter.dict.DictSynoAntoAdapter
import com.qxtao.easydict.adapter.dict.DictThesurusOuterAdapter
import com.qxtao.easydict.adapter.dict.DictTypoAdapter
import com.qxtao.easydict.adapter.dict.DictWebTransOuterAdapter
import com.qxtao.easydict.databinding.FragmentDictDetailJmBinding
import com.qxtao.easydict.ui.activity.dict.ANTONYM_PART_MORE
import com.qxtao.easydict.ui.activity.dict.AUTH_SENTS_PART_MORE
import com.qxtao.easydict.ui.activity.dict.Antonym
import com.qxtao.easydict.ui.activity.dict.BLNG_SENTS_PART_MORE
import com.qxtao.easydict.ui.activity.dict.DICT_DETAIL_MODE_NORMAL
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.activity.dict.ETYM_PART_MORE
import com.qxtao.easydict.ui.activity.dict.EXTERNAL_DICT_PART_MORE
import com.qxtao.easydict.ui.activity.dict.EXTERNAL_TRANS_PART_MORE
import com.qxtao.easydict.ui.activity.dict.Etym
import com.qxtao.easydict.ui.activity.dict.JM_FRAGMENT
import com.qxtao.easydict.ui.activity.dict.PHRASE_PART_MORE
import com.qxtao.easydict.ui.activity.dict.REL_WORD_PART_MORE
import com.qxtao.easydict.ui.activity.dict.RelInfo
import com.qxtao.easydict.ui.activity.dict.SEARCH_LE_EN
import com.qxtao.easydict.ui.activity.dict.SPECIAL_PART_MORE
import com.qxtao.easydict.ui.activity.dict.SYNONYM_PART_MORE
import com.qxtao.easydict.ui.activity.dict.Synonym
import com.qxtao.easydict.ui.activity.dict.THESAURUS_PART_MORE
import com.qxtao.easydict.ui.activity.dict.VOICE_AUTH_PART
import com.qxtao.easydict.ui.activity.dict.VOICE_BLNG_PART
import com.qxtao.easydict.ui.activity.dict.VOICE_EH_PART
import com.qxtao.easydict.ui.activity.dict.WEB_TRANS_PART_MORE
import com.qxtao.easydict.ui.activity.web.WebActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.ExpandableTextView
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.factory.appWidth


class DictDetailJMFragment : BaseFragment<FragmentDictDetailJmBinding>(FragmentDictDetailJmBinding::inflate) {
    // define variable
    private var isInitView = false
    private lateinit var dictViewModel: DictViewModel
    private lateinit var rvEhHeaderExplainAdapter: DictEhHeaderExplainAdapter
    private lateinit var rvEhHeaderInflectionAdapter: DictEhHeaderInflectionAdapter
    private lateinit var rvHeHeaderExplainAdapter: DictHeHeaderExplainAdapter
    private lateinit var rvBlngSentsPartClassificationAdapter: DictBlngClassificationAdapter
    private lateinit var rvDictBlngSentsAdapter: DictBlngSentsAdapter
    private lateinit var rvDictAuthSentsAdapter: DictAuthSentsAdapter
    private lateinit var rvDictSpecialOuterAdapter: DictSpecialOuterAdapter
    private lateinit var rvDictWebTransOuterAdapter: DictWebTransOuterAdapter
    private lateinit var rvDictThesurusOuterAdapter: DictThesurusOuterAdapter
    private lateinit var rvDictAntoAdapter: DictSynoAntoAdapter<Antonym>
    private lateinit var rvDictSynoAdapter: DictSynoAntoAdapter<Synonym>
    private lateinit var rvPhraseAdapter: DictPhraseAdapter
    private lateinit var rvRelWordAdapter: DictRelWordAdapter
    private lateinit var rvTypoAdapter: DictTypoAdapter
    private lateinit var rvEtymAdapter: DictEtymAdapter
    private lateinit var rvExternalDictAdapter: DictExternalDictTransAdapter
    private lateinit var rvExternalTransAdapter: DictExternalDictTransAdapter

    // define widget
    private lateinit var nsvContent: NestedScrollView
    // 第一部分：英汉基础信息
    private lateinit var llEhHeaderPart: LinearLayout
    private lateinit var rvEhHeaderExplain: RecyclerView
    private lateinit var rvEhHeaderInflection: RecyclerView
    private lateinit var tvEhHeaderExamType: TextView
    // 第二部分：汉英基本信息
    private lateinit var llHeHeaderPart: LinearLayout
    private lateinit var rvHeHeaderExplain: RecyclerView
    // 第三部分：双语例句
    private lateinit var clBlngSentsPart: ConstraintLayout
    private lateinit var rvBlngSentsPartClassification: RecyclerView
    private lateinit var rvBlngSentsPartSents: RecyclerView
    private lateinit var tvBlngSentsPartMore: TextView
    // 第四部分：权威例句
    private lateinit var clAuthSentsPart: ConstraintLayout
    private lateinit var rvAuthSentsPart: RecyclerView
    private lateinit var tvAuthSentsPartMore: TextView
    // 第五部分：网络释义
    private lateinit var clWebTransPart: ConstraintLayout
    private lateinit var rvWebTransPartMean: RecyclerView
    private lateinit var tvWebTransPartMore: TextView
    // 第六部分：专业释义
    private lateinit var clSpecialPart: ConstraintLayout
    private lateinit var rvSpecialPartMean: RecyclerView
    private lateinit var tvSpecialPartMore: TextView
    // 第七部分：同反义词
    private lateinit var clThesaurusPart: ConstraintLayout
    private lateinit var rvThesaurusPartMean: RecyclerView
    private lateinit var tvThesaurusPartMore: TextView
    // 第八部分：同义词
    private lateinit var clSynonymPart: ConstraintLayout
    private lateinit var rvSynonymPartMean: RecyclerView
    private lateinit var tvSynonymPartMore: TextView
    // 第九部分：反义词
    private lateinit var clAntonymPart: ConstraintLayout
    private lateinit var rvAntonymPartMean: RecyclerView
    private lateinit var tvAntonymPartMore: TextView
    // 第十部分：短语词组
    private lateinit var clPhrasePart: ConstraintLayout
    private lateinit var rvPhrasePartMean: RecyclerView
    private lateinit var tvPhrasePartMore: TextView
    // 第十一部分：同根词
    private lateinit var clRelWordPart: ConstraintLayout
    private lateinit var tvRelWordPartInf: TextView
    private lateinit var rvRelWordPartMean: RecyclerView
    private lateinit var tvRelWordPartMore: TextView
    // 第十二部分：词源
    private lateinit var clEtymPart: ConstraintLayout
    private lateinit var rvEtymPartMean: RecyclerView
    private lateinit var tvEtymPartMore: TextView
    // 第十三部分：百科
    private lateinit var clBaikeDigestPart: ConstraintLayout
    private lateinit var tvBaikeDigestPartMore: TextView
    private lateinit var tvBaikeDigestPartWord: TextView
    private lateinit var tvBaikeDigestPartDesc: ExpandableTextView
    private lateinit var tvSourceBaike: TextView
    // 第十四部分：拼写错误
    private lateinit var llTyposPart: LinearLayout
    private lateinit var rvTyposExplain: RecyclerView
    // 第十五部分：重新输入
    private lateinit var clSearchNotResult: ConstraintLayout
    private lateinit var tvRetype: TextView
    private lateinit var cvRetype: CardView
    // 第十六部分：外部词典
    private lateinit var clExternalDictPart: ConstraintLayout
    private lateinit var rvExternalDictPartMean: RecyclerView
    private lateinit var tvExternalDictPartMore: TextView
    // 第十七部分：外部翻译
    private lateinit var clExternalTransPart: ConstraintLayout
    private lateinit var rvExternalTransPartMean: RecyclerView
    private lateinit var tvExternalTransPartMore: TextView

    override fun bindViews() {
        nsvContent = binding.nsvContent
        // 第一部分：英汉基础信息
        llEhHeaderPart = binding.llEhHeaderPart
        rvEhHeaderExplain = binding.rvEhHeaderExplain
        rvEhHeaderInflection = binding.rvEhHeaderInflection
        tvEhHeaderExamType = binding.tvEhHeaderExamType
        // 第二部分：汉英基础信息
        llHeHeaderPart = binding.llHeHeaderPart
        rvHeHeaderExplain = binding.rvHeHeaderExplain
        // 第三部分：双语例句
        rvBlngSentsPartClassification = binding.rvBlngSentsPartClassification
        rvBlngSentsPartSents = binding.rvBlngSentsPartSents
        clBlngSentsPart = binding.clBlngSentsPart
        tvBlngSentsPartMore = binding.tvBlngSentsPartMore
        // 第四部分：权威例句
        rvAuthSentsPart = binding.rvAuthSentsPart
        clAuthSentsPart = binding.clAuthSentsPart
        tvAuthSentsPartMore = binding.tvAuthSentsPartMore
        // 第五部分：网络释义
        clWebTransPart = binding.clWebTransPart
        rvWebTransPartMean = binding.rvWebTransPartMean
        tvWebTransPartMore = binding.tvWebTransPartMore
        // 第六部分：专业释义
        clSpecialPart = binding.clSpecialPart
        rvSpecialPartMean = binding.rvSpecialPartMean
        tvSpecialPartMore = binding.tvSpecialPartMore
        // 第七部分：同反义词
        clThesaurusPart = binding.clThesaurusPart
        rvThesaurusPartMean = binding.rvThesaurusPartMean
        tvThesaurusPartMore = binding.tvThesaurusPartMore
        // 第八部分：同义词
        clSynonymPart = binding.clSynonymPart
        rvSynonymPartMean = binding.rvSynonymPartMean
        tvSynonymPartMore = binding.tvSynonymPartMore
        // 第九部分：反义词
        clAntonymPart = binding.clAntonymPart
        rvAntonymPartMean = binding.rvAntonymPartMean
        tvAntonymPartMore = binding.tvAntonymPartMore
        // 第十部分：短语词组
        clPhrasePart = binding.clPhrasePart
        rvPhrasePartMean = binding.rvPhrasePartMean
        tvPhrasePartMore = binding.tvPhrasePartMore
        // 第十一部分：同根词
        clRelWordPart = binding.clRelWordPart
        tvRelWordPartInf = binding.tvRelWordPartInf
        rvRelWordPartMean = binding.rvRelWordPartMean
        tvRelWordPartMore = binding.tvRelWordPartMore
        // 第十二部分：词源
        clEtymPart = binding.clEtymPart
        rvEtymPartMean = binding.rvEtymPartMean
        tvEtymPartMore = binding.tvEtymPartMore
        // 第十三部分：百科
        tvBaikeDigestPartWord = binding.tvBaikeDigestPartWord
        tvBaikeDigestPartDesc = binding.tvBaikeDigestPartDesc
        tvSourceBaike = binding.tvSourceBaike
        clBaikeDigestPart = binding.clBaikeDigestPart
        tvBaikeDigestPartMore = binding.tvBaikeDigestPartMore
        // 第十四部分：拼写错误
        llTyposPart = binding.llTyposPart
        rvTyposExplain = binding.rvTyposExplain
        // 第十五部分：重新输入
        clSearchNotResult = binding.clRetypePart
        tvRetype = binding.tvRetype
        cvRetype = binding.cvRetype
        // 第十六部分：外部词典
        clExternalDictPart = binding.clExternalDictPart
        tvExternalDictPartMore = binding.tvExternalDictPartMore
        rvExternalDictPartMean = binding.rvExternalDictPartMean
        // 第十七部分：外部翻译
        clExternalTransPart = binding.clExternalTransPart
        tvExternalTransPartMore = binding.tvExternalTransPartMore
        rvExternalTransPartMean = binding.rvExternalTransPartMean
    }

    override fun initViews() {
        isInitView = true
        dictViewModel = (activity as DictActivity).getDictViewModel()

        // 第一部分：英汉基础信息
        rvEhHeaderExplainAdapter = DictEhHeaderExplainAdapter(ArrayList())
        rvEhHeaderExplain.adapter = rvEhHeaderExplainAdapter
        rvEhHeaderExplain.layoutManager = LinearLayoutManager(requireActivity())
        rvEhHeaderInflectionAdapter = DictEhHeaderInflectionAdapter(ArrayList())
        rvEhHeaderInflection.adapter = rvEhHeaderInflectionAdapter
        rvEhHeaderInflection.layoutManager = FlexboxLayoutManager(requireActivity())
        dictViewModel.ehResponse.observe(this){
            llEhHeaderPart.visibility = if (it == null || it.isTran == true) View.GONE else {
                dictViewModel.hasSearchResult.value = false
                View.VISIBLE
            }
            it?.trs.let { it1 -> rvEhHeaderExplain.visibility = if (it1?.isNotEmpty() == true) {
                rvEhHeaderExplainAdapter.setData(it1)
                View.VISIBLE } else View.GONE
            }
        }
        dictViewModel.inflectionResponse.observe(this){
            it?.inflections.let{ it1 -> rvEhHeaderInflection.visibility = if (it1?.isNotEmpty() == true){
                rvEhHeaderInflectionAdapter.setData(it1)
                View.VISIBLE } else View.GONE }
        }
        dictViewModel.examTypeResponse.observe(this){
            if ((it?.types?.size ?: 0) > 0){
                for (i in 0 until (it?.types?.size ?: 0)){
                    tvEhHeaderExamType.visibility = View.VISIBLE
                    tvEhHeaderExamType.text = it?.types?.joinToString(" / ")
                }
            } else tvEhHeaderExamType.visibility = View.GONE
        }

        // 第二部分：汉英基本信息
        rvHeHeaderExplainAdapter = DictHeHeaderExplainAdapter(ArrayList())
        rvHeHeaderExplain.adapter = rvHeHeaderExplainAdapter
        rvHeHeaderExplain.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.heResponse.observe(this){
            llHeHeaderPart.visibility = if (it == null || it.isTran == true) View.GONE else {
                dictViewModel.hasSearchResult.value = false
                View.VISIBLE
            }
            it?.trans.let { it1 -> rvHeHeaderExplain.visibility = if (it1?.isNotEmpty() == true) {
                rvHeHeaderExplainAdapter.setData(it1)
                View.VISIBLE } else View.GONE
            }
        }

        // 第三部分：双语例句
        rvBlngSentsPartClassificationAdapter = DictBlngClassificationAdapter(ArrayList())
        rvBlngSentsPartClassification.adapter = rvBlngSentsPartClassificationAdapter
        rvBlngSentsPartClassification.layoutManager =
            LinearLayoutManager(requireActivity()).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvDictBlngSentsAdapter = DictBlngSentsAdapter(ArrayList())
        rvBlngSentsPartSents.adapter = rvDictBlngSentsAdapter
        rvBlngSentsPartSents.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.blngClassification.observe(this) {
            if (it?.isNotEmpty() == true) {
                rvBlngSentsPartClassificationAdapter.setData(it)
                rvBlngSentsPartClassification.visibility = View.VISIBLE
            } else rvBlngSentsPartClassification.visibility = View.GONE
        }
        dictViewModel.blngSents.observe(this) {
            if (it?.isNotEmpty() == true) {
                rvDictBlngSentsAdapter.setData(it[rvBlngSentsPartClassificationAdapter.getSelectedItemText()]?.take(3))
                dictViewModel.hasSearchResult.value = false
                clBlngSentsPart.visibility = View.VISIBLE
            } else clBlngSentsPart.visibility = View.GONE
        }
        dictViewModel.blngSelectedItem.observe(this){
            rvDictBlngSentsAdapter.setData(dictViewModel.blngSents.value?.get(it)?.take(3))
        }

        // 第四部分：权威例句
        rvDictAuthSentsAdapter = DictAuthSentsAdapter(ArrayList())
        rvAuthSentsPart.adapter = rvDictAuthSentsAdapter
        rvAuthSentsPart.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.authSentenceResponse.observe(this){
            if ((it?.sent?.size ?: 0) > 0){
                dictViewModel.hasSearchResult.value = false
                clAuthSentsPart.visibility = View.VISIBLE
                rvDictAuthSentsAdapter.setData(it?.sent?.take(3))
            } else {
                clAuthSentsPart.visibility = View.GONE
            }
        }

        // 第五部分：网络释义
        rvDictWebTransOuterAdapter = DictWebTransOuterAdapter(ArrayList(), 2)
        rvWebTransPartMean.adapter = rvDictWebTransOuterAdapter
        rvWebTransPartMean.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.webTransResponse.observe(this){
            if((it?.`web-translation`?.size ?: 0) > 0) {
                dictViewModel.hasSearchResult.value = false
                clWebTransPart.visibility = View.VISIBLE
                rvDictWebTransOuterAdapter.setData(it?.`web-translation`?.take(1))
            } else {
                clWebTransPart.visibility = View.GONE
            }
        }

        // 第六部分：专业释义
        rvDictSpecialOuterAdapter = DictSpecialOuterAdapter(ArrayList(), 1)
        rvSpecialPartMean.adapter = rvDictSpecialOuterAdapter
        rvSpecialPartMean.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.specialResponse.observe(this){
            if((it?.entries?.size ?: 0) > 0){
                dictViewModel.hasSearchResult.value = false
                clSpecialPart.visibility = View.VISIBLE
                rvDictSpecialOuterAdapter.setData(it?.entries?.filter { it1 ->
                    (it1.entry?.trs?.size ?: 0) > 0
                }?.take(2))
            } else {
                clSpecialPart.visibility = View.GONE
            }
        }

        // 第七部分：同反义词
        rvDictThesurusOuterAdapter = DictThesurusOuterAdapter(ArrayList(), 3)
        rvThesaurusPartMean.adapter = rvDictThesurusOuterAdapter
        rvThesaurusPartMean.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.thesaurusResponse.observe(this){
            if((it?.thesauruses?.size ?: 0) > 0){
                dictViewModel.hasSearchResult.value = false
                clThesaurusPart.visibility = View.VISIBLE
                rvDictThesurusOuterAdapter.setData(it?.thesauruses?.filter { it1 ->
                    (it1.thesaurus?.size ?: 0) > 0
                }?.take(1))
            } else {
                clThesaurusPart.visibility = View.GONE
            }
        }

        // 第八部分：同义词
        rvDictSynoAdapter = DictSynoAntoAdapter(ArrayList())
        rvSynonymPartMean.adapter = rvDictSynoAdapter
        rvSynonymPartMean.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.synoResponse.observe(this){
            if ((it?.synos?.size ?: 0) > 0 && (dictViewModel.thesaurusResponse.value?.thesauruses?.size ?: 0) <= 0){
                dictViewModel.hasSearchResult.value = false
                clSynonymPart.visibility = View.VISIBLE
                rvDictSynoAdapter.setData(it?.synos?.take(3))
            } else {
                clSynonymPart.visibility = View.GONE
            }
        }

        // 第九部分：反义词
        rvDictAntoAdapter = DictSynoAntoAdapter(ArrayList())
        rvAntonymPartMean.adapter = rvDictAntoAdapter
        rvAntonymPartMean.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.antoResponse.observe(this){
            if ((it?.antos?.size ?: 0) > 0 && (dictViewModel.thesaurusResponse.value?.thesauruses?.size ?: 0) <= 0){
                dictViewModel.hasSearchResult.value = false
                clAntonymPart.visibility = View.VISIBLE
                rvDictAntoAdapter.setData(it?.antos?.take(3))
            } else {
                clAntonymPart.visibility = View.GONE
            }
        }

        // 第十部分：短语词组
        rvPhraseAdapter = DictPhraseAdapter(ArrayList())
        rvPhrasePartMean.adapter = rvPhraseAdapter
        rvPhrasePartMean.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.phrsResponse.observe(this){
            if ((it?.phrs?.size ?: 0) > 0){
                dictViewModel.hasSearchResult.value = false
                clPhrasePart.visibility = View.VISIBLE
                rvPhraseAdapter.setData(it?.phrs?.take(3))
            } else {
                clPhrasePart.visibility = View.GONE
            }
        }

        // 第十一部分：同根词
        rvRelWordAdapter = DictRelWordAdapter(ArrayList())
        rvRelWordPartMean.adapter = rvRelWordAdapter
        rvRelWordPartMean.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.relWordResponse.observe(this){
            if ((it?.rels?.size ?: 0) > 0){
                dictViewModel.hasSearchResult.value = false
                tvRelWordPartInf.text = it?.stem
                val rel = mutableListOf<RelInfo>()
                it?.rels?.forEach { it1 -> it1.rel?.let{ it2 -> rel.add(it2)} }
                clRelWordPart.visibility = View.VISIBLE
                rvRelWordAdapter.setData(rel.take(3))
            } else {
                clRelWordPart.visibility = View.GONE
            }
        }

        // 第十二部分：词源
        rvEtymAdapter = DictEtymAdapter(ArrayList())
        rvEtymPartMean.adapter = rvEtymAdapter
        rvEtymPartMean.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.etymResponse.observe(this){
            if ((it?.etyms?.zh?.size ?: 0) > 0){
                dictViewModel.hasSearchResult.value = false
                clEtymPart.visibility = View.VISIBLE
                val etyms = mutableListOf<Etym>()
                it?.etyms?.zh?.forEach { it1 -> etyms.add(it1) }
                it?.etyms?.en?.forEach { it1 -> etyms.add(it1) }
                rvEtymAdapter.setData(etyms.take(2))
            } else {
                clEtymPart.visibility = View.GONE
            }
        }

        // 第十三部分：百科
        dictViewModel.baikeDigest.observe(this){
            if ((it?.summarys?.size ?: 0) > 0){
                dictViewModel.hasSearchResult.value = false
                clBaikeDigestPart.visibility = View.VISIBLE
                tvBaikeDigestPartWord.text = it?.summarys?.get(0)?.key
                it?.summarys?.get(0)?.summary?.let { it1 ->
                    if (it1.isBlank()) tvBaikeDigestPartDesc.visibility = View.GONE
                    tvBaikeDigestPartDesc.text = it1
                    tvBaikeDigestPartDesc.setExpandableText(it1) }
                tvSourceBaike.text = it?.source?.name
            } else {
                clBaikeDigestPart.visibility = View.GONE
            }
        }

        // 第十四部分：拼写错误
        rvTypoAdapter = DictTypoAdapter(ArrayList())
        rvTyposExplain.adapter = rvTypoAdapter
        rvTyposExplain.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.typoResponse.observe(this){
            if ((it?.typo?.size ?: 0) > 0){
                llTyposPart.visibility = View.VISIBLE
                rvTypoAdapter.setData(it?.typo)
            } else {
                llTyposPart.visibility = View.GONE
            }
        }

        // 第十五部分：重新输入
        dictViewModel.hasSearchResult.observe(this){
            clSearchNotResult.visibility = if (it) View.VISIBLE else View.GONE
            cvRetype.visibility = if (dictViewModel.dictDetailMode == DICT_DETAIL_MODE_NORMAL) View.VISIBLE else View.GONE
        }

        // 第十六部分：外部词典
        rvExternalDictAdapter = DictExternalDictTransAdapter(ArrayList())
        rvExternalDictPartMean.adapter = rvExternalDictAdapter
        rvExternalDictPartMean.layoutManager = FlexboxLayoutManager(requireActivity())
        dictViewModel.dictExternalDict.observe(this){
            if ((it?.size ?: 0) > 0){
                clExternalDictPart.visibility = View.VISIBLE
                rvExternalDictAdapter.setData(it?.take((requireActivity().appWidth - SizeUtils.dp2px(30f)) / SizeUtils.dp2px(76f)))
            } else {
                clExternalDictPart.visibility = View.GONE
            }
        }

        // 第十七部分：外部翻译
        rvExternalTransAdapter = DictExternalDictTransAdapter(ArrayList())
        rvExternalTransPartMean.adapter = rvExternalTransAdapter
        rvExternalTransPartMean.layoutManager = FlexboxLayoutManager(requireActivity())
        dictViewModel.dictExternalTrans.observe(this){
            if ((it?.size ?: 0) > 0){
                clExternalTransPart.visibility = View.VISIBLE
                rvExternalTransAdapter.setData(it?.take((requireActivity().appWidth - SizeUtils.dp2px(30f)) / SizeUtils.dp2px(76f)))
            } else {
                clExternalTransPart.visibility = View.GONE
            }
        }

        // 音频播放状态
        dictViewModel.playPosition.observe(this) {
            it[VOICE_AUTH_PART]?.let { it1 ->
                if (it1 != -1) rvDictAuthSentsAdapter.setPositionPlayingSound(it1)
                else rvDictAuthSentsAdapter.resetPlaySound()
            }
            it[VOICE_BLNG_PART]?.let { it1 ->
                if (it1 != -1) rvDictBlngSentsAdapter.setPositionPlayingSound(it1)
                else rvDictBlngSentsAdapter.resetPlaySound()
            }
            it[VOICE_EH_PART]?.let { it1 ->
                if (it1 != -1) rvHeHeaderExplainAdapter.setPositionPlayingSound(it1)
                else rvHeHeaderExplainAdapter.resetPlaySound()
            }
        }

        nsvContent.scrollY = dictViewModel.nsvOffset[JM_FRAGMENT] ?: 0
        isInitView = false
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(nsvContent){ view, insets ->
            view.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }
        rvDictAuthSentsAdapter.setOnPlayButtonClickListener(object : DictAuthSentsAdapter.OnPlayButtonClickListener{
            override fun onPlayButtonClick(position: Int) {
                val item = rvDictAuthSentsAdapter.getItem(position)
                dictViewModel.startPlaySound(VOICE_AUTH_PART, position, item.speech!!, SEARCH_LE_EN)
            }
        })
        rvDictBlngSentsAdapter.setOnPlayButtonClickListener(object : DictBlngSentsAdapter.OnPlayButtonClickListener{
            override fun onPlayButtonClick(position: Int) {
                val item = rvDictBlngSentsAdapter.getItem(position)
                dictViewModel.startPlaySound(VOICE_BLNG_PART, position, item.speech!!, SEARCH_LE_EN)
            }
        })
        rvHeHeaderExplainAdapter.setOnPlayButtonClickListener(object : DictHeHeaderExplainAdapter.OnPlayButtonClickListener{
            override fun onPlayButtonClick(position: Int) {
                val item = rvHeHeaderExplainAdapter.getItem(position)
                dictViewModel.startPlaySound(VOICE_EH_PART, position, item.w!!, SEARCH_LE_EN)
            }
        })
        tvBaikeDigestPartDesc.setOnClickListener {
            tvBaikeDigestPartDesc.toggleExpand()
        }
        nsvContent.viewTreeObserver.addOnScrollChangedListener {
            if (!isInitView){
                dictViewModel.setNsvScrollOffset(JM_FRAGMENT, nsvContent.scrollY)
            }
        }
        tvRetype.setOnClickListener {
            mListener.onFragmentInteraction("toHasFragment")
        }
        rvBlngSentsPartClassificationAdapter.setOnItemClickListener(object : DictBlngClassificationAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val item = rvBlngSentsPartClassificationAdapter.getItem(position).text
                dictViewModel.setBlngClassification(item)
                rvBlngSentsPartClassification.scrollToPosition(position)
            }
        })
        rvDictAuthSentsAdapter.setOnSourceUrlClickListener(object : DictAuthSentsAdapter.OnSourceUrlClickListener{
            override fun onSourceUrlClick(position: Int) {
                WebActivity.start(
                    requireActivity(),
                    rvDictAuthSentsAdapter.getItem(position).url!!,
                    allowOtherUrls = true,
                    useWebTitle = true
                )
            }
        })
        tvBaikeDigestPartMore.setOnClickListener {
            when (dictViewModel.baikeDigest.value?.source?.name){
                getString(R.string.BaiduBaike) -> {
                    val url = dictViewModel.baikeDigest.value?.source?.url.let {
                        if (it.isNullOrBlank()) {
                            "https://baike.baidu.com/item/${dictViewModel.baikeDigest.value?.summarys?.get(0)?.key}"
                        } else it.replace("http://", "https://")
                    }
                    WebActivity.start(
                        requireActivity(), url,
                        allowOtherUrls = true,
                        useWebTitle = true
                    )
                }
                getString(R.string.wikipedia), getString(R.string.wikipedia_en) -> {
                    val url = dictViewModel.baikeDigest.value?.source?.url.let {
                        if (it.isNullOrBlank()) {
                            "https://en.wikipedia.org/wiki/${dictViewModel.baikeDigest.value?.summarys?.get(0)?.key}"
                        } else it.replace("http://", "https://")
                    }
                    WebActivity.start(
                        requireActivity(), url,
                        allowOtherUrls = true,
                        useWebTitle = true
                    )
                }
                else -> {
                    showShortToast(getString(R.string.nothing_to_access))
                    tvBaikeDigestPartMore.visibility = View.GONE
                }
            }
        }
        rvExternalTransAdapter.setOnItemClickListener(object : DictExternalDictTransAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val cm: ClipboardManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText(null, dictViewModel.searchText.value?.searchText))
                showShortToast(getString(R.string.copied_to_trans))
            }
        })
        tvAuthSentsPartMore.setOnClickListener{
            mListener.onFragmentInteraction("toExtraFragment", AUTH_SENTS_PART_MORE )
        }
        tvAntonymPartMore.setOnClickListener{
            mListener.onFragmentInteraction("toExtraFragment", ANTONYM_PART_MORE )
        }
        tvPhrasePartMore.setOnClickListener{
            mListener.onFragmentInteraction("toExtraFragment", PHRASE_PART_MORE )
        }
        tvSynonymPartMore.setOnClickListener{
            mListener.onFragmentInteraction("toExtraFragment", SYNONYM_PART_MORE )
        }
        tvThesaurusPartMore.setOnClickListener{
            mListener.onFragmentInteraction("toExtraFragment", THESAURUS_PART_MORE )
        }
        tvBlngSentsPartMore.setOnClickListener{
            mListener.onFragmentInteraction("toExtraFragment", BLNG_SENTS_PART_MORE )
        }
        tvRelWordPartMore.setOnClickListener{
            mListener.onFragmentInteraction("toExtraFragment", REL_WORD_PART_MORE )
        }
        tvEtymPartMore.setOnClickListener {
            mListener.onFragmentInteraction("toExtraFragment", ETYM_PART_MORE )
        }
        tvWebTransPartMore.setOnClickListener {
            mListener.onFragmentInteraction("toExtraFragment", WEB_TRANS_PART_MORE )
        }
        tvSpecialPartMore.setOnClickListener {
            mListener.onFragmentInteraction("toExtraFragment", SPECIAL_PART_MORE )
        }
        tvExternalDictPartMore.setOnClickListener {
            mListener.onFragmentInteraction("toExtraFragment", EXTERNAL_DICT_PART_MORE )
        }
        tvExternalTransPartMore.setOnClickListener {
            mListener.onFragmentInteraction("toExtraFragment", EXTERNAL_TRANS_PART_MORE )
        }
    }
}