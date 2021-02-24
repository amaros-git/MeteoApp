package lv.maros.meteoapp.data.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.SocketTimeoutException

private const val BASE_URL = "https://wft-geo-db.p.rapidapi.com/v1/geo"

private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(CitiesHttpClient.getClient())
        .baseUrl(BASE_URL)
        .build()

/** All methods may throw the following:
 * @throws HttpException
 * @throws SocketTimeoutException
 * @throws Exception no data is received
 * @throws JsonDataException error parsing jaon
 * @throws IOException error reading json
 * @throws UnknownHostException
 *
 */
interface CitiesApiService {

    @GET
    suspend fun getCitiesEntryPoint()
}

object CitiesApi {
    val retrofitService: CitiesApiService by lazy {
        retrofit.create(CitiesApiService::class.java)
    }
}