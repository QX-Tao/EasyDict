package com.qxtao.easydict.ui.activity.daysentence

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.qxtao.easydict.R
import com.qxtao.easydict.database.DailySentenceData
import com.qxtao.easydict.databinding.ActivityDaySentenceBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.fragment.daysentence.DaySentenceFragment
import com.qxtao.easydict.utils.common.TimeUtils


class DaySentenceActivity : BaseActivity<ActivityDaySentenceBinding>(ActivityDaySentenceBinding::inflate),
    BaseFragment.OnFragmentInteractionListener  {
    // define variable
    private val daySentencePagerAdapter by lazy { DaySentencePagerAdapter(this) }
    private lateinit var daySentenceViewModel: DaySentenceViewModel
    private lateinit var dailySentenceData: DailySentenceData
    private val dispatcher: OnBackPressedDispatcher = onBackPressedDispatcher
    private val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() { finish() }
    }
    // define widget
    private lateinit var mtTitle : MaterialToolbar
    private lateinit var daySentenceViewPager : ViewPager2

    override fun onCreate() {
        dailySentenceData = DailySentenceData(mContext)
        daySentenceViewModel = ViewModelProvider(this, DaySentenceViewModel.Factory(dailySentenceData))[DaySentenceViewModel::class.java]
        dispatcher.addCallback(callback)
    }

    override fun bindViews() {
        mtTitle = binding.mtTitle
        daySentenceViewPager = binding.daySentenceViewPager
    }

    override fun initViews() {
        daySentenceViewPager.adapter = daySentencePagerAdapter
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener { finish() }
        mtTitle.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.back_to_today -> daySentenceViewPager.setCurrentItem(0, false)
                R.id.jump_date -> showDatePicker()
                R.id.reload -> {
                    val currentItem = daySentenceViewPager.currentItem
                    val cFragment = supportFragmentManager.findFragmentByTag("f$currentItem") as? DaySentenceFragment
                    cFragment?.reloadData()
                }
            }
            true
        }
    }

    override fun onFragmentInteraction(vararg data: Any?) {}

    fun getDaySentenceViewModel(): DaySentenceViewModel = daySentenceViewModel

    private inner class DaySentencePagerAdapter(fActivity: FragmentActivity) :
        FragmentStateAdapter(fActivity) {
        override fun getItemCount() = TimeUtils.calculateDateDifference(ORIGIN_DATE, "yyyy-MM-dd").toInt() + 1
        override fun createFragment(position: Int) : Fragment {
            return DaySentenceFragment().newInstance(TimeUtils.getDateByPatternAndD(position,"yyyy-MM-dd"))
        }
    }

    override fun onDestroy1() {
        super.onDestroy1()
        if (this::dailySentenceData.isInitialized) dailySentenceData.close()
        if (this::daySentenceViewModel.isInitialized) daySentenceViewModel.stopPlaySound()
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        val date = TimeUtils.getDateByPatternAndD(daySentenceViewPager.currentItem,"yyyy-MM-dd")
        builder.setSelection(TimeUtils.getTimestampByFormatDate(date, "yyyy-MM-dd") + TimeUtils.getTimeZoneOffset())
        builder.setCalendarConstraints(CalendarConstraints.Builder()
            .setValidator(CompositeDateValidator.allOf(listOf(
                DateValidatorPointForward.from(TimeUtils.getTimestampByFormatDate(ORIGIN_DATE, "yyyy-MM-dd") + TimeUtils.getTimeZoneOffset()),
                DateValidatorPointBackward.now())))
            .build()
        )
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            val datee = TimeUtils.getFormatDateByTimeStamp(it,"yyyy-MM-dd")
            daySentenceViewPager.setCurrentItem(TimeUtils.calculateDateDifference(datee, "yyyy-MM-dd").toInt(), false)
        }
        picker.show(supportFragmentManager, picker.toString())
    }

}