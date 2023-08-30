package com.example.mobprog_tp2.ui.Maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.mobprog_tp2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.example.mobprog_tp2.MainActivity


class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var mapContainer: FrameLayout
    private lateinit var MapWarning: TextView
    private lateinit var MapWarning2: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private var lastKnownLocation = null
    private val updateUIVisibilityLiveData = MutableLiveData<Boolean>()

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(MainActivity.DEBUG_TAG, "permission granted")
                /*
                Recall perm check to get necessary val and func
                and automatically refresh fragment
                 */
                getLocationPermission()
            } else {
                Log.d(MainActivity.DEBUG_TAG, "permission denied")
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        // workaround use findViewById for direct bind
        // https://stackoverflow.com/questions/18508354/android-findviewbyid-in-custom-view
        MapWarning = view.findViewById(R.id.MapWarning)
        MapWarning2 = view.findViewById(R.id.MapWarning2)


        // Init map container
        mapContainer = view.findViewById(R.id.mapContainer)
        val mapFragment = childFragmentManager.findFragmentByTag("mapFragment") as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapContainer, it, "mapFragment")
                    .commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        // check if map ready before calling perm to avoid crash
        googleMap = map
        getLocationPermission()
    }


    @SuppressLint("DetachAndAttachSameFragment")
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mapContainer.visibility = View.VISIBLE
                MapWarning.visibility = View.INVISIBLE
                MapWarning2.visibility = View.INVISIBLE
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = true
                updateUIVisibilityLiveData.value = true
            } else {
                googleMap.isMyLocationEnabled = false
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e(MainActivity.ERROR_TAG, e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Set current location to LatLng
                    val latLng = LatLng(location.latitude, location.longitude)
                    // Call refresh fragment and update map position
                    updateMapAndFragment(latLng)
                }
            }
    }

    private fun updateMapAndFragment(latLng: LatLng) {
        // Update map camera position
        val cameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(15f)
            // NOTE: something not working correctly because 17f causing DEVICE crash,
            // manual says no constrain on float
            .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }


    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            googleMap.isMyLocationEnabled = true
            updateUIVisibilityLiveData.value = true
            // Change UI visibility
            updateLocationUI()
            // Get current device location and set the position of the map.
            getDeviceLocation()
        } else {
            /*
             https://developer.android.com/training/permissions/requesting#kotlin
             ActivityCompat.requestPermissions() and onRequestPermissionsResult()
             Deprecated, use requestPermissionLauncher.launch()
             */
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION)
            Log.d(MainActivity.DEBUG_TAG, "Location permission requested")
        }
    }

}
