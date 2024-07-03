package com.qxtao.easydict.ui.view.ratingbar

import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable


class StarDrawable(rattingAttr: RattingAttr) : LayerDrawable(rattingAttr.layerList) {
    init {
        setId(0, android.R.id.background)
        setId(1, android.R.id.secondaryProgress)
        setId(2, android.R.id.progress)
        initStyle(rattingAttr)
    }

    private fun initStyle(rattingAttr: RattingAttr) {
        with(rattingAttr) {
            listOf(
                getTileDrawableByLayerId(android.R.id.background) to bgColor,
                getTileDrawableByLayerId(android.R.id.secondaryProgress) to secondaryStarColor,
                getTileDrawableByLayerId(android.R.id.progress) to starColor
            ).forEach { (tileDrawable, color) ->
                tileDrawable?.apply {
                    tileCount = starCount
                    color?.let { setTintList(it) }
                }
            }
        }
    }

    val tileRatio: Float
        get() {
            val drawable = getTileDrawableByLayerId(android.R.id.progress)!!.drawable
            return drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
        }

    fun setStarCount(count: Int) {
        listOf(
            getTileDrawableByLayerId(android.R.id.background),
            getTileDrawableByLayerId(android.R.id.secondaryProgress),
            getTileDrawableByLayerId(android.R.id.progress)
        ).forEach { it?.tileCount = count }
    }

    private fun getTileDrawableByLayerId(id: Int): TileDrawable? {
        val layerDrawable = findDrawableByLayerId(id)
        return when (id) {
            android.R.id.background -> layerDrawable as? TileDrawable
            android.R.id.progress, android.R.id.secondaryProgress -> {
                val clipDrawable = layerDrawable as ClipDrawable
                clipDrawable.drawable as? TileDrawable
            }
            else -> throw RuntimeException("Unknown layer ID")
        }
    }
}
