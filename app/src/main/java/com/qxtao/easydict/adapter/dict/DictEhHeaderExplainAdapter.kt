package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.TabStopSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.Trs
import com.qxtao.easydict.utils.common.SizeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class DictEhHeaderExplainAdapter(trsItems: ArrayList<Trs>) :
    RecyclerView.Adapter<DictEhHeaderExplainAdapter.ViewHolder>() {
    private val trsItems: ArrayList<Trs>


    init {
        this.trsItems = trsItems
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_eh_header_explain, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val l = mutableListOf<String>()
        var posMaxCharNum = 2
        for (i in trsItems.indices){
            posMaxCharNum = if ((trsItems[i].pos?.length ?: 0) > posMaxCharNum)
                trsItems[i].pos?.length!! else posMaxCharNum
            l.add(trsItems[i].pos + "\t" + trsItems[i].tran)
        }
        val trans = l.joinToString("\n\r")

        val typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/Georgia Italic.ttf")
        val d = ContextCompat.getDrawable(holder.itemView.context, R.drawable.sp_paragraph_space)
        d!!.setBounds(0, 0, 1, SizeUtils.dp2px(27f))
        val span = SpannableStringBuilder(trans)
        span.setSpan(TabStopSpan.Standard(SizeUtils.dp2px((posMaxCharNum * 10).toFloat())), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(LeadingMarginSpan.Standard(0, SizeUtils.dp2px((posMaxCharNum * 10).toFloat())), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val nextParagraphBeginIndexes = mutableListOf<Int>() // 记录下一个段落的开始位置
        trans.indices.filter { trans.startsWith("\n\r", it) }.forEach{ nextParagraphBeginIndexes.add(it + 1)}
        for (index in nextParagraphBeginIndexes) {
            span.setSpan(ImageSpan(d), index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val linesBeginIndexes = listOf(0) + nextParagraphBeginIndexes
        for (i in trsItems.indices){
            span.setSpan(TypefaceSpan(typeface), linesBeginIndexes[i], linesBeginIndexes[i] + (trsItems[i].pos?.length ?: 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        holder.textTranslation.setText(span, TextView.BufferType.SPANNABLE)
    }

    fun setData(data: List<Trs>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                trsItems.clear()
                trsItems.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textTranslation: TextView = itemView.findViewById(R.id.tv_explain_trans)
    }

}
