package com.qxtao.easydict.ui.fragment.dict

import android.content.Intent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dict.DictSearchSugWordAdapter
import com.qxtao.easydict.databinding.FragmentDictWelcomeBinding
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceActivity
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.activity.grammarcheck.GrammarCheckActivity
import com.qxtao.easydict.ui.activity.settings.SettingsActivity
import com.qxtao.easydict.ui.activity.wordbook.WordBookActivity
import com.qxtao.easydict.ui.activity.wordlist.WordListActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.LoadFailedView
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.constant.ShareConstant.COUNT_DOWN_NAME
import com.qxtao.easydict.utils.constant.ShareConstant.COUNT_DOWN_NAME_OFF
import com.qxtao.easydict.utils.constant.ShareConstant.COUNT_DOWN_TIME
import com.qxtao.easydict.utils.constant.ShareConstant.COUNT_DOWN_TIME_OFF
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_COUNT_DOWN
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_DAILY_SENTENCE
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_GRAMMAR_CHECK
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_SUGGEST_SEARCH
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_WORD_BOOK
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_WORD_LIST


class DictWelcomeFragment : BaseFragment<FragmentDictWelcomeBinding>(FragmentDictWelcomeBinding::inflate) {
    // define variable
    private lateinit var dictViewModel: DictViewModel
    private lateinit var rvSearchSugWordAdapter: DictSearchSugWordAdapter

    // define widget
    private lateinit var mcvSuggestSearch : MaterialCardView
    private lateinit var mcvDaySentence : MaterialCardView
    private lateinit var mcvWordList : MaterialCardView
    private lateinit var mcvWordBook : MaterialCardView
    private lateinit var mcvGrammarCheck : MaterialCardView
    private lateinit var tvSearchBox : TextView
    private lateinit var vDaySentence : View
    private lateinit var vGrammarCheck : View
    private lateinit var vWordBook : View
    private lateinit var vWordList : View
    private lateinit var ivVoice : ImageView
    private lateinit var ivSuggestSearchRefresh : ImageView
    private lateinit var rvSearchSuggestion : RecyclerView
    private lateinit var mtTitle : MaterialToolbar
    private lateinit var ivDaySentence : ImageView
    private lateinit var tvDsDate : TextView
    private lateinit var tvDsCn : TextView
    private lateinit var tvDsEn : TextView
    private lateinit var lvDsLoadFailed : LoadFailedView
    private lateinit var lvDsLoading : LoadingView
    private lateinit var tvWordListLearned : TextView
    private lateinit var tvWordListProgress : TextView
    private lateinit var tvWordListNoBookSelected : TextView
    private lateinit var tvWordListTitle : TextView
    private lateinit var ivWordListRefresh : ImageView

    override fun bindViews() {
        mcvSuggestSearch = binding.mcvSuggestSearch
        mcvDaySentence = binding.mcvDaySentence
        mcvWordList = binding.mcvWordList
        mcvWordBook = binding.mcvWordBook
        mcvGrammarCheck = binding.mcvGrammarCheck
        tvSearchBox = binding.tvSearchBox
        vDaySentence = binding.vDaySentence
        vGrammarCheck = binding.vGrammarCheck
        vWordBook = binding.vWordBook
        vWordList = binding.vWordList
        ivVoice = binding.ivVoice
        ivSuggestSearchRefresh = binding.ivSuggestSearchRefresh
        rvSearchSuggestion = binding.rvSearchSuggestion
        mtTitle = binding.mtTitle
        ivDaySentence = binding.ivDaySentence
        tvDsDate = binding.tvDsDate
        tvDsCn = binding.tvDsCn
        tvDsEn = binding.tvDsEn
        lvDsLoadFailed = binding.lvDsLoadFailed
        lvDsLoading = binding.lvDsLoading
        tvWordListLearned = binding.tvWordListLearned
        tvWordListProgress = binding.tvWordListProgress
        tvWordListNoBookSelected = binding.tvWordListNoBookSelected
        tvWordListTitle = binding.tvWordListTitle
        ivWordListRefresh = binding.ivWordListRefresh
    }

    override fun initViews() {
        dictViewModel = (activity as DictActivity).getDictViewModel()

        mtTitle.title = getMtTitle()
        mcvSuggestSearch.visibility = if (ShareUtils.getBoolean(mContext, IS_USE_SUGGEST_SEARCH, true)) View.VISIBLE else View.GONE
        mcvDaySentence.visibility = if (ShareUtils.getBoolean(mContext, IS_USE_DAILY_SENTENCE, true)) View.VISIBLE else View.GONE
        mcvWordList.visibility = if (ShareUtils.getBoolean(mContext, IS_USE_WORD_LIST, true)) View.VISIBLE else View.GONE
        mcvWordBook.visibility = if (ShareUtils.getBoolean(mContext, IS_USE_WORD_BOOK, true)) View.VISIBLE else View.GONE
        mcvGrammarCheck.visibility = if (ShareUtils.getBoolean(mContext, IS_USE_GRAMMAR_CHECK, true)) View.VISIBLE else View.GONE

        rvSearchSugWordAdapter = DictSearchSugWordAdapter(ArrayList())
        rvSearchSuggestion.adapter = rvSearchSugWordAdapter
        rvSearchSuggestion.layoutManager = FlexboxLayoutManager(requireActivity())
        dictViewModel.dictSearchSugWord.observe(this){
            rvSearchSugWordAdapter.setData(it)
        }
        dictViewModel.dailySentenceItem.observe(this){
            when(it?.first){
                true -> {
                    lvDsLoading.visibility = View.GONE
                    tvDsCn.text = it.second?.cnSentence
                    tvDsEn.text = it.second?.enSentence
                    tvDsDate.text = TimeUtils.getCurrentDateByPattern("yyyy-MM-dd")
                    ivDaySentence.load(it.second?.imageUrl){ crossfade(true) }
                }
                false -> {
                    lvDsLoading.visibility = View.GONE
                    lvDsLoadFailed.visibility = View.VISIBLE
                }
                else -> {
                    lvDsLoading.visibility = View.VISIBLE
                    lvDsLoadFailed.visibility = View.GONE
                }
            }
        }
        dictViewModel.wordListInfo.observe(this){
            when(it?.first){
                true -> {
                    tvWordListLearned.text = it.second?.third.toString()
                    tvWordListProgress.text = (it.second?.third!! * 100 / it.second?.second!!).toString()
                    tvWordListNoBookSelected.visibility = View.GONE
                    tvWordListTitle.text = dictViewModel.wordNameMap[it.second?.first]
                }
                false -> {
                    tvWordListTitle.text = getString(R.string.recite_word)
                    tvWordListNoBookSelected.visibility = View.VISIBLE
                }
                else -> {}
            }
        }
        dictViewModel.getDictSearchSugWord()
        dictViewModel.getDailySentence()
        dictViewModel.getWordListInfo()
    }

    override fun addListener() {
        rvSearchSugWordAdapter.setOnItemClickListener(object : DictSearchSugWordAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                mListener.onFragmentInteraction("toDetailFragment", dictViewModel.dictSearchSugWord.value!![position].origin)
            }
        })
        mtTitle.setOnMenuItemClickListener {
            startActivity(Intent(mContext, SettingsActivity::class.java))
            true
        }
        lvDsLoadFailed.setOnClickListener {
            dictViewModel.getDailySentence()
        }
        ivSuggestSearchRefresh.setOnClickListener {
            dictViewModel.getDictSearchSugWord()
        }
        ivWordListRefresh.setOnClickListener {
            dictViewModel.getWordListInfo()
        }
        tvSearchBox.setOnClickListener {
            dictViewModel.setSearchText(getString(R.string.empty_string),  0, 0)
            mListener.onFragmentInteraction("toDictSearchFragment")
        }
        ivVoice.setOnClickListener {
            mListener.onFragmentInteraction("voiceSearch")
        }
        vDaySentence.setOnClickListener {
            startActivity(Intent(mContext, DaySentenceActivity::class.java))
        }
        vGrammarCheck.setOnClickListener {
            startActivity(Intent(mContext, GrammarCheckActivity::class.java))
        }
        vWordBook.setOnClickListener {
            startActivity(Intent(mContext, WordBookActivity::class.java))
        }
        vWordList.setOnClickListener {
            startActivity(Intent(mContext, WordListActivity::class.java))
        }
    }

    private fun getMtTitle(): String{
        val isUseCountDown = ShareUtils.getBoolean(mContext, IS_USE_COUNT_DOWN, false)
        val countDownName = ShareUtils.getString(mContext, COUNT_DOWN_NAME, COUNT_DOWN_NAME_OFF)
        val countDownTime = ShareUtils.getLong(mContext, COUNT_DOWN_TIME, COUNT_DOWN_TIME_OFF)
        return if (isUseCountDown && countDownName.isNotEmpty() && countDownTime > 0){
            if (countDownTime < System.currentTimeMillis()){
                if (TimeUtils.isGivenTimeIn24Hours(countDownTime)){
                    getString(R.string.mt_title_count_down_today, countDownName) } else {
                    ShareUtils.putBoolean(mContext, IS_USE_COUNT_DOWN, false)
                    ShareUtils.delShare(mContext, COUNT_DOWN_NAME)
                    ShareUtils.delShare(mContext, COUNT_DOWN_TIME)
                    getString(R.string.app_name)
                }
            } else {
                val dateCountDown = TimeUtils.calculateDateDifference(countDownTime, false) + 1
                if (dateCountDown == 1L) getString(R.string.mt_title_count_down_tomorrow, countDownName) else getString(R.string.mt_title_count_down, countDownName, dateCountDown)
            }
        } else getString(R.string.app_name)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) { //表示是一个进入动作，比如add.show等
            return if (enter) { //普通的进入的动作
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_right)
            } else { //比如一个已经Fragment被另一个replace，是一个进入动作，被replace的那个就是false
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_out_left)
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) { //表示一个退出动作，比如出栈，hide，detach等
            return if (enter) { //之前被replace的重新进入到界面或者Fragment回到栈顶
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_left)
            } else { //Fragment退出，出栈
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_out_right)
            }
        }
        return null
    }
}