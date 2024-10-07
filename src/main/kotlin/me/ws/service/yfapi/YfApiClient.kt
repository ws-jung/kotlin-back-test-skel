package me.ws.service.yfapi

import me.ws.model.Symbol
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YfApiClient {
    @GET("/v8/finance/chart/{symbol}")
    fun getChart(
        @Path("symbol") symbol: Symbol,
        @Query("range") range: String = "10y",
        @Query("region") region: String = "US",
        @Query("interval") interval: String = "1d",
        @Query("lang") lang: String = "en",
        @Query("events") events: String = "div,split"
    ): Call<String>

    companion object {
        fun create(): YfApiClient = Retrofit.Builder()
            .baseUrl("https://yfapi.net")
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor {
                        val request = it.request().newBuilder()
                            .addHeader("X-API-KEY", "taCRJzEwki2PQWTWc2Xo75MUmtbGkW662kDxmUrW")
                            .build()
                        it.proceed(request)
                    }
                    .build()
            )
            .build()
            .create(YfApiClient::class.java)
    }
}