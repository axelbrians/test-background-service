package com.machina.test_background_task.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAlarm(alarm: Alarm)

    @Update
    suspend fun updateAlarm(alarm: Alarm)


    @Query("SELECT * FROM alarm_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Alarm>>

}