package com.qxtao.easydict.ui.fragment.settings

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.settings.AppUpdateDataAdapter
import com.qxtao.easydict.databinding.FragmentSettingsAppUpdateBinding
import com.qxtao.easydict.ui.activity.settings.SettingsActivity
import com.qxtao.easydict.ui.activity.settings.SettingsViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.LoadFailedView
import com.qxtao.easydict.ui.view.LoadingView

class SettingsAppUpdateFragment : BaseFragment<FragmentSettingsAppUpdateBinding>(FragmentSettingsAppUpdateBinding::inflate) {
    // define variable
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var appUpdateDataAdapter: AppUpdateDataAdapter
    // define widget
    private lateinit var llDisplayEarlierVersion : LinearLayout
    private lateinit var lvLoading : LoadingView
    private lateinit var lvLoadFailed : LoadFailedView
    private lateinit var rvAppUpdateInfo: RecyclerView
    private lateinit var mtTitle : MaterialToolbar
    private lateinit var tvAppUpdateHint : TextView

    override fun bindViews() {
        lvLoading = binding.lvLoading
        lvLoadFailed = binding.lvLoadFailed
        llDisplayEarlierVersion = binding.llDisplayEarlierVersion
        rvAppUpdateInfo = binding.rvAppUpdateInfo
        mtTitle = binding.mtTitle
        tvAppUpdateHint = binding.tvAppUpdateHint
    }

    override fun initViews() {
        settingsViewModel = (activity as SettingsActivity).getSettingsViewModel()
        settingsViewModel.appUpdateDataLoadInfo.observe(this){
            when(it) {
                0 -> {
                    lvLoading.visibility = View.VISIBLE
                    lvLoadFailed.visibility = View.GONE
                }
                1 -> {
                    lvLoading.visibility = View.GONE
                    lvLoadFailed.visibility = View.GONE
                }
                2 -> {
                    lvLoading.visibility = View.GONE
                    lvLoadFailed.visibility = View.VISIBLE
                }
            }
        }
        appUpdateDataAdapter = AppUpdateDataAdapter(ArrayList())
        rvAppUpdateInfo.adapter = appUpdateDataAdapter
        rvAppUpdateInfo.layoutManager = LinearLayoutManager(requireActivity())
        settingsViewModel.appUpdateDataItems.observe(this){
            llDisplayEarlierVersion.visibility = if (it.size > 1) View.VISIBLE else View.GONE
            tvAppUpdateHint.text = getAppUpdateHintText()
            appUpdateDataAdapter.setData(it.take(1))
        }
        settingsViewModel.getAppUpdateData()
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener{
            mListener.onFragmentInteraction("onBackPressed")
        }
        lvLoadFailed.setOnClickListener { settingsViewModel.getAppUpdateData() }
        llDisplayEarlierVersion.setOnClickListener {
            appUpdateDataAdapter.setData(settingsViewModel.appUpdateDataItems.value)
            llDisplayEarlierVersion.visibility = View.GONE
        }
    }

    private fun getAppUpdateHintText(): String{
        val latestVersionName = settingsViewModel.appUpdateDataItems.value?.get(0)?.name
        val currentVersionName = getString(R.string.app_version, requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName)
        return when {
            latestVersionName == currentVersionName -> getString(R.string.current_version_is_latest, currentVersionName)
            else -> getString(R.string.found_new_version_desc, currentVersionName, latestVersionName)
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