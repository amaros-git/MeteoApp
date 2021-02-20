package lv.maros.meteoapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class MapViewModel @Inject constructor(
    val app: Application
) : AndroidViewModel(app), LocationListener {

    private val locationManager: LocationManager? =
        app.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng>
        get() = _currentLocation


    /**
     * throws if location permission is not provided in advance
     */
    @SuppressLint("MissingPermission")
    private fun registerLocationListener() {
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 10f, this)
    }


    override fun onLocationChanged(location: Location) {
        TODO("Not yet implemented")
    }

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