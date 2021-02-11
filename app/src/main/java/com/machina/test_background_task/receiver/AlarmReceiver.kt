package com.machina.test_background_task.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.machina.test_background_task.helper.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val helper = context?.let { NotificationHelper(it) }

        if (helper != null) {
            val nb = helper.getChannelNotification()
            helper.getManager().notify(0, nb.build())
        }
    }
}