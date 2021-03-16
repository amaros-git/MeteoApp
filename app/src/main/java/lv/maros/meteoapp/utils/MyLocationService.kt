package lv.maros.meteoapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * at the moment one service : one listener
 */
class MyLocationService @Inject constructor(
    appContext: Context
) : LocationListener {

    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    private var mMyLocationListener: MyLocationListener? = null

    private var meteoCurrentLocation: MeteoLocation? by Delegates.observable(null) { _, old, new ->
        Timber.d("old = $old, new = $new")
        //send location to the listener if both exist
        new?.let { myLocation ->
            mMyLocationListener?.onMyLocationChanged(myLocation)
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
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 10f, this)
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
        //myCurrentLocation = createMyLocation(location)
    }

    /*private fun createMyLocation(location: Location): MyLocation {
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

    }*/

    /**
     * To set location from outside
     */
    fun setMyLocation(location: MeteoLocation) {
        meteoCurrentLocation = location
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