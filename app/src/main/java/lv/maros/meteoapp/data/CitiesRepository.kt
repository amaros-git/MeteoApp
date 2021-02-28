package lv.maros.meteoapp.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lv.maros.meteoapp.data.network.CitiesApi
import lv.maros.meteoapp.data.network.Result
import lv.maros.meteoapp.data.network.models.RegionsByCountryResponse

class CitiesRepository (
    private val network: CitiesApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getRegion(
        country: String = "LV"
    ): Result<RegionsByCountryResponse> = withContext(ioDispatcher) {
        val response = CitiesApi.retrofitService.getRegions()
        if (null != response) {
            Result.Success(response)
        } else {
            Result.Error("connection is OK, but empty response is received")
        }
    }
}