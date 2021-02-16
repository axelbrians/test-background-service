package com.machina.test_background_task.model

import android.app.Application
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.machina.test_background_task.ListAlarmOnActivity
import com.machina.test_background_task.data.Alarm
import com.machina.test_background_task.data.AlarmDatabase
import com.machina.test_background_task.data.AlarmRepository
import com.machina.test_background_task.databinding.ActivityEditAlarmBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
    }

    companion object {
        private const val TAG = "EditAlarmViewModel"
    }


    fun addAlarm(binding: ActivityEditAlarmBinding, calendar: Calendar) {
        val timeString = DateFormat.format("HH:mm", calendar.time)
        val alarm = alarmBuilder(0, calendar.timeInMillis, timeString.toString(), true, binding)

        Log.d(TAG, "monday chip status: $alarm")

        viewModelScope.launch(Dispatchers.IO) {
            repository.addAlarm(alarm)
        }
    }

    fun updateAlarm(binding: ActivityEditAlarmBinding, calendar: Calendar, alarm: Alarm) {
        val timeString = DateFormat.format("HH:mm", calendar.time)
        val newAlarm = alarmBuilder(alarm.id, calendar.timeInMillis, timeString.toString(), alarm.isOn, binding)

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlarm(newAlarm)
        }
    }

    private fun alarmBuilder(id: Int, time: Long, timeText: String, isOn: Boolean, binding: ActivityEditAlarmBinding): Alarm {
        return Alarm(id, time, timeText, isOn,
            binding.mon.isChecked,
            binding.tue.isChecked,
            binding.wed.isChecked,
            binding.thu.isChecked,
            binding.fri.isChecked,
            binding.sat.isChecked,
            binding.sun.isChecked)
    }

    fun fetchTimePickerChip(binding: ActivityEditAlarmBinding, intent: Intent, calendar: Calendar, context: Context) {
        val timePicker = binding.editAlarmTimePicker
        val isIntent = (intent.getParcelableExtra<Alarm>(ListAlarmOnActivity.ALARM_EXTRA) != null)
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))

        if (isIntent) {
            val alarm = intent.getParcelableExtra<Alarm>(ListAlarmOnActivity.ALARM_EXTRA)
            if (alarm != null) {
                calendar.timeInMillis = alarm.time
            }

            timePicker.apply {
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
            }

            if (alarm != null) {
                setRepeat(binding, alarm)
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
    }

    private fun setRepeat(binding: ActivityEditAlarmBinding, alarm: Alarm) {
        binding.apply {
            mon.isChecked = alarm.mon
            tue.isChecked = alarm.tue
            wed.isChecked = alarm.wed
            thu.isChecked = alarm.thu
            fri.isChecked = alarm.fri
            sat.isChecked = alarm.sat
            sun.isChecked = alarm.sun
        }
    }

}