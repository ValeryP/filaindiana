package com.filaindiana.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat.forPattern
import org.joda.time.format.DateTimeFormatter

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 24.04.2020
 */
val timestampPattern: DateTimeFormatter = forPattern("yyyy-MM-dd HH:mm:ss")
const val KEY_SUBSCRIPTON_LOCATION = "SUBSCRIPTON_LOCATION"
const val KEY_SUBSCRIPTONS_ID = "SUBSCRIPTONS_ID"

@Entity(tableName = "subscriptions")
class Subscription(
    @PrimaryKey val shopId: String,
    val shopName: String,
    val shopAddress: String,
    val shopBrand: String,
    val lat: Double,
    val lng: Double,
    val isActive: Boolean = true,
    val timestamp: String = DateTime.now().toString(timestampPattern)
) {
    fun getLocation() = LatLng(lat, lng)
    fun getTime(): DateTime = DateTime.parse(timestamp, timestampPattern)
}