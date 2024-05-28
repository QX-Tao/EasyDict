package com.qxtao.easydict.ui.fragment.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.popupmenu.PopupMenuAdapter
import com.qxtao.easydict.adapter.popupmenu.PopupMenuItem
import com.qxtao.easydict.databinding.FragmentSettingsMainBinding
import com.qxtao.easydict.ui.activity.settings.SETTINGS_KEY_ABOUT
import com.qxtao.easydict.ui.activity.settings.SettingsActivity
import com.qxtao.easydict.ui.activity.settings.SettingsViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.CustomPopWindow
import com.qxtao.easydict.utils.common.ActivityUtils
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.common.ThemeUtils
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.constant.ShareConstant.DARK_MODE
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_SYSTEM_THEME
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_WEB_VIEW
import com.qxtao.easydict.utils.constant.ShareConstant.MODE_NIGHT_FOLLOW_SYSTEM
import com.qxtao.easydict.utils.factory.screenRotation
import org.w3c.dom.Text

class SettingsMainFragment : BaseFragment<FragmentSettingsMainBinding>(FragmentSettingsMainBinding::inflate) {
    // define variable
    private lateinit var settingsViewModel: SettingsViewModel
    // define widget
    private lateinit var vHolder: View
    private lateinit var nsvContent: NestedScrollView
    private lateinit var clAbout: ConstraintLayout
    private lateinit var clAppBrowser: ConstraintLayout
    private lateinit var clClearCache: ConstraintLayout
    private lateinit var clDarkMode: ConstraintLayout
    private lateinit var tvDarkModeDesc: TextView
    private lateinit var tvThemeColorDesc: TextView
    private lateinit var clSystemThemeColor: ConstraintLayout
    private lateinit var clThemeColor: ConstraintLayout
    private lateinit var swAppBrowser: MaterialSwitch
    private lateinit var swSystemThemeColor: MaterialSwitch
    private lateinit var mtTitle: MaterialToolbar
    private lateinit var clClearHistory: ConstraintLayout
    private lateinit var clClearWordBook: ConstraintLayout
    private lateinit var clClearWordList: ConstraintLayout

    override fun bindViews() {
        vHolder = binding.vHolder
        nsvContent = binding.nsvContent
        clAbout = binding.clAbout
        swSystemThemeColor = binding.swSystemThemeColor
        swAppBrowser = binding.swAppBrowser
        clAppBrowser = binding.clAppBrowser
        clClearCache = binding.clClearCache
        clDarkMode = binding.clDarkMode
        clSystemThemeColor = binding.clSystemThemeColor
        clThemeColor = binding.clThemeColor
        tvDarkModeDesc = binding.tvDarkModeDesc
        tvThemeColorDesc = binding.tvThemeColorDesc
        mtTitle = binding.mtTitle
        clClearHistory = binding.clClearHistory
        clClearWordBook = binding.clClearWordBook
        clClearWordList = binding.clClearWordList
    }

    override fun initViews() {
        settingsViewModel = (activity as SettingsActivity).getSettingsViewModel()
        swAppBrowser.isChecked = ShareUtils.getBoolean(mContext, IS_USE_WEB_VIEW, true)
        swSystemThemeColor.isChecked = ShareUtils.getBoolean(mContext, IS_USE_SYSTEM_THEME, false)
        clSystemThemeColor.visibility = if (ThemeUtils.isDynamicColorAvailable()) View.VISIBLE else View.GONE
        clThemeColor.visibility = if (!swSystemThemeColor.isChecked || !ThemeUtils.isDynamicColorAvailable()) View.VISIBLE else View.GONE
        tvDarkModeDesc.text = settingsViewModel.darkModeMap[ThemeUtils.getDarkTheme(mContext)]
        tvThemeColorDesc.text = settingsViewModel.themeColorMap[ThemeUtils.getThemeColor(mContext)]
        settingsViewModel.selectPopupMenuList(settingsViewModel.darkModeList, settingsViewModel.darkModeMap[ThemeUtils.getDarkTheme(mContext)]!!)
        settingsViewModel.selectPopupMenuList(settingsViewModel.themeColorList, settingsViewModel.themeColorMap[ThemeUtils.getThemeColor(mContext)]!!)
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(vHolder){ view, insets ->
            nsvContent.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            val displayCutout = insets.displayCutout
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
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
        mtTitle.setNavigationOnClickListener{
            mListener.onFragmentInteraction("onBackPressed")
        }
        clAbout.setOnClickListener {
            mListener.onFragmentInteraction("toDetailFragment", SETTINGS_KEY_ABOUT)
        }
        clClearCache.setOnClickListener {
            mListener.onFragmentInteraction("clearCache")
        }
        clClearHistory.setOnClickListener {
            mListener.onFragmentInteraction("clearHistory")
        }
        clClearWordBook.setOnClickListener {
            mListener.onFragmentInteraction("clearWordBook")
        }
        clClearWordList.setOnClickListener {
            mListener.onFragmentInteraction("clearWordList")
        }
        clDarkMode.setOnClickListener {
            showPopupMenu(clDarkMode, settingsViewModel.darkModeList).also {
                it.second.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
                    override fun onMenuItemClick(position: Int) {
                        ThemeUtils.setDarkTheme(mContext, settingsViewModel.darkModeMap.keys.toList()[position])
                        if(settingsViewModel.selectPopupMenuList(settingsViewModel.darkModeList, position)){
                            AppCompatDelegate.setDefaultNightMode(settingsViewModel.darkModeMap.keys.toList()[position])
                        }
                        tvDarkModeDesc.text = settingsViewModel.darkModeMap[ThemeUtils.getDarkTheme(mContext)]
                        it.first.dissmiss()
                    }
                })
            }
        }
        clThemeColor.setOnClickListener {
            showPopupMenu(clThemeColor, settingsViewModel.themeColorList).also {
                it.second.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
                    override fun onMenuItemClick(position: Int) {
                        ThemeUtils.setThemeColor(mContext, settingsViewModel.themeColorMap.keys.toList()[position])
                        if(settingsViewModel.selectPopupMenuList(settingsViewModel.themeColorList, position)){
                            ActivityUtils.restartAllActivities()
                        }
                        tvThemeColorDesc.text = settingsViewModel.themeColorMap[ThemeUtils.getThemeColor(mContext)]
                        it.first.dissmiss()
                    }
                })
            }
        }
        clAppBrowser.setOnClickListener { swAppBrowser.isChecked = !swAppBrowser.isChecked }
        swAppBrowser.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_WEB_VIEW, isChecked) }
        clSystemThemeColor.setOnClickListener { swSystemThemeColor.isChecked = !swSystemThemeColor.isChecked }
        swSystemThemeColor.setOnCheckedChangeListener { _, isChecked ->
            ShareUtils.putBoolean(mContext, IS_USE_SYSTEM_THEME, isChecked)
            ActivityUtils.restartAllActivities()
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

    private fun showPopupMenu(view: View, popWindowList: List<PopupMenuItem>): Pair<CustomPopWindow, PopupMenuAdapter>{
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.pop_menu_recycleview, null)
        val popWindow = CustomPopWindow.PopupWindowBuilder(mContext)
            .setView(contentView)
            .enableBackgroundDark(true)
            .create()
            .showAsDropDown(view, view.right, view.top - view.bottom)
        val recycleView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = PopupMenuAdapter(popWindowList)
        recycleView.adapter = adapter
        recycleView.layoutManager = LinearLayoutManager(mContext)
        adapter.selectItem(popWindowList.find { it.isMenuItemSelected }?.position ?: 0)
        adapter.setOnMenuItemClickListener(object : PopupMenuAdapter.OnMenuItemClickListener{
            override fun onMenuItemClick(position: Int) {
                popWindow.dissmiss()
            }
        })
        return Pair(popWindow, adapter)
    }


}