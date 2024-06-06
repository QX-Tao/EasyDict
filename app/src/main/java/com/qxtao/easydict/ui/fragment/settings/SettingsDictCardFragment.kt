package com.qxtao.easydict.ui.fragment.settings

import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.materialswitch.MaterialSwitch
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.FragmentSettingsDictCardBinding
import com.qxtao.easydict.ui.activity.settings.SettingsActivity
import com.qxtao.easydict.ui.activity.settings.SettingsViewModel
import com.qxtao.easydict.ui.base.BaseFragment
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_AUTH_SENTS
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_BAIKE_DIGEST
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_BLNG_SENTS
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_ETYM
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_EXTERNAL
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_PHRASE
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_REL_WORD
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_SPECIAL
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_THESAURUS
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_WEB_TRANS

class SettingsDictCardFragment : BaseFragment<FragmentSettingsDictCardBinding>(FragmentSettingsDictCardBinding::inflate) {
    // define variable
    private lateinit var settingsViewModel: SettingsViewModel
    // define widget
    private lateinit var clBlngSents: ConstraintLayout
    private lateinit var clAuthSents: ConstraintLayout
    private lateinit var clWebTrans: ConstraintLayout
    private lateinit var clSpecial: ConstraintLayout
    private lateinit var clThesaurus: ConstraintLayout
    private lateinit var clPhrase: ConstraintLayout
    private lateinit var clRelWord: ConstraintLayout
    private lateinit var clEtym: ConstraintLayout
    private lateinit var clBaikeDigest: ConstraintLayout
    private lateinit var clExternal: ConstraintLayout
    private lateinit var swBlngSents: MaterialSwitch
    private lateinit var swAuthSents: MaterialSwitch
    private lateinit var swWebTrans: MaterialSwitch
    private lateinit var swSpecial: MaterialSwitch
    private lateinit var swThesaurus: MaterialSwitch
    private lateinit var swPhrase: MaterialSwitch
    private lateinit var swRelWord: MaterialSwitch
    private lateinit var swEtym: MaterialSwitch
    private lateinit var swBaikeDigest: MaterialSwitch
    private lateinit var swExternal: MaterialSwitch

    override fun bindViews() {
        clBlngSents = binding.clBlngSents
        clAuthSents = binding.clAuthSents
        clWebTrans = binding.clWebTrans
        clSpecial = binding.clSpecial
        clThesaurus = binding.clThesaurus
        clPhrase = binding.clPhrase
        clRelWord = binding.clRelWord
        clEtym = binding.clEtym
        clBaikeDigest = binding.clBaikeDigest
        clExternal = binding.clExternal
        swBlngSents = binding.swBlngSents
        swAuthSents = binding.swAuthSents
        swWebTrans = binding.swWebTrans
        swSpecial = binding.swSpecial
        swThesaurus = binding.swThesaurus
        swPhrase = binding.swPhrase
        swRelWord = binding.swRelWord
        swEtym = binding.swEtym
        swBaikeDigest = binding.swBaikeDigest
        swExternal = binding.swExternal
    }

    override fun initViews() {
        settingsViewModel = (activity as SettingsActivity).getSettingsViewModel()
        swBlngSents.isChecked = ShareUtils.getBoolean(mContext, IS_USE_BLNG_SENTS, true)
        swAuthSents.isChecked = ShareUtils.getBoolean(mContext, IS_USE_AUTH_SENTS, true)
        swWebTrans.isChecked = ShareUtils.getBoolean(mContext, IS_USE_WEB_TRANS, true)
        swSpecial.isChecked = ShareUtils.getBoolean(mContext, IS_USE_SPECIAL, true)
        swThesaurus.isChecked = ShareUtils.getBoolean(mContext, IS_USE_THESAURUS, true)
        swPhrase.isChecked = ShareUtils.getBoolean(mContext, IS_USE_PHRASE, true)
        swRelWord.isChecked = ShareUtils.getBoolean(mContext, IS_USE_REL_WORD, true)
        swEtym.isChecked = ShareUtils.getBoolean(mContext, IS_USE_ETYM, true)
        swBaikeDigest.isChecked = ShareUtils.getBoolean(mContext, IS_USE_BAIKE_DIGEST, true)
        swExternal.isChecked = ShareUtils.getBoolean(mContext, IS_USE_EXTERNAL, true)
    }

    override fun addListener() {
        binding.mtTitle.setNavigationOnClickListener{
            mListener.onFragmentInteraction("onBackPressed")
        }
        clBlngSents.setOnClickListener { swBlngSents.isChecked = !swBlngSents.isChecked }
        clAuthSents.setOnClickListener { swAuthSents.isChecked = !swAuthSents.isChecked }
        clWebTrans.setOnClickListener { swWebTrans.isChecked = !swWebTrans.isChecked }
        clSpecial.setOnClickListener { swSpecial.isChecked = !swSpecial.isChecked }
        clThesaurus.setOnClickListener { swThesaurus.isChecked = !swThesaurus.isChecked }
        clPhrase.setOnClickListener { swPhrase.isChecked = !swPhrase.isChecked }
        clRelWord.setOnClickListener { swRelWord.isChecked = !swRelWord.isChecked }
        clEtym.setOnClickListener { swEtym.isChecked = !swEtym.isChecked }
        clBaikeDigest.setOnClickListener { swBaikeDigest.isChecked = !swBaikeDigest.isChecked }
        clExternal.setOnClickListener { swExternal.isChecked = !swExternal.isChecked }

        swBlngSents.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_BLNG_SENTS, isChecked) }
        swAuthSents.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_AUTH_SENTS, isChecked) }
        swWebTrans.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_WEB_TRANS, isChecked) }
        swSpecial.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_SPECIAL, isChecked) }
        swThesaurus.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_THESAURUS, isChecked) }
        swPhrase.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_PHRASE, isChecked) }
        swRelWord.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_REL_WORD, isChecked) }
        swEtym.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_ETYM, isChecked) }
        swBaikeDigest.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_BAIKE_DIGEST, isChecked) }
        swExternal.setOnCheckedChangeListener { _, isChecked -> ShareUtils.putBoolean(mContext, IS_USE_EXTERNAL, isChecked) }
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