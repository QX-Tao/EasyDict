package com.qxtao.easydict.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.qxtao.easydict.ui.view.imageviewer.PhotoView


abstract class BaseFragment<VB : ViewBinding>(val bindingBlock:(LayoutInflater, ViewGroup?,Boolean) -> VB) : Fragment() {

    private lateinit var _binding: VB
    private lateinit var _context: Context
    private var _listener: OnFragmentInteractionListener? = null
    protected val binding get() = _binding
    protected val mContext get() = _context
    protected val mListener get() = _listener!!
    protected val photoView by lazy { PhotoView(requireActivity()) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
        _listener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(
                "$context must implement OnFragmentInteractionListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        _listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(vararg data: Any?)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        onCreateView()
        _binding = bindingBlock(inflater,container,false)
        return _binding.root
    }

    protected open fun onCreateView() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        onViewCreated1(view, savedInstanceState)
        initViews()
        addListener()
    }

    protected open fun onViewCreated1(view: View, savedInstanceState: Bundle?) {}

    /**
     * Callback [bindViews] method
     *
     * 回调 [bindViews] 方法
     */
    protected open fun bindViews(){}

    /**
     * Callback [initViews] method
     *
     * 回调 [initViews] 方法
     */
    protected open fun initViews(){}

    /**
     * Callback [addListener] method
     *
     * 回调 [addListener] 方法
     */
    protected open fun addListener() {}

    /**
     * show short toast
     *
     * Toast通知
     */
    protected fun showShortToast(str: String) {
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show()
    }

    /**
     * show long toast
     *
     * Toast通知
     */
    protected fun showLongToast(str: String) {
        Toast.makeText(mContext, str, Toast.LENGTH_LONG).show()
    }

}