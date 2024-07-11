package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.RelInfo
import com.qxtao.easydict.utils.LinkClickMovementMethod
import com.qxtao.easydict.utils.common.ColorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class DictRelWordAdapter(private val relWordItems: ArrayList<RelInfo>) :
    RecyclerView.Adapter<DictRelWordAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_rel_word, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: RelInfo = relWordItems[position]
        val typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/Georgia Italic.ttf")
        holder.textInf.typeface = typeface
        holder.textInf.text = item.pos
        val content = if ((item.words?.size ?: 0) > 0) {
            val list = mutableListOf<String>()
            for (i in 0 until item.words!!.size) {
                item.words[i].let { list.add(it.word.toString()) }
            }
            list.joinToString("</u> / <u>","<u>", "</u>")
        } else ""
        val conText = Html.fromHtml(content, FROM_HTML_MODE_COMPACT)
        val spannable = SpannableString(conText).also {
            if (it.isNotBlank()){
                for (spa in it.getSpans(0, it.length, Any::class.java)){
                    if (spa is UnderlineSpan){
                        val clickableSpan = object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                DictActivity.onSearchStr(
                                    holder.itemView.context,
                                    it.substring(it.getSpanStart(spa), it.getSpanEnd(spa))
                                )
                            }
                            override fun updateDrawState(ds: TextPaint) {
                                ds.isUnderlineText = false
                                ds.color = ColorUtils.colorTertiary(holder.itemView.context)
                            }
                        }
                        it.setSpan(clickableSpan, conText.getSpanStart(spa), conText.getSpanEnd(spa), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
        holder.textContent.text = spannable
        holder.textContent.movementMethod = LinkClickMovementMethod()

    }

    fun setData(data: List<RelInfo>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                relWordItems.clear()
                relWordItems.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return relWordItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textInf: TextView = itemView.findViewById(R.id.tv_item_inf)
        var textContent: TextView = itemView.findViewById(R.id.tv_item_content)
    }

}
