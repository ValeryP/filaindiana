package com.filaindiana.network


import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

class ShopsResponse : ArrayList<ShopsResponse.ShopsResponseItem>() {
    data class ShopsResponseItem(
        @SerializedName("state")
        val state: State,
        @SerializedName("supermarket")
        val supermarket: Supermarket
    ) {
        data class State(
            @SerializedName("data_source")
            val dataSource: String,
            @SerializedName("market_id")
            val marketId: String,
            @SerializedName("queue_size_people")
            val queueSizePeople: Int,
            @SerializedName("queue_wait_minutes")
            val queueWaitMinutes: Int,
            @SerializedName("reports_number")
            val reportsNumber: String,
            @SerializedName("timestamp")
            val timestamp: String,
            @SerializedName("type_active_beginning_of_geohash")
            val typeActiveBeginningOfGeohash: String,
            @SerializedName("updated_at")
            val updatedAt: String
        ) {
            fun getLastUpdate(): DateTime {
                return DateTime(timestamp)
            }
        }

        data class Supermarket(
            @SerializedName("address")
            val address: String,
            @SerializedName("average_waiting_time")
            val averageWaitingTime: String,
            @SerializedName("brand")
            val brand: String,
            @SerializedName("city")
            val city: String,
            @SerializedName("is_open")
            val isOpen: Boolean,
            @SerializedName("lat")
            val lat: String,
            @SerializedName("long")
            val long: String,
            @SerializedName("market_id")
            val marketId: String,
            @SerializedName("name")
            val name: String,
            @SerializedName("opening_hours")
            val openingHours: String,
            @SerializedName("type_active_beginning_of_geohash")
            val typeActiveBeginningOfGeohash: String,
            @SerializedName("updated_at")
            val updatedAt: String
        ) {
            fun getOpeningHours(): Pair<String, String> {
                val hours = openingHours.split("'").filter { it.contains(":") }.chunked(2)
                val dayOfWeek = DateTime.now().dayOfWeek
                return hours[dayOfWeek].zipWithNext().first()
            }

            fun getLocation(): LatLng {
                return LatLng(lat.toDouble(), long.toDouble())
            }

            fun getImg(): String {
                return "https://www.filaindiana.it/brands/$brand.png"
            }
        }
    }
}