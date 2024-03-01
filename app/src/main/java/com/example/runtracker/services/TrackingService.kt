package com.example.runtracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.runtracker.R
import com.example.runtracker.others.Constants.ACTION_PAUSE_SERVICE
import com.example.runtracker.others.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runtracker.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker.others.Constants.ACTION_STOP_SERVICE
import com.example.runtracker.others.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runtracker.others.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runtracker.others.Constants.NOTIFICATION_ID
import com.example.runtracker.ui.MainActivity
import timber.log.Timber

class TrackingService: LifecycleService() {

    var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE ->{
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }else{
                        Timber.d("Resumed service")
                    }
                }
                ACTION_PAUSE_SERVICE ->{
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE ->{
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
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
}