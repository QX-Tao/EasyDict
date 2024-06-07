package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.SpecialTrs
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.factory.fixTextSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictSpecialInnerAdapter(private val mItemList: ArrayList<SpecialTrs>) :
    RecyclerView.Adapter<DictSpecialInnerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_special_inner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: SpecialTrs = mItemList[position]
        holder.textTrans.text = item.tr?.nat
        holder.textTrans.visibility = if (item.tr?.nat.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.textTransTimes.text = String.format(holder.itemView.context.getString(R.string.cited_times_eee), item.tr?.cite)
        holder.textTransTimes.visibility = if (item.tr?.cite.isNullOrBlank() || item.tr?.cite == "0") View.GONE else View.VISIBLE

        val engSentence = item.tr?.engSent?.also {
            val engSentenceText = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
            val engSentenceSpannable = SpannableString(engSentenceText)
            for (span in engSentenceText.getSpans(0, engSentenceText.length, Any::class.java)){
                if (span is StyleSpan && span.style == Typeface.BOLD){
                    engSentenceSpannable.removeSpan(span)
                    engSentenceSpannable.setSpan(
                        ForegroundColorSpan(ColorUtils.colorPrimary(holder.itemView.context)),
                        engSentenceText.getSpanStart(span), engSentenceText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            holder.textSents.text = engSentenceSpannable
        }
        holder.textSents.visibility = if (engSentence.isNullOrBlank()) View.GONE else View.VISIBLE

        val chnSentence = item.tr?.chnSent?.also{
            val chnSentenceText = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
            val chnSentenceSpannable = SpannableString(chnSentenceText)
            for (span in chnSentenceText.getSpans(0, chnSentenceText.length, Any::class.java)){
                if (span is StyleSpan && span.style == Typeface.BOLD){
                    chnSentenceSpannable.removeSpan(span)
                    chnSentenceSpannable.setSpan(
                        ForegroundColorSpan(ColorUtils.colorPrimary(holder.itemView.context)),
                        chnSentenceText.getSpanStart(span), chnSentenceText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            holder.textSentsTrans.text = chnSentenceSpannable

        }
        holder.textSentsTrans.visibility = if (chnSentence.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.textSource.text = String.format(holder.itemView.context.getString(R.string.source_from_eeee), item.tr?.docTitle)
        holder.textSource.visibility = if (item.tr?.docTitle.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.textTrans.fixTextSelection()
        holder.textSents.fixTextSelection()
        holder.textSentsTrans.fixTextSelection()
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }


    fun setData(data: List<SpecialTrs>?) {
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
        val textTrans: TextView = itemView.findViewById(R.id.tv_item_trans)
        val textTransTimes: TextView = itemView.findViewById(R.id.tv_item_trans_times)
        val textSents: TextView = itemView.findViewById(R.id.tv_item_sents)
        val textSentsTrans : TextView = itemView.findViewById(R.id.tv_item_sents_trans)
        val textSource : TextView = itemView.findViewById(R.id.tv_item_source)
    }

}

