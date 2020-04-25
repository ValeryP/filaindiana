package com.filaindiana.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 24.04.2020
 */
@Entity(tableName = "subscriptions")
class Subscription(
    @PrimaryKey val shopId: String,
    val shopName: String,
    val shopAddress: String,
    val shopBrand: String,
    val lat: Double,
    val lng: Double,
    val isActive: Boolean
)