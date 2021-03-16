package lv.maros.meteoapp

import lv.maros.meteoapp.utils.MeteoLocation

data class MeteoIcon(
    val location: MeteoLocation,
    val iconResId: Int
)
