package com.qxtao.easydict.ui.activity.quicksearch

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.qxtao.easydict.R
import com.qxtao.easydict.utils.common.ShareUtils
import com.qxtao.easydict.utils.constant.ActionConstant.ACTION_CLOSE_QUICK_SEARCH_NOTIFICATION
import com.qxtao.easydict.utils.constant.ActionConstant.ACTION_CREATE_QUICK_SEARCH_NOTIFICATION
import com.qxtao.easydict.utils.constant.ShareConstant.IS_USE_QUICK_SEARCH
import kotlin.apply
import kotlin.collections.forEach
import kotlin.jvm.java


class QuickSearchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_CLOSE_QUICK_SEARCH_NOTIFICATION -> {
                val mNotificationId = 1
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(mNotificationId)
                ShareUtils.delShare(context, IS_USE_QUICK_SEARCH)
            }
            ACTION_CREATE_QUICK_SEARCH_NOTIFICATION -> {
                val mChannelId = context.getString(R.string.quick_search_notification_channel)
                val mChannelName = context.getString(R.string.quick_search)
                val mNotificationId = 1
                val mFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

                // 判断通知是否已经存在 存在则直接返回
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.activeNotifications.forEach { if (it.id == mNotificationId) { return } }

                val channel = NotificationChannel(
                    mChannelId,
                    mChannelName,
                    NotificationManager.IMPORTANCE_LOW
                ).apply { setShowBadge(false) }
                val mManager = context.getSystemService(NotificationManager::class.java).apply { createNotificationChannel(channel) }
                val quickSearchIntent = Intent(context, QuickSearchActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                val searchPendingIntent = PendingIntent.getActivity(context, 0, quickSearchIntent, mFlag)
                val closeIntent = Intent(context, QuickSearchReceiver::class.java).apply { action = ACTION_CLOSE_QUICK_SEARCH_NOTIFICATION }
                val cancelPendingIntent = PendingIntent.getBroadcast(context, 0, closeIntent, mFlag)
                val mBuilder = NotificationCompat.Builder(context, mChannelId)
                    .setSmallIcon(R.drawable.ic_app_notify_icon)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.quick_search_desc1))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentIntent(searchPendingIntent)
                    .addAction(R.drawable.ic_clear, context.getString(R.string.close), cancelPendingIntent)
                    .setOngoing(true)
                    .setAutoCancel(false)
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
                val mNotification = mBuilder.build()
                mManager.notify(mNotificationId, mNotification)
            }
        }
    }
}