package com.machina.test_background_task.recycler

import androidx.recyclerview.selection.ItemKeyProvider

class AlarmSelectionTracker(private val adapter: ListAlarmAdapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String {
        return adapter.getItem(position).id.toString()
    }

    override fun getPosition(key: String): Int {
        return adapter.getItemPosition(key)
    }

}