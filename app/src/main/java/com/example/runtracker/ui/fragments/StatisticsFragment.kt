package com.example.runtracker.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runtracker.R
import com.example.runtracker.ui.viewmodels.MainViewModel
import com.example.runtracker.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {
    //since the dagger manages all the viewmodel factories, we can simply write it below the way its written and dagger ...
    // ...would know the correct viewmodel and apply it below
    private val viewModelMain: StatisticsViewModel by viewModels()
}