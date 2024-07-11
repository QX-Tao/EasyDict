package com.qxtao.easydict.ui.activity.dict

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class RecognitionHelper(private val context: Context) : RecognitionListener {
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var mResultListener: ASRResultListener


    fun prepareRecognition(resultListener: ASRResultListener): Boolean {
        val serviceComponent = Settings.Secure.getString(context.contentResolver, "voice_recognition_service")
        if (serviceComponent.isNullOrEmpty()) {
            return false
        }
        val component = ComponentName.unflattenFromString(serviceComponent) ?: return false
        var isRecognizerServiceValid = false
        var currentRecognitionCmp: ComponentName? = null
        val list: List<ResolveInfo> = context.packageManager
            .queryIntentServices(Intent(RecognitionService.SERVICE_INTERFACE),
                PackageManager.MATCH_ALL
            )
        if (list.isNotEmpty()) {
            for (info in list) {
                if (info.serviceInfo.packageName == component.packageName) {
                    isRecognizerServiceValid = true
                    break
                } else {
                    currentRecognitionCmp =
                        ComponentName(info.serviceInfo.packageName, info.serviceInfo.name)
                }
            }
        } else {
            return false
        }
        recognizer =  if (isRecognizerServiceValid) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context, currentRecognitionCmp)
        }
        recognizer.setRecognitionListener(this)
        mResultListener = resultListener
        return true
    }

    fun startRecognition() {
        val intent = createRecognitionIntent()
        recognizer.startListening(intent)
    }

    fun stopRecognition() {
        recognizer.stopListening()
    }

    fun releaseRecognition() {
        recognizer.cancel()
        recognizer.destroy()
    }

    override fun onReadyForSpeech(p0: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(p0: Int) {
        Log.d("SpeechRecognition", "onError: $p0")
        mResultListener.onError(p0)
    }

    override fun onEvent(p0: Int, p1: Bundle?) {}

    override fun onPartialResults(bundle: Bundle?) {
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
            mResultListener.onPartialResult(it[0])
        }
    }

    override fun onResults(bundle: Bundle?) {
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
            mResultListener.onFinalResult(it[0])
        }
    }

}

fun createRecognitionIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
}

interface ASRResultListener {
    fun onPartialResult(result: String)

    fun onFinalResult(result: String)
    fun onError(errCode: Int)
}