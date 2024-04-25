package com.example.runtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.runtracker.R
import com.example.runtracker.databinding.FragmentSettingsBinding
import com.example.runtracker.databinding.FragmentSetupBinding
import com.example.runtracker.others.Constants
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Initialize the binding variable
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldsFromSharedPref()

        binding.btnApplyChanges.setOnClickListener {

            val success = applyChangesToSharedPref()
            if (success){
                Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(view, "Pls fill out all fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // this loads the prev data from shared Pref and populate the TextInput fields in the Settings Fragment
    private fun loadFieldsFromSharedPref(){
        val name = sharedPreferences.getString(Constants.KEY_NAME, "")
        val weight = sharedPreferences.getFloat(Constants.KEY_WEIGHT, 80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    // this apply changes to the shared Pref by changing the data in the shared pref after the user updates the fields in the Settings Fragment
    private fun applyChangesToSharedPref(): Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(Constants.KEY_NAME, name)
            .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
            .apply()
        return true
    }

}