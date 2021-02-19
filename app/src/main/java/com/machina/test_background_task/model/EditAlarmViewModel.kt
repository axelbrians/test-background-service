package com.machina.test_background_task.model

import android.app.Application
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.machina.test_background_task.ListAlarmActivity
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


    fun addAlarm(binding: ActivityEditAlarmBinding, calendar: Calendar): Alarm {
        val timeString = DateFormat.format("HH:mm", calendar.time)
        val newAlarm = alarmBuilder(0, calendar.timeInMillis, timeString.toString(), true, binding)

        viewModelScope.launch(Dispatchers.IO) {
            repository.addAlarm(newAlarm)
        }

        return newAlarm
    }

    fun updateAlarm(binding: ActivityEditAlarmBinding, calendar: Calendar, alarm: Alarm): Alarm {
        val timeString = DateFormat.format("HH:mm", calendar.time)
        val newAlarm = alarmBuilder(alarm.id, calendar.timeInMillis, timeString.toString(), true, binding)

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlarm(newAlarm)
        }

        return newAlarm
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
        val isIntent = (intent.getParcelableExtra<Alarm>(ListAlarmActivity.ALARM_EXTRA) != null)
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))

        if (isIntent) {
            val alarm = intent.getParcelableExtra<Alarm>(ListAlarmActivity.ALARM_EXTRA)
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
                set(Calendar.SECOND, 0)
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

    fun setChipClickListener(binding: ActivityEditAlarmBinding) {
        binding.mon.setOnClickListener { chip ->
            onChecked(binding, chip)
        }
        binding.tue.setOnClickListener { chip ->
            onChecked(binding, chip)
        }
        binding.wed.setOnClickListener { chip ->
            onChecked(binding, chip)
        }
        binding.thu.setOnClickListener { chip ->
            onChecked(binding, chip)
        }
        binding.fri.setOnClickListener { chip ->
            onChecked(binding, chip)
        }
        binding.sat.setOnClickListener { chip ->
            onChecked(binding, chip)
        }
        binding.sun.setOnClickListener { chip ->
            onChecked(binding, chip)
        }
    }

    private fun onChecked(binding: ActivityEditAlarmBinding, chip: View) {
        when (chip) {
            binding.mon -> {
                binding.tue.isChecked = false
                binding.wed.isChecked = false
                binding.thu.isChecked = false
                binding.fri.isChecked = false
                binding.sat.isChecked = false
                binding.sun.isChecked = false
            }
            binding.tue -> {
                binding.mon.isChecked = false
                binding.wed.isChecked = false
                binding.thu.isChecked = false
                binding.fri.isChecked = false
                binding.sat.isChecked = false
                binding.sun.isChecked = false
            }
            binding.wed -> {
                binding.mon.isChecked = false
                binding.tue.isChecked = false
                binding.thu.isChecked = false
                binding.fri.isChecked = false
                binding.sat.isChecked = false
                binding.sun.isChecked = false
            }
            binding.thu -> {
                binding.mon.isChecked = false
                binding.tue.isChecked = false
                binding.wed.isChecked = false
                binding.fri.isChecked = false
                binding.sat.isChecked = false
                binding.sun.isChecked = false
            }
            binding.fri -> {
                binding.mon.isChecked = false
                binding.tue.isChecked = false
                binding.wed.isChecked = false
                binding.thu.isChecked = false
                binding.sat.isChecked = false
                binding.sun.isChecked = false
            }
            binding.sat -> {
                binding.mon.isChecked = false
                binding.tue.isChecked = false
                binding.wed.isChecked = false
                binding.thu.isChecked = false
                binding.fri.isChecked = false
                binding.sun.isChecked = false
            }
            binding.sun -> {
                binding.mon.isChecked = false
                binding.tue.isChecked = false
                binding.wed.isChecked = false
                binding.thu.isChecked = false
                binding.fri.isChecked = false
                binding.sat.isChecked = false
            }
        }

    }

}