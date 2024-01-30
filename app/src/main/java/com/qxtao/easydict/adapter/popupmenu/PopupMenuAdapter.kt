package com.qxtao.easydict.adapter.popupmenu

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R


@SuppressLint("NotifyDataSetChanged")
class PopupMenuAdapter(popupMenuItems: List<PopupMenuItem>) : RecyclerView.Adapter<PopupMenuAdapter.ViewHolder>() {
    private val popupMenuItems: List<PopupMenuItem>
    private var meanItemClickListener: OnMenuItemClickListener? = null

    init {
        this.popupMenuItems = popupMenuItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_popup_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: PopupMenuItem = popupMenuItems[position]
        holder.textMenuContent.text = item.menuItemText
        if (item.isMenuItemSelected){
            holder.textMenuContent.setTextColor(holder.layoutMenuItem.context.getColor(R.color.themeThirdColor))
            holder.imageMenuSelect.setColorFilter(holder.layoutMenuItem.context.getColor(R.color.themeThirdColor))
            holder.imageMenuSelect.visibility = View.VISIBLE
            holder.layoutMenuItem.setBackgroundColor(holder.layoutMenuItem.context.getColor(R.color.colorBgLightYellow3))
        } else {
            val typedValue = TypedValue()
            holder.layoutMenuItem.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
            val attribute = intArrayOf(android.R.attr.selectableItemBackground)
            val typedArray: TypedArray = holder.layoutMenuItem.context.theme.obtainStyledAttributes(typedValue.resourceId, attribute)
            holder.layoutMenuItem.background = typedArray.getDrawable(0)
            holder.imageMenuSelect.visibility = View.INVISIBLE
            holder.textMenuContent.setTextColor(holder.layoutMenuItem.context.getColor(R.color.secondTextColor))
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
        val layoutMenuItem: LinearLayout = itemView.findViewById(R.id.ll_linearlayout)
    }
}