package com.machina.test_background_task.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAlarm(alarm: Alarm)

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Query("DELETE FROM alarm_table WHERE id IN (:ids)")
    suspend fun deleteAlarm(ids: List<Long>)


    @Query("SELECT * FROM alarm_table ORDER BY time ASC")
    fun readAllData(): LiveData<List<Alarm>>

    @Query("UPDATE alarm_table SET isOn = (:reset) WHERE id = (:newId)")
    fun resetAlarm(newId: Int, reset: Boolean)
}