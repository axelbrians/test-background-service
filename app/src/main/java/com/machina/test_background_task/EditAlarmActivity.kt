package com.machina.test_background_task

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import androidx.appcompat.app.AppCompatActivity
import com.machina.test_background_task.databinding.ActivityEditAlarmBinding

class EditAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeUi()
    }

    private fun initializeUi() {
        binding.editAlarmTimePicker.setIs24HourView(is24HourFormat(this))

        binding.editAlarmCancel.setOnClickListener { finish() }
        binding.editAlarmSave.setOnClickListener {

        }
    }
}