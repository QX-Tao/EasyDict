package com.qxtao.easydict.adapter.dailysentence

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.view.LoadingView
import com.qxtao.easydict.utils.common.ColorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class DailySentenceAdapter(private val dailySentenceItems: ArrayList<DailySentenceItem>) :
    RecyclerView.Adapter<DailySentenceAdapter.MViewHolder>() {
    private var playButtonClickListener: OnPlayButtonClickListener? = null
    private var loadingFailClickListener: OnLoadingFailClickListener? = null
    private var photoViewButtonClickListener: OnPhotoViewButtonClickListener? = null
    private var itemLongClickListener: OnItemLongClickListener? = null
    private var playPosition: Int = -1
    private var isLoadingFail: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_day_sentence, parent, false)
        return MViewHolder(view)
    }


    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        val item: DailySentenceItem? = dailySentenceItems.getOrNull(position)
        if (item != null) {
            if (item.type != TYPE_FOOTER) {
                holder.lvLoadingView.visibility = View.GONE
                holder.llLoadingFail.visibility = View.GONE

                holder.textDate.text = item.date
                holder.textSentenceEn.text = item.enSentence
                holder.textSentenceCh.text = item.cnSentence
                holder.imageBackground.load(item.imageUrl){ crossfade(true) }
                holder.mcvItemContent.setCardBackgroundColor(ColorUtils.paletteNeutral20(holder.itemView.context))

                val imageResId = if (position == playPosition) {
                    R.drawable.ic_sound_on
                } else R.drawable.ic_sound
                holder.imageSound.setImageResource(imageResId)
                holder.imageSound.setOnClickListener {
                    playPosition = holder.absoluteAdapterPosition
                    holder.imageSound.setImageResource(imageResId)
                    playButtonClickListener?.onPlayButtonClick(position)
                }
                holder.imageUnfold.setOnClickListener {
                    photoViewButtonClickListener?.onPhotoViewButtonClick(position)
                }

            } else {
                holder.textDate.visibility = View.GONE
                holder.textSentenceEn.visibility = View.GONE
                holder.textSentenceCh.visibility = View.GONE
                holder.imageBackground.visibility = View.GONE
                holder.imageUnfold.visibility = View.GONE
                holder.imageSound.visibility = View.GONE
                isLoadingFail.let {
                    if (it){
                        holder.llLoadingFail.visibility = View.VISIBLE
                        holder.lvLoadingView.visibility = View.GONE
                    } else {
                        holder.lvLoadingView.visibility = View.VISIBLE
                        holder.llLoadingFail.visibility = View.GONE
                    }
                }
                holder.llLoadingFail.setOnClickListener {
                    loadingFailClickListener?.onLoadingFailClick(position)
                }
            }
            holder.itemView.setOnLongClickListener{
                itemLongClickListener?.onItemLongClick(position)
                true
            }
        }

    }

    override fun getItemCount(): Int {
        return dailySentenceItems.size
    }

    override fun getItemViewType(position: Int): Int = dailySentenceItems[position].type

    fun addData(data: ArrayList<DailySentenceItem>?) {
        CoroutineScope(Dispatchers.Main).launch {
            if (data.isNullOrEmpty()) return@launch
            val lastIdx = dailySentenceItems.size - 1
            dailySentenceItems.addAll(data)
            notifyItemChanged(lastIdx, data.size)
        }
    }

    fun setData(data: ArrayList<DailySentenceItem>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                dailySentenceItems.clear()
                dailySentenceItems.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    fun setLoadingFail(isVisible: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            if (dailySentenceItems.size > 0 && dailySentenceItems[dailySentenceItems.size - 1].type == TYPE_FOOTER) {
                val item = dailySentenceItems.getOrNull(dailySentenceItems.size - 1)
                if (item != null && item.type == TYPE_FOOTER) {
                    isLoadingFail = isVisible
                    notifyItemChanged(dailySentenceItems.size - 1)
                }
            }
        }
    }

    interface OnPlayButtonClickListener {
        fun onPlayButtonClick(position: Int)
    }
    fun setOnPlayButtonClickListener(listener: OnPlayButtonClickListener) {
        playButtonClickListener = listener
    }

    interface OnLoadingFailClickListener {
        fun onLoadingFailClick(position: Int)
    }
    fun setOnLoadingFailClickListener(listener: OnLoadingFailClickListener) {
        loadingFailClickListener = listener
    }

    interface OnPhotoViewButtonClickListener {
        fun onPhotoViewButtonClick(position: Int)
    }
    fun setOnPhotoViewButtonClickListener(listener: OnPhotoViewButtonClickListener) {
        photoViewButtonClickListener = listener
    }

    interface OnItemLongClickListener{
        fun onItemLongClick(position: Int)
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener){
        itemLongClickListener = listener
    }


    fun resetPlaySound() {
        playPosition = -1
        notifyDataSetChanged()
    }

    fun setPositionPlayingSound(position: Int) {
        playPosition = position
        notifyDataSetChanged()
    }

    class MViewHolder(mItemView: View): RecyclerView.ViewHolder(mItemView){
        val textDate: TextView = mItemView.findViewById(R.id.tv_day_sentence_item_date)
        val textSentenceEn: TextView = mItemView.findViewById(R.id.tv_day_sentence_item_sentence_en)
        val textSentenceCh: TextView = mItemView.findViewById(R.id.tv_day_sentence_item_sentence_ch)
        val imageBackground: ImageView = mItemView.findViewById(R.id.iv_day_sentence_item_image)
        val imageSound: ImageView = mItemView.findViewById(R.id.iv_ds_sound)
        val imageUnfold: ImageView = mItemView.findViewById(R.id.iv_unfold)
        val llLoadingFail: LinearLayout = mItemView.findViewById(R.id.ll_loading_fail)
        val lvLoadingView: LoadingView = mItemView.findViewById(R.id.lv_loading)
        val mcvItemContent: MaterialCardView = mItemView.findViewById(R.id.mcv_day_sentence_item_content)
    }

}