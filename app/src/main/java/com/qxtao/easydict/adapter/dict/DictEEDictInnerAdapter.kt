package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.EETranslationItem
import com.qxtao.easydict.utils.common.SizeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictEEDictInnerAdapter(mItemList: ArrayList<EETranslationItem>) :
    RecyclerView.Adapter<DictEEDictInnerAdapter.ViewHolder>() {
    private val mItemList: ArrayList<EETranslationItem>

    init {
        this.mItemList = mItemList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_ee_inner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: EETranslationItem = mItemList[position]
        holder.tvNum.text = "${position + 1}"
        holder.tvTran.text = item.tran

        val s = mutableListOf<String>()
        item.examples.let {
            if (it?.isNotEmpty() == true){
                holder.tvExam.visibility = View.VISIBLE
                for (it1 in it){
                    s.add(it1)
                }
            } else {
                holder.tvExam.visibility = View.GONE
            }
        }
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

        val t = mutableListOf<String>()
        item.`similar-words`.let {
            if (it?.isNotEmpty() == true){
                holder.tvWord.visibility = View.VISIBLE
                for (it1 in it){
                    t.add(it1)
                }
            } else {
                holder.tvWord.visibility = View.GONE
            }
        }
        val wordText = Html.fromHtml("Synonyms:&nbsp;&nbsp;" + t.joinToString("</u> / <u>","<u>", "</u>"), FROM_HTML_MODE_COMPACT)
        val spannable = SpannableString(wordText).also {
            if (it.isNotBlank()){
                for (spa in it.getSpans(0, it.length, Any::class.java)){
                    if (spa is UnderlineSpan){
                        val clickableSpan = object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                DictActivity.onSearchStr(holder.itemView.context, it.substring(it.getSpanStart(spa), it.getSpanEnd(spa)))
                            }
                            override fun updateDrawState(ds: TextPaint) {
                                ds.isUnderlineText = false
                                ds.color = holder.itemView.context.getColor(R.color.theme_color_ori)
                            }
                        }
                        it.setSpan(clickableSpan, wordText.getSpanStart(spa), wordText.getSpanEnd(spa), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
        holder.tvWord.movementMethod = LinkMovementMethod.getInstance()
        holder.tvWord.text = spannable

    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setData(data: List<EETranslationItem>?) {
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
        val tvWord: TextView = itemView.findViewById(R.id.tv_item_word)
    }
}

