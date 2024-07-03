package com.qxtao.easydict.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.qxtao.easydict.R
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.SizeUtils

class LoadFailedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val hintImageView: ImageView
    private val hintTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_load_failed, this, true)

        hintImageView = findViewById(R.id.imageView)
        hintTextView = findViewById(R.id.textView)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadFailedView,
            0, 0
        ).apply {
            try {
                val hintDrawable = getDrawable(R.styleable.LoadFailedView_loadFailedViewHintDrawable) ?: context.getDrawable(R.drawable.ic_load_failed)
                val hintDrawableSize = getDimensionPixelSize(R.styleable.LoadFailedView_loadFailedViewHintDrawableSize, SizeUtils.dp2px(48f))
                val hintText = getText(R.styleable.LoadFailedView_loadFailedViewHintText) ?: context.getString(R.string.loading_failure)
                val hintTextColor = getColor(R.styleable.LoadFailedView_loadFailedViewHintTextColor, ColorUtils.colorOnSurface(context))
                val hintTextSize = getDimensionPixelSize(R.styleable.LoadFailedView_loadFailedViewHintTextSize, resources.getDimensionPixelSize(R.dimen.text_size_normal))
                val showHintText = getBoolean(R.styleable.LoadFailedView_showLoadFailedViewHintText, true)
                val showHintDrawable = getBoolean(R.styleable.LoadFailedView_showLoadFailedViewHintDrawable, true)

                hintImageView.setImageDrawable(hintDrawable)
                hintImageView.layoutParams.width = hintDrawableSize
                hintImageView.layoutParams.height = hintDrawableSize
                hintTextView.text = hintText
                hintTextView.setTextColor(hintTextColor)
                hintTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, hintTextSize.toFloat())
                hintImageView.visibility = if (showHintDrawable) VISIBLE else GONE
                hintTextView.visibility = if (showHintText) VISIBLE else GONE
            } finally {
                recycle()
            }
        }
    }

    fun setHintText(text: String) {
        hintTextView.text = text
    }

    fun setHintDrawable(drawable: Int) {
        hintImageView.setImageResource(drawable)
    }

    fun showHintText(show: Boolean) {
        hintTextView.visibility = if (show) VISIBLE else GONE
    }

    fun showHintDrawable(show: Boolean) {
        hintImageView.visibility = if (show) VISIBLE else GONE
    }
}