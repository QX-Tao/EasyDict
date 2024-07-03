package com.qxtao.easydict.ui.view.ratingbar

import android.graphics.Canvas
import android.graphics.drawable.Drawable


class TileDrawable(var drawable: Drawable) : BaseDrawable() {
    private var mTileCount = -1

    var tileCount: Int
        get() = mTileCount
        set(value) {
            mTileCount = value
            invalidateSelf()
        }

    override fun mutate(): Drawable {
        drawable = drawable.mutate()
        return this
    }

    override fun onDraw(canvas: Canvas, width: Int, height: Int) {
        drawable.alpha = mAlpha
        colorFilterForDrawing?.let {
            drawable.colorFilter = it
        }

        val tileHeight = drawable.intrinsicHeight
        val scale = height.toFloat() / tileHeight
        canvas.scale(scale, scale)
        val scaledWidth = width / scale
        val tileWidth = drawable.intrinsicWidth
        var x = 0

        if (mTileCount < 0) {
            while (x < scaledWidth.toInt()) {
                drawable.setBounds(x, 0, x + tileWidth, tileHeight)
                drawable.draw(canvas)
                x += tileWidth
            }
        } else {
            val tileStepWidth = scaledWidth / mTileCount
            for (i in 0 until mTileCount) {
                val tileCenter = tileStepWidth * (i + 0.5f)
                val halfDrawableWidth = tileWidth / 2.0f
                drawable.setBounds(
                    Math.round(tileCenter - halfDrawableWidth),
                    0,
                    Math.round(tileCenter + halfDrawableWidth),
                    tileHeight
                )
                drawable.draw(canvas)
            }
        }
    }
}
