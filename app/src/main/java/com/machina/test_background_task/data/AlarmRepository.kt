package com.machina.test_background_task.data

import androidx.lifecycle.LiveData

class AlarmRepository(private val alarmDao: AlarmDao) {

    val readAllData: LiveData<List<Alarm>> = alarmDao.readAllData()

    suspend fun addAlarm(alarm: Alarm) {
        alarmDao.addAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(ids: List<Long>){
        alarmDao.deleteAlarm(ids)
    }
}