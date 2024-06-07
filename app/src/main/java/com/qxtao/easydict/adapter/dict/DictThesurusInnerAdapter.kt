package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.Color
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.ThesaurusEntry
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.factory.fixTextSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictThesurusInnerAdapter(private val mItemThesaurusList: ArrayList<ThesaurusEntry>) :
    RecyclerView.Adapter<DictThesurusInnerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_thesurus_inner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ThesaurusEntry = mItemThesaurusList[position]
        val num = position + 1
        holder.textNum.text = num.toString()
        holder.textExplain.text = String.format(holder.itemView.context.getString(R.string.explain_with_eee), item.tran)
        if ((item.syno?.size ?: 0) > 0){
            val synoList = mutableListOf<String>()
            for (i in 0 until item.syno!!.size){
                item.syno[i].w?.let { synoList.add(it) }
            }
            val synoText = Html.fromHtml(synoList.joinToString("</u> / <u>","<u>", "</u>"), Html.FROM_HTML_MODE_COMPACT)
            val spannable = SpannableString(synoText).also {
                if (it.isNotBlank()){
                    for (spa in it.getSpans(0, it.length, Any::class.java)){
                        if (spa is UnderlineSpan){
                            val clickableSpan = object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    DictActivity.onSearchStr(holder.itemView.context, it.substring(it.getSpanStart(spa), it.getSpanEnd(spa)))
                                }
                                override fun updateDrawState(ds: TextPaint) {
                                    ds.isUnderlineText = false
                                    ds.color = ColorUtils.colorTertiary(holder.itemView.context)
                                }
                            }
                            it.setSpan(clickableSpan, synoText.getSpanStart(spa), synoText.getSpanEnd(spa), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
            holder.textSyno.movementMethod = LinkMovementMethod.getInstance()
            holder.textSyno.text = spannable
        } else {
            holder.textSyno.visibility = View.GONE
            holder.textSynoTag.visibility = View.GONE
        }
        if ((item.anto?.size ?: 0) > 0){
            val  antoList = mutableListOf<String>()
            for (i in 0 until item.anto!!.size){
                item.anto[i].w?.let { antoList.add(it) }
            }
            val antoText = Html.fromHtml(antoList.joinToString("</u> / <u>","<u>", "</u>"), Html.FROM_HTML_MODE_COMPACT)
            val spannable = SpannableString(antoText).also {
                if (it.isNotBlank()){
                    for (spa in it.getSpans(0, it.length, Any::class.java)){
                        if (spa is UnderlineSpan){
                            val clickableSpan = object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    DictActivity.onSearchStr(holder.itemView.context, it.substring(it.getSpanStart(spa), it.getSpanEnd(spa)))
                                }
                                override fun updateDrawState(ds: TextPaint) {
                                    ds.isUnderlineText = false
                                    ds.color = ColorUtils.colorTertiary(holder.itemView.context)
                                }
                            }
                            it.setSpan(clickableSpan, antoText.getSpanStart(spa), antoText.getSpanEnd(spa), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
            holder.textAnto.movementMethod = LinkMovementMethod.getInstance()
            holder.textAnto.text = spannable
        } else {
            holder.textAnto.visibility = View.GONE
            holder.textAntoTag.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return mItemThesaurusList.size
    }

    fun setData(data: List<ThesaurusEntry>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemThesaurusList.clear()
                mItemThesaurusList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    fun getItem(position: Int) = mItemThesaurusList[position]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textNum: TextView = itemView.findViewById(R.id.tv_item_num)
        var textExplain: TextView = itemView.findViewById(R.id.tv_item_explain)
        var textSyno: TextView = itemView.findViewById(R.id.tv_item_syno)
        var textAnto : TextView = itemView.findViewById(R.id.tv_item_anto)
        var textSynoTag : TextView = itemView.findViewById(R.id.tv_item_syno_tag)
        var textAntoTag : TextView = itemView.findViewById(R.id.tv_item_anto_tag)
    }

}

