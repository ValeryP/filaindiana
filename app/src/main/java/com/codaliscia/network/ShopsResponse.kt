package com.codaliscia.network


import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import androidx.annotation.DrawableRes
import com.codaliscia.R
import com.codaliscia.utils.GraphicsProvider
import com.codaliscia.utils.logException
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

class ShopsResponse : ArrayList<ShopsResponse.Shop>() {
    data class Shop(
        @SerializedName("state")
        val shopShopState: ShopState?,
        @SerializedName("supermarket")
        val shopData: ShopData
    ) {
        fun isReportingRequired() = shopShopState == null
        fun isOpen() = shopData.isOpen

        data class ShopState(
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
            fun getLastUpdate(): String? {
                return DateUtils.getRelativeTimeSpanString(
                    getUpdateTime().millis,
                    DateTime.now().millis,
                    MINUTE_IN_MILLIS
                ).toString()
            }

            fun getUpdateTime(): DateTime {
                val pattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                return try {
                    LocalDateTime.parse(timestamp.split(".").first(), pattern).toDateTime()
                } catch (e: Exception) {
                    LocalDateTime.parse(updatedAt, pattern).plusHours(2).toDateTime()
                }
            }

            fun getUpdateFreshness(): Float {
                val lastUpdate = getUpdateTime()
                return if (lastUpdate.isAfterNow) {
                    1f
                } else when (val hours =
                    Interval(lastUpdate, DateTime.now()).toDuration().standardHours) {
                    in 0..4 -> 1 - 0.2f * hours
                    else -> 0.2f
                }
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

        data class ShopData(
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
            @Suppress("NestedLambdaShadowedImplicitParameter")
            fun getOpeningHoursFormatted() = try {
                openingHours.split("[[")
                    .asSequence()
                    .map { it.replace(Regex("[\\[\\]\']"), "") }
                    .filter { it.length > 4 }
                    .map {
                        it.split(",")
                            .map { it.trim() }
                            .filter { it.length > 4 }
                    }
                    .map {
                        if (it.size > 2) {
                            val result = mutableListOf<String>()
                            it.forEach {
                                if (it.length == 5) {
                                    result.add(it)
                                } else {
                                    result.add(it.slice(0 until it.length / 2))
                                    result.add(it.slice(it.length / 2 until it.length))
                                }
                            }
                            result
                        } else it
                    }.toList()[DateTime.now().dayOfWeek - 1].let {
                    if (it.size == 2) {
                        "${it[0]} - ${it[1]}"
                    } else {
                        "${it[0]} - ${it[1]}, ${it[2]} - ${it[3]}"
                    }.replace("\"", "")
                }
            } catch (e: Exception) {
                logException(e, "Open hours raw: $openingHours")
                "–"
            }

            fun getLocation(): LatLng = LatLng(lat.toDouble(), long.toDouble())

            fun getImgResId(): Int = GraphicsProvider.getShopImgResId(brand)
        }
    }
}