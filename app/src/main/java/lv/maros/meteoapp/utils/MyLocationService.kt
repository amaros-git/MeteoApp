package lv.maros.meteoapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * at the moment one service : one listener
 */
class MyLocationService @Inject constructor(
        private val appContext: Context
) : LocationListener {

    private val locationManager =
            appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    private var mMyLocationListener: MyLocationListener? = null

    private var meteoCurrentLocation: MeteoLocation?
            by Delegates.observable(null) { _, old, new ->
                Timber.d("old = $old, new = $new")
                //send location to the listener if both exist
                new?.let { meteoLocation ->
                    mMyLocationListener?.onMyLocationChanged(meteoLocation)
                }
            }

    fun setMyLocationListener(l: MyLocationListener) {
        mMyLocationListener = l
    }

    /**
     * MUST BE STOPPED via [stopMyLocationService]
     */
    @SuppressLint("MissingPermission")
    fun startMyLocationService() {
        try {
            locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 400, 10f, this
            )
        } catch (e: Exception) {
            Timber.e("Exception occurred, check location permission. message = ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopMyLocationService() {
        try {
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            Timber.e("Exception occurred, check location permission. message = ${e.message}")
        }
    }

    override fun onLocationChanged(location: Location) {
        meteoCurrentLocation = createMyLocation(location)
    }

    /**
     * To set location from outside
     */
    fun setLocation(location: MeteoLocation) {
        meteoCurrentLocation = location
    }

    private fun createMyLocation(location: Location): MeteoLocation {
        val geocoder = Geocoder(appContext, Locale.getDefault())
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .first()

        return MeteoLocation(
                location.latitude,
                location.longitude,
                address.locality,
                address.countryName
        )

    }

    /**
     * This method is deprecated in Q+. But on API 25 it crashes if you do not implement it,
     * because it was NOT default
     */
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}

fun interface MyLocationListener {

    fun onMyLocationChanged(location: MeteoLocation)
}