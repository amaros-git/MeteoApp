package lv.maros.meteoapp.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import lv.maros.meteoapp.data.local.CitiesDatabase
import lv.maros.meteoapp.data.network.CitiesApi
import lv.maros.meteoapp.data.network.GEODB_CITIES_BASE_URL
import lv.maros.meteoapp.data.network.MAX_RESPONSE_ENTRY_COUNT
import lv.maros.meteoapp.data.network.Result
import lv.maros.meteoapp.data.network.models.City
import lv.maros.meteoapp.data.network.models.Region
import lv.maros.meteoapp.data.network.models.RegionsByCountryResponse
import timber.log.Timber

class CitiesRepository(
    private val network: CitiesApi,
    private val citiesDb: CitiesDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * I'm using Rapid API GeoDB. To get cities by country few steps are required:
     * 1. Get all regions by country
     */
    suspend fun getCitiesByCountry(
        country: String = "LV"
    ): Result<List<City>> = withContext(ioDispatcher) {
        val regions = getRegionsFromNetwork()
        regions.forEach {
            Timber.d(it.toString())
            //citiesDb.citiesDao.insertRegion(it)
        }

        /*  val regions = citiesDb.citiesDao.getRegions()
          regions.forEach {
              Timber.d("local region = $it")
          }*/


        val cities = getCitiesFromNetwork(regions)
        return@withContext Result.Success(emptyList<City>())
    }

    private suspend fun getRegionsFromNetwork(
        country: String = "LV"
    ): List<Region> = withContext(ioDispatcher) {
        val regions = mutableListOf<Region>()
        var currentOffset = 0
        do {
            val response = network.retrofitService.getRegions(currentOffset)
            if (!isRegionResponseValid(response)) {
                return@withContext emptyList<Region>()
            }
            regions.addAll(response!!.data)
            //actually each Rapid API GeoDB response contains the same totalCount,
            //but to avoid additional logic I simply create it each time
            val regionsTotalCount = response.metadata.totalCount

            currentOffset += MAX_RESPONSE_ENTRY_COUNT

            delay(1500) //otherwise retrofit2.HttpException: HTTP 429 Too Many Requests
        } while (currentOffset <= regionsTotalCount)

        return@withContext regions
    }

    private fun isRegionResponseValid(response: RegionsByCountryResponse?) =
        if (null == response) {
            false
        } else {
            !((response.data.isEmpty()) || (response.metadata.totalCount <= 0))
        }

    private suspend fun getCitiesFromNetwork(
        country: String = "LV",
        regions: List<Region>
    ): List<Region> = withContext(ioDispatcher) {
        val cities = mutableListOf<City>()
        var currentOffset = 0
        do {
            val response = network.retrofitService.getCities(
                "$GEODB_CITIES_BASE_URL$country/regions/${}" +
            )
            if (!isRegionResponseValid(response)) {
                return@withContext emptyList<Region>()
            }
            regions.addAll(response!!.data)
            //actually each Rapid API GeoDB response contains the same totalCount,
            //but to avoid additional logic I simply create it each time
            val regionsTotalCount = response.metadata.totalCount

            currentOffset += MAX_RESPONSE_ENTRY_COUNT

            delay(1500) //otherwise retrofit2.HttpException: HTTP 429 Too Many Requests
        } while (currentOffset <= regionsTotalCount)

        return@withContext regions
    }
}