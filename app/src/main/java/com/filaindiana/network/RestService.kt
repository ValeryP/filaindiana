package com.filaindiana.network

import okhttp3.Call
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 19.04.2020
 */
interface RestService {
    @POST("prod/getNearbyClassifiedSupermarketsV2")
    suspend fun getSupermarkets(@Body bytes: RequestBody): ShopsResponse
}