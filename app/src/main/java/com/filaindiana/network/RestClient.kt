package com.filaindiana.network

import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 19.04.2020
 */
class RestClient {
    companion object {
        interface GetShopsApiService {
            @POST("prod/getNearbyClassifiedSupermarketsV2")
            suspend fun getSupermarkets(@Body bytes: RequestBody): ShopsResponse
        }

        interface ReportApiService {
            @POST("prod/reportsPostV3")
            suspend fun report(@Body bytes: RequestBody): ShopsResponse
        }

        suspend fun getShops(lat: Double, lng: Double): ShopsResponse {
            val data = "{\"lat\":$lat,\"long\":$lng,\"debug\":\"false\"}"
            val body = RequestBody.create(MediaType.parse("application/octet-stream"), data)
            val url = "https://13uf82pp52.execute-api.eu-central-1.amazonaws.com/"
            return Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(GetShopsApiService::class.java)
                .getSupermarkets(body)
        }

        suspend fun report(
            userLocation: LatLng,
            shopId: String,
            userId: String,
            queueSize: Int,
            queueWaitingTime: Int
        ): ShopsResponse {
            val data =
                "{\"market_id\":$shopId,\"user_id\":$userId,\"lat\":${userLocation.latitude},\"long\":${userLocation.longitude},\"queue_size\":$queueSize,\"queue_wait_minutes\":$queueWaitingTime}"
            val body = RequestBody.create(MediaType.parse("application/octet-stream"), data)
            val url = "https://gxlae9f8tk.execute-api.eu-central-1.amazonaws.com/"
            return Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(ReportApiService::class.java)
                .report(body)
        }
    }
}