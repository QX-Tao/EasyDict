package com.qxtao.easydict.ui.activity.daysentence

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.qxtao.easydict.R
import com.qxtao.easydict.database.DailySentenceData
import com.qxtao.easydict.databinding.ActivityDaySentenceBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.fragment.daysentence.DaySentenceMoreFragment
import com.qxtao.easydict.ui.fragment.daysentence.DaySentenceDetailFragment
import com.qxtao.easydict.ui.fragment.daysentence.DaySentenceOneFragment
import com.qxtao.easydict.ui.view.OnDoubleClickListener
import com.qxtao.easydict.ui.view.OnDoubleClickListener.DoubleClickCallback
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.factory.isAppearanceLight


class DaySentenceActivity : BaseActivity<ActivityDaySentenceBinding>(ActivityDaySentenceBinding::inflate),
    BaseFragment.OnFragmentInteractionListener  {
    // define variable
    private val daySentencePagerAdapter by lazy { DaySentencePagerAdapter(this) }
    val daySentenceOnePagerAdapter by lazy { DaySentenceOnePagerAdapter(this) }
    private lateinit var daySentenceViewModel: DaySentenceViewModel
    private lateinit var dailySentenceData: DailySentenceData
    private val d by lazy {
        intent?.getIntExtra("fragmentPosition",0)
    }
    private val dispatcher: OnBackPressedDispatcher = onBackPressedDispatcher
    private val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
    // define widget
    private lateinit var ivBackButton : ImageView
    private lateinit var ivSwitchButton : ImageView
    private lateinit var tvTitle : TextView
    private lateinit var daySentenceViewPager : ViewPager2

    companion object{
        fun start(activity: Activity, d: Int){
            val intent = Intent(activity, DaySentenceActivity::class.java)
            intent.putExtra("fragmentPosition", d)
            activity.startActivity(intent)
        }
    }

    override fun onCreate() {
        isAppearanceLight = false
        dailySentenceData = DailySentenceData(mContext)
        daySentenceViewModel = ViewModelProvider(this, DaySentenceViewModel.Factory(dailySentenceData))[DaySentenceViewModel::class.java]
        dispatcher.addCallback(callback)
    }

    override fun bindViews() {
        tvTitle = binding.tvTitle
        ivBackButton = binding.ivBackButton
        ivSwitchButton = binding.ivSwitchButton
        daySentenceViewPager = binding.daySentenceViewPager
    }

    override fun initViews() {
        tvTitle.text = getString(R.string.daily_sentence)
        daySentenceViewPager.adapter = daySentencePagerAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun addListener() {
        ivBackButton.setOnClickListener { finish() }
        ivSwitchButton.setOnClickListener{
            daySentenceViewPager.currentItem = if (daySentenceViewPager.currentItem == 0) 1 else 0
        }
        tvTitle.setOnTouchListener(OnDoubleClickListener(object : DoubleClickCallback {
            override fun onDoubleClick() {
                if (daySentenceViewPager.currentItem == 1) {
                    daySentenceViewModel.isDoubleClickHeader.value = true
                }
            }
        }))

    }

    override fun onFragmentInteraction(vararg data: Any?) {

    }

    fun getDaySentenceViewModel(): DaySentenceViewModel = daySentenceViewModel

    /**
     * PagerAdapter Class
     * @constructor
     */
    private inner class DaySentencePagerAdapter(fActivity: FragmentActivity) :
        FragmentStateAdapter(fActivity) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int) = when(position){
            0 -> DaySentenceOneFragment().newInstance(d!!)
            1 -> DaySentenceMoreFragment()
            else -> DaySentenceOneFragment()
        }
    }

    inner class DaySentenceOnePagerAdapter(fActivity: FragmentActivity) :
        FragmentStateAdapter(fActivity) {
        override fun getItemCount() = TimeUtils.calculateDateDifference(ORIGIN_DATE, "yyyy-MM-dd").toInt() + 1
        override fun createFragment(position: Int) : Fragment {
            return DaySentenceDetailFragment().newInstance(TimeUtils.getDateByPatternAndD(position,"yyyy-MM-dd"))
        }
    }

    override fun onDestroy1() {
        super.onDestroy1()
        dailySentenceData.close()
        daySentenceViewModel.stopPlaySound()
    }

}