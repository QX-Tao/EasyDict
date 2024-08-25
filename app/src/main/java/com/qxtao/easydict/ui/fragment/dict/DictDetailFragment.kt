package com.qxtao.easydict.ui.fragment.dict

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.FragmentDictDetailBinding
import com.qxtao.easydict.ui.activity.dict.DICT_SEARCH_FRAGMENT_TAG
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.activity.dict.SEARCH_LE_EN
import com.qxtao.easydict.ui.activity.dict.SEARCH_LE_ZH
import com.qxtao.easydict.ui.activity.dict.VOICE_NORMAL
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.ExpandableTextView
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.ui.view.imageviewer.PhotoView
import com.qxtao.easydict.utils.common.ClipboardUtils
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.constant.ShareConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DictDetailFragment : BaseFragment<FragmentDictDetailBinding>(FragmentDictDetailBinding::inflate) {
    // define variable
    private lateinit var dictDetailPagerAdapter : DictDetailPagerAdapter
    private val tabLayoutMap = mapOf(0 to "简明", 1 to "柯林斯", 2 to "英英")
    private lateinit var dictViewModel: DictViewModel
    // define widget
    private lateinit var searchDetailTablayout: TabLayout
    private lateinit var searchDetailViewpager: ViewPager2
    private lateinit var tvTransFromRoman: ExpandableTextView
    private lateinit var tvTransFrom: TextView
    private lateinit var tvTransToRoman: ExpandableTextView
    private lateinit var tvTransTo: TextView
    private lateinit var llHeader: LinearLayout
    private lateinit var clWordHeader: ConstraintLayout
    private lateinit var clTransHeader: ConstraintLayout
    private lateinit var lvLoading: LoadingView
    private lateinit var tvWord: TextView
    private lateinit var tvUkVoice: TextView
    private lateinit var tvUsVoice: TextView
    private lateinit var ivUkVoice: ImageView
    private lateinit var ivUsVoice: ImageView
    private lateinit var ivWdPic: ImageView
    private lateinit var ivNormalVoice: ImageView
    private lateinit var ivTransFromCopy: ImageView
    private lateinit var ivTransToCopy: ImageView
    private lateinit var ivTransFromVoice: ImageView
    private lateinit var ivTransToVoice: ImageView
    private lateinit var ivTransCollect: ImageView
    private lateinit var ivWordCollect: ImageView
    private lateinit var tvOfflineWord: TextView
    private lateinit var tvOfflineTrans: ExpandableTextView
    private lateinit var tvOfflineTranslation: TextView
    private lateinit var tvReload: TextView
    private lateinit var nsvOfflineResult: NestedScrollView
    private lateinit var clOnlineResult: CoordinatorLayout
    private lateinit var clVoice: ConstraintLayout

    override fun bindViews() {
        searchDetailTablayout = binding.searchDetailTablayout
        searchDetailViewpager = binding.searchDetailViewpager
        tvTransFromRoman = binding.tvTransFromRoman
        tvTransFrom = binding.tvTransFrom
        tvTransToRoman = binding.tvTransToRoman
        tvTransTo = binding.tvTransTo
        clTransHeader = binding.clTransHeader
        clWordHeader = binding.clWordHeader
        llHeader = binding.llHeader
        tvWord = binding.tvWord
        tvUkVoice = binding.tvUkVoice
        tvUsVoice = binding.tvUsVoice
        ivUkVoice = binding.ivUkVoice
        ivUsVoice = binding.ivUsVoice
        ivWdPic = binding.ivWdPic
        ivNormalVoice = binding.ivNormalVoice
        ivTransFromCopy = binding.ivTransFromCopy
        ivTransToCopy = binding.ivTransToCopy
        ivTransFromVoice = binding.ivTransFromVoice
        ivTransToVoice = binding.ivTransToVoice
        lvLoading = binding.lvLoading
        tvOfflineWord = binding.tvOfflineWord
        tvOfflineTrans = binding.tvOfflineTrans
        tvOfflineTranslation = binding.tvOfflineTranslation
        nsvOfflineResult = binding.nsvOfflineResult
        clOnlineResult = binding.clOnlineResult
        tvReload = binding.tvReload
        clVoice = binding.clVoice
        ivTransCollect = binding.ivTransCollect
        ivWordCollect = binding.ivWordCollect
    }

    override fun initViews() {
        dictViewModel = (activity as DictActivity).getDictViewModel()
        dictViewModel.playSound = if (ShareUtils.getString(mContext, ShareConstant.DEF_VOICE, ShareConstant.MEI) == ShareConstant.MEI) 0 else 1
        if (dictViewModel.currentFragmentTag == DICT_SEARCH_FRAGMENT_TAG){
            (parentFragment as DictSearchFragment).exitEditTextFocus()
        }
        dictViewModel.dataLoadInfo.observe(this) {
            when (it) {
                0 -> {
                    lvLoading.visibility = View.VISIBLE
                    clOnlineResult.visibility = View.GONE
                    nsvOfflineResult.visibility = View.GONE
                }
                1 -> {
                    tvWord.text = dictViewModel.searchText.value?.searchText
                    dictDetailPagerAdapter = DictDetailPagerAdapter(requireActivity())
                    nsvOfflineResult.visibility = View.GONE
                    clOnlineResult.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).launch{
                        delay(50)
                        withContext(Dispatchers.Main){
                            lvLoading.visibility = View.GONE
                        } }
                    searchDetailViewpager.offscreenPageLimit = 2
                    searchDetailViewpager.adapter = dictDetailPagerAdapter
                    TabLayoutMediator(searchDetailTablayout, searchDetailViewpager) { tab, position ->
                        val truePositions = dictViewModel.hasShowDetailFragment.toList()
                            .mapIndexedNotNull { index, value -> if (value) index else null }
                        if (position in truePositions.indices) {
                            tab.text = tabLayoutMap[truePositions[position]]
                        }
                    }.attach()
                }
                2 -> {
                    nsvOfflineResult.visibility = View.VISIBLE
                    clOnlineResult.visibility = View.GONE
                    dictViewModel.searchText.value?.editSearchText.let { it1 ->
                        if ((it1?.length ?: 0) > 40) {
                            tvOfflineTrans.originalText = it1!!
                            tvOfflineWord.visibility = View.GONE
                            tvOfflineTrans.visibility = View.VISIBLE
                        } else {
                            tvOfflineWord.text = it1!!
                            tvOfflineWord.visibility = View.VISIBLE
                            tvOfflineTrans.visibility = View.GONE
                        }
                        dictViewModel.offlineSearch(it1).let { it2 ->
                            tvOfflineTranslation.text = if (it2.isNullOrEmpty()) getString(R.string.no_result) else it2.replace("\\|", "\n")
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch{
                        delay(50)
                        withContext(Dispatchers.Main){
                            lvLoading.visibility = View.GONE
                        }
                    }
                }
            }
        }
        dictViewModel.ehResponse.observe(this) {
            if (it != null) {
                if (it.isTran == true) {  // 翻译
                    clWordHeader.visibility = View.GONE
                    clTransHeader.visibility = View.VISIBLE
                    tvTransFromRoman.visibility = View.GONE
                    it.`return-phrase`?.let { it1 -> tvTransFrom.text = it1 }
                    if ((it.trs?.size ?: 0) > 0) {
                        it.trs?.get(0)?.tran?.let { it1 -> tvTransTo.text = it1 }
                        it.trs?.get(0)?.`tran-roman`?.let { it1 ->
                            tvTransToRoman.visibility = View.VISIBLE
                            tvTransToRoman.originalText = it1
                        }
                    }
                } else { // 单词短语
                    clWordHeader.visibility = View.VISIBLE
                    clTransHeader.visibility = View.GONE
                    it.`return-phrase`?.let { it1 -> tvWord.text = it1 }
                    it.ukphone?.let { it1 ->
                        tvUkVoice.text = getString(R.string.uk_voice, it1)
                    }
                    it.usphone?.let { it1 ->
                        tvUsVoice.text = getString(R.string.us_voice, it1)
                    }
                    clVoice.visibility = View.VISIBLE
                    tvUkVoice.visibility = if (it.ukphone.isNullOrEmpty()) View.GONE else View.VISIBLE
                    tvUsVoice.visibility = if (it.usphone.isNullOrEmpty()) View.GONE else View.VISIBLE
                    ivUkVoice.visibility = tvUkVoice.visibility
                    ivUsVoice.visibility = tvUsVoice.visibility
                    ivNormalVoice.visibility = if (it.ukphone.isNullOrEmpty() && it.usphone.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
        dictViewModel.heResponse.observe(this) {
            if (it != null) {
                if (it.isTran == true) {  // 翻译
                    clWordHeader.visibility = View.GONE
                    clTransHeader.visibility = View.VISIBLE
                    tvTransToRoman.visibility = View.GONE
                    if ((it.trans?.size ?: 0) > 0) {
                        it.trans?.get(0)?.w?.let { it1 -> tvTransTo.text = it1 }
                    }
                    it.`return-phrase`?.word?.let { it1 -> tvTransFrom.text = it1 }
                    it.`return-phrase`?.`query-roman`?.let { it1 ->
                        tvTransFromRoman.visibility = View.VISIBLE
                        tvTransFromRoman.originalText = it1
                    }
                } else { // 单词短语
                    clWordHeader.visibility = View.VISIBLE
                    clTransHeader.visibility = View.GONE
                    clVoice.visibility = View.GONE
                    it.`return-phrase`?.word?.let { it1 -> tvWord.text = it1 }
                }
            }
        }
        dictViewModel.lanMatchResponse.observe(this){
            if (it != null && it.match == false){
                clWordHeader.visibility = View.VISIBLE
                clTransHeader.visibility = View.GONE
                clVoice.visibility = View.GONE
                ivWdPic.visibility = View.GONE
            }
        }
        dictViewModel.picDictResponse.observe(this) {
            if (it != null && (it.pic?.size ?: 0) > 0) {
                ivWdPic.visibility = View.VISIBLE
                ivWdPic.load(it.pic?.get(0)?.url)
            } else ivWdPic.visibility = View.GONE
        }
        dictViewModel.playPosition.observe(this){
            it[VOICE_NORMAL].let {it1 ->
                val voicePlayResId = R.drawable.ic_voice_on
                val voiceResId = R.drawable.ic_voice
                ivUkVoice.setImageResource(if (it1 == 0) voicePlayResId else voiceResId)
                ivUsVoice.setImageResource(if (it1 == 1) voicePlayResId else voiceResId)
                ivNormalVoice.setImageResource(if (it1 == 2) voicePlayResId else voiceResId)
                ivTransFromVoice.setImageResource(if (it1 == 3) voicePlayResId else voiceResId)
                ivTransToVoice.setImageResource(if (it1 == 4) voicePlayResId else voiceResId)
            }
        }
        dictViewModel.isSearchTextFavorite.observe(this) {
            if (it){
                ivTransCollect.setImageResource(R.drawable.ic_collected)
                ivWordCollect.setImageResource(R.drawable.ic_collected)
            } else {
                ivTransCollect.setImageResource(R.drawable.ic_collect1)
                ivWordCollect.setImageResource(R.drawable.ic_collect1)
            }
        }
    }

    override fun addListener() {
        ivWdPic.setOnClickListener {
            dictViewModel.picDictResponse.value?.pic?.get(0)?.url?.let {
                photoView.show(it, ivWdPic)
            }
        }
        tvReload.setOnClickListener {
            mListener.onFragmentInteraction("toDetailFragment", dictViewModel.searchText.value?.editSearchText)
        }
        ivUkVoice.setOnClickListener {
            dictViewModel.ehResponse.value?.ukspeech?.let { it1 ->
                dictViewModel.startPlaySound(VOICE_NORMAL, 0, it1, SEARCH_LE_EN, -1)
            }
        }
        ivUsVoice.setOnClickListener {
            dictViewModel.ehResponse.value?.usspeech?.let { it1 ->
                dictViewModel.startPlaySound(VOICE_NORMAL, 1, it1, SEARCH_LE_EN, -1)
            }
        }
        ivNormalVoice.setOnClickListener {
            dictViewModel.ehResponse.value?.`return-phrase`?.let { it1 ->
                dictViewModel.startPlaySound(VOICE_NORMAL, 2, it1, SEARCH_LE_EN)
            }
        }
        ivTransFromVoice.setOnClickListener {
            dictViewModel.searchInfoResponse.value?.from?.let {
                when (it) {
                    SEARCH_LE_ZH -> {
                        dictViewModel.heResponse.value?.`return-phrase`?.word?.let { it1 ->
                            dictViewModel.startPlaySound(VOICE_NORMAL, 3, it1, SEARCH_LE_ZH, -1)
                        }
                    }
                    SEARCH_LE_EN -> {
                        dictViewModel.ehResponse.value?.`return-phrase`?.let { it1 ->
                            dictViewModel.startPlaySound(VOICE_NORMAL, 3, it1, SEARCH_LE_EN)
                        }
                    }
                    else -> showShortToast(getString(R.string.play_failure))
                }
            }
        }
        ivTransToVoice.setOnClickListener {
            dictViewModel.searchInfoResponse.value?.to?.let {
                when (it) {
                    SEARCH_LE_EN -> {
                        dictViewModel.heResponse.value?.trans?.get(0)?.w?.let { it1 ->
                            dictViewModel.startPlaySound(VOICE_NORMAL, 4, it1, SEARCH_LE_EN)
                        }
                    }
                    SEARCH_LE_ZH -> {
                        dictViewModel.ehResponse.value?.trs?.get(0)?.tran?.let { it1 ->
                            dictViewModel.startPlaySound(VOICE_NORMAL, 4, it1, SEARCH_LE_ZH, -1)
                        }
                    }
                    else -> showShortToast(getString(R.string.play_failure))
                }
            }
        }
        ivTransToCopy.setOnClickListener {
            ClipboardUtils.copyTextToClipboard(mContext, tvTransTo.text, getString(R.string.copied))
        }
        ivTransFromCopy.setOnClickListener {
            ClipboardUtils.copyTextToClipboard(mContext, tvTransFrom.text, getString(R.string.copied))
        }
        ivWordCollect.setOnClickListener {
            if (dictViewModel.isSearchTextFavorite.value == true){
                dictViewModel.removeSearchTextFromWordBook()
                showShortToast(getString(R.string.remove_from_word_book))
            } else {
                mListener.onFragmentInteraction("showSelectWordBookDialog")
            }
        }
        ivTransCollect.setOnClickListener {
            if (dictViewModel.isSearchTextFavorite.value == true){
                dictViewModel.removeSearchTextFromWordBook()
                showShortToast(getString(R.string.remove_from_word_book))
            } else {
                mListener.onFragmentInteraction("showSelectWordBookDialog")
            }
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) { //表示是一个进入动作，比如add.show等
            return if (enter) { //普通的进入的动作
                AnimationUtils.loadAnimation(mContext, R.anim.anim_romove_anim_in)
            } else { //比如一个已经Fragment被另一个replace，是一个进入动作，被replace的那个就是false
                AnimationUtils.loadAnimation(mContext, R.anim.anim_romove_anim_out)
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) { //表示一个退出动作，比如出栈，hide，detach等
            return if (enter) { //之前被replace的重新进入到界面或者Fragment回到栈顶
                AnimationUtils.loadAnimation(mContext, R.anim.anim_romove_anim_in)
            } else { //Fragment退出，出栈
                AnimationUtils.loadAnimation(mContext, R.anim.anim_romove_anim_out)
            }
        }
        return null
    }

    private inner class DictDetailPagerAdapter(fActivity: FragmentActivity) : FragmentStateAdapter(fActivity) {
        private val fragmentList = mutableListOf<Fragment>()
        init {
            dictViewModel.hasShowDetailFragment.apply {
                if (first) fragmentList.add(DictDetailJMFragment())
                if (second) fragmentList.add(DictDetailCOFragment())
                if (third) fragmentList.add(DictDetailEEFragment())
            }
            if (fragmentList.size == 1 && fragmentList[0] is DictDetailJMFragment){
                searchDetailTablayout.visibility = View.GONE
            } else searchDetailTablayout.visibility = View.VISIBLE
        }
        override fun getItemCount(): Int = fragmentList.size
        override fun createFragment(position: Int): Fragment = fragmentList[position]
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::dictViewModel.isInitialized) dictViewModel.stopPlaySound()
    }
}