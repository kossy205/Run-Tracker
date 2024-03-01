package com.example.runtracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runtracker.R
import com.example.runtracker.databinding.FragmentRunBinding
import com.example.runtracker.databinding.FragmentTrackingBinding
import com.example.runtracker.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker.services.TrackingService
import com.example.runtracker.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {
    //since the dagger manages all the viewmodel factories, we can simply write it below the way its written and dagger ...
    // ...would know the correct viewmodel and apply it below
    private val viewModelMain: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private lateinit var binding: FragmentTrackingBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the binding variable
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        binding.mapView.onSaveInstanceState(outState)
    }

    //this function is used to send command to the tracking service
    private fun sendCommandToService(action: String){
        Intent(requireContext(), TrackingService::class.java).also{
            it.action = action
            // the "startService(it)" sends the action or command (start or resume or stop or pause) passed to this function to the service.
            // it doesnt mean its used to start the service.
            //the service class it what handles the stopping, starting or pausing of the services class
            requireContext().startService(it)
        }
    }
}