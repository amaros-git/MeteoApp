package lv.maros.meteoapp.data.network.models

data class Region(
    val countryCode: String,
    val fipsCode: String,
    val isoCode: String,
    val name: String,
    val wikiDataId: String
)
