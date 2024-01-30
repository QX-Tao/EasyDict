package com.qxtao.easydict.ui.view

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.util.Stack

open class PerformEdit(private val editText: EditText) {
    var index = 0
    var history = Stack<Action>() // 撤销栈
    var historyBack = Stack<Action>() // 恢复栈
    private var editable: Editable = editText.text
    private var flag = false

    init {
        checkNull(editText, "EditText is not null")
        editText.addTextChangedListener(Watcher())
    }

    protected open fun onEditableChanged(s: Editable?) {}
    protected open fun onTextChanged(s: Editable?) {}

    fun clearHistory() {
        history.clear()
        historyBack.clear()
    }

    fun undo() {
        if (history.empty()) return
        flag = true
        val action = history.pop()
        historyBack.push(action)
        if (action.isAdd) {
            editable.delete(action.startCursor, action.startCursor + action.actionTarget.length)
            editText.setSelection(action.startCursor, action.startCursor)
        } else {
            editable.insert(action.startCursor, action.actionTarget)
            if (action.endCursor == action.startCursor) {
                editText.setSelection(action.startCursor + action.actionTarget.length)
            } else editText.setSelection(action.startCursor, action.endCursor)
        }
        flag = false
        if (!history.empty() && history.peek().index == action.index) undo()
    }

    fun redo() {
        if (historyBack.empty()) return
        flag = true
        val action = historyBack.pop()
        history.push(action)
        if (action.isAdd) {
            editable.insert(action.startCursor, action.actionTarget)
            if (action.endCursor == action.startCursor) {
                editText.setSelection(action.startCursor + action.actionTarget.length)
            } else
                editText.setSelection(action.startCursor, action.endCursor)
        } else {
            editable.delete(action.startCursor, action.startCursor + action.actionTarget.length)
            editText.setSelection(action.startCursor, action.startCursor)
        }
        flag = false
        if (!historyBack.empty() && historyBack.peek().index == action.index) redo()
    }

    fun setDefaultText(text: CharSequence?) {
        clearHistory()
        flag = true
        editable.replace(0, editable.length, text)
        flag = false
    }

    fun isHasUndos() =  !history.empty()

    fun isHasRedos() = !historyBack.empty()

    private inner class Watcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (flag) return
            val end = start + count
            if (end > start && end <= s.length) {
                val charSequence = s.subSequence(start, end)
                if (charSequence.isNotEmpty()) {
                    val action = Action(charSequence, start, false)
                    if (count > 1 || (count == 1 && count == after)) {
                        action.setSelectCount(count)
                    }
                    history.push(action)
                    historyBack.clear()
                    action.index = ++index
                }
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (flag) return
            val end = start + count
            if (end > start) {
                val charSequence = s.subSequence(start, end)
                if (charSequence.isNotEmpty()) {
                    val action = Action(charSequence, start, true)
                    history.push(action)
                    historyBack.clear()
                    action.index = if (before > 0) index else ++index
                }
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (flag) return
            if (s !== editable) {
                editable = s
                onEditableChanged(s)
            }
            this@PerformEdit.onTextChanged(s)
        }
    }

    inner class Action(
        var actionTarget: CharSequence,
        var startCursor: Int,
        add: Boolean
    ) {
        var endCursor: Int = startCursor
        var isAdd: Boolean = add
        var index = 0

        fun setSelectCount(count: Int) {
            endCursor += count
        }
    }

    companion object {
        private fun checkNull(o: Any?, message: String) {
            checkNotNull(o) { message }
        }
    }
}
