package com.udacity.locationreminderapp.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.locationreminderapp.R
import com.udacity.locationreminderapp.base.BaseFragment
import com.udacity.locationreminderapp.base.NavigationCommand
import com.udacity.locationreminderapp.databinding.FragmentSaveReminderBinding
import com.udacity.locationreminderapp.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.locationreminderapp.locationreminders.reminderslist.ReminderDataItem
import com.udacity.locationreminderapp.utils.hasAllLocationPermissions
import com.udacity.locationreminderapp.utils.setDisplayHomeAsUpEnabled
import com.udacity.locationreminderapp.utils.showPermissionSnackBar
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT = "LocationReminder.action.ACTION_GEOFENCE_EVENT"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        _viewModel.showSnackBarInt.observe(viewLifecycleOwner, Observer {
            Snackbar.make(
                binding.root,
                it, Snackbar.LENGTH_LONG
            ).setAction(android.R.string.ok) {
                // Do nothing
            }.show()
        })

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.saveReminder.setOnClickListener {

            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value ?: 0.0
            val longitude = _viewModel.longitude.value ?: 0.0

            val id = _viewModel.selectedPOI.value?.placeId ?: ""

            val canSave = _viewModel.validateEnteredData(
                ReminderDataItem(
                    title,
                    description,
                    location,
                    latitude,
                    longitude,
                    id
                )
            )

            if (canSave) {
                val geofence = Geofence.Builder()
                    .setRequestId(id)
                    .setCircularRegion(
                        latitude,
                        longitude,
                        100f
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

                if (requireActivity().hasAllLocationPermissions()) {
                    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                        addOnFailureListener {
                            Log.v("Geofence", "Failed adding..." + it.message)
                        }
                        addOnSuccessListener {
                            _viewModel.validateAndSaveReminder(
                                ReminderDataItem(
                                    title,
                                    description,
                                    location,
                                    latitude,
                                    longitude,
                                    geofence.requestId
                                )
                            )
                            Log.v("Geofence Item", "Added successfully...")
                        }
                    }

                } else {
                    requireActivity().showPermissionSnackBar(binding.root)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

}
