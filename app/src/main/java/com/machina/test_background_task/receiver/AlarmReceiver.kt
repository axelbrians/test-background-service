package com.machina.test_background_task.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.machina.test_background_task.ListAlarmActivity
import com.machina.test_background_task.data.AlarmDatabase
import com.machina.test_background_task.data.AlarmRepository
import com.machina.test_background_task.helper.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val helper = context?.let { NotificationHelper(it) }

        if (helper != null && intent != null) {
            val title = intent.getStringExtra(ListAlarmActivity.NOTIFICATION_TITLE)
            val text = intent.getStringExtra(ListAlarmActivity.NOTIFICATION_TEXT)
            val id = intent.getIntExtra(ListAlarmActivity.NOTIFY_ID, 10)
            val repeat = intent.getBooleanExtra(ListAlarmActivity.REPEAT, false)

            val notificationHelper = helper.getChannelNotification(title, text)


            Log.d("AlarmReceiver", "notification with id: $id")
            helper.getManager().notify(id, notificationHelper.build())

            checkRepeat(context, repeat, intent)

        }
    }

    private fun checkRepeat(context: Context, repeat: Boolean, intent: Intent) {
        val time = Calendar.getInstance().timeInMillis
        val alarmId = intent.getIntExtra(
                ListAlarmActivity.ALARM_ID,
                time.toInt())
        val id = intent.getIntExtra(ListAlarmActivity.NOTIFY_ID, 10)

        if (repeat) {
            val pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            manager.setExact(AlarmManager.RTC_WAKEUP, time + (24 * 60 * 60 * 1000) , pendingIntent)
            Log.d("receiver", "alarm scheduled for next time")
        }
    }


}