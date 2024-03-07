package com.qxtao.easydict.adapter.wordbook

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.qxtao.easydict.R
import com.qxtao.easydict.database.WordBookData
import com.qxtao.easydict.ui.activity.dict.DictActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class WordBookDetailAdapter(bookWordList: ArrayList<WordBookData.Word>) : RecyclerView.Adapter<WordBookDetailAdapter.ViewHolder>() {
    private val bookWordList: ArrayList<WordBookData.Word>
    var multiSelectedList = mutableSetOf<Int>()
    private var inMultiSelectMode = false
    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    init {
        this.bookWordList = bookWordList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_word_book_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = bookWordList[position]
        holder.textOrigin.text = item.word
        holder.textTranslation.text = item.translation
        holder.viewBackground.setBackgroundColor(holder.itemView.context.getColor(if(multiSelectedList.contains(position)) R.color.colorBgWhite2 else R.color.colorBgWhite1))
        if (inMultiSelectMode){
            holder.layoutContent.setOnClickListener {
                if (multiSelectedList.contains(position)){
                    multiSelectedList.remove(position)
                } else {
                    multiSelectedList.add(position)
                }
                notifyDataSetChanged()
                onItemClickListener?.onItemClick(position)
            }
        } else {
            holder.layoutContent.setOnClickListener {
                DictActivity.onSearchStr(holder.itemView.context, item.word)
            }
            holder.layoutContent.setOnLongClickListener {
                multiSelectedList.add(position)
                notifyDataSetChanged()
                onItemLongClickListener?.onItemLongClick(position)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return bookWordList.size
    }

    fun exitMultiSelectMode(){
        inMultiSelectMode = false
        multiSelectedList.clear()
        notifyDataSetChanged()
    }
    fun joinMultiSelectMode() {
        inMultiSelectMode = true
        notifyDataSetChanged()
    }

    private fun selectAll(){
        if (inMultiSelectMode) {
            multiSelectedList.clear()
            bookWordList.forEachIndexed { index, _ ->
                multiSelectedList.add(index)
            }
        }
        notifyDataSetChanged()
    }
    private fun unselectAll(){
        if (inMultiSelectMode){
            multiSelectedList.clear()
        }
        notifyDataSetChanged()
    }
    fun selectOrUnselectAll(){
        if (inMultiSelectMode) {
            if (isSelectedAll()) {
                unselectAll()
            } else {
                selectAll()
            }
        }
    }

    fun isSelectedAll(): Boolean = multiSelectedList.size == bookWordList.size

    fun getItem(position: Int): WordBookData.Word = bookWordList[position]

    fun getSelectedCount(): Int = multiSelectedList.size

    fun getSelectedItems(): List<String> = multiSelectedList.map { bookWordList[it].word }


    fun setData(data: List<WordBookData.Word>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                bookWordList.clear()
                bookWordList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutContent: LinearLayout = itemView.findViewById(R.id.ll_item_root)
        val viewBackground: View = itemView.findViewById(R.id.v_background)
        val textOrigin: TextView = itemView.findViewById(R.id.tv_origin_text)
        val textTranslation: TextView = itemView.findViewById(R.id.tv_translation)
    }
}