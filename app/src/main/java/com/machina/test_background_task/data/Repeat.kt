package com.machina.test_background_task.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


data class Repeat(
    val id: Int,
    val mon: Boolean,
    val tue: Boolean,
    val wed: Boolean,
    val thu: Boolean,
    val fri: Boolean,
    val sat: Boolean,
    val sun: Boolean)