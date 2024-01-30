package com.qxtao.easydict.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText

class LimitEditText : AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var commitTextListener: ICommitTextListener? = null


    /**
     * 输入法
     * @param outAttrs
     * @return
     */
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        return InnerInputConnection(
            super.onCreateInputConnection(outAttrs),
            false
        )
    }

    internal inner class InnerInputConnection(target: InputConnection?, mutable: Boolean) :
        InputConnectionWrapper(target, mutable),
        InputConnection {

        /**
         * 对输入的内容进行拦截
         *
         * @param text
         * @param newCursorPosition
         * @return
         */
        override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
            val filteredInput = commitTextListener?.onCommitText(text)
            return super<InputConnectionWrapper>.commitText(filteredInput, newCursorPosition)
        }

        override fun sendKeyEvent(event: KeyEvent?): Boolean {
            return super.sendKeyEvent(event)
        }

        override fun setSelection(start: Int, end: Int): Boolean {
            return super.setSelection(start, end)
        }
    }

    interface ICommitTextListener{
        fun onCommitText(text: CharSequence): CharSequence
    }

    fun commitTextListener(listener: ICommitTextListener) {
        commitTextListener = listener
    }

}