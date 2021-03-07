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
import lv.maros.meteoapp.data.network.models.CitiesByRegionResponse
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
        /*val regions = getRegionsFromNetwork()
        regions.forEach {
            //Timber.d(it.toString())
            citiesDb.citiesDao.insertRegion(it)
        }

        val localRegions = citiesDb.citiesDao.getRegions()
        localRegions.forEach {
            Timber.d("local region = $it")
        }

        val cities = getCitiesFromNetwork(localRegions)
        cities.forEach {
            citiesDb.citiesDao.saveCity(it)
        }*/

        val cities = citiesDb.citiesDao.getCities()
        cities.forEach {
            Timber.d(it.toString())
        }
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

    private suspend fun getCitiesFromNetwork(
        regions: List<Region>,
        country: String = "LV"
    ): List<City> = withContext(ioDispatcher) {
        val cities = mutableListOf<City>()
        //we have the list of regions. Go through the list and for each region
        //create correspond link, get cities and save into database.
        for (i in regions.indices) {
            Timber.d("Current region = ${regions[i]}")
            var currentOffset = 0
            val link = createCitiesByRegionLink(country, regions[i])
            Timber.d("link = $link")
            do {
                var citiesTotalCount = -1
                for (tryCount in 1..3) {
                    try {
                        val response = network.retrofitService.getCities(link, currentOffset)
                        Timber.d("totalCount = ${response?.metadata?.totalCount}, data size = ${response?.data?.size} ")
                        if (!isCityResponseValid(response)) {
                            break
                        }
                        cities.addAll(response!!.data)
                        //actually each Rapid API GeoDB response contains the same totalCount,
                        //but to avoid additional logic I simply create it each time
                        citiesTotalCount = response.metadata.totalCount

                        currentOffset += MAX_RESPONSE_ENTRY_COUNT

                        break
                    } catch (e: Exception) {
                        Timber.e("Exception, tryCount = $tryCount")
                        e.printStackTrace()
                        delay(5000) //otherwise retrofit2.HttpException: HTTP 429 Too Many Requests

                        continue
                    }
                }

                delay(1500) //otherwise retrofit2.HttpException: HTTP 429 Too Many Requests
            } while (currentOffset <= citiesTotalCount)
        }

        return@withContext cities
    }

    private fun createCitiesByRegionLink(country: String, region: Region): String {
        val regionCode = if (region.fipsCode.isNullOrEmpty()) {
            region.isoCode
        } else {
            region.fipsCode
        }

        return "$GEODB_CITIES_BASE_URL$country/regions/$regionCode/cities"
    }

    private fun isRegionResponseValid(response: RegionsByCountryResponse?) =
        if (null == response) {
            false
        } else {
            !((response.data.isEmpty()) || (response.metadata.totalCount <= 0))
        }

    private fun isCityResponseValid(response: CitiesByRegionResponse?) =
        if (null == response) {
            false
        } else {
            !((response.data.isEmpty()) || (response.metadata.totalCount <= 0))
        }
}