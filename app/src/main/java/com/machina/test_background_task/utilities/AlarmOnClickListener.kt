package com.machina.test_background_task.utilities

import com.machina.test_background_task.data.Alarm

interface AlarmOnClickListener {
    fun onAlarmClicked(alarm: Alarm)
}