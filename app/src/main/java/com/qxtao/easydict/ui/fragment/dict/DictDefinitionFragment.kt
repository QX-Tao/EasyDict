package com.qxtao.easydict.ui.fragment.dict

import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.FragmentDictDefinitionBinding
import com.qxtao.easydict.ui.activity.dict.DictActivity
import com.qxtao.easydict.ui.activity.dict.DictViewModel
import com.qxtao.easydict.ui.base.BaseFragment


class DictDefinitionFragment : BaseFragment<FragmentDictDefinitionBinding>(FragmentDictDefinitionBinding::inflate) {
    // define variable
    private lateinit var dictViewModel: DictViewModel

    // define widget
    private lateinit var mtTitle : MaterialToolbar
    override fun bindViews() {
        mtTitle = binding.mtTitle
    }

    override fun initViews() {
        dictViewModel = (activity as DictActivity).getDictViewModel()
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.dict_definition_fragment, DictDetailFragment::class.java, null)
            addToBackStack(null)
        }
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener {
            mListener.onFragmentInteraction("onBackPressed")
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) { //表示是一个进入动作，比如add.show等
            return if (enter) { //普通的进入的动作
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_right)
            } else { //比如一个已经Fragment被另一个replace，是一个进入动作，被replace的那个就是false
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_out_left)
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) { //表示一个退出动作，比如出栈，hide，detach等
            return if (enter) { //之前被replace的重新进入到界面或者Fragment回到栈顶
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_in_left)
            } else { //Fragment退出，出栈
                AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_out_right)
            }
        }
        return null
    }

}