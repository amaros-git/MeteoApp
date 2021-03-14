package lv.maros.meteoapp.tools

import android.content.Context
import android.location.Location
import android.location.LocationListener
import timber.log.Timber
import kotlin.properties.Delegates

class MyLocationService : LocationListener {

    var myCurrentLocation: MyLocation? by Delegates.observable(null) { _, old, new ->
        Timber.d("old = $old, new = $new")
    }

    companion object {

        @Volatile
        private var INSTANCE: MyLocationService? = null

        fun getInstance(context: Context): MyLocationService {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = MyLocationService()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        TODO("Not yet implemented")
    }

    /**
     * To set location from outside
     */
    fun setMyLocation(location: MyLocation) {
        myCurrentLocation = location
    }
}

interface myLocationListener {

    fun onMyLocationChanged(location: MyLocation)
}