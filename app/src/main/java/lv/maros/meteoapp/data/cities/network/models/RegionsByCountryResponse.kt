package lv.maros.meteoapp.data.cities.network.models

data class RegionsByCountryResponse(
    val data: List<Region>,
    val metadata: GeoDbMetadata
)
