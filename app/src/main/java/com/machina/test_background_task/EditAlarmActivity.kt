package com.machina.test_background_task

import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.machina.test_background_task.data.Alarm
import com.machina.test_background_task.data.AlarmViewModel
import com.machina.test_background_task.databinding.ActivityEditAlarmBinding

class EditAlarmActivity : AppCompatActivity(), TimePicker.OnTimeChangedListener {

    companion object {
        private const val TAG = "EditAlarmActivity"
    }
    
    
    private lateinit var binding: ActivityEditAlarmBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private var calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmViewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)

        initializeUi()
    }

    private fun initializeUi() {
        val timePicker = binding.editAlarmTimePicker
        val isIntent = (intent.getParcelableExtra<Alarm>(ListAlarmActivity.ALARM_EXTRA) != null)
        timePicker.setIs24HourView(is24HourFormat(this))
        if (isIntent) {
            val alarm = intent.getParcelableExtra<Alarm>(ListAlarmActivity.ALARM_EXTRA)
            if (alarm != null) {
                calendar.timeInMillis = alarm.time
            }

            timePicker.apply {
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
            }
        } else {
            timePicker.apply {
                hour = 0
                minute = 0
            }
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }
        }

        timePicker.setOnTimeChangedListener(this)


        binding.editAlarmCancel.setOnClickListener {
            setResult(ListAlarmActivity.OPTION_CANCEL)
            finish()
        }
        binding.editAlarmSave.setOnClickListener {
            if (isIntent) {
                updateDataInDatabase()
            } else {
                insertDataToDatabase()
            }

            setResult(ListAlarmActivity.OPTION_SAVE)
            finish()
        }
    }

    private fun insertDataToDatabase() {
        val timeString = DateFormat.format("HH:mm", calendar.time)
        val alarm = Alarm(0, calendar.timeInMillis, timeString.toString())

        alarmViewModel.addAlarm(alarm)

        Log.d(TAG, "alarm inserted with time: $timeString")
    }

    private fun updateDataInDatabase() {
        val timeString = DateFormat.format("HH:mm", calendar.time)
        val id = intent.getParcelableExtra<Alarm>(ListAlarmActivity.ALARM_EXTRA)!!.id
        val newAlarm = Alarm(id, calendar.timeInMillis, timeString.toString())

        alarmViewModel.updateAlarm(newAlarm)

        Log.d(TAG, "alarm updated with time: $timeString.toString()")
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        Log.d(TAG, "time changed to ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")

    }


}