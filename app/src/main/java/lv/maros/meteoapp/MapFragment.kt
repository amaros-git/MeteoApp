package lv.maros.meteoapp


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import lv.maros.meteoapp.databinding.FragmentMapBinding
import timber.log.Timber
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
        Timber.d("location permission result = $result")
        if (result) {
            enableLocationFeatures()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentLocation.observe(viewLifecycleOwner) {
            processLocationChange(it)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun showToastWithExplanation() {
        Snackbar.make(
            binding.root,
            requireContext().getString(R.string.please_enable_location),
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.why) {
                //TODO show dialog with explanation
            }
            show()
        }
    }

    /**
     * Must be called from onMapReady()
     */
    private fun enableLocationFeatures() {
        enableMyLocation()
    }

    private fun processLocationChange(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val zoomLevel = viewModel.getZoomLevel(location)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

/*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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
}*/


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        } else {
            enableLocationFeatures()
        }

        with(map.uiSettings) {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }



        /*setMapLongClick(map)

        setPoiClick(map)

        setMapStyle(map)*/
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isLocationPermissionGranted()) {
            viewModel.startLocationListener()
            map.isMyLocationEnabled = true
        }
    }
/*
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
    }*/

/* private fun setMapLongClick(map: GoogleMap) {
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
     }
 }*/

/* private fun setMapStyle(map: GoogleMap) {
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
 }*/

    private fun isLocationPermissionGranted(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestLocationPermission() {
        startForLocationPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}
