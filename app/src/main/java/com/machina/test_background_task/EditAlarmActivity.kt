package com.machina.test_background_task

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.machina.test_background_task.data.Alarm
import com.machina.test_background_task.databinding.ActivityEditAlarmBinding
import com.machina.test_background_task.model.EditAlarmViewModel

class EditAlarmActivity : AppCompatActivity(), TimePicker.OnTimeChangedListener {

    companion object {
        private const val TAG = "EditAlarmActivity"
    }

    private lateinit var binding: ActivityEditAlarmBinding
    private lateinit var mViewModel: EditAlarmViewModel
    private var calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mViewModel = ViewModelProvider(this).get(EditAlarmViewModel::class.java)
        initializeUi()
    }

    private fun initializeUi() {
        mViewModel.fetchTimePickerChip(binding, intent, calendar, this)
        mViewModel.setChipClickListener(binding)

        binding.editAlarmTimePicker.setOnTimeChangedListener(this)

        binding.editAlarmCancel.setOnClickListener {
            setResult(ListAlarmActivity.OPTION_CANCEL)
            finish()
        }

        binding.editAlarmSave.setOnClickListener {
            val newAlarm: Alarm
            val intentExtra = intent.getParcelableExtra<Alarm>(ListAlarmActivity.ALARM_EXTRA)

            if (intentExtra != null) {
                newAlarm = mViewModel.updateAlarm(binding, calendar, intentExtra)
                Log.d(TAG, "alarm updated with: ${calendar.time}")
            } else {
                newAlarm = mViewModel.addAlarm(binding, calendar)
                Log.d(TAG, "alarm inserted with: ${calendar.time}")
            }

            val intentResult = Intent()
            intentResult.putExtra(ListAlarmActivity.ALARM_EXTRA, newAlarm)

            setResult(ListAlarmActivity.OPTION_SAVE, intentResult)
            finish()
        }
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
//        Log.d(TAG, "time changed to ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")

    }


}