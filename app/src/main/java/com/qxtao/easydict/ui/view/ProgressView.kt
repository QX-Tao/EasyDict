package com.qxtao.easydict.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.qxtao.easydict.utils.common.ColorUtils
import com.qxtao.easydict.utils.common.SizeUtils

class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    @ColorInt private var mColor = ColorUtils.colorSecondary(context)
    private var mAnimator: Animator? = null
    private val mTargetWidth: Int = context.resources.displayMetrics.widthPixels
    private var mTargetHeight: Int = SizeUtils.dp2px(WEB_PROGRESS_DEFAULT_HEIGHT.toFloat())
    private var mTag = UN_START //当前进度条标志
    private var isShow = false
    private var mCurrentProgress = 0f
    private var mPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = mColor
        isDither = true
        strokeCap = Paint.Cap.SQUARE
    }

    companion object {
        //默认匀速动画最大的时长
        private const val MAX_UNIFORM_SPEED_DURATION: Int = 8 * 1000
        //默认加速后减速动画最大时长
        private const val MAX_DECELERATE_SPEED_DURATION: Int = 450
        //95f-100f时，透明度1f-0f时长
        private const val DO_END_ALPHA_DURATION: Int = 630
        //95f - 100f动画时长
        private const val DO_END_PROGRESS_DURATION: Int = 500
        //当前匀速动画最大的时长
        private var CURRENT_MAX_UNIFORM_SPEED_DURATION = MAX_UNIFORM_SPEED_DURATION
        //当前加速后减速动画最大时长
        private var CURRENT_MAX_DECELERATE_SPEED_DURATION = MAX_DECELERATE_SPEED_DURATION
        //默认的高度(dp)
        private const val WEB_PROGRESS_DEFAULT_HEIGHT: Int = 3

        private const val UN_START: Int = 0
        private const val STARTED: Int = 1
        private const val FINISH: Int = 2
    }

    init {
        setWillNotDraw(false)
    }

    //设置单色进度条
    fun setColor(@ColorInt color: Int) {
        mColor = color
        mPaint.color = color
    }
    fun setColor(color: String) {
        setColor(Color.parseColor(color))
    }

    //设置渐变色进度条
    fun setColor(@ColorInt startColor: Int, @ColorInt endColor: Int) {
        mPaint.shader  = LinearGradient(
            0f, 0f, mTargetWidth.toFloat(), mTargetHeight.toFloat(),
            startColor, endColor, Shader.TileMode.CLAMP
        )
    }
    fun setColor(startColor: String, endColor: String) {
        setColor(Color.parseColor(startColor), Color.parseColor(endColor))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            mTargetHeight } else { MeasureSpec.getSize(heightMeasureSpec) }
        setMeasuredDimension(width, height)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mCurrentProgress / 100 * width.toFloat(), height.toFloat(), mPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val screenWidth = context.resources.displayMetrics.widthPixels
        val rate = w.toFloat() / screenWidth
        CURRENT_MAX_UNIFORM_SPEED_DURATION = (MAX_UNIFORM_SPEED_DURATION * rate).toInt()
        CURRENT_MAX_DECELERATE_SPEED_DURATION = (MAX_DECELERATE_SPEED_DURATION * rate).toInt()
    }

    private fun setFinish() {
        isShow = false
        mTag = FINISH
    }

    private fun startAnim(isFinished: Boolean) {
        val targetProgress = if (isFinished) 100f else 95f
        mAnimator?.takeIf { it.isStarted }?.cancel()

        mCurrentProgress = if (mCurrentProgress == 0f) 0.00000001f else mCurrentProgress
        // 可能由于透明度造成突然出现的问题
        alpha = 1f

        if (!isFinished) {
            val mAnimator = ValueAnimator.ofFloat(mCurrentProgress, targetProgress)
            val residue = 1f - mCurrentProgress / 100 - 0.05f
            mAnimator.interpolator = LinearInterpolator()
            mAnimator.setDuration((residue * CURRENT_MAX_UNIFORM_SPEED_DURATION).toLong())
            mAnimator.addUpdateListener(mAnimatorUpdateListener)
            mAnimator.start()
            this.mAnimator = mAnimator
        } else {
            val segment95Animator = if (mCurrentProgress < 95) {
                ValueAnimator.ofFloat(mCurrentProgress, 95f).apply {
                    val residue = 1f - mCurrentProgress / 100f - 0.05f
                    interpolator = DecelerateInterpolator()
                    duration = ((residue * CURRENT_MAX_DECELERATE_SPEED_DURATION).toLong())
                    addUpdateListener(mAnimatorUpdateListener)
                }
            } else null

            val alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
                duration = DO_END_ALPHA_DURATION.toLong()
            }
            val endProgressAnimator = ValueAnimator.ofFloat(95f, 100f).apply {
                duration = DO_END_PROGRESS_DURATION.toLong()
                addUpdateListener(mAnimatorUpdateListener)
            }
            val mainAnimatorSet = AnimatorSet().apply {
                playTogether(alphaAnimator, endProgressAnimator)
            }
            val fullAnimatorSet = if (segment95Animator != null) {
                AnimatorSet().apply {
                    play(mainAnimatorSet).after(segment95Animator)
                } } else { mainAnimatorSet }
            fullAnimatorSet.addListener(mAnimatorListenerAdapter)
            fullAnimatorSet.start()
            mAnimator = fullAnimatorSet
        }

        mTag = STARTED
    }

    private val mAnimatorUpdateListener =
        AnimatorUpdateListener { animation ->
            mCurrentProgress = animation.animatedValue as Float
            invalidate()
        }

    private val mAnimatorListenerAdapter: AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                doEnd()
            }
        }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAnimator?.takeIf { it.isStarted }?.cancel()
        mAnimator = null
    }

    private fun doEnd() {
        if (mTag == FINISH && mCurrentProgress == 100f) {
            visibility = GONE
            mCurrentProgress = 0f
            this.alpha = 1f
        }
        mTag = UN_START
    }

    fun reset() {
        mCurrentProgress = 0f
        mAnimator?.takeIf { it.isStarted }?.cancel()
    }

    fun setProgress(newProgress: Int) {
        setProgress(newProgress.toFloat())
    }


    fun offerLayoutParams(): LayoutParams {
        return LayoutParams(mTargetWidth, mTargetHeight)
    }

    fun setHeight(heightDp: Int): ProgressView {
        this.mTargetHeight = SizeUtils.dp2px(heightDp.toFloat())
        return this
    }

    fun setProgress(progress: Float) {
        when {
            mTag == UN_START && progress == 100f -> {
                visibility = GONE
            }
            visibility == GONE -> {
                visibility = VISIBLE
            }
            progress < 95 -> return
            mTag != FINISH -> startAnim(true)
        }
    }

    /**
     * 显示进度条
     */
    fun show() {
        isShow = true
        visibility = VISIBLE
        mCurrentProgress = 0f
        startAnim(false)
    }

    /**
     * 进度完成后消失
     */
    fun hide() {
        setWebProgress(100)
    }

    /**
     * 为单独处理WebView进度条
     */
    fun setWebProgress(newProgress: Int) {
        when {
            newProgress in 0..94 -> {
                if (!isShow) show() else setProgress(newProgress)
            }
            else -> {
                setProgress(newProgress)
                setFinish()
            }
        }
    }
}