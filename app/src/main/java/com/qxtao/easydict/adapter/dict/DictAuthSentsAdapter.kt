package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.Spannable
import android.text.SpannableString
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictAuthSentsAdapter(mItemSentenceList: ArrayList<AuthSentence>) :
    RecyclerView.Adapter<DictAuthSentsAdapter.ViewHolder>() {
    private val mItemSentenceList: ArrayList<AuthSentence>
    private var playButtonClickListener: OnPlayButtonClickListener? = null
    private var sourceUrlClickListener: OnSourceUrlClickListener? = null
    private var playPosition: Int = -1

    init {
        this.mItemSentenceList = mItemSentenceList
    }

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
                foreignSpannable.setSpan(ForegroundColorSpan(holder.itemView.context.getColor(R.color.theme_color_gol)),
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
                sourceSpannable.setSpan(ForegroundColorSpan(holder.itemView.context.getColor(R.color.theme_color_gol)),
                    sourceText.getSpanStart(span), sourceText.getSpanEnd(span), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        holder.source.text = sourceSpannable

        val imageResId = if (position == playPosition) {
            R.drawable.ic_voice_on
        } else R.drawable.ic_voice
        holder.imgVoice.setImageResource(imageResId)
        holder.imgVoice.setOnClickListener {
            playPosition = holder.absoluteAdapterPosition
            holder.imgVoice.setImageResource(imageResId)
            playButtonClickListener?.onPlayButtonClick(position)
        }
        holder.source.setOnClickListener {
            sourceUrlClickListener?.onSourceUrlClick(position)
        }
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

