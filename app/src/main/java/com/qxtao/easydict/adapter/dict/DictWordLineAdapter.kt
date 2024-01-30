package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.database.SearchHistoryData
import com.qxtao.easydict.database.SimpleDictData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class DictWordLineAdapter<T>(mItemData: ArrayList<T>) :
    RecyclerView.Adapter<DictWordLineAdapter<T>.ViewHolder>() {

    private val mItemData: ArrayList<T>
    private var itemClickListener: OnItemClickListener? = null

    init {
        this.mItemData = mItemData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dict_search_wordline, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: T = mItemData[position]
        val word: String
        val translation: String

        when (item) {
            is SimpleDictData.SimpleDict -> {
                word = item.origin
                translation = item.translation.replace("\\|", " ")
            }
            is SearchHistoryData.SearchHistory -> {
                word = item.origin
                translation = item.translation.replace("\\|", " ")
            }
            else -> {
                word = ""
                translation = ""
            }
        }

        holder.textWord.text = word
        holder.textTranslation.text = translation
        holder.itemLayout.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    override fun getItemCount(): Int = mItemData.size

    fun setData(data: List<T>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemData.clear()
                mItemData.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textWord: TextView = itemView.findViewById(R.id.tv_word)
        var textTranslation: TextView = itemView.findViewById(R.id.tv_translation)
        var itemLayout: LinearLayout = itemView.findViewById(R.id.ll_layout)
    }
}

