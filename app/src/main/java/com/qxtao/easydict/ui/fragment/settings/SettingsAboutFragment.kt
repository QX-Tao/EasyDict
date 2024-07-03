package com.qxtao.easydict.ui.fragment.settings

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.settings.OpenSourceAdapter
import com.qxtao.easydict.databinding.FragmentSettingsAboutBinding
import com.qxtao.easydict.ui.activity.settings.SETTINGS_APP_UPDATE
import com.qxtao.easydict.ui.activity.settings.SettingsActivity
import com.qxtao.easydict.ui.activity.settings.SettingsViewModel
import com.qxtao.easydict.ui.activity.web.WebActivity
import com.qxtao.easydict.ui.base.BaseFragment

class SettingsAboutFragment : BaseFragment<FragmentSettingsAboutBinding>(FragmentSettingsAboutBinding::inflate) {
    // define variable
    private lateinit var settingsViewModel: SettingsViewModel
    // define widget
    private lateinit var clAppPackageInfo : ConstraintLayout
    private lateinit var clAppIconSource : ConstraintLayout
    private lateinit var clAppOpenSource : ConstraintLayout
    private lateinit var tvAppPackageName : TextView
    private lateinit var tvAppPackageInfo : TextView
    private lateinit var tvAppPackageVersion : TextView
    private lateinit var tvAppFoundNewVersion : TextView
    private lateinit var rvOpenSource: RecyclerView
    private lateinit var mtTitle : MaterialToolbar

    override fun bindViews() {
        clAppPackageInfo = binding.clAppPackageInfo
        clAppIconSource = binding.clAppIconSource
        clAppOpenSource = binding.clAppOpenSource
        tvAppPackageName = binding.tvAppPackageName
        tvAppPackageInfo = binding.tvAppPackageInfo
        tvAppPackageVersion = binding.tvAppPackageVersion
        tvAppFoundNewVersion = binding.tvAppFoundNewVersion
        rvOpenSource = binding.rvOpenSource
        mtTitle = binding.mtTitle
    }

    override fun initViews() {
        settingsViewModel = (activity as SettingsActivity).getSettingsViewModel()
        tvAppPackageName.text = requireActivity().packageManager.getApplicationLabel(requireActivity().applicationInfo)
        tvAppPackageInfo.text = requireActivity().packageName
        val currentVersionName = getString(R.string.app_version, requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName)
        tvAppPackageVersion.text = currentVersionName
        rvOpenSource.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        settingsViewModel.openSourceItems.observe(this){
            rvOpenSource.adapter = OpenSourceAdapter(it)
        }
        settingsViewModel.appUpdateDataItems.observe(this){
            tvAppFoundNewVersion.visibility = if (it[0].name != currentVersionName) View.VISIBLE else View.GONE
        }
        settingsViewModel.getOpenSourceItems()
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener{
            mListener.onFragmentInteraction("onBackPressed")
        }
        clAppPackageInfo.setOnClickListener {
            mListener.onFragmentInteraction("toDetailFragment", SETTINGS_APP_UPDATE)
        }
        clAppOpenSource.setOnClickListener{
            val url = "https://github.com/QX-Tao/EasyDict"
            WebActivity.start(requireActivity(), url)
        }
        clAppIconSource.setOnClickListener{
            val url = "https://www.iconfont.cn"
            WebActivity.start(requireActivity(), url)
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