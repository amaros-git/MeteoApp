package lv.maros.meteoapp.data.network

import okhttp3.OkHttpClient

/**
 * For https://rapidapi.com/wirefreethought/api/geodb-cities/endpoints
 */
class CitiesHttpClient : OkHttpClient() {

    companion object {

        private const val HEADER_API_KEY = "55964d3013msh8838d26afee1273p1f33a5jsna40d4e410dc2"
        private const val HEADER_RAPID_HOST = "x-rapidapi-host"
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