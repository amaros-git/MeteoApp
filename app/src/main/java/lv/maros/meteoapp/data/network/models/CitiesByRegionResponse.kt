package lv.maros.meteoapp.data.network.models

data class CitiesByRegionResponse(
    val data: List<City>,
    val metadata: GeoDbMetadata
)
