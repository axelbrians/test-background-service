package com.machina.test_background_task.recycler

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.machina.test_background_task.R
import com.machina.test_background_task.data.Alarm
import com.machina.test_background_task.utilities.AlarmClickListener

class ListAlarmAdapter(private val alarmClickListener: AlarmClickListener) : RecyclerView.Adapter<AlarmViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private var alarmList = emptyList<Alarm>()
    var tracker: SelectionTracker<String> ?= null

    fun setData(alarm: List<Alarm>) {
        alarmList = alarm
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_alarm, parent, false)

        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarmList[position]
        tracker?.let {
            holder.onBind(alarm, alarmClickListener, it.isSelected(alarm.id.toString()))
        }
    }

    override fun getItemId(position: Int): Long {
        Log.d("adapter", "id retrieved: ${alarmList[position].id.toLong()}")
        return alarmList[position].id.toLong()
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    fun getItemPosition(key: String): Int {
        return alarmList.indexOfFirst {
            it.id.toString() == key
        }
    }

    fun getItem(position: Int): Alarm {
        return if (alarmList.isNotEmpty()){
            alarmList[position]
        } else {
            Alarm(0,0, "empty")
        }
    }
}


class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val alarmContainer = view.findViewById<ConstraintLayout>(R.id.view_holder_alarm_container)
    private val alarmTimeText = view.findViewById<TextView>(R.id.view_holder_alarm_time)
    private lateinit var alarm: Alarm

    fun onBind(alarm: Alarm, alarmClickListener: AlarmClickListener, isActivated: Boolean = false) {
        this.alarm = alarm
        alarmTimeText.text = alarm.timeText
        alarmContainer.apply {
            setOnClickListener {
                alarmClickListener.onAlarmClicked(alarm)
            }
            this.isActivated = isActivated
        }
    }

    fun getItemDetails() : ItemDetailsLookup.ItemDetails<String> {
        return object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = adapterPosition

            override fun getSelectionKey(): String? = itemId.toString()
        }
    }
}