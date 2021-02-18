package lv.maros.meteoapp


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListener {

    private val TAG = SelectLocationFragment::class.java.simpleName

    private lateinit var map: GoogleMap

    private lateinit var locationManager: LocationManager

    private var selectedLocationLatLng: LatLng? = null
    private var selectedLocationName = "Location"

    private var currentMarker: Marker? = null

    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    //When navigate back showing Snackbar with an action, on the different screen app crashes
    //with "not attached to Activity". Thus I will remove Snackbar once this Fragment is destroyed
    private var snackBarGoToSettings: Snackbar? = null

    @SuppressLint("NewApi")
    private val startForForegroundLocationPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        Log.d(TAG, "foreground permission result = $result")
        if (result) { //check if background permissions are provided
            if ((runningQOrLater) && (!isBackgroundLocationPermissionAllowed())) {
                requestBackGroundLocationPermission()
            }
            enableMyLocation()
        } else {
            snackBarGoToSettings = showToastWithSettingsAction(
                binding.root,
                R.string.location_required_error
            ).apply {
                show()
            }
        }
    }

    private val startForBackgroundLocationPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        Log.d(TAG, "background permission result = $result")
        if (!result) {
            snackBarGoToSettings = showToastWithSettingsAction(
                binding.root,
                R.string.background_permission_denied_explanation
            ).apply {
                show()
            }
        }
    }


    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveLocationButton.setOnClickListener {
            if (null == selectedLocationLatLng) {
                _viewModel.showToast.value = getString(R.string.please_select_location)
            } else {
                onLocationSelected(selectedLocationLatLng!!)
            }
        }

        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)

        snackBarGoToSettings?.dismiss()
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
