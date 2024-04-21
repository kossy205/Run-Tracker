package com.example.runtracker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    // this is not executed in a coroutine because the function returns livedata which is also asynchronous
    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunSortedByRunDuration(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurnt DESC")
    fun getAllRunSortedByCaloriesBurnt(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedKMH DESC")
    fun getAllRunSortedByAvgSpeedKMH(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceCoveredMeters DESC")
    fun getAllRunSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalRunDuration(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurnt) FROM running_table")
    fun getTotalCaloriesBurnt(): LiveData<Int>

    @Query("SELECT SUM(distanceCoveredMeters) FROM running_table")
    fun getTotalDistanceCoveredMeters(): LiveData<Int>

    //here we use AVG instead of SUM because we do not want the summation of all speeds, but the avg of the speed
    @Query("SELECT AVG(avgSpeedKMH) FROM running_table")
    fun getTotalAvgSpeedKMH(): LiveData<Float>

}