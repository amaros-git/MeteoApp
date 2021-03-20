package lv.maros.meteoapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnGroundOverlayClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import lv.maros.meteoapp.databinding.FragmentMapBinding
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment :
    Fragment(), OnMapReadyCallback, OnSeekBarChangeListener, OnGroundOverlayClickListener
{
    @Inject
    lateinit var viewModel: MapViewModel

    private lateinit var binding: FragmentMapBinding

    private lateinit var map: GoogleMap
    private val images: MutableList<BitmapDescriptor> = ArrayList()
    private var groundOverlay: GroundOverlay? = null
    private var groundOverlayRotated: GroundOverlay? = null
    private lateinit var transparencyBar: SeekBar
    private var currentEntry = 0


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

        viewModel.showMeteoIconEvent.observe(viewLifecycleOwner) {
            Timber.d("Received meteoIcon = $it")
            showMeteoIcon(it)
            moveCamera(it)
        }

        transparencyBar = binding.transparencySeekBar
        transparencyBar.max = TRANSPARENCY_MAX
        transparencyBar.progress = 0

        binding.toggleClickability.setOnClickListener {
            toggleClickability(it)
        }

        binding.switchImage.setOnClickListener {
            switchImage(it)
        }

    }

    override fun onStart() {
        super.onStart()

        viewModel.getCities("LV")

        //TODO TEST ONLY
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

        map.setMinZoomPreference(MAP_MIN_ZOOM_LEVEL)
        map.setMaxZoomPreference(MAP_MAX_ZOOM_LEVEL)

        map.addMarker(MarkerOptions()
            .position(NEWARK)
            .icon(viewModel.bitmapDescriptorFromVector(requireContext(), R.drawable.ic_sunny))
            .title("Opa")
        )

        transparencyBar.setOnSeekBarChangeListener(this)

        map.setContentDescription("Google Map with ground overlay.")

        setMapStyle(map)
    }

     private fun setMapStyle(map: GoogleMap) {
         try {
             val success = map.setMapStyle(
                 MapStyleOptions.loadRawResourceStyle(
                     requireContext(),
                     R.raw.map_style_retro_almost_all_hidden
                 )
             )
             if (!success) {
                 Timber.d("Google Map style parsing error")
             }
         } catch (e: Resources.NotFoundException) {
             Timber.e("Can't find google map style. Error: $e")
         }
     }

    private fun showMeteoIcon(icon: MeteoIcon) {
        map.addMarker(MarkerOptions() //TODO I don't check if map is ready (how ?), so what if someone calls it before it is initialized ?
            .position(LatLng(icon.location.latitude, icon.location.longitude))
            .icon(viewModel.bitmapDescriptorFromVector(requireContext(), icon.iconResId))
            .title(icon.location.cityName)
        )
    }

    private fun moveCamera(icon: MeteoIcon) {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(icon.location.latitude, icon.location.longitude),
                MAP_MIN_ZOOM_LEVEL
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isLocationPermissionGranted()) {
            viewModel.startLocationListener()
            map.isMyLocationEnabled = true
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestLocationPermission() {
        startForLocationPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        groundOverlay?.transparency = progress.toFloat() / TRANSPARENCY_MAX.toFloat()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        Timber.d("onStartTrackingTouch called")
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        Timber.d("onStopTrackingTouch called")
    }

    private fun switchImage(view: View?) {
        val overlay = groundOverlay ?: return
        Timber.d("Here")
        currentEntry = (currentEntry + 1) % images.size
        overlay.setImage(images[currentEntry])
    }

    /**
     * Toggles the visibility between 100% and 50% when a [GroundOverlay] is clicked.
     */
    override fun onGroundOverlayClick(groundOverlay: GroundOverlay) {
        // Toggle transparency value between 0.0f and 0.5f. Initial default value is 0.0f.
        val overlayRotated = groundOverlayRotated ?: return
        overlayRotated.transparency = 0.5f - overlayRotated.transparency
    }

    /**
     * Toggles the clickability of the smaller, rotated overlay based on the state of the View that
     * triggered this call.
     * This callback is defined on the CheckBox in the layout for this Activity.
     */
    private fun toggleClickability(view: View) {
        groundOverlayRotated?.isClickable = (view as CheckBox).isChecked
    }

    companion object {
        private const val MAP_MIN_ZOOM_LEVEL = 10.0f
        private const val MAP_MAX_ZOOM_LEVEL = 15.0f

        private const val TRANSPARENCY_MAX = 100
        private val NEWARK = LatLng(40.714086, -74.228697)
        private val NEAR_NEWARK = LatLng(
            NEWARK.latitude - 0.001,
            NEWARK.longitude - 0.025
        )
    }

}


/*private fun addGroundOverlays() {
        map.setOnGroundOverlayClickListener(this)
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                NEWARK,
                11f
            )
        )
        images.clear()
        images.add(BitmapDescriptorFactory.fromResource(R.drawable.ic_sunny))
        images.add(BitmapDescriptorFactory.fromResource(R.drawable.newark_prudential_sunny))

        // Add a small, rotated overlay that is clickable by default
        // (set by the initial state of the checkbox.)
        groundOverlayRotated = map.addGroundOverlay(
            GroundOverlayOptions()
                .image(images[1]).anchor(0f, 1f)
                .position(NEAR_NEWARK, 4300f, 3025f)
                .bearing(30f)
                .clickable((binding.toggleClickability).isChecked)
        )

        // Add a large overlay at Newark on top of the smaller overlay.
        groundOverlay = map.addGroundOverlay(
            GroundOverlayOptions()
                .image(images[currentEntry]).anchor(0f, 1f)
                .position(NEWARK, 8600f, 6500f)
        )

    }*/

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
