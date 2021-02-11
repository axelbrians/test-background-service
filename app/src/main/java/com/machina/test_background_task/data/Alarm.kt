package com.machina.test_background_task.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_table")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val time: Long,
    val timeText: String
)