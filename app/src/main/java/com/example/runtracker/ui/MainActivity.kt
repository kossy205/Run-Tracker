package com.example.runtracker.ui

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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navController = findNavController(R.id.navHostFragment)
        // If using an ActionBar, set up the ActionBar with NavController
        setSupportActionBar(findViewById(R.id.toolbar))
        binding.bottomNavigationView.setupWithNavController(navController)


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


    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp() || super.onSupportNavigateUp()
    }

}