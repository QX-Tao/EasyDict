package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.Thesaurus
import com.qxtao.easydict.ui.activity.dict.ThesaurusEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("NotifyDataSetChanged")
class DictThesurusOuterAdapter(
    private val mItemThesaurusList: ArrayList<Thesaurus>,
    private val take: Int? = null
) :
    RecyclerView.Adapter<DictThesurusOuterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_thesurus_outer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Thesaurus = mItemThesaurusList[position]
        val typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/Georgia Italic.ttf")
        holder.textInf.typeface = typeface
        holder.textInf.text = item.pos
        item.thesaurus?.let { holder.setInnerData(it, take) }
    }

    override fun getItemCount(): Int {
        return mItemThesaurusList.size
    }

    fun setData(data: List<Thesaurus>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                mItemThesaurusList.clear()
                mItemThesaurusList.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    fun getItem(position: Int) = mItemThesaurusList[position]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var dictThesurusInnerAdapter : DictThesurusInnerAdapter = DictThesurusInnerAdapter(ArrayList())
        var textInf: TextView = itemView.findViewById(R.id.tv_item_inf)
        private val rvDictThesurusInner : RecyclerView = itemView.findViewById(R.id.rv_item_thesurus)

        init {
            rvDictThesurusInner.adapter = dictThesurusInnerAdapter
            rvDictThesurusInner.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun setInnerData(item: List<ThesaurusEntry>, take: Int?) {
            dictThesurusInnerAdapter.setData(item.take(take ?: item.size))
        }

    }

}

