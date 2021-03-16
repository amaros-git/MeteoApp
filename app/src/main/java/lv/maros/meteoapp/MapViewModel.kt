package lv.maros.meteoapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.launch
import lv.maros.meteoapp.data.cities.CitiesRepository
import lv.maros.meteoapp.data.cities.network.Result
import lv.maros.meteoapp.utils.MyLocationService
import lv.maros.meteoapp.utils.SingleLiveEvent
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MapViewModel @Inject constructor(
    private val app: Application,
    private val citiesRepo: CitiesRepository
) : AndroidViewModel(app) {

    @Inject
    lateinit var myLocationManager: MyLocationService


    val showMeteoIconEvent = SingleLiveEvent<MeteoIcon>()

    /**
     * throws if location permission is not provided in advance
     */
    @SuppressLint("MissingPermission")
    fun startLocationListener() {
        myLocationManager.setMyLocationListener {
            //create icon
            showMeteoIconEvent.value = icon
        }
        myLocationManager.startMyLocationService()
    }

    /**
     * @throws IOException if gps disabled. ALso if internet is disabled too TODO check it
     */
    private fun getCityNameFromLocation(location: Location): String {
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .first()
            .locality
    }

    fun getCities(country: String) {
        viewModelScope.launch {
            val result = citiesRepo.getCitiesByCountry(country)
            if (result is Result.Success) {
                result.data.forEach {
                    //Timber.d(it.toString())
                }
            } else {
                Timber.d((result as Result.Error).message)
            }
        }
    }

    //TODO rework to some MeteoIconProvider class
    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            app.resources.configuration.densityDpi //TODO do I need density or I can use intrinsic values ?
            Timber.d("intrinsicWidth = $intrinsicWidth, intrinsicHeight = $intrinsicHeight")
            val width = intrinsicWidth * 2
            val height = intrinsicHeight * 2
            setBounds(0, 0, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCleared() {
        myLocationManager.stopMyLocationService()
        super.onCleared()
    }

}