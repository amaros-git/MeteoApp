package lv.maros.meteoapp.data.network.models

data class RegionsByCountryResponse(
    val data: List<Region>,
    val metadata: RegionsMetadata
)
