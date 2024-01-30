package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.EETranslation
import com.qxtao.easydict.ui.activity.dict.EETranslationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictEEDictOuterAdapter(mItemList: ArrayList<EETranslation>) :
    RecyclerView.Adapter<DictEEDictOuterAdapter.ViewHolder>() {
    private val mItemList: ArrayList<EETranslation>

    init {
        this.mItemList = mItemList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_ee_outer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: EETranslation = mItemList[position]
        val typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/Georgia Italic.ttf")
        holder.tvPos.typeface = typeface
        holder.tvPos.text = item.pos
        item.tr?.let { holder.setInnerData(it) }
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setData(data: List<EETranslation>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemList.clear()
                mItemList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    fun getItem(position: Int) = mItemList[position]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var dictEeDictInnerAdapter : DictEEDictInnerAdapter = DictEEDictInnerAdapter(ArrayList())
        private val rvDictEeInner : RecyclerView = itemView.findViewById(R.id.rv_item_ee_dict)
        val tvPos: TextView = itemView.findViewById(R.id.tv_item_pos)

        init {
            rvDictEeInner.adapter = dictEeDictInnerAdapter
            rvDictEeInner.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun setInnerData(item: List<EETranslationItem>) {
            dictEeDictInnerAdapter.setData(item)
        }

    }

}

