package com.qxtao.easydict.ui.fragment.daysentence

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import coil.load
import com.qxtao.easydict.R
import com.qxtao.easydict.database.DailySentenceData
import com.qxtao.easydict.databinding.FragmentDaySentenceBinding
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceActivity
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceViewModel
import com.qxtao.easydict.ui.activity.photoview.PhotoViewActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DaySentenceFragment : BaseFragment<FragmentDaySentenceBinding>(FragmentDaySentenceBinding::inflate) {
    // define variable
    private val monthMap = mapOf("01" to "Jan", "02" to "Feb", "03" to "Mar", "04" to "Apr", "05" to "May",
        "06" to "Jun", "07" to "Jul", "08" to "Aug", "09" to "Sep", "10" to "Oct", "11" to "Nov", "12" to "Dec")
    private lateinit var date: String
    private var d: Int = -1
    private var dailySentenceItem: DailySentenceData.DailySentence? = null
    private lateinit var daySentenceViewModel: DaySentenceViewModel
    // define widget
    private lateinit var lvLoading: LoadingView
    private lateinit var ivDsImage: ImageView
    private lateinit var ivDsSound: ImageView
    private lateinit var ivDsUnfold: ImageView
    private lateinit var tvSentenceCn: TextView
    private lateinit var tvSentenceEn: TextView
    private lateinit var tvDsYear: TextView
    private lateinit var tvDsMonth: TextView
    private lateinit var tvDsDate: TextView
    private lateinit var llLoadingFail: LinearLayout

    fun newInstance(date: String): DaySentenceFragment {
        val args = Bundle()
        args.putString("date", date)
        val fragment = DaySentenceFragment()
        fragment.arguments = args
        return fragment
    }

    private fun getDate(): String {
        return arguments?.getString("date").toString()
    }

    override fun bindViews() {
        lvLoading = binding.lvLoading
        ivDsImage = binding.ivDsImage
        tvSentenceCn = binding.tvSentenceCn
        tvSentenceEn = binding.tvSentenceEn
        ivDsSound = binding.ivDsSound
        ivDsUnfold = binding.ivDsUnfold
        tvDsYear = binding.tvDsYear
        tvDsMonth = binding.tvDsMonth
        tvDsDate = binding.tvDsDate
        llLoadingFail = binding.llLoadingFail
    }

    override fun initViews() {
        daySentenceViewModel = (activity as DaySentenceActivity).getDaySentenceViewModel()
        date = getDate()
        d = TimeUtils.calculateDateDifference(date,"yyyy-MM-dd").toInt()
        prepareDailyData()
        tvDsYear.text = TimeUtils.getDateByPatternAndD(d,"yyyy")
        tvDsMonth.text = monthMap[TimeUtils.getDateByPatternAndD(d,"MM")]
        tvDsDate.text = TimeUtils.getDateByPatternAndD(d,"dd")
        daySentenceViewModel.playPosition.observe(this){
            if (it == d){
                ivDsSound.setImageResource(R.drawable.ic_sound_on)
            } else {
                ivDsSound.setImageResource(R.drawable.ic_sound)
            }
        }
    }

    override fun addListener() {
        ivDsSound.setOnClickListener {daySentenceViewModel.startPlaySound(d, dailySentenceItem!!.ttsUrl) }
        ivDsUnfold.setOnClickListener {
            val pictureList = ArrayList<String>()
            pictureList.add(dailySentenceItem!!.imageUrl)
            PhotoViewActivity.start(requireActivity(), pictureList, 0)
        }
        llLoadingFail.setOnClickListener {
            prepareDailyData()
            llLoadingFail.visibility = View.GONE
        }
    }

    private fun prepareDailyData(reload: Boolean = false) {
        lvLoading.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            dailySentenceItem = daySentenceViewModel.getData(date, reload)
            withContext(Dispatchers.Main) {
                if (dailySentenceItem == null) {
                    llLoadingFail.visibility = View.VISIBLE
                } else {
                    tvSentenceEn.text = dailySentenceItem!!.enSentence
                    tvSentenceCn.text = dailySentenceItem!!.cnSentence
                    withContext(Dispatchers.IO){
                        ivDsImage.load(dailySentenceItem!!.imageUrl){
                            crossfade(true)
                        }
                    }
                    lvLoading.visibility = View.GONE
                }
            }
        }
    }
    fun reloadData() = prepareDailyData(true)
}