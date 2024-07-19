package com.qxtao.easydict.adapter.settings

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.web.WebActivity
import com.qxtao.easydict.utils.common.ClipboardUtils
import com.qxtao.easydict.utils.common.SizeUtils
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.factory.fixTextSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone


@SuppressLint("NotifyDataSetChanged")
class AppUpdateDataAdapter(private val appUpdateDataItems: ArrayList<AppUpdateDataItem>) :
    RecyclerView.Adapter<AppUpdateDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_settings_app_update_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: AppUpdateDataItem = appUpdateDataItems[position]
        holder.tvAppVersionName.text = holder.itemView.context.getString(R.string.update_app_name_version, item.name)
        holder.tvPublishTime.text = holder.itemView.context.getString(
            R.string.update_publish_at,
            TimeUtils.getFormatDateTimeByPattern(item.publishedAt, "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd HH:mm", TimeZone.getTimeZone("UTC"))
        )

        val updateLogList = item.body.split("\n")
            .filter { it.isNotBlank() && it.startsWith("-")  }
            .map { it.trim('-').trim() }
        val updateLogText = "\r" + updateLogList.joinToString("\n\r")
        val d = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_bullist_dot)
        d!!.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
        val span = SpannableStringBuilder(updateLogText)
        span.setSpan(LeadingMarginSpan.Standard(0, SizeUtils.dp2px(10f)), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val paragraphBeginIndexes = mutableListOf<Int>() // 记录下一个段落的开始位置
        updateLogText.indices.filter { updateLogText.startsWith("\r", it) }.forEach{ paragraphBeginIndexes.add(it) }
        for (index in paragraphBeginIndexes) {
            span.setSpan(ImageSpan(d), index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        holder.tvUpdateLogDesc.setText(span, TextView.BufferType.SPANNABLE)

        holder.ivViewInWebsite.setOnClickListener {
            WebActivity.start(holder.itemView.context, item.htmlUrl)
        }
        holder.tvDownloadInGithub.setOnClickListener {
            WebActivity.start(holder.itemView.context, item.browserDownloadUrl)
        }
        holder.tvDownloadInNetdisk.setOnClickListener {
            val url = "https://www.123pan.com/s/W1LKVv-Emo7A.html"
            WebActivity.start(holder.itemView.context, url)
            ClipboardUtils.copyTextToClipboard(holder.itemView.context, "kDva", holder.itemView.context.getString(R.string.pwd_eee, "kDva"))
        }
        holder.tvUpdateLogDesc.fixTextSelection()
    }

    override fun getItemCount(): Int = appUpdateDataItems.size

    fun setData(data: List<AppUpdateDataItem>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                appUpdateDataItems.clear()
                appUpdateDataItems.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAppVersionName: TextView = itemView.findViewById(R.id.tv_app_version_name)
        val tvPublishTime: TextView = itemView.findViewById(R.id.tv_publish_time)
        val tvUpdateLogDesc: TextView = itemView.findViewById(R.id.tv_update_log_desc)
        val ivViewInWebsite: ImageView = itemView.findViewById(R.id.iv_view_in_website)
        val tvDownloadInGithub: TextView = itemView.findViewById(R.id.tv_download_in_github)
        val tvDownloadInNetdisk: TextView = itemView.findViewById(R.id.tv_download_in_netdisk)
    }
}