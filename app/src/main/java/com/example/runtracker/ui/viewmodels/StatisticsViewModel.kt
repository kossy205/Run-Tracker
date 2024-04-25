package com.example.runtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.runtracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    val totalTimeRun = mainRepository.getTotalRunDuration()
    val totalDistance = mainRepository.getTotalDistanceCoveredMeters()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurnt()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeedKMH()

    val runsSortedByDate = mainRepository.getAllRunSortedByDate()
}