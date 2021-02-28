package lv.maros.meteoapp.data.network

import lv.maros.meteoapp.BuildConfig
import okhttp3.OkHttpClient
import java.util.*

/**
 * For https://rapidapi.com/wirefreethought/api/geodb-cities/endpoints
 */
class CitiesHttpClient : OkHttpClient() {

    companion object {

        private const val HEADER_API_KEY = BuildConfig.RAPID_KEY
        private const val HEADER_RAPID_HOST = "wft-geo-db.p.rapidapi.com"
        private const val HEADER_USE_QUERY_STRING = true

        fun getClient(): OkHttpClient {
            return Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val url = original
                        .url()
                        .newBuilder()
                        .build()
                    val request = original
                        .newBuilder()
                        .url(url)
                        .addHeader("x-rapidapi-key", HEADER_API_KEY)
                        .addHeader("x-rapidapi-host", HEADER_RAPID_HOST)
                        .build()
                    chain.proceed(request)
                }
                .build()
        }
    }

}