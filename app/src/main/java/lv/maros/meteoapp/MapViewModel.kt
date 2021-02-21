package lv.maros.meteoapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import androidx.annotation.Nullable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import timber.log.Timber.d
import java.util.*
import javax.inject.Inject

class MapViewModel @Inject constructor(
    val app: Application
) : AndroidViewModel(app), LocationListener {

    private val locationManager: LocationManager? =
        app.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location>
        get() = _currentLocation


    /**
     * throws if location permission is not provided in advance
     */
    @SuppressLint("MissingPermission")
    fun startLocationListener() {
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 10f, this)
    }


    override fun onLocationChanged(location: Location) {
        _currentLocation.value = location
        getCitiesFromLocation(location)
    }

    private fun getCitiesFromLocation(location: Location) {
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())
        //val city = geocoder.getFromLocation()
        Timber.d("$geocoder.get")

    }

    fun getZoomLevel(location: Location) = 5.0f



    /**
     * This method is deprecated in Q+. But on API 25 it crashes if you do not implement it
     */
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        //Nothing
    }

    override fun onProviderEnabled(provider: String) {
        //Nothing
    }

    override fun onProviderDisabled(provider: String) {
        //Nothing
    }

    @SuppressLint("MissingPermission")
    override fun onCleared() {
        super.onCleared()
        locationManager?.removeUpdates(this)
    }

}