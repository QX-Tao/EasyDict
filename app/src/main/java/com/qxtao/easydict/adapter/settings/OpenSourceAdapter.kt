package com.qxtao.easydict.adapter.settings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.web.WebActivity


class OpenSourceAdapter(private val openSourceItems: List<OpenSourceItem>) :
    RecyclerView.Adapter<OpenSourceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_settings_open_source, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: OpenSourceItem = openSourceItems[position]
        holder.textTitle.text = item.title
        holder.textInfo.text = item.info
        holder.textLink.text = item.link
        holder.textLicense.text = item.license

        holder.itemView.setOnClickListener {
            val url = item.link
            WebActivity.start(holder.itemView.context, url)
        }
    }

    override fun getItemCount(): Int {
        return openSourceItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.tv_open_source_item_title)
        val textInfo: TextView = itemView.findViewById(R.id.tv_open_source_item_info)
        val textLink: TextView = itemView.findViewById(R.id.tv_open_source_item_link)
        val textLicense: TextView = itemView.findViewById(R.id.tv_open_source_item_license)
    }
}