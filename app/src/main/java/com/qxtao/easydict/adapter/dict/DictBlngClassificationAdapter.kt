package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.dict.TYPE_BLNG_CLASSIFICATION_NORMAL
import com.qxtao.easydict.adapter.dict.TYPE_BLNG_CLASSIFICATION_SELECT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DictBlngClassificationAdapter(dictBlngClassificationItems: MutableList<DictBlngClassificationItem>) :
    RecyclerView.Adapter<DictBlngClassificationAdapter.ViewHolder>() {
    private val dictBlngClassificationItems: MutableList<DictBlngClassificationItem>
    private var itemClickListener: OnItemClickListener? = null

    init {
        this.dictBlngClassificationItems = dictBlngClassificationItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_blng_classification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: DictBlngClassificationItem = dictBlngClassificationItems[position]
        holder.text.text = item.text
        holder.text.setTextColor(ContextCompat.getColor(holder.text.context, R.color.secondTextColor))
        when (item.type){
            TYPE_BLNG_CLASSIFICATION_SELECT -> {
                holder.text.setBackgroundColor(ContextCompat.getColor(holder.text.context, R.color.themeMainColor))
            }
            TYPE_BLNG_CLASSIFICATION_NORMAL -> {
                holder.text.setBackgroundColor(ContextCompat.getColor(holder.text.context, R.color.colorBgLightBlack4))
            }
        }
        holder.text.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<DictBlngClassificationItem>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                dictBlngClassificationItems.clear()
                dictBlngClassificationItems.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelected(position: Int) {
        for (i in dictBlngClassificationItems.indices){
            dictBlngClassificationItems[i].type = TYPE_BLNG_CLASSIFICATION_NORMAL
        }
        dictBlngClassificationItems[position].type = TYPE_BLNG_CLASSIFICATION_SELECT
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelected(text: String) {
        for (i in dictBlngClassificationItems.indices) {
            if (dictBlngClassificationItems[i].text == text) {
                dictBlngClassificationItems[i].type = TYPE_BLNG_CLASSIFICATION_SELECT
            } else {
                dictBlngClassificationItems[i].type = TYPE_BLNG_CLASSIFICATION_NORMAL
            }
        }
        notifyDataSetChanged()
    }

    fun getItem(position: Int): DictBlngClassificationItem {
        return dictBlngClassificationItems[position]
    }

    fun getSelectItemText(position: Int) : String{
        return dictBlngClassificationItems[position].text
    }

    fun getSelectedItemText() : String {
        return dictBlngClassificationItems.find { it.type == TYPE_BLNG_CLASSIFICATION_SELECT }?.text ?: "所有"
    }

    override fun getItemCount(): Int {
        return dictBlngClassificationItems.size
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        itemClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.tv_blng_classification)
    }

    data class DictBlngClassificationItem (
        var type: Int,
        val text: String
    )

}