package com.example.runtracker.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runtracker.R
import com.example.runtracker.databinding.FragmentRunBinding
import com.example.runtracker.databinding.FragmentSetupBinding
import com.example.runtracker.others.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runtracker.others.TrackingUtility
import com.example.runtracker.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    //since the dagger manages all the viewmodel factories, we can simply write it below the way its written and dagger ...
    // ...would know the correct viewmodel and apply it below
    private val viewModelMain: MainViewModel by viewModels()
    private lateinit var binding: FragmentRunBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the binding variable
        binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }


    // request permission
    private fun requestPermissions() {
        // first checks if it has permissions
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }

        //ACCESS_BACKGROUND_LOCATION permission doesn't need to be requested for android versions below Q
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }


    // this takes care of permissions denied permanently(denied twice) and permissions denied but not yet permanent (denied once)
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //show dialog to lead them to settings app to manually add permissions since they denied it permanently
            AppSettingsDialog.Builder(this).build().show()

        } else {
            // request permissions again since its not yet permanently denied
            requestPermissions()
        }
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            REQUEST_CODE_LOCATION_PERMISSION,
            permissions,
            grantResults,
            this
        )
    }

}