package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.Trans
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.factory.fixTextSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictHeHeaderExplainAdapter(private val mItemList: ArrayList<Trans>) :
    RecyclerView.Adapter<DictHeHeaderExplainAdapter.ViewHolder>() {
    private var playButtonClickListener: OnPlayButtonClickListener? = null
    private var playPosition: Int = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_he_header_explain, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItemList[position]
        item.w.let {
            if (it?.isBlank() == false){
                val spannableString = SpannableString(it)
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        DictActivity.onSearchStr(holder.itemView.context, it)
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = false
                        ds.color = ColorUtils.colorTertiary(holder.itemView.context)
                    }
                }
                spannableString.setSpan(clickableSpan, 0, it.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.textEn.movementMethod = LinkMovementMethod.getInstance()
                holder.textEn.text = spannableString
            } else return
        }
        holder.imgVoice.setOnClickListener {
            playButtonClickListener?.onPlayButtonClick(position)
        }
        holder.textCn.apply { if (item.trans.isNullOrBlank()){ this.visibility = View.GONE } else text = item.trans }

        val imageResId = if (position == playPosition) {
            R.drawable.ic_voice_on
        } else R.drawable.ic_voice
        holder.imgVoice.setImageResource(imageResId)
        holder.imgVoice.setOnClickListener {
            playPosition = holder.absoluteAdapterPosition
            holder.imgVoice.setImageResource(imageResId)
            playButtonClickListener?.onPlayButtonClick(position)
        }

        holder.textCn.fixTextSelection()
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setData(data: List<Trans>?) {
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textEn: TextView = itemView.findViewById(R.id.tv_item_tv_en)
        var textCn: TextView = itemView.findViewById(R.id.tv_item_tv_cn)
        var imgVoice : ImageView = itemView.findViewById(R.id.iv_item_voice)
    }

}

