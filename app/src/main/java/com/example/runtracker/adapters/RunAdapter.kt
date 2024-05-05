package com.example.runtracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtracker.databinding.ItemRunBinding
import com.example.runtracker.db.Run
import com.example.runtracker.others.TrackingUtility
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter: RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(val binding: ItemRunBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding = ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RunViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]

        holder.binding.apply {
            Glide.with(root.context).load(run.img).into(ivRunImage)

            val calender = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }

            val dateFormat = SimpleDateFormat("dd.mm.yyyy", Locale.getDefault())
            tvDate.text = dateFormat.format(calender.time)

            val avgSpeed = "${run.avgSpeedKMH}km/h"
            tvAvgSpeed.text = avgSpeed

            val distanceInKM = "${run.distanceCoveredMeters / 1000f}km/h"
            tvDistance.text = distanceInKM

            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurnt}kcal"
            tvCalories.text = caloriesBurned
        }



    }

//    var onDeleteGesture: ((Run) -> Unit)? = null
//    var undoDelete: ((Run) -> Unit)? = null


    // used to check between two lists old and new and update only content and items that has changed...
    // instead of rebuilding the whole list again when ever we make changes.
    // it is very helpful for performance as it reduces lag.
    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)
}