package com.qxtao.easydict.ui.fragment.dict

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.adapter.dict.DictEEDictOuterAdapter
import com.qxtao.easydict.databinding.FragmentDictDetailEeBinding
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.activity.dict.EE_FRAGMENT
import com.qxtao.easydict.ui.base.BaseFragment


class DictDetailEEFragment : BaseFragment<FragmentDictDetailEeBinding>(FragmentDictDetailEeBinding::inflate) {
    // define variable
    private var isInitView = false
    private lateinit var dictViewModel: DictViewModel
    private lateinit var rvDictEEDictOuterAdapter: DictEEDictOuterAdapter

    // define widget
    private lateinit var nsvContent: NestedScrollView
    private lateinit var rvEEDict: RecyclerView

    override fun bindViews() {
        nsvContent = binding.nsvContent
        rvEEDict = binding.rvEeDict

    }

    override fun initViews() {
        isInitView = true
        dictViewModel = (activity as DictActivity).getDictViewModel()
        rvDictEEDictOuterAdapter = DictEEDictOuterAdapter(ArrayList())
        rvEEDict.adapter = rvDictEEDictOuterAdapter
        rvEEDict.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.eeDictionaryResponse.observe(this){
            if ((it?.word?.trs?.size ?: 0) > 0){
                rvDictEEDictOuterAdapter.setData(it?.word?.trs)
            }
        }
        nsvContent.scrollY = dictViewModel.nsvOffset[EE_FRAGMENT] ?: 0
        isInitView = false
    }

    override fun addListener() {
        ViewCompat.setOnApplyWindowInsetsListener(nsvContent){ view, insets ->
            view.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }
        nsvContent.viewTreeObserver.addOnScrollChangedListener {
            if (!isInitView){
                dictViewModel.setNsvScrollOffset(EE_FRAGMENT, nsvContent.scrollY)
            }
        }

    }


}