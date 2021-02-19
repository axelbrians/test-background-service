package com.machina.test_background_task.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.machina.test_background_task.ListAlarmActivity
import com.machina.test_background_task.OpenNotificationActivity
import com.machina.test_background_task.R

class NotificationHelper(private val context: Context) : ContextWrapper(context) {

    private lateinit var mManager: NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(ListAlarmActivity.CHANNEL_ID, ListAlarmActivity.CHANNEL_NAME, importance).apply {
                description = ListAlarmActivity.CHANNEL_DESC
            }
            // Register the channel with the system
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            getManager().createNotificationChannel(channel)
        }
    }

    fun getManager(): NotificationManager{
        return mManager
    }

    fun getChannelNotification(title: String?, text: String?): NotificationCompat.Builder {
        var newTitle = title
        var newText = text
        val intent = Intent(this, OpenNotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        if (newTitle.isNullOrEmpty())
            newTitle = "error code 404"
        if (newText.isNullOrEmpty())
            newText = "content not found"


        return NotificationCompat.Builder(this, ListAlarmActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_copyright_24)
                .setContentTitle(newTitle)
                .setContentText(newText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
    }

}