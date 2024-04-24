package com.example.runtracker.ui.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runtracker.R
import com.example.runtracker.adapters.RunAdapter
import com.example.runtracker.databinding.FragmentRunBinding
import com.example.runtracker.databinding.FragmentTrackingBinding
import com.example.runtracker.db.Run
import com.example.runtracker.others.Constants.ACTION_PAUSE_SERVICE
import com.example.runtracker.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker.others.Constants.ACTION_STOP_SERVICE
import com.example.runtracker.others.Constants.MAP_ZOOM
import com.example.runtracker.others.Constants.POLYLINE_COLOR
import com.example.runtracker.others.Constants.POLYLINE_WIDTH
import com.example.runtracker.others.TrackingUtility
import com.example.runtracker.services.Polyline
import com.example.runtracker.services.TrackingService
import com.example.runtracker.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.zip.Inflater
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {
    //since the dagger manages all the viewmodel factories, we can simply write it below the way its written and dagger ...
    // ...would know the correct viewmodel and apply it below
    private val viewModelMain: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private lateinit var binding: FragmentTrackingBinding

    private var isTracking = false
    // recall, list of latlng is a polyline, and we would have more than one polyline in a run because
    // the user might run and then rest or even stop tracking and continue later. This way, we now have list of Polylines.
    // this pathPoints below is the list of polylines
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMillis = 0L
    private var menu: Menu? = null

    //this way the weight gets what the user inputed
    @set:Inject
    var weight = 80f



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Initialize the binding variable
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDB()
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPolyline()
        }
        subscribeToObservers()
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

    //subscribes to our Tracking service and observes the isTracking/pathPoints livedata object there
    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer{
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if(isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            binding.btnToggleRun.text = "Start"
            //the below btn is used to save the run
            binding.btnFinishRun.visibility = View.VISIBLE
        }else{
            menu?.getItem(0)?.isVisible = true
            binding.btnToggleRun.text = "Stop"
            //the below btn is used to save the run
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    // this fun animates the map to view on the current point on the map
    //"pathPoints.last().last()" signifies current or latest polyline, while "pathPoints.last()" is the latest point in the latest polyline
    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }


    //when screen is rotated, the polyline is lost. but thanks to mvvm, the data is still in the live data.
    // what we do now is to redraw the polyline using the data in the live data.
    private fun addAllPolyline(){
        for(polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    //this would connect the last point to the second last point in the pathPoint list and draw a polyline over both points
    private fun addLatestPolyline(){
        // to add both the prelatest point(2nd latest) to the latest, we have to first check if the pathPoints are not empty
        // and if the current polyline we are tracking/dealing with has more that one point.
        // "pathPoint.last()" signifies polyline we are currently tracking
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastPoint = pathPoints.last()[pathPoints.last().size - 2]
            val lastPoint = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastPoint)
                .add(lastPoint)
            map?.addPolyline(polylineOptions)
        }
    }


    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.builder()
        for (polyline in pathPoints){
            for (positions in polyline){
                bounds.include(positions)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDB(){
        map?.snapshot { bmp->
            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            // avg speed in km/h
            val avgSpeed = round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimeStamp, avgSpeed, distanceInMeters, currentTimeInMillis, caloriesBurned)
            viewModelMain.insert(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    //this controls the visibility of the menu
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currentTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking ->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){
        val alertDialog = MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.AlertDialog_AppCompat)
            .setTitle("Cancel Run?")
            .setMessage("Are you sure you want to cancel current run and delete its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){_, _ ->
                stopRun()
            }
            .setNegativeButton("No"){dialogInterface, _ ->
                dialogInterface.cancel()
            }
    }

    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

}