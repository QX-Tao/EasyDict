package com.qxtao.easydict.ui.activity.photoview

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.ActivityPhotoViewBinding
import com.qxtao.easydict.ui.activity.photoview.preview.AlphaCallback
import com.qxtao.easydict.ui.activity.photoview.preview.PagerAdapter
import com.qxtao.easydict.ui.activity.photoview.preview.ScaleCallback
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.utils.common.EncryptUtils
import com.qxtao.easydict.utils.factory.isAppearanceLight
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.DownloadProgressCallBack
import com.xuexiang.xhttp2.exception.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@SuppressLint("ClickableViewAccessibility")
class PhotoViewActivity : BaseActivity<ActivityPhotoViewBinding>(ActivityPhotoViewBinding::inflate),
    AlphaCallback, ScaleCallback {
    private lateinit var viewPager: ViewPager2
    private lateinit var textView: TextView
    private lateinit var ivShareButton: ImageView
    private lateinit var ivDownloadButton: ImageView
    private lateinit var ivBackButton: ImageView
    private lateinit var clControl: ConstraintLayout
    private var isStatusHide = false
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var callback: OnBackPressedCallback
    private val data by lazy {
        intent?.getStringArrayListExtra("data")
    }
    private val index by lazy {
        intent?.getIntExtra("index",0)
    }
    private val mPagerAdapter by lazy {
        PagerAdapter<String>(
            this@PhotoViewActivity, data
        ).apply {
            onBind { holder, position, data ->
                fun loadImage(data: String?){
                    holder.imageView.load(data){
                        listener(
                            onStart = { _ ->
                                holder.loading.visibility = View.VISIBLE
                                holder.loadingFail.visibility = View.GONE
                            },
                            onSuccess = { _, _ ->
                                holder.loading.visibility = View.GONE
                                holder.loadingFail.visibility = View.GONE
                            },
                            onError = { _, _ ->
                                showShortToast(getString(R.string.loading_failure))
                                holder.loading.visibility = View.GONE
                                holder.loadingFail.visibility = View.VISIBLE
                            }
                        )
                        crossfade(true)
                    }
                }
                holder.loadingFail.setOnClickListener { loadImage(data) }

                val touchListener = object : View.OnTouchListener{
                    var downY = 0f
                    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                        when (motionEvent.action){
                            MotionEvent.ACTION_DOWN -> {
                                downY = motionEvent.y
                            }
                            MotionEvent.ACTION_UP -> {
                                if (motionEvent.y - downY > 120) {
                                    dispatcher.onBackPressed()
                                } else view.performClick()
                            }
                        }
                        return true
                    }
                }
                holder.loading.setOnTouchListener(touchListener)
                holder.loadingFail.setOnTouchListener(touchListener)

                holder.imageView.apply {
                    setAlphaCallback(this@PhotoViewActivity)
                    setScaleCallback(this@PhotoViewActivity)
                    if (position == index){
                        ViewCompat.setTransitionName(holder.imageView, "CONTENT")
                        ViewCompat.setTransitionName(viewPager, "-1")
                    } else {
                        ViewCompat.setTransitionName(holder.imageView, "index${position}")
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(120)
                        loadImage(data)
                    }
                }
            }
        }
    }


    companion object{
        fun start(activity: Activity, list: ArrayList<String>, recyclerView: RecyclerView, index:Int) {
            if (list.isEmpty()) return
            val mPair: Array<Pair<View, String>?> = arrayOfNulls(recyclerView.childCount)
            for (i in 0..recyclerView.childCount){
                val view = recyclerView.getChildAt(i)
                if (view != null){
                    if (index == view.tag as Int) {
                        ViewCompat.setTransitionName(view, "CONTENT")
                        mPair[i] = Pair(view, "CONTENT")
                    } else {
                        ViewCompat.setTransitionName(view, "index${i}")
                        mPair[i] = Pair(view, "index${i}")
                    }
                }
            }
            val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, *mPair
            )
            val intent = Intent(activity, PhotoViewActivity::class.java)
            intent.putStringArrayListExtra("data", list)
            intent.putExtra("index", index)
            ActivityCompat.startActivity(activity, intent, activityOptions.toBundle())
        }

        fun start(activity: Activity, list: ArrayList<String>, view: View, index:Int = 0) {
            if (list.isEmpty()) return
            val mPair: Array<Pair<View, String>?> = arrayOfNulls(1)
            ViewCompat.setTransitionName(view, "CONTENT")
            mPair[0] = Pair(view, "CONTENT")
            val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, *mPair
            )
            val intent = Intent(activity, PhotoViewActivity::class.java)
            intent.putStringArrayListExtra("data", list)
            intent.putExtra("index", index)
            ActivityCompat.startActivity(activity, intent, activityOptions.toBundle())
        }

        fun start(activity: Activity, list: ArrayList<String>, index: Int = 0){
            if (list.isEmpty()) return
            val mPair: Array<Pair<View, String>?> = arrayOfNulls(0)
            val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, *mPair
            )
            val intent = Intent(activity, PhotoViewActivity::class.java)
            intent.putStringArrayListExtra("data", list)
            intent.putExtra("index", index)
            ActivityCompat.startActivity(activity, intent, activityOptions.toBundle())
        }
    }

    override fun onCreate() {
        isAppearanceLight = false
        dispatcher = onBackPressedDispatcher
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAfterTransition()
            }
        }
        dispatcher.addCallback(callback)
        if (data.isNullOrEmpty()) finish()
    }

    override fun bindViews() {
        viewPager = binding.viewPager
        textView = binding.text
        ivBackButton = binding.ivBackButton
        ivShareButton = binding.ivShareButton
        ivDownloadButton = binding.ivDownloadButton
        clControl = binding.clControl
    }

    override fun initViews() {
        textView.text = String.format(getString(R.string.photo_current_of_all), index?.plus(1) ?: 1, data?.size)
        initViewPager()
    }

    override fun addListener() {
        ivBackButton.setOnClickListener { dispatcher.onBackPressed() }
        ivDownloadButton.setOnClickListener {
            val imgUrl = viewPager.currentItem.let { data?.get(it) }.toString()
            val savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/EasyDict/"
            val fileName = EncryptUtils.encryptMD5ToString(imgUrl)
            DataCache().cacheImage(imgUrl, fileName, savePath)
        }
        ivShareButton.setOnClickListener {
            val imgUrl = viewPager.currentItem.let { data?.get(it) }.toString()
            val savePath = mContext.cacheDir.path + "/Image/"
            val fileName = EncryptUtils.encryptMD5ToString(imgUrl)
            DataCache().shareImage(imgUrl, fileName, savePath)
        }
    }

    private fun initViewPager() {
        viewPager.apply { adapter = mPagerAdapter }
        viewPager.setCurrentItem(index ?: 0, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                textView.text = String.format(getString(R.string.photo_current_of_all), position + 1, data?.size)
            }
        })
    }
    private fun hideControlPanel(){
        if (!isStatusHide){
            isStatusHide = true
            val objectAnimator = ObjectAnimator.ofFloat(clControl, "alpha", clControl.alpha, 0f)
            objectAnimator.duration = 400
            objectAnimator.start()
            ivDownloadButton.apply {
                tooltipText = null
                isEnabled = false
                isClickable = false
            }
            ivShareButton.apply {
                tooltipText = null
                isEnabled = false
                isClickable = false
            }
        }
    }
    private fun showControlPanel(){
        if (isStatusHide) {
            isStatusHide = false
            val objectAnimator = ObjectAnimator.ofFloat(clControl, "alpha", clControl.alpha, 1f)
            objectAnimator.duration = 400
            objectAnimator.start()
            ivDownloadButton.apply {
                tooltipText = getString(R.string.save)
                isEnabled = true
                isClickable = true
            }
            ivShareButton.apply {
                tooltipText = getString(R.string.share)
                isEnabled = true
                isClickable = true
            }
        }
    }

    override fun onScaleCallback(scale: Float) {
        if (scale >= 1.2f) hideControlPanel() else showControlPanel()
    }

    override fun onChangeAlphaCallback(alpha: Float) {
        findViewById<View>(R.id.background_view).apply { setAlpha(alpha) }
    }

    override fun onChangeClose() {
        if (isStatusHide) showControlPanel() else callback.handleOnBackPressed()
    }

    interface ImageCacheCallback {
        fun onCached(path: String)
        fun onError()
    }
    inner class DataCache{
        fun cacheImage(imgUrl: String, fileName: String, savePath: String){
            cacheImage(imgUrl, fileName, savePath, object : ImageCacheCallback{
                override fun onCached(path: String) {
                    showShortToast(String.format(getString(R.string.save_success_desc), path))
                }
                override fun onError() {
                    showShortToast(getString(R.string.save_failure))
                }
            })
        }
        fun shareImage(imgUrl: String, fileName: String, savePath: String, ){
            cacheImage(imgUrl, fileName, savePath, object : ImageCacheCallback{
                override fun onCached(path: String) {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    val shareUri = FileProvider.getUriForFile(mContext, "com.qxtao.easydict.fileprovider", File(path))
                    shareIntent.type = "image/*"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri)
                    val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.share_image))
                    if (shareIntent.resolveActivity(packageManager) != null) {
                        startActivity(chooserIntent)
                    } else {
                        showShortToast(getString(R.string.no_share_app_available))
                    }
                }
                override fun onError() {
                    showShortToast(getString(R.string.share_failure))
                }
            })
        }

        private fun cacheImage(imgUrl: String, fileName: String, savePath: String, callback: ImageCacheCallback){
            val saveDir = File(savePath)
            if (!saveDir.exists()) { saveDir.mkdirs() }
            XHttp.downLoad(imgUrl)
                .savePath(savePath)
                .saveName(fileName)
                .execute<String>(object : DownloadProgressCallBack<String?>() {
                    override fun onStart() {}
                    override fun onError(e: ApiException) { callback.onError() }
                    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {}
                    override fun onComplete(path: String) { callback.onCached(path) }
                })
        }
    }
}

