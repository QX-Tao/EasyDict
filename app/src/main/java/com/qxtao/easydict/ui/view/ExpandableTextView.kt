package com.qxtao.easydict.ui.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import com.qxtao.easydict.R

class ExpandableTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatTextView(context, attrs, defStyleAttr) {

    private var mOriginText: CharSequence? = null
    private var mExpandedText: SpannableStringBuilder = createSpannableStringBuilder("")
    private var mFoldedText: SpannableStringBuilder = createSpannableStringBuilder("")

    private var mMeasuredWidth: Int = 0
    private var mFoldedHeight: Int = 0 //折叠后的高度
    private var mFoldAnimator: Animator? = null //折叠动画
    private var mExpandedHeight: Int = 0
    private var mExpandAnimator: Animator? = null

    /**
     * 折叠行数阈值，本文行数超过阈值时才可已折叠
     */
    private val mFoldedLines: Int
    private val mSuffixTextColor: Int
    private val mFoldedSuffix: SpannableString //折叠状态下的文本后缀
    private val mExpandedSuffix: SpannableString//展开状态下的文本后缀

    private var canFold: Boolean = true
    private var isFolded: Boolean = false

    private val mDuration: Long
    private var isAnimating: Boolean = false

    @Suppress("unused")
    var layoutHeight: Int = 0
        set(value) {
            field = value
            layoutParams.height = value
            requestLayout()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val typedValue = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        mOriginText = typedValue.getString(R.styleable.ExpandableTextView_expandableText)
        mDuration = typedValue.getInt(
            R.styleable.ExpandableTextView_expandDuration,
            DEFAULT_DURATION_TIME
        ).toLong()
        mFoldedLines = typedValue.getInt(
            R.styleable.ExpandableTextView_foldLines,
            DEFAULT_EXPANDABLE_LINES
        )
        mSuffixTextColor = typedValue.getColor(
            R.styleable.ExpandableTextView_suffixTextColor,
            DEFAULT_SUFFIX_TEXT_COLOR
        )
        val foldSuffix = typedValue.getString(R.styleable.ExpandableTextView_foldedSuffix)
            ?: DEFAULT_ACTION_TEXT_EXPAND
        mFoldedSuffix = createClickedSpannableString(
            ELLIPSIS_STRING + foldSuffix,
            ELLIPSIS_STRING.length
        )
        val expandText = typedValue.getString(R.styleable.ExpandableTextView_expandedSuffix)
            ?: DEFAULT_ACTION_TEXT_FOLD
        mExpandedSuffix = createClickedSpannableString(expandText)
        //取消clickableSpan点击背景
//        highlightColor = Color.argb(0, 0, 0, 0)
        typedValue.recycle()
    }

    /**
     * 设置原始文本
     * 若文本所需显示的函数小于[mFoldedLines]，则不会做任何处理，
     * 否则文本可以展开与折叠
     *
     * @param text TextView所需显示的原始文本
     */
    fun setExpandableText(text: CharSequence) {
        mOriginText = text
        if (mMeasuredWidth <= 0) return
        val layout = createStaticLayout(text)
        canFold = layout.lineCount > mFoldedLines
        isFolded = canFold
        if (canFold) {
            buildExpandableText(layout)
            setText(if (isFolded) mFoldedText else mExpandedText)
        } else {
            setText(mOriginText)
        }
    }

    /**
     * 展开/折叠
     */
    fun toggleExpand() {
        if (!canFold || isAnimating) return
        isFolded = !isFolded
        if (isFolded) {
            mFoldAnimator?.start()
        } else {
            mExpandAnimator?.start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if ((mMeasuredWidth == 0 || mMeasuredWidth != measuredWidth) && !isAnimating) {
            mMeasuredWidth = measuredWidth
            if (canFold) mOriginText?.let { setExpandableText(it) }
        }
    }

    /**
     * 重写方法以支持ClickSpan的点击事件
     * 直接设置LinkMovementMethod的话会导致TextView可以滑动，当执行折叠动画时整个文本会被向上推，达不到预期效果
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val curText = text
        val action = event?.action
        when {
            curText is Spanned && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) -> {
                val x = (event.x - totalPaddingLeft + scrollX).toInt()
                val y = (event.y - totalPaddingTop + scrollY).toInt()
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())
                val link = curText.getSpans(off, off, ClickableSpan::class.java)
                if (link.isNotEmpty()) {
                    if (action == MotionEvent.ACTION_UP) link[0].onClick(this)
                    return true
                }
            }
            else -> return super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    /**
     * 构建展开/折叠状态下的文本
     */
    private fun buildExpandableText(originLayout: Layout) {
        if (TextUtils.isEmpty(mOriginText)) return
        mExpandedText = createSpannableStringBuilder(mOriginText!!).append(mExpandedSuffix)
        mExpandedHeight = createStaticLayout(mExpandedText).height + paddingTop + paddingBottom

        val lineEnd = originLayout.getLineEnd(mFoldedLines - 1)
        var foldText = mOriginText!!.subSequence(0, lineEnd)
        var builder = createSpannableStringBuilder(foldText).append(mFoldedSuffix)
        while (createStaticLayout(builder).lineCount > mFoldedLines) {
            foldText = foldText.subSequence(0, foldText.length - 1)
            builder = createSpannableStringBuilder(foldText).append(mFoldedSuffix)
        }
//        val foldText =
//            mOriginText!!.subSequence(0, lineEnd - mFoldedSuffix.length - 1)
//        mFoldedText = createSpannableStringBuilder(foldText).append(mFoldedSuffix)
        mFoldedText = createSpannableStringBuilder(builder)
        mFoldedHeight = createStaticLayout(mFoldedText).height + paddingTop + paddingBottom

        mFoldAnimator = createAnimation(mExpandedHeight, mFoldedHeight, null) {
            text = mFoldedText
        }
        mExpandAnimator = createAnimation(mFoldedHeight, mExpandedHeight, {
            text = mExpandedText
        }, null)
    }

    /**
     * 根据[source]创建一个[StaticLayout]对象，用于辅助计算文本可显示行数、高度等
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun <T : CharSequence> createStaticLayout(source: T): Layout =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(source, 0, source.length, paint, mMeasuredWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(includeFontPadding)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                source,
                paint,
                mMeasuredWidth,
                Layout.Alignment.ALIGN_NORMAL,
                lineSpacingMultiplier,
                lineSpacingExtra,
                includeFontPadding
            )
        }

    private fun createClickedSpannableString(
        charSequence: CharSequence, start: Int = 0
    ): SpannableString = SpannableString(charSequence).apply {
        setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                toggleExpand()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = mSuffixTextColor
                ds.isUnderlineText = false
            }
        }, start, charSequence.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }

    private fun createSpannableStringBuilder(charSequence: CharSequence) =
        SpannableStringBuilder(charSequence)

    private fun createAnimation(
        start: Int,
        end: Int,
        startCallback: (() -> Unit)?,
        endCallback: (() -> Unit)?
    ): ObjectAnimator {
        val animator = ObjectAnimator.ofInt(this, "layoutHeight", start, end)
        animator.duration = mDuration
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isAnimating = true
                startCallback?.invoke()
            }

            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
                endCallback?.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }

        })
        return animator
    }

    companion object {
        val ELLIPSIS_STRING = String(charArrayOf('\u2026')) //省略号
        const val DEFAULT_EXPANDABLE_LINES = 4
        const val DEFAULT_DURATION_TIME = 300
        val DEFAULT_SUFFIX_TEXT_COLOR = Color.rgb(255, 97, 46)

        const val DEFAULT_ACTION_TEXT_FOLD = "收起"
        const val DEFAULT_ACTION_TEXT_EXPAND = "展开"
    }

}