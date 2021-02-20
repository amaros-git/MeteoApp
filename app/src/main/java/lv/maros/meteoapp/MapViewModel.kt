package lv.maros.meteoapp

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import javax.inject.Inject

class MapViewModel @Inject constructor(val app: Application) : LocationListener {

    private var locationManager: LocationManager =
        app.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun onMapReady(p0: GoogleMap?) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {
        TODO("Not yet implemented")
    }
}