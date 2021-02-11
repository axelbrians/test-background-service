package com.machina.test_background_task

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.machina.test_background_task.data.Alarm
import com.machina.test_background_task.data.AlarmViewModel
import com.machina.test_background_task.databinding.ActivityListAlarmBinding
import com.machina.test_background_task.receiver.AlarmReceiver
import com.machina.test_background_task.recycler.AlarmDetailsLookup
import com.machina.test_background_task.recycler.ListAlarmAdapter
import com.machina.test_background_task.utilities.AlarmClickListener

class ListAlarmActivity : AppCompatActivity(), AlarmClickListener {

    companion object {
        const val CHANNEL_ID = "channel200"
        const val CHANNEL_NAME = "nameChannel200"
        const val CHANNEL_DESC = "descChannel200"
        const val NOTIFY_ID = 200
        const val ALARM_CODE = 1
        const val NOTIF_CODE = 0

//        edit alarm val
        const val REQUEST_ADD = 1
        const val REQUEST_EDIT = 2
        const val OPTION_SAVE = 3
        const val OPTION_CANCEL = -1

        // alarm constant identifier
        const val ALARM_EXTRA = "extra_alarm"
    }

    private lateinit var binding: ActivityListAlarmBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var listAlarmAdapter: ListAlarmAdapter
    private var actionMode: ActionMode ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Alarm"

        alarmViewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)
        listAlarmAdapter = ListAlarmAdapter(this)


        binding.listAlarmRecycler.apply {
            adapter = listAlarmAdapter
            layoutManager = LinearLayoutManager(this@ListAlarmActivity)
            itemAnimator = DefaultItemAnimator()
        }

        listAlarmAdapter.tracker = createTracker()

        alarmViewModel.readAllData.observe(this, { alarm ->
            listAlarmAdapter.setData(alarm)
        })


        binding.listAlarmFab.setOnClickListener {
            val intent = Intent(this, EditAlarmActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD)
        }
    }

    private fun startAlarm(calendar: Calendar) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_CODE, intent, 0)

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1)
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_CODE, intent, 0)

        alarmManager.cancel(pendingIntent)
    }

    private fun createTracker(): SelectionTracker<Long> {
        val alarmTracker = SelectionTracker.Builder(
            "alarmSelection",
            binding.listAlarmRecycler,
            StableIdKeyProvider(binding.listAlarmRecycler),
            AlarmDetailsLookup(binding.listAlarmRecycler),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        val callback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.contextual_menu_list_alarm, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.action_menu_list_alarm_delete -> {
                        Log.d("listAlarm", "context appbar menu created")
                        alarmTracker.clearSelection()
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                mode?.finish()
                alarmTracker.clearSelection()
            }
        }

        alarmTracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val count: Int = alarmTracker.selection.size()

                    if (count > 0) {
                        if (!alarmViewModel.isSelecting){
                            alarmViewModel.updateIsSelecting()
                            actionMode = startSupportActionMode(callback)
                        }
                        val title = "$count selected"
                        actionMode?.title = title
                    } else {
                        alarmViewModel.updateIsSelecting()
                        actionMode?.finish()
                    }
                }
            }
        )

        return alarmTracker
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ADD -> {
                if (resultCode == OPTION_SAVE) {
                    Log.d("listAlarm", "masuk ke request add")
                }
            }
            REQUEST_EDIT -> {
                if (resultCode == OPTION_SAVE) {
                    Log.d("listAlarm", "masuk ke request edit")
                }
            }
        }

    }

    override fun onAlarmClicked(alarm: Alarm) {
        val intent = Intent(this, EditAlarmActivity::class.java)
        intent.putExtra(ALARM_EXTRA, alarm)
        startActivityForResult(intent, REQUEST_EDIT)
    }
}