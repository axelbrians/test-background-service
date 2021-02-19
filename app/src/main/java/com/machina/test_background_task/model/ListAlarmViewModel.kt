package com.machina.test_background_task.model

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.machina.test_background_task.ListAlarmActivity
import com.machina.test_background_task.R
import com.machina.test_background_task.data.Alarm
import com.machina.test_background_task.data.AlarmDatabase
import com.machina.test_background_task.data.AlarmRepository
import com.machina.test_background_task.databinding.ActivityEditAlarmBinding
import com.machina.test_background_task.receiver.AlarmReceiver
import com.machina.test_background_task.recycler.AlarmDetailsLookup
import com.machina.test_background_task.recycler.AlarmSelectionTracker
import com.machina.test_background_task.recycler.ListAlarmAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListAlarmViewModel(application: Application): AndroidViewModel(application) {


    private val repository: AlarmRepository
    val readAllData: LiveData<List<Alarm>>
    var isSelecting: Boolean = false

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        readAllData = repository.readAllData
    }

    companion object {
        private const val TAG = "ListAlarmViewModel"
    }

    fun updateIsSelecting() {
        isSelecting = !isSelecting
    }

    fun switchAlarm(alarm: Alarm, manager: AlarmManager, context: Context) {
        val newAlarm = alarm.copy(isOn = !alarm.isOn)

        if (newAlarm.isOn) {
            startAlarm(newAlarm, manager, context)
        } else {
            cancelAlarm(newAlarm, manager, context)
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlarm(newAlarm)
        }
    }

    private fun updateAlarm(newTime: Long, calendar: Calendar, alarm: Alarm) {
        val timeString = DateFormat.format("HH:mm", calendar.time).toString()
        val newAlarm = alarm.copy(time = newTime, timeText = timeString)

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlarm(newAlarm)
        }
    }

    fun startAlarm(alarm: Alarm, manager: AlarmManager, context: Context) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarm.time
        Log.d(TAG, "time in ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}:${calendar.get(Calendar.SECOND)}")

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.apply {
            putExtra(ListAlarmActivity.NOTIFY_ID, alarm.id)
            putExtra(ListAlarmActivity.NOTIFICATION_TITLE, "id ${alarm.id}")
            putExtra(ListAlarmActivity.NOTIFICATION_TEXT, "time in ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")
        }
        Log.d(TAG, "putExtra id: ${notificationId(alarm)}")

        if (alarm.mon) launchRepeat(context, intent, manager, 1, alarm.id, calendar.timeInMillis)
        else if (alarm.tue) launchRepeat(context, intent, manager, 2, alarm.id, calendar.timeInMillis)
        else if (alarm.wed) launchRepeat(context, intent, manager, 3, alarm.id, calendar.timeInMillis)
        else if (alarm.thu) launchRepeat(context, intent, manager, 4, alarm.id, calendar.timeInMillis)
        else if (alarm.fri) launchRepeat(context, intent, manager, 5, alarm.id, calendar.timeInMillis)
        else if (alarm.sat) launchRepeat(context, intent, manager, 6, alarm.id, calendar.timeInMillis)
        else if (alarm.sun) launchRepeat(context, intent, manager, 7, alarm.id, calendar.timeInMillis)
        else {
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
                updateAlarm(calendar.timeInMillis, calendar, alarm)
                Log.d(TAG, "calendar set for tomorrow")
            }
            intent.putExtra(ListAlarmActivity.REPEAT, false)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmId(0, alarm.id), intent, 0)
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Log.d(TAG, "single time alarm launched ${0}${alarm.id}")
        }


    }

    fun cancelAlarm(alarm: Alarm, manager: AlarmManager, context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        if (alarm.mon) cancelRepeat(context, intent, manager, 1, alarm.id)
        if (alarm.tue) cancelRepeat(context, intent, manager, 2, alarm.id)
        if (alarm.wed) cancelRepeat(context, intent, manager, 3, alarm.id)
        if (alarm.thu) cancelRepeat(context, intent, manager, 4, alarm.id)
        if (alarm.fri) cancelRepeat(context, intent, manager, 5, alarm.id)
        if (alarm.sat) cancelRepeat(context, intent, manager, 6, alarm.id)
        if (alarm.sun) cancelRepeat(context, intent, manager, 7, alarm.id)
        if (!alarm.mon && !alarm.tue && !alarm.wed && !alarm.thu && !alarm.fri && !alarm.sat && !alarm.sun) {
            intent.putExtra(ListAlarmActivity.REPEAT, false)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmId(0, alarm.id), intent, 0)
            manager.cancel(pendingIntent)
            Log.d(TAG, "single time alarm canceled ${0}${alarm.id}")
        }
    }

    private fun launchRepeat(context: Context, intent: Intent, manager: AlarmManager, day: Int, id: Int, time: Long) {
        intent.putExtra(ListAlarmActivity.REPEAT, true)
        intent.putExtra(ListAlarmActivity.ALARM_ID, alarmId(day, id))

        val pendingIntent = PendingIntent.getBroadcast(context, alarmId(day, id), intent, 0)

        manager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        Log.d(TAG, "repeat alarm id: ${day}${id}")
    }

    private fun cancelRepeat(context: Context, intent: Intent, manager: AlarmManager, day: Int, id: Int) {
        val pendingIntent = PendingIntent.getBroadcast(context, alarmId(day, id), intent, 0)
        manager.cancel(pendingIntent)
        Log.d(TAG, "alarm with id: ${day}${id} canceled")
    }

    private fun alarmId(day: Int, id: Int): Int {
        return when (day) {
            1 -> ("$id" + "1000000").toInt()
            2 -> ("$id" + "0100000").toInt()
            3 -> ("$id" + "0010000").toInt()
            4 -> ("$id" + "0001000").toInt()
            5 -> ("$id" + "0000100").toInt()
            6 -> ("$id" + "0000010").toInt()
            7 -> ("$id" + "0000001").toInt()
            else -> ("$id" + "0000000").toInt()
        }
    }

    private fun notificationId(alarm: Alarm): Int {
        var id = "${alarm.id}"
        id += if (alarm.mon) "1" else "0"
        id += if (alarm.tue) "1" else "0"
        id += if (alarm.wed) "1" else "0"
        id += if (alarm.thu) "1" else "0"
        id += if (alarm.fri) "1" else "0"
        id += if (alarm.sat) "1" else "0"
        id += if (alarm.sun) "1" else "0"

        return id.toInt()
    }



    fun createSelectionTracker(adapter: ListAlarmAdapter, listAlarmRecycler: RecyclerView) : SelectionTracker<String> {
        return SelectionTracker.Builder(
            "alarmSelection",
            listAlarmRecycler,
            AlarmSelectionTracker(adapter),
            AlarmDetailsLookup(listAlarmRecycler),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
    }

    fun createActionModeCallback(alarmTracker: SelectionTracker<String>): ActionMode.Callback {
        return object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.contextual_menu_list_alarm, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.action_menu_list_alarm_delete -> {
                        Log.d(TAG, "delete menu selected")
                        val ids = mutableListOf<Long>()

                        alarmTracker.selection.forEach {
                            ids.add(it.toLong())
                            Log.d(TAG, "id : $it")
                        }

                        viewModelScope.launch(Dispatchers.IO) {
                            repository.deleteAlarm(ids.toList())
                        }

                        onDestroyActionMode(mode)
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {

                if (alarmTracker.selection.size() != 0) {
                    alarmTracker.clearSelection()
                    Log.d(TAG, "tracker selection cleared")
                }
            }
        }
    }

    fun scrollListenerBuilder(materialFab: FloatingActionButton) : RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            val fab = materialFab
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
            }
        }
    }
}