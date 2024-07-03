package com.qxtao.easydict.ui.view.ratingbar

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import com.qxtao.easydict.R

class AndRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RatingBar(context, attrs, defStyleAttr), OnRatingBarChangeListener {
    private var mStarColor: ColorStateList? = null
    private var mSubStarColor: ColorStateList? = null
    private var mBgColor: ColorStateList? = null
    private var mStarDrawable = 0
    private var mBgDrawable = 0
    private var mKeepOriginColor = false
    private var scaleFactor = 0f
    private var starSpacing = 0f
    private var right2Left = false
    private var mDrawable: StarDrawable? = null
    private var mOnRatingChangeListener: OnRatingChangeListener? = null
    private var mTempRating = 0f

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AndRatingBar, defStyleAttr, 0)
        right2Left = typedArray.getBoolean(R.styleable.AndRatingBar_right2Left, false)
        mStarColor = if (right2Left) typedArray.getColorStateList(R.styleable.AndRatingBar_bgColor)
        else typedArray.getColorStateList(R.styleable.AndRatingBar_starColor)

        mBgColor = if (right2Left) typedArray.getColorStateList(R.styleable.AndRatingBar_starColor)
        else typedArray.getColorStateList(R.styleable.AndRatingBar_bgColor)

        if (!right2Left && typedArray.hasValue(R.styleable.AndRatingBar_subStarColor)) {
            mSubStarColor = typedArray.getColorStateList(R.styleable.AndRatingBar_subStarColor)
        }

        mKeepOriginColor = typedArray.getBoolean(R.styleable.AndRatingBar_keepOriginColor, false)
        scaleFactor = typedArray.getFloat(R.styleable.AndRatingBar_scaleFactor, 1.0f)
        starSpacing = typedArray.getDimension(R.styleable.AndRatingBar_starSpacing, 0.0f)
        mStarDrawable = typedArray.getResourceId(R.styleable.AndRatingBar_starDrawable, R.drawable.ic_star_solid)
        mBgDrawable = typedArray.getResourceId(R.styleable.AndRatingBar_bgDrawable, mStarDrawable)

        typedArray.recycle()

        val starAttr = RattingAttr(context, numStars, mBgDrawable, mStarDrawable, mBgColor, mSubStarColor, mStarColor, mKeepOriginColor)
        mDrawable = StarDrawable(starAttr)
        progressDrawable = mDrawable
    }

    override fun setRating(rating: Float) {
        super.setRating(if (right2Left) numStars.toFloat() - rating else rating)
    }

    override fun setNumStars(numStars: Int) {
        super.setNumStars(numStars)
        mDrawable?.setStarCount(numStars)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = measuredHeight
        val width = (height * mDrawable!!.tileRatio * numStars * scaleFactor + (numStars - 1) * starSpacing).toInt()
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0), height)
    }

    fun setOnRatingChangeListener(listener: OnRatingChangeListener?) {
        mOnRatingChangeListener = listener
        onRatingBarChangeListener = this
    }

    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        mOnRatingChangeListener?.let {
            if (rating != mTempRating) {
                val adjustedRating = if (right2Left) numStars.toFloat() - rating else rating
                it.onRatingChanged(this, adjustedRating, fromUser)
            }
        }
        mTempRating = rating
    }

    fun setScaleFactor(scaleFactor: Float) {
        this.scaleFactor = scaleFactor
        requestLayout()
    }

    fun setStarSpacing(starSpacing: Float) {
        this.starSpacing = starSpacing
        requestLayout()
    }

    interface OnRatingChangeListener {
        fun onRatingChanged(ratingBar: AndRatingBar?, rating: Float, fromUser: Boolean)
    }
}