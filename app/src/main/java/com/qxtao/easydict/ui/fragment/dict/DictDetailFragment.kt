package com.qxtao.easydict.ui.fragment.dict

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.FragmentDictDetailBinding
import com.qxtao.easydict.ui.activity.dict.APPBAR_LAYOUT_COLLAPSED
import com.qxtao.easydict.ui.activity.dict.APPBAR_LAYOUT_EXPANDED
import com.qxtao.easydict.ui.activity.dict.APPBAR_LAYOUT_INTERMEDIATE
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.activity.dict.SEARCH_LE_EN
import com.qxtao.easydict.ui.activity.dict.SEARCH_LE_ZH
import com.qxtao.easydict.ui.activity.dict.VOICE_NORMAL
import com.qxtao.easydict.ui.activity.photoview.PhotoViewActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.ExpandableTextView
import com.qxtao.easydict.utils.common.SizeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


class DictDetailFragment : BaseFragment<FragmentDictDetailBinding>(FragmentDictDetailBinding::inflate) {
    // define variable
    private lateinit var dictDetailPagerAdapter : DictDetailPagerAdapter
    private val tabLayoutMap = mapOf(0 to "简明", 1 to "柯林斯", 2 to "英英")
    private lateinit var dictViewModel: DictViewModel
    private var isInitView = false
    // define widget
    private lateinit var mcvSearchDetailTablayout: MaterialCardView
    private lateinit var searchDetailTablayout: TabLayout
    private lateinit var searchDetailViewpager: ViewPager2
    private lateinit var tvTransFromRoman: ExpandableTextView
    private lateinit var tvTransFrom: TextView
    private lateinit var tvTransToRoman: ExpandableTextView
    private lateinit var tvTransTo: TextView
    private lateinit var llHeader: LinearLayout
    private lateinit var clWordHeader: ConstraintLayout
    private lateinit var clTransHeader: ConstraintLayout
    private lateinit var clLoading: ConstraintLayout
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
    private lateinit var tvOfflineWord: TextView
    private lateinit var tvOfflineTranslation: TextView
    private lateinit var tvReload: TextView
    private lateinit var nsvOfflineResult: NestedScrollView
    private lateinit var clOnlineResult: CoordinatorLayout
    private lateinit var clVoice: ConstraintLayout

    override fun bindViews() {
        mcvSearchDetailTablayout = binding.mcvSearchDetailTablayout
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
        clLoading = binding.clLoading
        tvOfflineWord = binding.tvOfflineWord
        tvOfflineTranslation = binding.tvOfflineTranslation
        nsvOfflineResult = binding.nsvOfflineResult
        clOnlineResult = binding.clOnlineResult
        tvReload = binding.tvReload
        clVoice = binding.clVoice

    }

    override fun initViews() {
        isInitView = true
        mListener.onFragmentInteraction("changeSearchFragmentBackgroundColor")
        dictViewModel = (activity as DictActivity).getDictViewModel()
        (parentFragment as? DictSearchFragment)?.exitEditTextFocus()
        dictViewModel.dataLoadInfo.observe(this) {
            when (it) {
                0 -> {
                    clLoading.visibility = View.VISIBLE
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
                            clLoading.visibility = View.GONE
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
                        tvOfflineWord.text = it1!!
                        dictViewModel.offlineSearch(it1).let { it2 ->
                            tvOfflineTranslation.text = if (it2.isNullOrEmpty()) getString(R.string.no_result) else it2.replace("\\|", "\n")
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch{
                        delay(50)
                        withContext(Dispatchers.Main){
                            clLoading.visibility = View.GONE
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
                            tvTransToRoman.setExpandableText(it1)
                        }
                    }
                } else { // 单词短语
                    clWordHeader.visibility = View.VISIBLE
                    clTransHeader.visibility = View.GONE
                    it.`return-phrase`?.let { it1 -> tvWord.text = it1 }
                    it.ukphone?.let { it1 ->
                        tvUkVoice.text = String.format(getString(R.string.uk_voice), it1)
                    }
                    it.usphone?.let { it1 ->
                        tvUsVoice.text = String.format(getString(R.string.us_voice), it1)
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
                        tvTransFromRoman.setExpandableText(it1)
                    }
                } else { // 单词短语
                    clWordHeader.visibility = View.VISIBLE
                    clTransHeader.visibility = View.GONE
                    clVoice.visibility = View.GONE
                    it.`return-phrase`?.word?.let { it1 -> tvWord.text = it1 }
                }
            }
        }
        dictViewModel.picDictResponse.observe(this) {
            if (it != null && (it.pic?.size ?: 0) > 0) {
                ivWdPic.visibility = View.VISIBLE
                ivWdPic.load(it.pic?.get(0)?.url?.replace("http://","https://"))
            }
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
        binding.appBarLayout.setExpanded(dictViewModel.detailFragmentAppBarExpanded.value == APPBAR_LAYOUT_EXPANDED, false)
        isInitView = false
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(nsvOfflineResult){ view, insets ->
            view.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }
        tvTransFromRoman.setOnClickListener { tvTransFromRoman.toggleExpand() }
        tvTransToRoman.setOnClickListener { tvTransToRoman.toggleExpand() }
        ivWdPic.setOnClickListener {
            val pictureList = ArrayList<String>()
            dictViewModel.picDictResponse.value?.pic?.get(0)?.url.let {
                if (it.isNullOrEmpty()) return@setOnClickListener
                pictureList.add(it.replace("http://","https://"))
                PhotoViewActivity.start(requireActivity(), pictureList, ivWdPic)
            }
        }
        binding.appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (!isInitView) {
                dictViewModel.detailFragmentAppBarExpanded.value =
                    if (verticalOffset == 0) APPBAR_LAYOUT_EXPANDED
                    else if (abs(verticalOffset) >= binding.appBarLayout.totalScrollRange) APPBAR_LAYOUT_COLLAPSED
                    else APPBAR_LAYOUT_INTERMEDIATE
            }
        }
        tvReload.setOnClickListener {
            mListener.onFragmentInteraction("toDetailFragment", dictViewModel.searchText.value?.editSearchText)
        }
        ivUkVoice.setOnClickListener {
            dictViewModel.ehResponse.value?.usspeech?.let { it1 ->
                dictViewModel.startPlaySound(VOICE_NORMAL, 0, it1, SEARCH_LE_EN)
            }
        }
        ivUsVoice.setOnClickListener {
            dictViewModel.ehResponse.value?.ukspeech?.let { it1 ->
                dictViewModel.startPlaySound(VOICE_NORMAL, 1, it1, SEARCH_LE_EN)
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
                            dictViewModel.startPlaySound(VOICE_NORMAL, 3, it1, SEARCH_LE_ZH)
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
                            dictViewModel.startPlaySound(VOICE_NORMAL, 4, it1, SEARCH_LE_ZH)
                        }
                    }
                    else -> showShortToast(getString(R.string.play_failure))
                }
            }
        }
        ivTransToCopy.setOnClickListener {
            val cm: ClipboardManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(
                ClipData.newPlainText(null, tvTransTo.text))
            showShortToast(getString(R.string.copied))
        }
        ivTransFromCopy.setOnClickListener {
            val cm: ClipboardManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(null, tvTransFrom.text))
            showShortToast(getString(R.string.copied))
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
                mcvSearchDetailTablayout.visibility = View.GONE
            } else mcvSearchDetailTablayout.visibility = View.VISIBLE
        }
        override fun getItemCount(): Int = fragmentList.size
        override fun createFragment(position: Int): Fragment = fragmentList[position]
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::dictViewModel.isInitialized) dictViewModel.stopPlaySound()
    }
}