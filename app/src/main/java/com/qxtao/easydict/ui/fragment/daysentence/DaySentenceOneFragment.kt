package com.qxtao.easydict.ui.fragment.daysentence

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.qxtao.easydict.databinding.FragmentDaySentenceOneBinding
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceActivity
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceViewModel
import com.qxtao.easydict.ui.base.BaseFragment

class DaySentenceOneFragment : BaseFragment<FragmentDaySentenceOneBinding>(FragmentDaySentenceOneBinding::inflate) {
    // define variable
    private lateinit var daySentenceViewModel: DaySentenceViewModel
    // define widget
    private lateinit var viewPager : ViewPager2

    fun newInstance(d: Int): DaySentenceOneFragment {
        val args = Bundle()
        args.putInt("d", d)
        val fragment = DaySentenceOneFragment()
        fragment.arguments = args
        return fragment
    }

    private fun getD(): Int {
        return arguments?.getInt("d") ?: 0
    }

    override fun bindViews() {
        viewPager = binding.viewPager2
    }

    override fun initViews() {
        daySentenceViewModel = (activity as DaySentenceActivity).getDaySentenceViewModel()
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        viewPager.adapter = (activity as DaySentenceActivity).daySentenceOnePagerAdapter
        viewPager.setCurrentItem(getD(), false)
    }
}