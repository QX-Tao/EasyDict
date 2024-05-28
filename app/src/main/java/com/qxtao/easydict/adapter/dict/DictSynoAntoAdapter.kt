package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.Antonym
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.Synonym
import com.qxtao.easydict.utils.common.ColorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictSynoAntoAdapter<T>(private val mItemData: ArrayList<T>) :
    RecyclerView.Adapter<DictSynoAntoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_syno_anto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: T = mItemData[position]
        val inf : String
        val explain: String
        val content: String
        when (item){
            is Synonym -> {
                inf = item.pos.toString()
                explain = item.tran.toString()
                content = if ((item.ws?.size ?: 0) > 0) {
                    val list = mutableListOf<String>()
                    for (i in 0 until item.ws!!.size) {
                        item.ws[i].let { list.add(it) }
                    }
                    item.ws.joinToString("</u> / <u>","<u>", "</u>")
                } else ""
            }
            is Antonym -> {
                inf = item.pos.toString()
                explain = item.tran.toString()
                content = if ((item.ws?.size ?: 0) > 0) {
                    val list = mutableListOf<String>()
                    for (i in 0 until item.ws!!.size) {
                        item.ws[i].let { list.add(it) }
                    }
                    item.ws.joinToString("</u> / <u>","<u>", "</u>")
                } else ""
            }
            else -> {
                inf =  ""
                explain = ""
                content = ""
            }
        }
        val typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/Georgia Italic.ttf")
        holder.textInf.typeface = typeface
        holder.textInf.text = inf
        holder.textExplain.text = explain

        val conText = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
        val spannable = SpannableString(conText).also {
            if (it.isNotBlank()){
                for (spa in it.getSpans(0, it.length, Any::class.java)){
                    if (spa is UnderlineSpan){
                        val clickableSpan = object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                DictActivity.onSearchStr(holder.itemView.context, it.substring(it.getSpanStart(spa), it.getSpanEnd(spa)))
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
        holder.textContent.movementMethod = LinkMovementMethod.getInstance()
        holder.textContent.text = spannable
    }

    override fun getItemCount(): Int {
        return mItemData.size
    }

    fun setData(data: List<T>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemData.clear()
                mItemData.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    fun getItem(position: Int) = mItemData[position]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textInf: TextView = itemView.findViewById(R.id.tv_item_inf)
        var textExplain: TextView = itemView.findViewById(R.id.tv_item_explain)
        var textContent: TextView = itemView.findViewById(R.id.tv_item_content)
    }

}

