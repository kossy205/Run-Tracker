package com.example.runtracker.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.runtracker.R
import com.example.runtracker.adapters.RunAdapter
import com.example.runtracker.databinding.FragmentRunBinding
import com.example.runtracker.databinding.FragmentSetupBinding
import com.example.runtracker.others.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runtracker.others.SortType
import com.example.runtracker.others.TrackingUtility
import com.example.runtracker.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    //since the dagger manages all the viewmodel factories, we can simply write it below the way its written and dagger ...
    // ...would know the correct viewmodel and apply it below
    private val viewModelMain: MainViewModel by viewModels()
    private lateinit var binding: FragmentRunBinding
    private lateinit var runAdapter: RunAdapter


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

        Timber.d("view created")
        requestPermissions()
        setUpRecyclerView()

        when(viewModelMain.sortType){
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.AVG_SPEED -> binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0 -> viewModelMain.sortRuns(SortType.DATE)
                    1 -> viewModelMain.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModelMain.sortRuns(SortType.DISTANCE)
                    3 -> viewModelMain.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModelMain.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        observeRun()
        Timber.d("list observed 1")

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun observeRun(){
        viewModelMain.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })
    }

    private fun setUpRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // We don't want move functionality in this case.
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val runToDelete = runAdapter.differ.currentList[position]
                viewModelMain.deleteRun(runToDelete)

                Snackbar.make(binding.rvRuns, "Run deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("UNDO") {
                        viewModelMain.insert(runToDelete) // a method to re-insert a deleted run
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvRuns)

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