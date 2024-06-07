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
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.WebTrans
import com.qxtao.easydict.ui.activity.dict.WebTranslation
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.factory.fixTextSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictWebTransOuterAdapter(
    private val mItemList: ArrayList<WebTranslation>,
    private val take: Int? = null
) :
    RecyclerView.Adapter<DictWebTransOuterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_web_trans_outer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: WebTranslation = mItemList[position]
        when (position){
            0 -> {
                holder.layoutContent.visibility = View.GONE
                holder.rvDictWebTransInner.visibility = View.VISIBLE
                item.trans?.let { holder.setInnerData(it, take) }
                return
            }
            1 -> {
                holder.textTitle.visibility = View.VISIBLE
                holder.textTitle.text = holder.itemView.context.getString(R.string.web_phrase)
            }
        }
        holder.textNum.text = position.toString()

        val l = mutableListOf<String>()
        item.trans?.let {
            for (i in item.trans.indices){
                it[i].value?.let { it1 -> l.add(it1) }
            }
        }
        holder.textLine.text = l.joinToString("; ")
        holder.textLine.visibility = if (l.isEmpty()) View.GONE else View.VISIBLE

        item.key?.let {
            val span = SpannableString(item.key)
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    DictActivity.onSearchStr(holder.itemView.context, item.key)
                }
                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                    ds.color = ColorUtils.colorTertiary(holder.itemView.context)
                }
            }
            span.setSpan(clickableSpan, 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.textValue.movementMethod = LinkMovementMethod.getInstance()
            holder.textValue.text = span
        }
        holder.textValue.visibility = if (item.key.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.textLine.fixTextSelection()
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setData(data: List<WebTranslation>?) {
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


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var dictWebTransInnerAdapter : DictWebTransInnerAdapter = DictWebTransInnerAdapter(ArrayList())
        val textNum: TextView = itemView.findViewById(R.id.tv_item_num)
        val textTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        val textValue: TextView = itemView.findViewById(R.id.tv_item_value)
        val textLine: TextView = itemView.findViewById(R.id.tv_item_line)
        val layoutContent : ConstraintLayout = itemView.findViewById(R.id.cl_item_content)
        val rvDictWebTransInner : RecyclerView = itemView.findViewById(R.id.rv_item_web_trans)


        init {
            rvDictWebTransInner.adapter = dictWebTransInnerAdapter
            rvDictWebTransInner.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun setInnerData(item: List<WebTrans>, take: Int?) {
            dictWebTransInnerAdapter.setData(item.take(take ?: item.size))
        }

    }

}

