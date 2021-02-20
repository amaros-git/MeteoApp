package lv.maros.meteoapp


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import lv.maros.meteoapp.databinding.FragmentMapBinding
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    @Inject
    lateinit var viewModel: MapViewModel

    private lateinit var binding: FragmentMapBinding

    private lateinit var map: GoogleMap

    //private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private val startForLocationPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        Timber.d("permission result = $result")
        if (result) {
            //getMyLocation()
        } else {
            showToastWithExplanation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        //setHasOptionsMenu(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
    }

    private fun showToastWithExplanation() {

        Snackbar.make(
            binding.root,
            requireContext().getString(R.string.restore_reminder),
            Snackbar.LENGTH_LONG
        ).apply
        {
            setAction(R.string.undo) {
                restoreDeletedReminder(reminder)
            }

            show()
        }
    }

    private fun onLocationSelected(location: LatLng) {
        if (validateSelectedLocation()) {
            _viewModel.latitude.value = location.latitude
            _viewModel.longitude.value = location.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedLocationName

            _viewModel.navigationCommand.value = NavigationCommand.Back
        }

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

    @SuppressLint("MissingPermission")
    private fun registerLocationListener() {
        if (isForegroundLocationPermissionAllowed()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 10f, this)
        }
    }

    private fun isForegroundLocationPermissionAllowed(): Boolean =
        PackageManager.PERMISSION_GRANTED == checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun requestForeGroundLocationPermission() {
        startForForegroundLocationPermissionResult
            .launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isBackgroundLocationPermissionAllowed(): Boolean =
        PackageManager.PERMISSION_GRANTED == checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackGroundLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            Log.d(TAG, "shouldShowRequestPermissionRationale")
            val builder = AlertDialog.Builder(requireContext()).apply {
                setTitle("This app needs background location access")
                setMessage("Please grant \"Allow all the time\" location permission so this app can track your reminders and send notifications")
                setPositiveButton(android.R.string.ok, null)
                setOnDismissListener {
                    startForBackgroundLocationPermissionResult
                        .launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }.show()
        } else {
            startForBackgroundLocationPermissionResult
                .launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        with(map.uiSettings) {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }

        enableMyLocation()

        setMapLongClick(map)

        setPoiClick(map)

        setMapStyle(map)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!isForegroundLocationPermissionAllowed()) {
            requestForeGroundLocationPermission()
            return
        }
        registerLocationListener()
        map.isMyLocationEnabled = true
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            currentMarker?.remove()

            currentMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            ).apply {
                showInfoWindow()
            }

            selectedLocationLatLng = poi.latLng
            selectedLocationName = poi.name
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            currentMarker?.remove()

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            currentMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            selectedLocationLatLng = latLng
            selectedLocationName = "Custom location"
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.d(TAG, "Google Map style parsing error")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find google map style. Error: $e")
        }
    }

    private fun showToastWithSettingsAction(
        view: View,
        textRId: Int,
        length: Int = Snackbar.LENGTH_LONG
    ): Snackbar {
        return Snackbar.make(view, textRId, length).apply {
            setAction(R.string.settings) {
                // Displays App settings screen.
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
    }

    private fun validateSelectedLocation() =
        if (null == selectedLocationLatLng) {
            _viewModel.showToast.value = "Please select location"
            false
        } else {
            true
        }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val zoomLevel = 12.0f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

        //unregister, we need the current location only once
        locationManager.removeUpdates(this)
    }

    /**
     * This method is deprecated in Q+. But on API 25 it crashes if you do not implement it
     */
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d(TAG, "onStatusChanged called")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "onProviderEnabled called")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "onProviderDisabled called")
    }

}
