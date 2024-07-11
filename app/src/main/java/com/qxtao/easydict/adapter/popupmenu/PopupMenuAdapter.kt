package com.qxtao.easydict.adapter.popupmenu

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.utils.common.ColorUtils


@SuppressLint("NotifyDataSetChanged")
class PopupMenuAdapter(private val popupMenuItems: List<PopupMenuItem>) : RecyclerView.Adapter<PopupMenuAdapter.ViewHolder>() {
    private var meanItemClickListener: OnMenuItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_popup_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: PopupMenuItem = popupMenuItems[position]
        holder.textMenuContent.text = item.menuItemText
        if (item.isMenuItemSelected){
            holder.textMenuContent.setTextColor(ColorUtils.colorPrimary(holder.itemView.context))
            holder.imageMenuSelect.setColorFilter(ColorUtils.colorPrimary(holder.itemView.context))
            holder.imageMenuSelect.visibility = View.VISIBLE
            holder.layoutHolder.visibility = View.VISIBLE
        } else {
            holder.layoutHolder.visibility = View.GONE
            holder.imageMenuSelect.visibility = View.INVISIBLE
            holder.textMenuContent.setTextColor(ColorUtils.colorOnSurface(holder.itemView.context))
        }

        holder.itemView.setOnClickListener {
            meanItemClickListener?.onMenuItemClick(position)
            selectItem(position)
        }

    }

    fun selectItem(position: Int){
        for (element in popupMenuItems){
            element.isMenuItemSelected = false
        }
        popupMenuItems[position].isMenuItemSelected = true
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return popupMenuItems.size
    }

    interface  OnMenuItemClickListener {
        fun onMenuItemClick(position: Int)
    }

    fun setOnMenuItemClickListener(listener: OnMenuItemClickListener) {
        meanItemClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMenuContent: TextView = itemView.findViewById(R.id.tv_text)
        val imageMenuSelect: ImageView = itemView.findViewById(R.id.iv_image)
        val layoutHolder: View = itemView.findViewById(R.id.v_holder)
    }
}