package com.qxtao.easydict.ui.fragment.dict

import android.content.Intent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dict.DictSearchSugWordAdapter
import com.qxtao.easydict.database.WordListData
import com.qxtao.easydict.databinding.FragmentDictWelcomeBinding
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceActivity
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.activity.grammarcheck.GrammarCheckActivity
import com.qxtao.easydict.ui.activity.wordlist.WordListActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.factory.isLandscape
import com.qxtao.easydict.utils.factory.screenRotation


class DictWelcomeFragment : BaseFragment<FragmentDictWelcomeBinding>(FragmentDictWelcomeBinding::inflate) {
    // define variable
    private lateinit var dictViewModel: DictViewModel
    private lateinit var rvSearchSugWordAdapter: DictSearchSugWordAdapter

    // define widget
    private lateinit var mcvSearchBox : MaterialCardView
    private lateinit var tvSearchBox : TextView
    private lateinit var tvDaySentence : TextView
    private lateinit var tvGrammyCheck : TextView
    private lateinit var tvWordList : TextView
    private lateinit var ivMoreButton : ImageView
    private lateinit var ivBackButton : ImageView
    private lateinit var tvTitle : TextView
    private lateinit var ivVoice : ImageView
    private lateinit var rvSearchSuggestion : RecyclerView
    private lateinit var ivDinosaur : ImageView
    private lateinit var vHolder: View

    override fun bindViews() {
        mcvSearchBox = binding.mcvSearchBox
        tvSearchBox = binding.tvSearchBox
        tvDaySentence = binding.tvDaySentence
        tvGrammyCheck = binding.tvGrammyCheck
        tvWordList = binding.tvWordList
        tvTitle = binding.includeTitleBarSecond.tvTitle
        ivBackButton = binding.includeTitleBarSecond.ivBackButton
        ivMoreButton = binding.includeTitleBarSecond.ivMoreButton
        ivVoice = binding.ivVoice
        rvSearchSuggestion = binding.rvSearchSuggestion
        vHolder = binding.vHolder
        ivDinosaur = binding.ivDinosaur
    }

    override fun initViews() {
        dictViewModel = (activity as DictActivity).getDictViewModel()
        tvTitle.text = getString(R.string.easy_dict)
        ivMoreButton.visibility = View.GONE
        ivDinosaur.setImageResource(resources.getIdentifier("dinosaur_${TimeUtils.getDayOfMonth() % 12}", "drawable", mContext.packageName))
        rvSearchSugWordAdapter = DictSearchSugWordAdapter(ArrayList())
        rvSearchSuggestion.adapter = rvSearchSugWordAdapter
        rvSearchSuggestion.layoutManager =
            LinearLayoutManager(requireActivity()).apply { orientation = LinearLayoutManager.HORIZONTAL }
        dictViewModel.dictSearchSugWord.observe(this){
            rvSearchSugWordAdapter.setData(it)
        }
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(vHolder){ view, insets ->
            val displayCutout = insets.displayCutout
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + SizeUtils.dp2px(56f)
            params.bottomMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            when (requireActivity().screenRotation){
                90 -> {
                    params.leftMargin = displayCutout?.safeInsetLeft ?: insets.getInsets(WindowInsetsCompat.Type.systemBars()).left
                    params.rightMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).right
                }
                270 -> {
                    params.rightMargin = displayCutout?.safeInsetRight ?: insets.getInsets(WindowInsetsCompat.Type.systemBars()).right
                    params.leftMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).left
                }
            }
            insets
        }
        rvSearchSugWordAdapter.setOnItemClickListener(object : DictSearchSugWordAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                mListener.onFragmentInteraction("toDetailFragment", dictViewModel.dictSearchSugWord.value!![position].origin)
            }
        })
        tvSearchBox.setOnClickListener {
            mListener.onFragmentInteraction("toDictSearchFragment")
        }
        ivBackButton.setOnClickListener{
            mListener.onFragmentInteraction("finishActivity")
        }
        ivVoice.setOnClickListener {
            mListener.onFragmentInteraction("voiceSearch")
        }
        tvDaySentence.setOnClickListener {
            startActivity(Intent(mContext, DaySentenceActivity::class.java))
        }
        tvGrammyCheck.setOnClickListener {
            startActivity(Intent(mContext, GrammarCheckActivity::class.java))
        }
        tvWordList.setOnClickListener {
            startActivity(Intent(mContext, WordListActivity::class.java))
        }
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