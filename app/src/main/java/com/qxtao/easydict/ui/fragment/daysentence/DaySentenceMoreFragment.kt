package com.qxtao.easydict.ui.fragment.daysentence

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dailysentence.DailySentenceAdapter
import com.qxtao.easydict.adapter.dailysentence.DailySentenceItem
import com.qxtao.easydict.databinding.FragmentDaySentenceMoreBinding
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceActivity
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceViewModel
import com.qxtao.easydict.ui.activity.photoview.PhotoViewActivity
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.factory.isLandscape
import com.qxtao.easydict.utils.factory.screenRotation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DaySentenceMoreFragment :  BaseFragment<FragmentDaySentenceMoreBinding>(
    FragmentDaySentenceMoreBinding::inflate)  {
    // define variable
    private lateinit var adapter: DailySentenceAdapter
    private lateinit var daySentenceViewModel: DaySentenceViewModel
    private var isInitView = false
    // define widget
    private lateinit var rvDayMore: RecyclerView
    private lateinit var vHolder: View

    override fun bindViews() {
        rvDayMore = binding.rvDayMore
        vHolder = binding.vHolder
    }

    override fun initViews() {
        daySentenceViewModel = (activity as DaySentenceActivity).getDaySentenceViewModel()
        isInitView = true
        rvDayMore.layoutManager = LinearLayoutManager(mContext)
        adapter = DailySentenceAdapter(ArrayList())
        daySentenceViewModel.dataList.observe(this) {
            adapter.setData(it)
        }
        rvDayMore.adapter = adapter
        daySentenceViewModel.playPosition.observe(this){
            if (it != -1) {
                adapter.setPositionPlayingSound(it)
            } else {
                adapter.resetPlaySound()
            }
        }
        daySentenceViewModel.isDoubleClickHeader.observe(this){
            if (it) {
                rvDayMore.smoothScrollToPosition(0)
                daySentenceViewModel.isDoubleClickHeader.value = false
            }
        }
        daySentenceViewModel.dataLoadInfo.observe(this) {
            when(it){
                2 -> { adapter.setLoadingFail(true) }
            }
        }
        val lm = rvDayMore.layoutManager as? LinearLayoutManager ?: return
        CoroutineScope(Dispatchers.Main).launch {
            lm.scrollToPositionWithOffset(daySentenceViewModel.firstVisibleItemPosition, daySentenceViewModel.topOffset)
            isInitView = false
        }
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(vHolder){ view, insets ->
            val displayCutout = insets.displayCutout
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            rvDayMore.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            params.topMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + SizeUtils.dp2px(56f)
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
        adapter.setOnPlayButtonClickListener(object : DailySentenceAdapter.OnPlayButtonClickListener {
            override fun onPlayButtonClick(position: Int) {
                val item: DailySentenceItem = daySentenceViewModel.getData()[position]
                daySentenceViewModel.startPlaySound(position, item.ttsUrl)
            }
        })
        adapter.setOnLoadingFailClickListener(object : DailySentenceAdapter.OnLoadingFailClickListener{
            override fun onLoadingFailClick(position: Int) {
                if (!daySentenceViewModel.isPullingData) {
                    pullData()
                }
            }
        })
        adapter.setOnPhotoViewButtonClickListener(object : DailySentenceAdapter.OnPhotoViewButtonClickListener{
            override fun onPhotoViewButtonClick(position: Int) {
                val pictureList = ArrayList<String>()
                daySentenceViewModel.dataList.let {
                    for (i in 0 until it.value!!.size - 1){
                        pictureList.add(it.value!![i].imageUrl)
                    }
                }
                PhotoViewActivity.start(requireActivity(), pictureList, position)
            }
        })
        adapter.setOnItemLongClickListener(object : DailySentenceAdapter.OnItemLongClickListener{
            override fun onItemLongClick(position: Int) {
                val cm: ClipboardManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText(null, daySentenceViewModel.dataList.value!![position].enSentence
                        + "\n" + daySentenceViewModel.dataList.value!![position].cnSentence))
                showShortToast(getString(R.string.copied))
            }
        })
        rvDayMore.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (!isInitView) {
                    daySentenceViewModel.firstVisibleItemPosition =
                        lm.findFirstVisibleItemPosition()
                    daySentenceViewModel.topOffset =
                        lm.findViewByPosition(daySentenceViewModel.firstVisibleItemPosition)?.top
                            ?: 0
                }
                val lastVisibleItem = lm.findLastVisibleItemPosition()
                val totalItemCount = lm.itemCount
                if (lastVisibleItem == totalItemCount - 1 && !daySentenceViewModel.isLoadingNextPage) {
                    val findLastVisibleItemPosition = lm.findLastVisibleItemPosition()
                    if (findLastVisibleItemPosition == lm.itemCount - 1 ) {
                        if (!daySentenceViewModel.isPullingData) {
                            if (daySentenceViewModel.dataLoadInfo.value == 3){
                                showShortToast(getString(R.string.loaded_all_data))
                            } else pullData()
                        }
                    }
                }
            }
        })
    }

    private fun pullData() {
        adapter.setLoadingFail(false)
        lifecycleScope.launch(Dispatchers.IO) {
            daySentenceViewModel.isLoadingNextPage = true
            daySentenceViewModel.isPullingData = true
            daySentenceViewModel.getData(dataSize = 10)
            daySentenceViewModel.isLoadingNextPage = false
            daySentenceViewModel.isPullingData = false
        }
    }

}