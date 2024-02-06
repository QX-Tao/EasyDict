package com.qxtao.easydict.ui.fragment.dict

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.adapter.dict.DictCollinsOuterAdapter
import com.qxtao.easydict.databinding.FragmentDictDetailCoBinding
import com.qxtao.easydict.ui.activity.dict.CO_FRAGMENT
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.utils.common.SizeUtils


class DictDetailCOFragment : BaseFragment<FragmentDictDetailCoBinding>(FragmentDictDetailCoBinding::inflate) {
    // define variable
    private var isInitView = false
    private lateinit var dictViewModel: DictViewModel
    private lateinit var rvDictCollinsOuterAdapter: DictCollinsOuterAdapter

    // define widget
    private lateinit var nsvContent: NestedScrollView
    private lateinit var rvCollins: RecyclerView

    override fun bindViews() {
        nsvContent = binding.nsvContent
        rvCollins = binding.rvCollins
    }

    override fun initViews() {
        isInitView = true
        dictViewModel = (activity as DictActivity).getDictViewModel()
        rvDictCollinsOuterAdapter = DictCollinsOuterAdapter(ArrayList())
        rvCollins.adapter = rvDictCollinsOuterAdapter
        rvCollins.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.collinsResponse.observe(this){
            if ((it?.collins_entries?.size ?: 0) > 0){
                rvDictCollinsOuterAdapter.setData(it?.collins_entries)
            }
        }
        nsvContent.scrollY = dictViewModel.nsvOffset[CO_FRAGMENT] ?: 0
        isInitView = false
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(nsvContent){ view, insets ->
            view.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }
        nsvContent.viewTreeObserver.addOnScrollChangedListener {
            if (!isInitView){
                dictViewModel.setNsvScrollOffset(CO_FRAGMENT, nsvContent.scrollY)
            }
        }
    }

}