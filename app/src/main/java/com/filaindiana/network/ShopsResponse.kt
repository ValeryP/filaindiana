package com.filaindiana.network


import androidx.annotation.DrawableRes
import com.filaindiana.R
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

class ShopsResponse : ArrayList<ShopsResponse.ShopsResponseItem>() {
    data class ShopsResponseItem(
        @SerializedName("state")
        val state: State?,
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

            @DrawableRes
            fun getStatusColor(): Int {
                return when (queueSizePeople) {
                    in 0..15 -> R.drawable.bg_rounded_green
                    in 15..30 -> R.drawable.bg_rounded_orange
                    in 30..Int.MAX_VALUE -> R.drawable.bg_rounded_red
                    else -> R.drawable.bg_rounded_grey
                }
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
                val hours = openingHours.split(Regex("['\"]")).filter { it.contains(Regex("[:.]")) }
                    .chunked(2)
                val dayOfWeek = DateTime.now().dayOfWeek
                return hours[dayOfWeek].zipWithNext().first()
            }

            fun getLocation(): LatLng {
                return LatLng(lat.toDouble(), long.toDouble())
            }

            fun getImgResId(): Int {
                return when (brand) {
                    "auchan" -> R.drawable.auchan
                    "bennet" -> R.drawable.bennet
                    "carrefour" -> R.drawable.carrefour
                    "coop" -> R.drawable.coop
                    "crai" -> R.drawable.crai
                    "despar" -> R.drawable.despar
                    "eataly" -> R.drawable.eataly
                    "ekom" -> R.drawable.ekom
                    "esselunga" -> R.drawable.esselunga
                    "eurospin" -> R.drawable.eurospin
                    "famila" -> R.drawable.famila
                    "galassia" -> R.drawable.galassia
                    "gigante" -> R.drawable.gigante
                    "iper" -> R.drawable.iper
                    "iperal" -> R.drawable.iperal
                    "lidl" -> R.drawable.lidl
                    "md" -> R.drawable.md
                    "naturasì" -> R.drawable.naturasi
                    "pam" -> R.drawable.pam
                    "penny" -> R.drawable.penny
                    "simply" -> R.drawable.simply
                    "superc" -> R.drawable.superc
                    "tigros" -> R.drawable.tigros
                    "unes" -> R.drawable.unes
                    else -> R.drawable.generic
                }
            }
        }
    }
}