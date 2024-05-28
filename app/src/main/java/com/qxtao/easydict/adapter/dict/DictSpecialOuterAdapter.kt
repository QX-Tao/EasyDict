package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.SpecialEntries
import com.qxtao.easydict.ui.activity.dict.SpecialTrs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictSpecialOuterAdapter(
    private val mItemSpecialEntriesList: ArrayList<SpecialEntries>,
    private val take: Int? = null
) :
    RecyclerView.Adapter<DictSpecialOuterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_special_outer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: SpecialEntries = mItemSpecialEntriesList[position]
        val num = position + 1
        holder.textNum.text = num.toString()
        holder.textMajor.text = item.entry?.major
        holder.textMajor.visibility = if (item.entry?.major.isNullOrEmpty()) View.GONE else View.VISIBLE
        item.entry?.trs?.let { holder.setInnerData(it, take) }
    }

    override fun getItemCount(): Int {
        return mItemSpecialEntriesList.size
    }

    fun setData(data: List<SpecialEntries>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemSpecialEntriesList.clear()
                mItemSpecialEntriesList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    fun getItem(position: Int) = mItemSpecialEntriesList[position]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var dictSpecialInnerAdapter : DictSpecialInnerAdapter = DictSpecialInnerAdapter(ArrayList())
        var textNum: TextView = itemView.findViewById(R.id.tv_item_num)
        var textMajor: TextView = itemView.findViewById(R.id.tv_item_major)
        private val rvDictSpecialInner : RecyclerView = itemView.findViewById(R.id.rv_item_special)

        init {
            rvDictSpecialInner.adapter = dictSpecialInnerAdapter
            rvDictSpecialInner.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun setInnerData(item: List<SpecialTrs>, take: Int?) {
            dictSpecialInnerAdapter.setData(item.take(take ?: item.size))
        }

    }

}

