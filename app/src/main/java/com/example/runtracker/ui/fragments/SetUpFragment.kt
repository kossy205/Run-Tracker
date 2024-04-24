package com.example.runtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runtracker.R
import com.example.runtracker.databinding.FragmentSetupBinding
import com.example.runtracker.others.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runtracker.others.Constants.KEY_NAME
import com.example.runtracker.others.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class SetUpFragment: Fragment(R.layout.fragment_setup) {

    private lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstTimeOpen = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if(!isFirstTimeOpen){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setUpFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setUpFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        // Initialize the binding variable
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if(success){
                findNavController().navigate(R.id.action_setUpFragment_to_runFragment)
            }else{
                Snackbar.make(requireView(), "Please enter all fields", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        val toolBarText = "let's go $name"


        return true
    }
}













