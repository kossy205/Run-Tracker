package com.example.runtracker.repositories

import androidx.lifecycle.LiveData
import com.example.runtracker.db.Run
import com.example.runtracker.db.RunDao
import java.lang.reflect.Constructor
import javax.inject.Inject

class MainRepository @Inject constructor( val runDao: RunDao) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)



    // this is not executed in a coroutine because the function returns livedata which is also asynchronous
    fun getAllRunSortedByDate() = runDao.getAllRunSortedByDate()

    fun getAllRunSortedByRunDuration() = runDao.getAllRunSortedByRunDuration()

    fun getAllRunSortedByCaloriesBurnt() = runDao.getAllRunSortedByCaloriesBurnt()

    fun getAllRunSortedByAvgSpeedKMH() = runDao.getAllRunSortedByAvgSpeedKMH()

    fun getAllRunSortedByDistance() = runDao.getAllRunSortedByDistance()



    fun getTotalRunDuration() = runDao.getTotalRunDuration()

    fun getTotalCaloriesBurnt() = runDao.getTotalCaloriesBurnt()

    fun getTotalDistanceCoveredMeters() = runDao.getTotalDistanceCoveredMeters()

    fun getTotalAvgSpeedKMH() = runDao.getTotalAvgSpeedKMH()



}