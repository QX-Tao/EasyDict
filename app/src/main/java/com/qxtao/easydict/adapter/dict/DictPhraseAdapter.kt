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
import com.qxtao.easydict.ui.activity.dict.Phr
import com.qxtao.easydict.utils.LinkClickMovementMethod
import com.qxtao.easydict.utils.common.ColorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class DictPhraseAdapter(private val phrItems: ArrayList<Phr>) :
    RecyclerView.Adapter<DictPhraseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_phrs, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Phr = phrItems[position]
        val num = position + 1
        holder.textNnm.text = num.toString()

        val span = SpannableString(item.headword!!)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                DictActivity.onSearchStr(holder.itemView.context, item.headword)
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ColorUtils.colorTertiary(holder.itemView.context)
            }
        }
        span.setSpan(clickableSpan, 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.textContent.text = span
        holder.textContent.movementMethod = LinkClickMovementMethod()
    }

    fun setData(data: List<Phr>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                phrItems.clear()
                phrItems.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return phrItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textNnm: TextView = itemView.findViewById(R.id.tv_item_num)
        var textContent: TextView = itemView.findViewById(R.id.tv_item_content)
    }

}
