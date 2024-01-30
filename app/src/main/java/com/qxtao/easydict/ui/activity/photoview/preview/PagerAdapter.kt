package com.qxtao.easydict.ui.activity.photoview.preview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.qxtao.easydict.R


class PagerAdapter<E>(private val context: Context, private val list: ArrayList<E>?) :
    RecyclerView.Adapter<PagerAdapter.PVViewHolder>() {
    private var ifOnBind: OnBind<E>? = null

    interface OnBind<in E> {
        fun onBind(holder: PVViewHolder, position: Int, data: E?)
    }

    private fun setOnBind(ifOnBind: OnBind<E>) {
        this.ifOnBind = ifOnBind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PVViewHolder {
        return PVViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_pager_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PVViewHolder, position: Int) {
        ifOnBind?.onBind(holder, position, list?.get(position))
    }


    fun onBind(l: (holder: PVViewHolder, position: Int, data: E?) -> Unit) {
        setOnBind(object : OnBind<E> {
            override fun onBind(holder: PVViewHolder, position: Int, data: E?) {
                l(holder, position, data)
            }
        })
    }

    override fun getItemCount(): Int = list?.size ?: 0

    class PVViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: PhotoView = view.findViewById(R.id.image_content) as PhotoView
        var loadingFail: LinearLayout = view.findViewById(R.id.ll_loading_fail) as LinearLayout
        var loading: LinearLayout = view.findViewById(R.id.ll_loading) as LinearLayout
        var progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        init {
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }
}