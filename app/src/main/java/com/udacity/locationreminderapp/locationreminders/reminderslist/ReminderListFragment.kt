package com.udacity.locationreminderapp.locationreminders.reminderslist

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.udacity.locationreminderapp.BuildConfig
import com.udacity.locationreminderapp.R
import com.udacity.locationreminderapp.authentication.AuthenticationActivity
import com.udacity.locationreminderapp.base.BaseFragment
import com.udacity.locationreminderapp.base.NavigationCommand
import com.udacity.locationreminderapp.databinding.FragmentRemindersBinding
import com.udacity.locationreminderapp.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private val TAG = ReminderListFragment::class.java.simpleName
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private const val REQUEST_LOCATION_PERMISSION = 1

class ReminderListFragment : BaseFragment() {

    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    private var hasBaseLocationPermissions = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        checkDeviceLocationSetting()

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        _viewModel.showSnackBar.observe(viewLifecycleOwner, Observer {
            Snackbar.make(
                    binding.root,
                    R.string.error_occured, Snackbar.LENGTH_LONG
            ).setAction(android.R.string.ok) {
                //Do nothing?
            }.show()
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()

        binding.addReminderFAB.setOnClickListener {
            hasBaseLocationPermissions = requireActivity().hasBaseLocationPermissions()
            if(hasBaseLocationPermissions){
                if(requireActivity().hasAllLocationPermissions()){
                    navigateToAddReminder()
                } else {
                    requireActivity().showPermissionSnackBar(binding.root)
                }
            } else {
                requireActivity().requestBaseLocationPermissions()
            }

        }
    }

    override fun onResume() {
        super.onResume()

        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter { }
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
                val intent = Intent(requireContext(), AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            checkDeviceLocationSetting(false)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        if (grantResults.isEmpty() ||
                grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                (requestCode == REQUEST_LOCATION_PERMISSION &&
                        grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED))
        {
            Log.v(TAG, "Permissions are not granted.")
            Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
        } else {
            checkDeviceLocationSetting()
        }
    }

    private fun checkDeviceLocationSetting(resolve: Boolean = true) {
        requireActivity().getLocationRequestTask(resolve).addOnSuccessListener {
            hasBaseLocationPermissions = true
        }
    }

}


