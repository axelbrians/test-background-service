package com.machina.test_background_task

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.machina.test_background_task.data.Alarm
import com.machina.test_background_task.model.ListAlarmViewModel
import com.machina.test_background_task.databinding.ActivityListAlarmBinding
import com.machina.test_background_task.receiver.AlarmReceiver
import com.machina.test_background_task.recycler.ListAlarmAdapter
import com.machina.test_background_task.utilities.AlarmOnClickListener
import com.machina.test_background_task.utilities.AlarmOnSwitchListener

class ListAlarmActivity : AppCompatActivity(), AlarmOnClickListener, AlarmOnSwitchListener {

    companion object {
        const val CHANNEL_ID = "channel200"
        const val CHANNEL_NAME = "nameChannel200"
        const val CHANNEL_DESC = "descChannel200"
        const val NOTIFY_ID = "notificationExtra"
        const val ALARM_CODE = 1
        const val NOTIF_CODE = 0

//        edit alarm val
        const val REQUEST_ADD = 1
        const val REQUEST_EDIT = 2
        const val OPTION_SAVE = 3
        const val OPTION_CANCEL = -1

        // alarm constant identifier
        const val ALARM_EXTRA = "extra_alarm"

        private const val TAG = "ListAlarmActivity"
    }

    private lateinit var binding: ActivityListAlarmBinding
    private lateinit var listAlarmViewModel: ListAlarmViewModel
    private lateinit var listAlarmAdapter: ListAlarmAdapter
    private val alarmManager: AlarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    private var actionMode: ActionMode ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Alarm"

        listAlarmViewModel = ViewModelProvider(this).get(ListAlarmViewModel::class.java)
        listAlarmAdapter = ListAlarmAdapter(this, this)

        binding.listAlarmRecycler.apply {
            adapter = listAlarmAdapter
            layoutManager = LinearLayoutManager(this@ListAlarmActivity)
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(listAlarmViewModel.scrollListenerBuilder(binding.listAlarmFab))
        }

        listAlarmAdapter.tracker = createTracker()

        listAlarmViewModel.readAllData.observe(this, { alarm ->
            listAlarmAdapter.setData(alarm)
        })


        binding.listAlarmFab.setOnClickListener {
            val intent = Intent(this, EditAlarmActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD)
        }

    }

    private fun createTracker(): SelectionTracker<String> {
        val alarmTracker = listAlarmViewModel.createSelectionTracker(listAlarmAdapter, binding.listAlarmRecycler)
        val callback = listAlarmViewModel.createActionModeCallback(alarmTracker)

        alarmTracker.addObserver(
            object : SelectionTracker.SelectionObserver<String>() {
                override fun onSelectionChanged() {
                    val count: Int = alarmTracker.selection.size()

                    if (count > 0) {
                        if (!listAlarmViewModel.isSelecting){
                            listAlarmViewModel.updateIsSelecting()
                            actionMode = startSupportActionMode(callback)
                            if (actionMode != null) {
                                Log.d(TAG, "start action mode\n isSelecting: ${listAlarmViewModel.isSelecting}")
                            }
                        }

                        actionMode?.title = "$count selected"
                    } else {
                        listAlarmViewModel.updateIsSelecting()
                        actionMode?.finish()
                        alarmTracker.clearSelection()

                        Log.d(TAG, "action mode dismissed\n isSelecting: ${listAlarmViewModel.isSelecting}")
                    }
                }
            }
        )

        return alarmTracker
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val alarm = data?.getParcelableExtra<Alarm>(ALARM_EXTRA)
        when (requestCode) {
            REQUEST_ADD -> {
                if (resultCode == OPTION_SAVE) {
                    if (alarm != null) {
                        listAlarmViewModel.startAlarm(alarm, alarmManager, this)
                        Log.d(TAG, "masuk ke request add")
                    }
                }
            }
            REQUEST_EDIT -> {
                if (resultCode == OPTION_SAVE) {
                    if (alarm != null) {
                        listAlarmViewModel.startAlarm(alarm, alarmManager, this)
                        Log.d(TAG, "masuk ke request edit")
                    }
                }
            }
        }

    }

    override fun onAlarmClicked(alarm: Alarm) {
        val intent = Intent(this, EditAlarmActivity::class.java)
        intent.putExtra(ALARM_EXTRA, alarm)
        startActivityForResult(intent, REQUEST_EDIT)
    }

    override fun onAlarmSwitched(alarm: Alarm) {
        listAlarmViewModel.switchAlarm(alarm, alarmManager, this)
    }
}