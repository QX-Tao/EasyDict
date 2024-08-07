package com.qxtao.easydict.ui.fragment.dict

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.adapter.dict.DictEEDictOuterAdapter
import com.qxtao.easydict.databinding.FragmentDictDetailEeBinding
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.base.BaseFragment


class DictDetailEEFragment : BaseFragment<FragmentDictDetailEeBinding>(FragmentDictDetailEeBinding::inflate) {
    // define variable
    private lateinit var dictViewModel: DictViewModel
    private lateinit var rvDictEEDictOuterAdapter: DictEEDictOuterAdapter

    // define widget
    private lateinit var rvEEDict: RecyclerView

    override fun bindViews() {
        rvEEDict = binding.rvEeDict

    }

    override fun initViews() {
        dictViewModel = (activity as DictActivity).getDictViewModel()

        rvDictEEDictOuterAdapter = DictEEDictOuterAdapter(ArrayList())
        rvEEDict.adapter = rvDictEEDictOuterAdapter
        rvEEDict.layoutManager = LinearLayoutManager(requireActivity())
        dictViewModel.eeDictionaryResponse.observe(this){
            if ((it?.word?.trs?.size ?: 0) > 0){
                rvDictEEDictOuterAdapter.setData(it?.word?.trs)
            }
        }
    }

}