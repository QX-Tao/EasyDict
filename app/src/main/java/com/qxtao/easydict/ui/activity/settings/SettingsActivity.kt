package com.qxtao.easydict.ui.activity.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.qxtao.easydict.R
import com.qxtao.easydict.database.SearchHistoryData
import com.qxtao.easydict.database.WordBookData
import com.qxtao.easydict.database.WordListData
import com.qxtao.easydict.databinding.ActivitySettingsBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.fragment.settings.SettingsMainFragment
import com.qxtao.easydict.utils.constant.ActionConstant.ACTION_CLOSE_QUICK_SEARCH_NOTIFICATION
import com.qxtao.easydict.utils.constant.ActionConstant.ACTION_CREATE_QUICK_SEARCH_NOTIFICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : BaseActivity<ActivitySettingsBinding>(ActivitySettingsBinding::inflate),
    BaseFragment.OnFragmentInteractionListener{
    // define variable
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var callback: OnBackPressedCallback

    override fun onCreate1(savedInstanceState: Bundle?) {
        super.onCreate1(savedInstanceState)
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        if (savedInstanceState == null) { toMainFragment()  }
    }

    override fun onCreate() {
        dispatcher = onBackPressedDispatcher
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 判断是否只有一个fragment 如果是则finish 不是则pop
                if (supportFragmentManager.backStackEntryCount == 1) {
                    finish()
                } else {
                    supportFragmentManager.popBackStack(null, 0)
                }
            }
        }
        dispatcher.addCallback(this, callback)
    }

    override fun onFragmentInteraction(vararg data: Any?) {
        if (data.isNotEmpty()){
            when(data[0]){
                "onBackPressed" -> dispatcher.onBackPressed()
                "toDetailFragment" -> toDetailFragment(data[1] as String)
                "clearHistory" -> showClearHistoryDialog()
                "clearWordBook" -> showClearWordBookDialog()
                "clearWordList" -> showClearWordListDialog()
                "showQuickSearchNotification" -> sendBroadcast(Intent(ACTION_CREATE_QUICK_SEARCH_NOTIFICATION).apply { setPackage(packageName)})
                "cancelQuickSearchNotification" -> sendBroadcast(Intent(ACTION_CLOSE_QUICK_SEARCH_NOTIFICATION).apply { setPackage(packageName)})
            }
        }
    }

    fun getSettingsViewModel(): SettingsViewModel = settingsViewModel

    private fun toMainFragment() {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.settings_fragment, SettingsMainFragment::class.java, null)
            .addToBackStack(null)
            .commit()
    }

    private fun toDetailFragment(sData: String) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            replace(R.id.settings_fragment, settingsViewModel.detailFragmentMap[sData]!!, null)
            addToBackStack(null)
        }
    }

    private fun showClearHistoryDialog(){
        val dialog = AlertDialog.Builder(mContext)
            .setTitle(R.string.hint)
            .setMessage(R.string.clear_history_desc1)
            .setCancelable(true)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val searchHistoryData = SearchHistoryData(mContext)
                CoroutineScope(Dispatchers.IO).launch {
                    val result = async{ searchHistoryData.deleteAllSearchRecords() }.await()
                    withContext(Dispatchers.Main) {
                        showShortToast(getString(if (result) R.string.clear_history_success else R.string.operation_failure))
                        searchHistoryData.close()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.show()
    }
    private fun showClearWordBookDialog(){
        val dialog = AlertDialog.Builder(mContext)
            .setTitle(R.string.hint)
            .setMessage(R.string.clear_word_book_desc1)
            .setCancelable(true)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val wordBookData = WordBookData(mContext)
                CoroutineScope(Dispatchers.IO).launch {
                    val result = async{ wordBookData.deleteAllWordBooks() }.await()
                    withContext(Dispatchers.Main) {
                        showShortToast(getString(if (result) R.string.clear_word_book_success else R.string.operation_failure))
                        wordBookData.close()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.show()
    }
    private fun showClearWordListDialog(){
        val dialog = AlertDialog.Builder(mContext)
            .setTitle(R.string.hint)
            .setMessage(R.string.reload_list_desc)
            .setCancelable(true)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val wordListData = WordListData(mContext)
                CoroutineScope(Dispatchers.IO).launch {
                    val result = async{ wordListData.updateConfig(isConfig = false) }.await()
                    withContext(Dispatchers.Main) {
                        showShortToast(getString(if (result) R.string.clear_word_list_success else R.string.operation_failure))
                        wordListData.close()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.show()
    }


}