package com.example.runtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runtracker.R
import com.example.runtracker.others.Constants.ACTION_PAUSE_SERVICE
import com.example.runtracker.others.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runtracker.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker.others.Constants.ACTION_STOP_SERVICE
import com.example.runtracker.others.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runtracker.others.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runtracker.others.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runtracker.others.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runtracker.others.Constants.NOTIFICATION_ID
import com.example.runtracker.others.Constants.TIMER_UPDATE_INTERVAL
import com.example.runtracker.others.TrackingUtility
import com.example.runtracker.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// to draw polyline on map, we need points, this points(latlng) stored in a list is a Polyline.
// there can be several Polylines on the map because the user can pause the run and resume, which means there would be several Polylines ...
// ... on the map. these Polylines can also be stored in a list and then become a list of polylines.
// the below defines a type for the Polyline(list of point(latlng)) and ...
// ... and a type for Polylines(list of Polyline)
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService: LifecycleService() {

    var isFirstRun = true
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var timeRunInSeconds = MutableLiveData<Long>()

    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        // this is a mutable list of Polylines. the Type is defined above "typealias Polyline = MutableList<LatLng>" and "typealias Polylines = MutableList<Polyline>".
        // to draw polyline on map, we need points, this points(latlng) stored in a list is a Polyline.
        // there can be several Polylines on the map because the user can pause the run and resume, which means there would be several Polylines ...
        // ... on the map. these Polylines can also be stored in a list
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE ->{
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }else{
                        Timber.d("Resumed service")
                        startForegroundService()
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE ->{
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE ->{
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean){
        if(isTracking){
            if (TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!){
                result?.locations?.let {locations ->
                    for(location in locations){
                        addPathPoint(location)
                        Timber.d("New location: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?){
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    // executes when the service is started for the first time
    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        val notificatioBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Run Tracker")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificatioBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT

    // this function would take a user to main activity when the notification is clicked.
    // but the main activity shows the run fragment and not the tracking fragment that we want to see.
    // this is because all these fragments are in the main activity and the run fragment is the default fragment to show.
    //what we would do is to go to main activity and check if its gets an intent with this action "ACTION_SHOW_TRACKING_FRAGMENT", ...
    //...it should tk us to the tracking fragment.
    // mind you we dont have any code or function to tk us to the tracking fragment in our navigation graph. se we would do that, ...
    //... and then ask mainactivity to tk us to tracking fragment if we get an intent with the specified action "ACTION_SHOW_TRACKING_FRAGMENT"
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        // WE USED NOTIFICATION OF LOW IMPORTANCE BECAUSE WE DON'T WANT IT TO MK SOUNDS WHENEVER WE UPDATE NOTIFICATION
        // WE WOULD BE UPDATING NOTIFICATION EVERY SECOND
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private var isTimerEnabled = false
    private var laptime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                //time difference between now and time started
                laptime = System.currentTimeMillis() - timeStarted
                //post the new lapTime
                timeRunInMillis.postValue(timeRun + laptime)
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += laptime
        }

    }
}