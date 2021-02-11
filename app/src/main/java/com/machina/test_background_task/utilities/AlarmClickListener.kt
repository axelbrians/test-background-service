package com.machina.test_background_task.utilities

import com.machina.test_background_task.data.Alarm

interface AlarmClickListener {
    fun onAlarmClicked(alarm: Alarm)
}