package com.example.runtracker.others

import android.content.Context
import android.widget.TextView
import com.example.runtracker.R
import com.example.runtracker.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>, c: Context, layoutId: Int
): MarkerView(c, layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }


    //

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        val tvDate: TextView = findViewById(R.id.tvDate)
        val tvAvgSpeed: TextView = findViewById(R.id.mtvAvgSpeed)
        val tvDistance: TextView = findViewById(R.id.mtvDistance)
        val tvDuration: TextView = findViewById(R.id.mtvDuration)
        val tvCaloriesBurned: TextView = findViewById(R.id.mtvCaloriesBurned)

        if(e == null) {
            return
        }
        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedKMH}km/h"
        tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceCoveredMeters / 1000f}km"
        tvDistance.text = distanceInKm

        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurnt}kcal"
        tvCaloriesBurned.text = caloriesBurned
    }

}