package com.machina.test_background_task.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.machina.test_background_task.ListAlarmActivity
import com.machina.test_background_task.helper.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val helper = context?.let { NotificationHelper(it) }

        if (helper != null && intent != null) {
            val notificationHelper = helper.getChannelNotification()
            val id = intent.getIntExtra(ListAlarmActivity.NOTIFY_ID, 10)
            Log.d("AlarmReceiver", "notification with id: $id")
            helper.getManager().notify(id, notificationHelper.build())
        }
    }
}