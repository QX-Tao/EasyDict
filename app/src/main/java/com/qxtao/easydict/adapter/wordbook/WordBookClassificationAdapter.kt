package com.qxtao.easydict.adapter.wordbook

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class WordBookClassificationAdapter(private val bookNameList: ArrayList<String>) : RecyclerView.Adapter<WordBookClassificationAdapter.ViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_word_book_classification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = bookNameList[position]
        holder.textBookName.text = item
        holder.cardBookList.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return bookNameList.size
    }

    interface  OnItemClickListener {
         fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
         onItemClickListener = listener
    }

    fun getBookName(position: Int) = bookNameList[position]

    fun setData(data: List<String>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                bookNameList.clear()
                bookNameList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textBookName: TextView = itemView.findViewById(R.id.tv_book_name)
        val cardBookList: MaterialCardView = itemView.findViewById(R.id.mcv_word_book_list)
    }
}