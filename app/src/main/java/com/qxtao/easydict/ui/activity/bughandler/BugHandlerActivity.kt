package com.qxtao.easydict.ui.activity.bughandler

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import com.google.android.material.appbar.MaterialToolbar
import com.qxtao.easydict.BuildConfig
import com.qxtao.easydict.R
import com.qxtao.easydict.databinding.ActivityBugHandlerBinding
import com.qxtao.easydict.ui.base.BaseActivity
import com.qxtao.easydict.utils.common.ClipboardUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BugHandlerActivity : BaseActivity<ActivityBugHandlerBinding>(ActivityBugHandlerBinding::inflate) {
    // define variable
    private lateinit var dispatcher: OnBackPressedDispatcher
    private lateinit var callback: OnBackPressedCallback
    private val exceptionMessage by lazy { intent?.getStringExtra("exception_message") }
    private val threadName by lazy { intent?.getStringExtra("thread") }

    // define widget
    private lateinit var mtTitle: MaterialToolbar
    private lateinit var tvError: TextView

    companion object {
        private const val TAG = "BugHandlerActivity"
        fun start(context: Context, exceptionMessage: String, threadName: String) {
            Log.e(TAG, "Error on thread $threadName:\n $exceptionMessage")
            val intent = Intent(context, BugHandlerActivity::class.java)
            intent.putExtra("exception_message", exceptionMessage)
            intent.putExtra("thread", threadName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate() {
        dispatcher = onBackPressedDispatcher
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
    }

    override fun bindViews() {
        mtTitle = binding.mtTitle
        tvError = binding.tvError
    }

    override fun initViews() {
        val deviceBrand = Build.BRAND
        val deviceModel = Build.MODEL
        val sdkLevel = Build.VERSION.SDK_INT
        val currentDateTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDateTime = formatter.format(currentDateTime)
        val currentVersion = getString(R.string.app_version, BuildConfig.VERSION_NAME)

        val combinedTextBuilder = StringBuilder()
        combinedTextBuilder
            .append(" _____                    ____   _        _   ").append('\n')
            .append("| ____| __ _  ___  _   _ |  _ \\ (_)  ___ | |_ ").append('\n')
            .append("|  _|  / _` |/ __|| | | || | | || | / __|| __|").append('\n')
            .append("| |___| (_| |\\__ \\| |_| || |_| || || (__ | |_ ").append('\n')
            .append("|_____|\\_,_||___/  \\__, ||____/ |_| \\___| \\__|").append('\n')
            .append("                   |___/                      ").append('\n').append('\n')
            .append(getString(R.string.crash_app_name)).append(':').append("   ").append(currentVersion).append('\n').append('\n')
            .append(getString(R.string.crash_phone_brand)).append(':').append("      ").append(deviceBrand).append('\n')
            .append(getString(R.string.crash_phone_model)).append(':').append("      ").append(deviceModel).append('\n')
            .append(getString(R.string.crash_sdk_level)).append(':').append("  ").append(sdkLevel).append('\n')
            .append(getString(R.string.crash_thread)).append(':').append("     ").append(threadName).append('\n').append('\n').append('\n')
            .append(getString(R.string.crash_time)).append(':').append(" ").append(formattedDateTime).append('\n')
            .append(getString(R.string.beginning_of_crash)).append('\n')
            .append(exceptionMessage)

        tvError.typeface = Typeface.MONOSPACE
        tvError.text = combinedTextBuilder.toString()
    }

    override fun addListener() {
        mtTitle.setNavigationOnClickListener { dispatcher.onBackPressed() }
        mtTitle.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.copy_to_clipboard -> {
                    ClipboardUtils.copyTextToClipboard(mContext, tvError.text, getString(R.string.copy_success))
                }
                R.id.save_as_file -> {
                    val savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/EasyDict/"
                    val saveDir = File(savePath)
                    if (!saveDir.exists()) { saveDir.mkdirs() }
                    val fileName = "CrashLog_${System.currentTimeMillis()}.txt"
                    val saveFile = File(savePath, fileName)
                    try {
                        saveFile.createNewFile()
                        FileOutputStream(saveFile).use { outputStream ->
                            outputStream.write(tvError.text.toString().toByteArray())
                        }
                        showShortToast(getString(R.string.save_file_success_desc, saveFile.absolutePath))
                    } catch (e: IOException) {
                        e.printStackTrace()
                        showShortToast(getString(R.string.save_failure))
                    }
                }
            }
            true
        }
    }
}