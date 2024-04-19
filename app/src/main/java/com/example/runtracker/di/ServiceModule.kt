package com.example.runtracker.di


import android.app.PendingIntent
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.runtracker.R
import com.example.runtracker.others.Constants
import com.example.runtracker.ui.MainActivity
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

//this modules holds all dependency of our tracking service.
// It would be scoped through the lifetime of the tracking service.
// this way the dep would live as long as our tracking service does and not as long as our app lives.

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

//    @ServiceScoped
//    @Provides
//    fun provideFusedLocationProviderClient(
//        @ApplicationContext app: Context
//    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }


    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT

        // this function would take a user to main activity when the notification is clicked.
        // but the main activity shows the run fragment and not the tracking fragment that we want to see.
        // this is because all these fragments are in the main activity and the run fragment is the default fragment to show.
        //what we would do is to go to main activity and check if its gets an intent with this action "ACTION_SHOW_TRACKING_FRAGMENT", ...
        //...it should tk us to the tracking fragment.
        // mind you we dont have any code or function to tk us to the tracking fragment in our navigation graph. se we would do that, ...
        //... and then ask mainactivity to tk us to tracking fragment if we get an intent with the specified action "ACTION_SHOW_TRACKING_FRAGMENT"
    )


    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Run Tracker")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}