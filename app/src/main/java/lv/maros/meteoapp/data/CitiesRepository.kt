package lv.maros.meteoapp.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lv.maros.meteoapp.data.network.CitiesApi
import lv.maros.meteoapp.data.network.MAX_RESPONSE_ENTRY_COUNT
import lv.maros.meteoapp.data.network.Result
import lv.maros.meteoapp.data.network.models.City
import lv.maros.meteoapp.data.network.models.Region
import lv.maros.meteoapp.data.network.models.RegionsByCountryResponse
import timber.log.Timber

class CitiesRepository(
    private val network: CitiesApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * I'm using Rapid API GeoDB. To get cities by country few steps are required:
     * 1. Get all regions by country
     */
    suspend fun getCitiesByCountry(
        country: String = "LV"
    ): Result<List<City>> = withContext(ioDispatcher) {
        val response = CitiesApi.retrofitService.getRegions(0)

        return@withContext Result.Success(emptyList<City>())
    }

    private suspend fun getRegions(
        country: String = "LV",
        offset: Int = 0
    ) = withContext(ioDispatcher) {
        val initialResponse = CitiesApi.retrofitService.getRegions(0)
        if (!isRegionResponseValid(initialResponse)) {
            return@withContext emptyList<Region>()
        }

        val regions = mutableListOf<Region>().apply {
            addAll(initialResponse!!.data)
        }

        val regionsTotalCount = initialResponse!!.metadata.totalCount
        Timber.d("totalCount = $regionsTotalCount")

        var currentOffset = MAX_RESPONSE_ENTRY_COUNT

        while (currentOffset <= regionsTotalCount) {
            Timber.d("currentOffset = $currentOffset")
            val response = CitiesApi.retrofitService.getRegions(currentOffset)
            if (!isRegionResponseValid(response)) {
                return@withContext emptyList<Region>()
            }
            regions.addAll(response!!.data)

            currentOffset =+ MAX_RESPONSE_ENTRY_COUNT
        }

        return@withContext regions
    }

    private fun isRegionResponseValid(response: RegionsByCountryResponse?) =
        if (null == response) {
            false
        } else {
            !((response.data.isEmpty()) || (response.metadata.totalCount <= 0))
        }
}