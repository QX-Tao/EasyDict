package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.Entry
import com.qxtao.easydict.utils.LinkClickMovementMethod
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.SizeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictCollinsInnerAdapter(private val mItemList: ArrayList<Entry>) :
    RecyclerView.Adapter<DictCollinsInnerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_collins_inner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemEntry: Entry = mItemList[position]
        val num = position + 1
        holder.tvNum.text = num.toString()
        itemEntry.tran_entry?.let {
            val tranEntry = itemEntry.tran_entry[0]
            val textPos = tranEntry.pos_entry?.pos?.let { if (it.isNotBlank()) "<i>${it}</i>" else null } ?: ""
            val textTran = tranEntry.tran ?: ""
            val textHeadWord = tranEntry.headword ?: ""
            val textSee = tranEntry.sees?.see?.get(0)?.let {
                if (it.seeword?.isNotBlank() == true) "→ see <u>${it.seeword}</u>" else null } ?: ""
            val textSeeAlso = tranEntry.seeAlsos?.seeAlso?.get(0)?.let {
                if (it.seeword?.isNotBlank() == true) "→ see also <u>${it.seeword}</u>" else null } ?: ""
            val tranTmpText = listOf(textPos, textTran, textHeadWord, textSee, textSeeAlso)
                .filter { it.isNotBlank() }
                .joinToString("&nbsp;&nbsp;")
            val tranText = HtmlCompat.fromHtml(tranTmpText, FROM_HTML_MODE_COMPACT)
            if (tranText.isBlank()){
                holder.tvTran.visibility = View.GONE
            } else {
                val tranTextSpannable = SpannableString(tranText).also {
                    if (it.isNotBlank()){
                        for (span in it.getSpans(0, it.length, Any::class.java)){
                            if (span is StyleSpan && span.style == Typeface.BOLD){
                                it.removeSpan(span)
                                it.setSpan(ForegroundColorSpan(ColorUtils.colorPrimary(holder.itemView.context)),
                                    tranText.getSpanStart(span), tranText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            if (span is StyleSpan && span.style == Typeface.ITALIC){
                                it.setSpan(ForegroundColorSpan(ColorUtils.colorOnSurfaceVariant(holder.itemView.context)),
                                    tranText.getSpanStart(span), tranText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            if (span is UnderlineSpan){
                                val clickableSpan = object : ClickableSpan() {
                                    override fun onClick(widget: View) {
                                        DictActivity.onSearchStr(
                                            holder.itemView.context,
                                            it.substring(it.getSpanStart(span), it.getSpanEnd(span))
                                        )
                                    }
                                    override fun updateDrawState(ds: TextPaint) {
                                        ds.isUnderlineText = false
                                        ds.color = ColorUtils.colorTertiary(holder.itemView.context)
                                        ds.bgColor = Color.TRANSPARENT
                                    }
                                }
                                it.setSpan(clickableSpan, tranText.getSpanStart(span), tranText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }
                }
                holder.tvTran.text = tranTextSpannable
                holder.tvTran.movementMethod = LinkClickMovementMethod()
            }

            val s = mutableListOf<String>()
            tranEntry.exam_sents?.sent.let {
                if (it?.isNotEmpty() == true){
                    holder.tvExam.visibility = View.VISIBLE
                    for (it1 in it){
                        it1.eng_sent?.let {  it2 -> s.add(it2)}
                    }
                } else {
                    holder.tvExam.visibility = View.GONE
                }
            }
            if (s.isNotEmpty()){
                val examText = "\r" + s.joinToString("\n\r")
                val d = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_bullist_dot)
                d!!.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                val span = SpannableStringBuilder(examText)
                span.setSpan(LeadingMarginSpan.Standard(0, SizeUtils.dp2px(10f)), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                val paragraphBeginIndexes = mutableListOf<Int>() // 记录下一个段落的开始位置
                examText.indices.filter { examText.startsWith("\r", it) }.forEach{ paragraphBeginIndexes.add(it) }
                for (index in paragraphBeginIndexes) {
                    span.setSpan(ImageSpan(d), index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                holder.tvExam.setText(span, TextView.BufferType.SPANNABLE)
            } else holder.tvExam.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setData(data: List<Entry>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemList.clear()
                mItemList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNum: TextView = itemView.findViewById(R.id.tv_item_num)
        val tvTran: TextView = itemView.findViewById(R.id.tv_item_tran)
        val tvExam: TextView = itemView.findViewById(R.id.tv_item_exam)
    }
}

