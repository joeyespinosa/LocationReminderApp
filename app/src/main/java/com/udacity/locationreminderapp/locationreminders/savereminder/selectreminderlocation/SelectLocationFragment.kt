package com.udacity.locationreminderapp.locationreminders.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.locationreminderapp.R
import com.udacity.locationreminderapp.base.BaseFragment
import com.udacity.locationreminderapp.databinding.FragmentSelectLocationBinding
import com.udacity.locationreminderapp.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.locationreminderapp.utils.*
import com.udacity.locationreminderapp.utils.EspressoIdlingResource.wrapEspressoIdlingResource
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG = SelectLocationFragment::class.java.simpleName

    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var selectedMarker: Marker
    private lateinit var selectedPointOfInterest: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        if (this::selectedPointOfInterest.isInitialized){
            _viewModel.selectedPOI.value = selectedPointOfInterest
            _viewModel.reminderSelectedLocationStr.value = selectedPointOfInterest.name
            _viewModel.latitude.value = selectedPointOfInterest.latLng.latitude
            _viewModel.longitude.value = selectedPointOfInterest.latLng.longitude
        }
        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)
        setMyLocation()
    }

    private fun setMapLongClick(map: GoogleMap){
        map.setOnMapLongClickListener {latLng ->
            if (this::selectedMarker.isInitialized){
                selectedMarker.remove()
            }

            val snippet = String.format(
                    Locale.getDefault(),
                    getString(R.string.lat_long_snippet),
                    latLng.latitude,
                    latLng.longitude
            )

            selectedPointOfInterest = PointOfInterest(latLng, snippet, snippet)

            selectedMarker = map.addMarker(
                    MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.reminder_location))
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            selectedMarker.showInfoWindow()
            wrapEspressoIdlingResource{
                _viewModel.locationSelected.postValue(true)
            }
        }
    }

    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener { poi ->
            if (this::selectedMarker.isInitialized){
                selectedMarker.remove()
            }

            selectedMarker = map.addMarker(
                    MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
            )

            selectedPointOfInterest = poi

            selectedMarker.showInfoWindow()
            wrapEspressoIdlingResource{
                _viewModel.locationSelected.postValue(true)
            }
        }
    }

    private fun setMapStyle(map: GoogleMap){
        try {
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
            )
            if(!success) {
                Log.e(TAG, "Style parsing failed.")
            }

        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find the style. Error: ", e)
        }
    }


    @SuppressLint("MissingPermission")
    private fun setMyLocation(){
        if(requireActivity().hasAllLocationPermissions()){
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation?.addOnSuccessListener {
                val snippet = String.format(
                        Locale.getDefault(),
                        getString(R.string.lat_long_snippet),
                        it.latitude,
                        it.longitude
                )
                val latlng = LatLng(it.latitude, it.longitude)

                selectedPointOfInterest = PointOfInterest(latlng, snippet, "Current Location")

                selectedMarker = map.addMarker(
                        MarkerOptions()
                                .position(latlng)
                                .title(getString(R.string.reminder_location))
                                .snippet(snippet)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )

                val zoom = 15f

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom))

                selectedMarker.showInfoWindow() }
        } else {
            requireActivity().showPermissionSnackBar(binding.root)
        }
    }

}
