package com.example.runtracker.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runtracker.db.Run
import com.example.runtracker.others.SortType
import com.example.runtracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunSortedByDate()
    private val runsSortedByRunDuration = mainRepository.getAllRunSortedByRunDuration()
    private val runsSortedByCaloriesBurnt = mainRepository.getAllRunSortedByCaloriesBurnt()
    private val runsSortedByAvgSpeedKMH = mainRepository.getAllRunSortedByAvgSpeedKMH()
    private val runsSortedByDistance = mainRepository.getAllRunSortedByDistance()

    // unlike the normal way to observe livedata,
    // where each sorted runs is observed individualy and accesses on the UI that way.
    //MediatorLivedata observe multiple LiveData at once and from diff sources.
    // It updates sorted runs based on the changes from any of these observed runs.
    // It ideal for complex data aggregation or transformation tasks.
    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATE


    init {
        //mind you, the runsSortedByDate and its collegues are feed to this MediatorLivedata ...
        // method "addSource"
        //this now, it takes the "runsSortedByDate" livedata and produce its result
        runs.addSource(runsSortedByDate){result->
            if (sortType == SortType.DATE){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByRunDuration){result->
            if (sortType == SortType.RUNNING_TIME){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance){result->
            if (sortType == SortType.DISTANCE){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByAvgSpeedKMH){result->
            if (sortType == SortType.AVG_SPEED){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByCaloriesBurnt){result->
            if (sortType == SortType.CALORIES_BURNED){
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType){
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByRunDuration.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurnt.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedByAvgSpeedKMH.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insert(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

}