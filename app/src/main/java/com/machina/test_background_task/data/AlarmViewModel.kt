package com.machina.test_background_task.data

import android.app.Application
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import com.machina.test_background_task.R
import com.machina.test_background_task.recycler.AlarmDetailsLookup
import com.machina.test_background_task.recycler.AlarmSelectionTracker
import com.machina.test_background_task.recycler.ListAlarmAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application): AndroidViewModel(application) {


    private val repository: AlarmRepository
    val readAllData: LiveData<List<Alarm>>
    var isSelecting: Boolean = false

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        readAllData = repository.readAllData
    }

    companion object {
        private const val TAG = "AlarmViewModel"
    }


    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlarm(alarm)
        }
    }

    fun updateIsSelecting() {
        isSelecting = !isSelecting
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


}