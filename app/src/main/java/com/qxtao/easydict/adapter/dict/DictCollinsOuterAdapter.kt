package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.CollinsEntry
import com.qxtao.easydict.ui.activity.dict.Entry
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.factory.fixTextSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import per.wsj.library.AndRatingBar


@SuppressLint("NotifyDataSetChanged")
class DictCollinsOuterAdapter(private val mItemList: ArrayList<CollinsEntry>) :
    RecyclerView.Adapter<DictCollinsOuterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_collins_outer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: CollinsEntry = mItemList[position]
        holder.tvHeadWord.text = item.headword
        item.super_headword.let { if (it.isNullOrEmpty()) holder.tvSuperHeadWord.visibility = View.GONE else holder.tvSuperHeadWord.text = it }
        item.headword.let { if (it.isNullOrEmpty()) holder.tvHeadWord.visibility = View.GONE else holder.tvHeadWord.text = it }
        item.phonetic.let { if (it.isNullOrEmpty()) holder.tvPhonetic.visibility = View.GONE else holder.tvPhonetic.text = "/$it/" }
        item.star.let { if (it.isNullOrEmpty()) holder.rbStar.visibility = View.GONE else holder.rbStar.numStars = it.toFloat().toInt() }
        
        val textForm : String?
        val w = mutableListOf<String>()
        item.basic_entries?.basic_entry?.let {
            val basicEntry = item.basic_entries.basic_entry[0]
            basicEntry.wordforms.let { it2 -> it2?.wordform?.forEach { it3 -> it3.word?.let { it4 -> w.add(it4) } } }

            val textPos = basicEntry.pos_entry?.pos?.let { if (it.isNotBlank()) "<i>${it}</i>" else null } ?: ""
            val textTran = basicEntry.tran ?: ""
            val textSee = basicEntry.sees?.see?.get(0)?.let {
                if (it.seeword?.isNotBlank() == true) "→ see <u>${it.seeword}</u>" else null } ?: ""
            val textSeeAlso = basicEntry.seeAlsos?.seeAlso?.get(0)?.let {
                if (it.seeword?.isNotBlank() == true) "→ see also <u>${it.seeword}</u>" else null } ?: ""
            val tranTmpText = listOf(textPos, textTran, textSee, textSeeAlso)
                .filter { it.isNotBlank() }
                .joinToString("&nbsp;&nbsp;")
            val tranText = HtmlCompat.fromHtml(tranTmpText, HtmlCompat.FROM_HTML_MODE_COMPACT)
            if (tranText.isBlank()){
                holder.tvTran.visibility = View.GONE
            } else {
                val tranTextSpannable = SpannableString(tranText).also {
                    if (it.isNotBlank()){
                        for (span in it.getSpans(0, it.length, Any::class.java)){
                            if (span is StyleSpan && span.style == Typeface.BOLD){
                                it.removeSpan(span)
                                it.setSpan(
                                    ForegroundColorSpan(ColorUtils.colorPrimary(holder.itemView.context)),
                                    tranText.getSpanStart(span), tranText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            if (span is StyleSpan && span.style == Typeface.ITALIC){
                                it.setSpan(
                                    ForegroundColorSpan(ColorUtils.colorOnSurfaceVariant(holder.itemView.context)),
                                    tranText.getSpanStart(span), tranText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            if (span is UnderlineSpan){
                                val clickableSpan = object : ClickableSpan() {
                                    override fun onClick(widget: View) {
                                        DictActivity.onSearchStr(holder.itemView.context, it.substring(it.getSpanStart(span), it.getSpanEnd(span)))
                                    }
                                    override fun updateDrawState(ds: TextPaint) {
                                        ds.isUnderlineText = false
                                        ds.color = ColorUtils.colorTertiary(holder.itemView.context)
                                    }
                                }
                                it.setSpan(clickableSpan, tranText.getSpanStart(span), tranText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }
                }
                holder.tvTran.movementMethod = LinkMovementMethod.getInstance()
                holder.tvTran.text = tranTextSpannable
            }

            val s = mutableListOf<String>()
            basicEntry.exam_sents?.sent.let {
                if (it?.isNotEmpty() == true){
                    holder.tvExam.visibility = View.VISIBLE
                    for (it1 in it){
                        it1.eng_sent?.let {  it2 -> s.add(it2)}
                    }
                } else {
                    holder.tvExam.visibility = View.GONE
                }
            }
            if (s.size > 0){
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
        textForm = w.joinToString(", ")
        if (textForm.isBlank()) holder.tvWordForm.visibility = View.GONE
        else holder.tvWordForm.text = "($textForm)"
        item.entries?.entry?.let { holder.setInnerData(it) }

        holder.tvTran.fixTextSelection()
        holder.tvExam.fixTextSelection()
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setData(data: List<CollinsEntry>?) {
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
        private var dictCollinsInnerAdapter : DictCollinsInnerAdapter = DictCollinsInnerAdapter(ArrayList())
        private val rvDictCollinsInner : RecyclerView = itemView.findViewById(R.id.rv_item_collins)
        val tvSuperHeadWord: TextView = itemView.findViewById(R.id.tv_super_header_word)
        val tvHeadWord: TextView = itemView.findViewById(R.id.tv_head_word)
        val tvPhonetic: TextView = itemView.findViewById(R.id.tv_phonetic)
        val tvWordForm: TextView = itemView.findViewById(R.id.tv_word_form)
        val tvTran: TextView = itemView.findViewById(R.id.tv_item_tran)
        val tvExam: TextView = itemView.findViewById(R.id.tv_item_exam)
        val rbStar: AndRatingBar = itemView.findViewById(R.id.rb_star)
        

        init {
            rvDictCollinsInner.adapter = dictCollinsInnerAdapter
            rvDictCollinsInner.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun setInnerData(item: List<Entry>) {
            dictCollinsInnerAdapter.setData(item)
        }

    }

}

