package com.qxtao.easydict.adapter.dict

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R
import com.qxtao.easydict.ui.activity.dict.Inflection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class DictEhHeaderInflectionAdapter(private val inflectionItems: ArrayList<Inflection>) :
    RecyclerView.Adapter<DictEhHeaderInflectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_dict_eh_header_inflection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Inflection = inflectionItems[position]
        holder.textNme.text = item.name
        holder.textValue.text = item.value
    }

    fun setData(data: List<Inflection>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newList = data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                inflectionItems.clear()
                inflectionItems.addAll(newList)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return inflectionItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textNme: TextView = itemView.findViewById(R.id.tv_inflection_name)
        var textValue: TextView = itemView.findViewById(R.id.tv_inflection_value)
    }

}
