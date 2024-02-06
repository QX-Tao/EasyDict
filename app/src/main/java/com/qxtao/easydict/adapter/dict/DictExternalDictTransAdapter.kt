package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.adapter.popupmenu.PopupMenuAdapter
import com.qxtao.easydict.ui.activity.web.WebActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import per.wsj.library.AndRatingBar

@SuppressLint("NotifyDataSetChanged")
class DictExternalDictTransAdapter(mItemList: ArrayList<ExternalDictTransItem>) :
    RecyclerView.Adapter<DictExternalDictTransAdapter.ViewHolder>() {
    private val mItemList: ArrayList<ExternalDictTransItem>
    private var itemClickListener: OnItemClickListener? = null

    init {
        this.mItemList = mItemList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_external_dict_trans, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = holder.itemView.context.getString(mItemList[position].nameId)
        val drawable = ResourcesCompat.getDrawable(holder.itemView.context.resources, mItemList[position].resId, null)
        holder.tvName.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
        holder.tvName.setOnClickListener {
            itemClickListener?.onItemClick(position)
            WebActivity.start(
                holder.itemView.context, mItemList[position].url,
                isUseTitleBarSpace = false,
                allowOtherUrls = true,
                useWebTitle = true,
                showOpenInBrowserButton = true
            )
        }
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setData(data: List<ExternalDictTransItem>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemList.clear()
                mItemList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun  setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun getItem(position: Int) = mItemList[position]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
    }
    
    data class ExternalDictTransItem(
        val resId: Int,
        val nameId: Int,
        val url: String
    )

}
