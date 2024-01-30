package com.qxtao.easydict.adapter.wordlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class WordListAdapter(wordListItems: ArrayList<WordListItem>) : RecyclerView.Adapter<WordListAdapter.ViewHolder>() {
    private val wordListItems: ArrayList<WordListItem>
    private var textMeanClickListener: OnTextMeanClickListener? = null
    private var textWordClickListener: OnTextWordClickListener? = null

    init {
        this.wordListItems = wordListItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_word_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: WordListItem = wordListItems[position]
        holder.textWord.text = item.wordName
        holder.textMean.text = item.wordMean.replace("\\|", "\n")
        holder.vShade.visibility = if (item.isOnClick) View.GONE else View.VISIBLE
        holder.textMean.setOnClickListener {
            textMeanClickListener?.onTextMeanClick(position)
        }
        holder.textWord.setOnClickListener {
            textWordClickListener?.onTextWordClick(position)
        }
        holder.imageSearch.setOnClickListener {
            DictActivity.onSearchStr(holder.itemView.context, holder.textWord.text.toString())
        }
    }

    override fun getItemCount(): Int {
        return wordListItems.size
    }


    interface OnTextMeanClickListener {
        fun onTextMeanClick(position: Int)
    }

    fun setOnTextMeanClickListener(listener: OnTextMeanClickListener) {
        textMeanClickListener = listener
    }

    interface OnTextWordClickListener {
        fun onTextWordClick(position: Int)
    }

    fun  setOnTextWordClickListener(listener: OnTextWordClickListener) {
        textWordClickListener = listener
    }

    fun setData(data: List<WordListItem>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                wordListItems.clear()
                wordListItems.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    fun removeItem(position: Int) {
        wordListItems.removeAt(position)
        notifyItemRemoved(position)
    }

    fun  restoreRemoveItem(position: Int, removeItem: WordListItem) {
        wordListItems.add(position, removeItem)
        notifyItemInserted(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textWord: TextView = itemView.findViewById(R.id.tv_list_word)
        val imageSearch: ImageView = itemView.findViewById(R.id.iv_list_search)
        val textMean: TextView = itemView.findViewById(R.id.tv_list_mean)
        val vShade: View = itemView.findViewById(R.id.v_mean_shade)
    }
}