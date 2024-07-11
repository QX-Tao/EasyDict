package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.AuthSentence
import com.qxtao.easydict.utils.LinkClickMovementMethod
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.factory.fixTextSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictAuthSentsAdapter(private val mItemSentenceList: ArrayList<AuthSentence>) :
    RecyclerView.Adapter<DictAuthSentsAdapter.ViewHolder>() {
    private var playButtonClickListener: OnPlayButtonClickListener? = null
    private var sourceUrlClickListener: OnSourceUrlClickListener? = null
    private var playPosition: Int = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_auth_sents, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemSentence: AuthSentence = mItemSentenceList[position]
        val num = position + 1
        holder.textNum.text = num.toString()

        val foreign = itemSentence.foreign
        val foreignText = Html.fromHtml(foreign, FROM_HTML_MODE_COMPACT)
        val foreignSpannable = SpannableString(foreignText)
        for (span in foreignText.getSpans(0, foreignText.length, Any::class.java)){
            if (span is StyleSpan && span.style == Typeface.BOLD){
                foreignSpannable.removeSpan(span)
                foreignSpannable.setSpan(ForegroundColorSpan(ColorUtils.colorPrimary(holder.itemView.context)),
                    foreignText.getSpanStart(span), foreignText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        holder.textEn.text = foreignSpannable

        val source = itemSentence.source
        val sourceText = Html.fromHtml(source, FROM_HTML_MODE_COMPACT)
        val sourceSpannable = SpannableString(sourceText)
        for (span in sourceText.getSpans(0, sourceText.length, Any::class.java)){
            if (span is StyleSpan && span.style == Typeface.BOLD){
                foreignSpannable.removeSpan(span)
                sourceSpannable.setSpan(ForegroundColorSpan(ColorUtils.colorPrimary(holder.itemView.context)),
                    sourceText.getSpanStart(span), sourceText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        sourceSpannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                sourceUrlClickListener?.onSourceUrlClick(holder.absoluteAdapterPosition)
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ColorUtils.colorTertiary(holder.itemView.context)
            }
        }, 0, sourceSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.source.text = sourceSpannable
        holder.source.movementMethod = LinkClickMovementMethod()

        val imageResId = if (position == playPosition) {
            R.drawable.ic_voice_on
        } else R.drawable.ic_voice
        holder.imgVoice.setImageResource(imageResId)
        holder.imgVoice.setOnClickListener {
            playPosition = holder.absoluteAdapterPosition
            holder.imgVoice.setImageResource(imageResId)
            playButtonClickListener?.onPlayButtonClick(position)
        }

        holder.textEn.fixTextSelection()
    }

    override fun getItemCount(): Int {
        return mItemSentenceList.size
    }

    fun setData(data: List<AuthSentence>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemSentenceList.clear()
                mItemSentenceList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    fun getItem(position: Int) = mItemSentenceList[position]

    fun resetPlaySound() {
        playPosition = -1
        notifyDataSetChanged()
    }

    fun setPositionPlayingSound(position: Int) {
        playPosition = position
        notifyDataSetChanged()
    }

    interface OnPlayButtonClickListener {
        fun onPlayButtonClick(position: Int)
    }
    fun setOnPlayButtonClickListener(listener: OnPlayButtonClickListener) {
        playButtonClickListener = listener
    }
    interface OnSourceUrlClickListener{
        fun onSourceUrlClick(position: Int)
    }

    fun setOnSourceUrlClickListener(listener: OnSourceUrlClickListener){
        sourceUrlClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNum: TextView = itemView.findViewById(R.id.tv_item_num)
        val textEn: TextView = itemView.findViewById(R.id.tv_item_sen_en)
        val source: TextView = itemView.findViewById(R.id.tv_item_sen_source)
        val imgVoice : ImageView = itemView.findViewById(R.id.iv_item_sen_voice)
    }

}

