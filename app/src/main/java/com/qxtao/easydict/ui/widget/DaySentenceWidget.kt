package com.qxtao.easydict.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.qxtao.easydict.R
import com.qxtao.easydict.database.DailySentenceData
import com.qxtao.easydict.ui.activity.daysentence.DaySentenceActivity
import com.qxtao.easydict.utils.common.HttpUtils
import com.qxtao.easydict.utils.common.TimeUtils
import com.qxtao.easydict.utils.constant.NetConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class DaySentenceWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_day_sentence)
        remoteViews.setOnClickPendingIntent(R.id.appwidget_image, openAppPendingIntent(context))
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

        CoroutineScope(Dispatchers.IO).launch{
            val res = async { getDailySentenceItem(context, TimeUtils.getCurrentDateByPattern("yyyy-MM-dd")) }
            val dailySentenceItem = res.await()
            if (dailySentenceItem.first){
                val imageUrl = dailySentenceItem.second?.imageUrl
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .target { result ->
                        remoteViews.setImageViewBitmap(R.id.appwidget_image, result.toBitmap())
                        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                    }
                    .build()
                imageLoader.enqueue(request)
            }
            val textEn = dailySentenceItem.second?.enSentence
            val textCn = dailySentenceItem.second?.cnSentence
            remoteViews.setTextViewText(R.id.tv_ds_en, textEn)
            remoteViews.setTextViewText(R.id.tv_ds_cn, textCn)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun openAppPendingIntent(context: Context):PendingIntent {
        val activityIntent = Intent(context, DaySentenceActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            context, 1, activityIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private suspend fun getDailySentenceItem(context: Context, date: String): Pair<Boolean, DailySentenceData.DailySentence?> {
        val dailySentenceData = DailySentenceData(context)
        return withContext(Dispatchers.IO){
            val tmp = dailySentenceData.getDataByDate(date)
            if (tmp != null){
                Pair(true, DailySentenceData.DailySentence(tmp.date, tmp.enSentence, tmp.cnSentence, tmp.imageUrl, tmp.ttsUrl))
            } else {
                try {
                    val imgResponse = HttpUtils.requestDisableCertificateValidationResponse(
                        NetConstant.imgApi + TimeUtils.getFormatDateTimeByPattern(date, "yyyy-MM-dd", "yyyyMMdd"))
                    val imageUrl = imgResponse.request.url.toString()

                    val dailySentenceResponseJson = HttpUtils.requestResult(NetConstant.dailySentenceApi + date)
                    val dailySentenceJsonObject = JSONObject(dailySentenceResponseJson)
                    val enSentence: String = dailySentenceJsonObject.getString("content").trim()
                    val chSentence: String = dailySentenceJsonObject.getString("note").trim()
                    val ttsUrl: String = dailySentenceJsonObject.getString("tts")

                    if (imageUrl.isNotEmpty() && enSentence.isNotEmpty() && chSentence.isNotEmpty() && ttsUrl.isNotEmpty()) {
                        dailySentenceData.insertData(date, enSentence, chSentence, imageUrl, ttsUrl)
                        Pair(true, DailySentenceData.DailySentence(date, enSentence, chSentence, imageUrl, ttsUrl))
                    } else {
                        Pair(false, null)
                    }

                }catch (e: Exception) {
                    Pair(false, null)
                }
            }
        }
    }
}