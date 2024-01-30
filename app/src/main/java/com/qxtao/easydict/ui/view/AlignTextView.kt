package com.qxtao.easydict.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.text.StaticLayout
import android.util.AttributeSet
import android.widget.TextView
import com.qxtao.easydict.R

@SuppressLint("AppCompatCustomView")
class AlignTextView : TextView {

    private var alignOnlyOneLine = false

    constructor(context: Context) : super(context) {
        initAttr(context, null)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initAttr(context, attributeSet)
    }

    constructor(
        context: Context,
        attributeSet: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attributeSet, defStyleAttr) {
        initAttr(context, attributeSet)
    }

    private fun initAttr(context: Context, attributeSet: AttributeSet?) {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.AlignTextView)
        alignOnlyOneLine = typedArray.getBoolean(R.styleable.AlignTextView_alignOnlyOneLine, false)
        typedArray.recycle()
    }


    override fun onDraw(canvas: Canvas) {
        if (text !is String) {
            super.onDraw(canvas)
        } else {
            paint.color = currentTextColor
            for (i in 0 until layout.lineCount) {
                val lineBaseline = layout.getLineBaseline(i) + paddingTop
                val lineStart = layout.getLineStart(i)
                val lineEnd = layout.getLineEnd(i)
                if (alignOnlyOneLine && layout.lineCount == 1) {
                    val line = text.substring(lineStart, lineEnd)
                    val width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, paint)
                    this.drawScaledText(canvas, line, lineBaseline.toFloat(), width)
                } else if (i == layout.lineCount - 1) {
                    canvas.drawText(
                        text.substring(lineStart),
                        paddingLeft.toFloat(),
                        lineBaseline.toFloat(),
                        paint
                    )
                    break
                } else {
                    val line = text.substring(lineStart, lineEnd)
                    val width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, paint)
                    this.drawScaledText(canvas, line, lineBaseline.toFloat(), width)
                }
            }
        }
    }

    private fun drawScaledText(canvas: Canvas?, line: String, baseLineY: Float, lineWidth: Float) {
        if (line.isEmpty()) {
            return
        }
        var x = paddingLeft.toFloat()
        val forceNextLine = line[line.length - 1].code == 10
        val length = line.length - 1
        if (forceNextLine || length == 0) {
            canvas?.drawText(line, x, baseLineY, paint)
            return
        }
        val oneTextWidth = (measuredWidth - lineWidth - paddingLeft - paddingRight) / length
        for (element in line) {
            val textStr = element.toString()
            val dw = StaticLayout.getDesiredWidth(textStr, this.paint)
            canvas?.drawText(textStr, x, baseLineY, this.paint)
            x += dw + oneTextWidth
        }
    }
}