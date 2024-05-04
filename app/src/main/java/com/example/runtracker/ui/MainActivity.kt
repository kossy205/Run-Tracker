package com.example.runtracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runtracker.R
import com.example.runtracker.databinding.ActivityMainBinding
import com.example.runtracker.db.RunDao
import com.example.runtracker.others.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // if the activity was by chance destroyed before the notification was clicked,
        // then we do this to pass the intent when the activity is relaunched with the pending intent
        navigateToTrackingFragmentIfNeeded(intent)

        val navController = findNavController(R.id.navHostFragment)
        // If using an ActionBar, set up the ActionBar with NavController
        //setSupportActionBar(findViewById(R.id.toolbar))
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemReselectedListener { /* NO-OPERATION */ }


        navController.addOnDestinationChangedListener {_, destination, _ ->
            when(destination.id){
                R.id.runFragment, R.id.statisticsFragment, R.id.settingsFragment ->{
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
                else ->{
                    binding.bottomNavigationView.visibility = View.GONE
                }
            }
        }

    }

    // if this activity is lauched with the pending intent and the activity wasnt ...
    // ... destroyed be4 this, then we use the func below
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        navigateToTrackingFragmentIfNeeded(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp() || super.onSupportNavigateUp()
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            findNavController(R.id.navHostFragment).navigate(R.id.action_global_trackingFragment)
        }
    }
}