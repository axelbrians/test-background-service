package com.machina.test_background_task.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Alarm::class], version = 3, exportSchema = false)
abstract class AlarmDatabase: RoomDatabase() {

    companion object{
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "alarm_database")
                        .fallbackToDestructiveMigration()
                        .build()

                INSTANCE = instance
                return instance
            }
        }
    }

    abstract fun alarmDao(): AlarmDao


}