package com.qxtao.easydict.ui.view.imageviewer

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.*
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.qxtao.easydict.ui.view.imageviewer.photoview.PhotoView

class ImageViewer : OnDragChangeListener {

    var context: Context? = null
    var imageLoader: ImageLoader? = null
    /**动画时长*/
    var animDuration: Long = 300L
    /**大图模式的背景色*/
    @ColorInt var bgColor: Int = Color.BLACK
    /**蒙层模块*/
    var imageViewerCover: ImageViewerCover? = null

    /**进入大图模式动画开始*/
    var onShowAnimateStart: (() -> Unit)? = null
    /**进入大图模式动画结束*/
    var onShowAnimateEnd: (() -> Unit)? = null
    /**退出大图模式动画开始*/
    var onDismissAnimateStart: (() -> Unit)? = null
    /**退出大图模式动画结束*/
    var onDismissAnimateEnd: (() -> Unit)? = null

    /**
     * 根据position获取当前大图对应的小图ImageView，如果要实现完美效果，必须实现它
     */
    var srcImageViewFetcher: (position: Int) -> ImageView? = { null }

    var onPageScrolled: ((position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit)? = null
    var onPageSelected: ((position: Int) -> Unit)? = null
    var onPageScrollStateChanged: ((state: Int) -> Unit)? = null

    private lateinit var containerView: ViewGroup
    private lateinit var photoViewContainer: PhotoViewContainer
    private lateinit var pager: ViewPager
    private lateinit var snapshotView: PhotoView

    private val originUrlList = mutableListOf<String?>()

    private var argbEvaluator: ArgbEvaluator = ArgbEvaluator()

    val currentPosition: Int
        get() = pager.currentItem

    /**当前展示状态*/
    var showing: Boolean = false
        private set

    fun init() {
        require(context != null) {
            "context can't be null"
        }
        require(imageLoader != null) {
            "imageLoader can't be null"
        }

        containerView = FrameLayout(context!!)
        photoViewContainer = PhotoViewContainer(context!!)
        pager = FixViewPager(context!!)
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                onPageScrollStateChanged?.invoke(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                onPageScrolled?.invoke(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                onPageSelected?.invoke(position)
            }
        })

        snapshotView = PhotoView(context!!)

        photoViewContainer.run {
            addView(pager, generateDefaultLayoutParams())
            addView(snapshotView, generateDefaultLayoutParams())
            viewPager = pager
            dragChangeListener = this@ImageViewer
        }

        containerView.addView(photoViewContainer, generateDefaultLayoutParams())
        containerView.translationZ = 100f
        imageViewerCover?.getCoverView()?.let {
            containerView.addView(it, generateDefaultLayoutParams())
        }
    }

    fun show(attachedView: ViewGroup, originUrlList: List<String?>, position: Int) {
        if (photoViewContainer.isAnimating) return
        showing = true

        this.originUrlList.clear()
        this.originUrlList.addAll(originUrlList)
        pager.adapter = ImageViewerAdapter()

        pager.setCurrentItem(position, false)
        pager.post { onPageSelected?.invoke(position) }

        photoViewContainer.isAnimating = true

        // 将snapshotView设置成列表中的srcView的样子
        val srcView = srcImageViewFetcher.invoke(position)
        updateSrcViewParams(attachedView, srcView)

        containerView.setBackgroundColor(Color.TRANSPARENT)

        (containerView.parent as? ViewGroup)?.removeView(containerView)
        attachedView.addView(containerView, generateDefaultLayoutParams())
        pager.visibility = View.INVISIBLE

        snapshotView.run {
            visibility = View.VISIBLE
            translationX = rect.left
            translationY = rect.top
            scaleX = 1f
            scaleY = 1f
            scaleType = if (srcView != null) srcView.scaleType else ImageView.ScaleType.CENTER_CROP

            layoutParams = layoutParams.apply {
                width = rect.width().toInt()
                height = rect.height().toInt()
            }

            setImageDrawable(srcView?.drawable?.constantState?.newDrawable())
        }

        snapshotView.post {
            onShowAnimateStart?.invoke()
            TransitionManager.beginDelayedTransition(
                snapshotView.parent as ViewGroup,
                TransitionSet()
                    .setDuration(animDuration)
                    .addTransition(ChangeBounds())
                    .addTransition(ChangeTransform())
                    .addTransition(ChangeImageTransform())
                    .setInterpolator(FastOutSlowInInterpolator())
                    .addListener(object : TransitionListenerAdapter() {
                        override fun onTransitionEnd(transition: Transition) {
                            photoViewContainer.isAnimating = false

                            pager.visibility = View.VISIBLE
                            snapshotView.visibility = View.INVISIBLE
                            pager.scaleX = 1f
                            pager.scaleY = 1f

                            onShowAnimateEnd?.invoke()
                        }
                    })
            )

            snapshotView.run {
                translationY = 0f
                translationX = 0f
                scaleX = 1f
                scaleY = 1f
                scaleType = ImageView.ScaleType.FIT_CENTER

                layoutParams = layoutParams.apply {
                    width = containerView.width
                    height = containerView.height
                }
            }

            animateShadowBg(bgColor)
            imageViewerCover?.getCoverView()?.animate()?.alpha(1f)?.setDuration(animDuration)?.start()
        }
    }

    private fun dismiss() {
        if (photoViewContainer.isAnimating) return
        photoViewContainer.isAnimating = true

        val srcView = srcImageViewFetcher.invoke(currentPosition)
        updateSrcViewParams((containerView.parent as ViewGroup), srcView)

        // 将snapshotView设置成当前pager中photoView的样子(matrix)
        (pager.adapter as ImageViewerAdapter).currentPhotoView?.let {
            snapshotView.run {
                setImageDrawable(it.drawable)

                val matrix = Matrix()
                it.getSuppMatrix(matrix)
                setSuppMatrix(matrix)
            }
        }

        // 替换成snapshotView来进行动画
        pager.visibility = View.INVISIBLE
        snapshotView.visibility = View.VISIBLE

        onDismissAnimateStart?.invoke()
        TransitionManager.beginDelayedTransition(
            snapshotView.parent as ViewGroup,
            TransitionSet()
                .setDuration(animDuration)
                .addTransition(ChangeBounds())
                .addTransition(ChangeTransform())
                .addTransition(ChangeImageTransform())
                .setInterpolator(FastOutSlowInInterpolator())
                .addListener(object : TransitionListenerAdapter() {
                    override fun onTransitionEnd(transition: Transition) {
                        photoViewContainer.isAnimating = false
                        (containerView.parent as? ViewGroup)?.removeView(containerView)
                        pager.visibility = View.INVISIBLE
                        (pager.adapter as? ImageViewerAdapter)?.currentPhotoView?.let {
                            imageLoader!!.stopLoad(it)
                        }
                        snapshotView.visibility = View.VISIBLE
                        pager.scaleX = 1f
                        pager.scaleY = 1f
                        snapshotView.scaleX = 1f
                        snapshotView.scaleY = 1f

                        onDismissAnimateEnd?.invoke()
                        reset()
                        showing = false
                    }
                })
        )

        snapshotView.run {
            translationY = rect.top
            translationX = rect.left
            scaleX = 1f
            scaleY = 1f
            scaleType = if (srcView != null) srcView.scaleType else ImageView.ScaleType.CENTER_CROP
            layoutParams = layoutParams.apply {
                width = rect.width().toInt()
                height = rect.height().toInt()
            }
        }

        animateShadowBg(Color.TRANSPARENT)
        imageViewerCover?.getCoverView()?.animate()?.alpha(0f)?.setDuration(animDuration)?.start()
    }

    fun handleBackPressed(): Boolean {
        if (photoViewContainer.isAnimating) return true

        if (showing) {
            dismiss()
            return true
        }
        return false
    }

    private fun animateShadowBg(endColor: Int) {
        val startColor = (containerView.background as ColorDrawable).color
        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                containerView.setBackgroundColor(
                    argbEvaluator.evaluate(it.animatedFraction, startColor, endColor) as Int
                )
            }
            duration = animDuration
            interpolator = LinearInterpolator()
        }.start()
    }

    private val rect = RectF()
    private val currentOriginViewLocation = intArrayOf(0, 0)
    /**
     * 更新srcView参数
     */
    private fun updateSrcViewParams(attachedView: ViewGroup, srcView: ImageView?) {
        if (srcView == null) {
            val x = (containerView.width / 2 + containerView.left).toFloat()
            val y = (containerView.height / 2 + containerView.top).toFloat()
            rect.set(x, y, x, y)
        } else {
            attachedView.getLocationOnScreen(currentOriginViewLocation)
            val containerX = currentOriginViewLocation[0]
            val containerY = currentOriginViewLocation[1]

            srcView.getLocationOnScreen(currentOriginViewLocation)
            val x = currentOriginViewLocation[0] - containerX.toFloat()
            val y = currentOriginViewLocation[1] - containerY.toFloat()
            rect.set(
                x,
                y,
                x + srcView.width,
                y + srcView.height
            )
        }
    }

    override fun onRelease() {
        dismiss()
    }

    override fun onDragChange(dy: Int, scale: Float, fraction: Float) {
        imageViewerCover?.getCoverView()?.let {
            it.alpha = 1 - fraction
        }

        containerView.setBackgroundColor(
            argbEvaluator.evaluate(
                fraction * 0.8f,
                bgColor,
                Color.TRANSPARENT
            ) as Int
        )
    }

    private fun reset() {
        snapshotView.setImageDrawable(null)
    }

    private fun generateDefaultLayoutParams() = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    inner class ImageViewerAdapter : PagerAdapter() {
        private val onClickListener = View.OnClickListener { dismiss() }
        var currentPhotoView: PhotoView? = null
            private set

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)
            currentPhotoView = (`object` as ViewGroup).getChildAt(0) as PhotoView
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = FrameLayout(container.context)
            container.addView(view, generateDefaultLayoutParams())

            val photoView = PhotoView(container.context)
            val loadingView = imageViewerCover?.getLoadingView()
            val loadFailedView = imageViewerCover?.getLoadFailedView()

            view.addView(photoView, generateDefaultLayoutParams())
            view.addView(loadingView, generateDefaultLayoutParams())
            view.addView(loadFailedView, generateDefaultLayoutParams())

            photoView.setOnClickListener(onClickListener)

            loadFailedView?.setOnClickListener {
                loadOriginImage(photoView, loadFailedView, loadingView, position)
            }

            // 再加载原图
            loadOriginImage(photoView, loadFailedView, loadingView, position)

            return view
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return originUrlList.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        private fun loadOriginImage(photoView: PhotoView, loadFailedView: View?, loadingView: View?, position: Int) {
            if (!showing) return

            loadFailedView?.visibility = View.GONE
            loadingView?.visibility = View.VISIBLE

            imageLoader!!.load(photoView, originUrlList[position], object :
                ImageLoader.ImageLoaderListener {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    loadingView?.visibility = View.GONE
                    loadFailedView?.visibility = View.VISIBLE
                }

                override fun onLoadSuccess(drawable: Drawable?) {
                    loadingView?.visibility = View.GONE
                    loadFailedView?.visibility = View.GONE
                }
            })
        }
    }
}

fun imageViewer(block: ImageViewer.() -> Unit) : ImageViewer {
    val v = ImageViewer()
    v.block()
    v.init()
    return v
}