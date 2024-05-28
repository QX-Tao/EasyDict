package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.Etym
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictEtymAdapter(private val mItemList: ArrayList<Etym>) :
    RecyclerView.Adapter<DictEtymAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_etym, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Etym = mItemList[position]
        val num = position + 1
        holder.textNum.text = num.toString()

        holder.textTitle.text = item.word
        holder.textTitle.visibility = if (item.word.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.textFrom.text = String.format( holder.itemView.context.getString(R.string.source_from_eeee), item.source)
        holder.textFrom.visibility = if (item.source.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.textDesc.text = item.desc
        holder.textDesc.visibility = if (item.desc.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.textContent.text = item.value?.split("\u000a").let{
            it?.map { it1 -> it1.trim() }
        }?.joinToString("\n")
        holder.textContent.visibility = if (item.value.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setData(data: List<Etym>?) {
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
        val textNum: TextView = itemView.findViewById(R.id.tv_item_num)
        val textTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        val textDesc : TextView = itemView.findViewById(R.id.tv_item_desc)
        val textContent: TextView = itemView.findViewById(R.id.tv_item_content)
        val textFrom : TextView = itemView.findViewById(R.id.tv_item_from)
    }

}

