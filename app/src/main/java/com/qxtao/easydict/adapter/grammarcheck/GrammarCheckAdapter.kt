package com.qxtao.easydict.adapter.grammarcheck

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.grammarcheck.GrammarCheckViewModel
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.factory.fixTextSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class GrammarCheckAdapter(private val sentFeedbackItems: ArrayList<GrammarCheckViewModel.SentFeedback>) :
    RecyclerView.Adapter<GrammarCheckAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_grammar_check, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemFeedback: GrammarCheckViewModel.SentFeedback = sentFeedbackItems[position]

        holder.tvCorrectSent.text = String.format(holder.itemView.context.getString(R.string.correct_sentence_eee), itemFeedback.correctedSent)
        holder.tvReason.text = String.format(holder.itemView.context.getString(R.string.reason_eee), itemFeedback.errorPosInfos[0].reason)
        holder.tvSuggestion.text = String.format(holder.itemView.context.getString(R.string.some_suggestion_eee), itemFeedback.errorPosInfos[0].orgChunk, itemFeedback.errorPosInfos[0].correctChunk)

        val spannableString = SpannableString(itemFeedback.rawSent)
        itemFeedback.errorPosInfos.forEach { errorPosInfo ->
            val start = errorPosInfo.startPos
            val end = errorPosInfo.endPos
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    holder.tvReason.text = String.format(holder.itemView.context.getString(R.string.reason_eee), errorPosInfo.reason)
                    if (errorPosInfo.reason.contains("冗余") && errorPosInfo.reason.contains("建议删除")){
                        val delSpannableString = SpannableString(String.format(holder.itemView.context.getString(R.string.some_suggestion_eee), errorPosInfo.orgChunk, errorPosInfo.orgChunk))
                        // 添加删除线
                        delSpannableString.setSpan(StrikethroughSpan(), delSpannableString.lastIndexOf(errorPosInfo.orgChunk), delSpannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        holder.tvSuggestion.text = delSpannableString
                    } else holder.tvSuggestion.text = String.format(holder.itemView.context.getString(R.string.some_suggestion_eee), errorPosInfo.orgChunk, errorPosInfo.correctChunk)
                }
                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                    ds.color = ColorUtils.colorTertiary(holder.itemView.context)
                }
            }
            spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        holder.tvRawSent.text = spannableString
        holder.tvRawSent.movementMethod = LinkMovementMethod.getInstance()

        holder.tvSuggestion.fixTextSelection()
        holder.tvCorrectSent.fixTextSelection()
        holder.tvReason.fixTextSelection()
        holder.tvRawSent.fixTextSelection()
    }

    override fun getItemCount(): Int = sentFeedbackItems.size

    fun setData(data: List<GrammarCheckViewModel.SentFeedback>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                sentFeedbackItems.clear()
                sentFeedbackItems.addAll(newList)
                notifyDataSetChanged()
            }
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRawSent: TextView = itemView.findViewById(R.id.tv_gc_raw_sent)
        val tvSuggestion: TextView = itemView.findViewById(R.id.tv_gc_suggestion)
        val tvReason: TextView = itemView.findViewById(R.id.tv_gc_reason)
        val tvCorrectSent: TextView = itemView.findViewById(R.id.tv_gc_cor_sent)
    }

}
