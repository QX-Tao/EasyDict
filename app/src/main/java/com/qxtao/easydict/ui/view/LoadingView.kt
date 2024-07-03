package com.qxtao.easydict.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.qxtao.easydict.R
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.SizeUtils


class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val progressBar: ProgressBar
    private val hintTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_loading_view, this, true)

        progressBar = findViewById(R.id.progressBar)
        hintTextView = findViewById(R.id.textView)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingView,
            0, 0
        ).apply {
            try {
                val progressBarSize = getDimensionPixelSize(R.styleable.LoadingView_loadingViewProgressBarSize, SizeUtils.dp2px(48f))
                val progressBarColor = getColor(R.styleable.LoadingView_loadingViewProgressBarColor, ColorUtils.colorSecondary(context))
                val hintText = getText(R.styleable.LoadingView_loadingViewHintText) ?: context.getString(R.string.loading)
                val hintTextColor = getColor(R.styleable.LoadingView_loadingViewHintTextColor, ColorUtils.colorOnSurface(context))
                val hintTextSize = getDimensionPixelSize(R.styleable.LoadingView_loadingViewHintTextSize, resources.getDimensionPixelSize(R.dimen.text_size_normal))
                val showHintText = getBoolean(R.styleable.LoadingView_showLoadingViewHintText, true)
                val showProgressBar = getBoolean(R.styleable.LoadingView_showLoadingViewProgressBar, true)

                progressBar.layoutParams.width = progressBarSize
                progressBar.layoutParams.height = progressBarSize
                progressBar.indeterminateDrawable.setTint(progressBarColor)
                hintTextView.text = hintText
                hintTextView.setTextColor(hintTextColor)
                hintTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, hintTextSize.toFloat())
                hintTextView.visibility = if (showHintText) VISIBLE else GONE
                progressBar.visibility = if (showProgressBar) VISIBLE else GONE
            } finally {
                recycle()
            }
        }
    }

    fun setHintText(text: String) {
        hintTextView.text = text
    }

    fun showProgressBar(show: Boolean) {
        progressBar.visibility = if (show) VISIBLE else GONE
    }

    fun showHintText(show: Boolean) {
        hintTextView.visibility = if (show) VISIBLE else GONE
    }
}