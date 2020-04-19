package com.filaindiana.network

import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 19.04.2020
 */
class RestClient private constructor(baseUrl: String) {
    private var service: RestService

    companion object {
        fun build(): RestClient {
            return RestClient("https://13uf82pp52.execute-api.eu-central-1.amazonaws.com/")
        }
    }

    init {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
        service = retrofit.create(RestService::class.java)
    }

    suspend fun getShopsLocations(lat: Double, lng: Double): ShopsResponse {
        val data = "{\"lat\":$lat,\"long\":$lng,\"debug\":\"false\"}"
        val body = RequestBody.create(MediaType.parse("application/octet-stream"), data)
        return service.getSupermarkets(body)
    }

}