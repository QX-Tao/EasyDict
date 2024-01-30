package com.qxtao.easydict.ui.fragment.dict

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionInflater
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.qxtao.easydict.databinding.FragmentDictSearchEditBinding
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.ui.view.PerformEdit


class DictSearchEditFragment : BaseFragment<FragmentDictSearchEditBinding>(FragmentDictSearchEditBinding::inflate) {
    // define variable
    private lateinit var dictViewModel: DictViewModel
    private lateinit var performEdit: PerformEdit
    // define widget
    private lateinit var clEditUnfold: ConstraintLayout
    private lateinit var etEditUnfold: EditText
    private lateinit var ivEditFold: ImageView
    private lateinit var ivRedo: ImageView
    private lateinit var ivUndo: ImageView
    private lateinit var ivBackspace: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(mContext)
            .inflateTransition(android.R.transition.move)
    }

    override fun bindViews() {
        clEditUnfold = binding.clEditUnfold
        etEditUnfold = binding.etEditUnfold
        ivEditFold = binding.ivFold
        ivRedo = binding.ivRedo
        ivUndo = binding.ivUndo
        ivBackspace = binding.ivBackspace
    }

    override fun initViews() {
        dictViewModel = (activity as DictActivity).getDictViewModel()
        mListener.onFragmentInteraction("editTextGetFocus", etEditUnfold)
        performEdit = PerformEdit(etEditUnfold)
        dictViewModel.searchText.observe(this){
            etEditUnfold.setText(it.editSearchText)
            etEditUnfold.setSelection(it.editCursor)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun addListener() {
        ivEditFold.setOnClickListener {
            mListener.onFragmentInteraction("onBackPressed")
        }
        etEditUnfold.doAfterTextChanged {
            ivRedo.isSelected = performEdit.isHasRedos()
            ivUndo.isSelected = performEdit.isHasUndos()
        }
        ivRedo.setOnClickListener { performEdit.redo() }
        ivUndo.setOnClickListener { performEdit.undo() }
        ivBackspace.apply {
            val handler = Handler(Looper.getMainLooper())
            var isDeleting = false
            val deleteRunnable = object : Runnable {
                override fun run() {
                    if (isDeleting) {
                        etEditUnfold.text?.let {
                            if (it.isNotEmpty()) {
                                etEditUnfold.selectionStart.let {it1 ->
                                    if (it1 > 0) it.delete(it1 - 1, it1)
                                }
                            }
                        }
                        handler.postDelayed(this, 50) // 间隔 50 毫秒执行下一次删除
                    }
                }
            }
            setOnClickListener {
                etEditUnfold.text?.let {
                    if (it.isNotEmpty()) {
                        etEditUnfold.selectionStart.let {it1 ->
                            if (it1 > 0) it.delete(it1 - 1, it1)
                        }
                    }
                }
            }
            setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                    // 停止连续删除
                    isDeleting = false
                    handler.removeCallbacks(deleteRunnable)
                }
                false
            }
            setOnLongClickListener {
                // 长按快速删除
                isDeleting = true
                handler.postDelayed(deleteRunnable, 100) // 间隔 100 毫秒执行第一次删除
                true
            }
        }
    }

    override fun onPause() {
        dictViewModel.setSearchText(etEditUnfold.text.toString(), etEditUnfold.selectionStart)
        dictViewModel.setHasShowRvInfo(first = etEditUnfold.text.isEmpty())
        super.onPause()
    }

}