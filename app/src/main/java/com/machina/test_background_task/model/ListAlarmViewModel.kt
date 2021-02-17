package com.machina.test_background_task.model

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
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

    fun startAlarm(alarm: Alarm, manager: AlarmManager, context: Context) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarm.time
//        Log.d(TAG, "time in ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(ListAlarmActivity.NOTIFY_ID, alarm.id)
        Log.d(TAG, "putExtra id: ${alarm.id}")

        val pendingIntent = PendingIntent.getBroadcast(context, alarm.id, intent, 0)

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1)
            Log.d(TAG, "calendar set for tomorrow")
        }

        manager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(alarm: Alarm, manager: AlarmManager, context: Context) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarm.time
        Log.d(TAG, "time in ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, alarm.id, intent, 0)
        Log.d(TAG, "alarm with id: ${alarm.id} canceled")

        manager.cancel(pendingIntent)
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