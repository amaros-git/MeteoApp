package lv.maros.meteoapp.utils

data class MeteoLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String, //TODO get nearest city if outside of city. HOW ?
    val countryName: String
)
