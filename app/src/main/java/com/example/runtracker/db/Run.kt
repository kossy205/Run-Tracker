package com.example.runtracker.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var img: Bitmap? = null,
    var runDateAndTime: Long = 0L,
    var avgSpeedKMH: Float = 0f,
    var distanceCoveredMeters: Int = 0,
    var runDuration: Long = 0L,
    var caloriesBurnt: Int = 0
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}