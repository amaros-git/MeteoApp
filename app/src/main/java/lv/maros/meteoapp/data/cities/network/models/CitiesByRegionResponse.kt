package lv.maros.meteoapp.data.cities.network.models

data class CitiesByRegionResponse(
    val data: List<City>,
    val metadata: GeoDbMetadata
)
