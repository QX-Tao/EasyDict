package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.Typo
import com.qxtao.easydict.utils.LinkClickMovementMethod
import com.qxtao.easydict.utils.common.ColorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class DictTypoAdapter(private val items: ArrayList<Typo>) :
    RecyclerView.Adapter<DictTypoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_typo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Typo = items[position]
        holder.textLang.text = item.lang

        val span = SpannableString(item.word!!)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                DictActivity.onSearchStr(holder.itemView.context, item.word)
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ColorUtils.colorTertiary(holder.itemView.context)
            }
        }
        span.setSpan(clickableSpan, 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.textWord.text = span
        holder.textWord.movementMethod = LinkClickMovementMethod()
    }

    fun setData(data: List<Typo>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                items.clear()
                items.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textLang: TextView = itemView.findViewById(R.id.tv_item_lang)
        var textWord: TextView = itemView.findViewById(R.id.tv_item_word)
    }

}
