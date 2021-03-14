package lv.maros.meteoapp.tools

data class MyLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String, //TODO get nearest city if outside of city. HOW ?
    val countryName: String
)
