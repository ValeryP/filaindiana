package com.filaindiana.map

import androidx.lifecycle.MutableLiveData
import com.filaindiana.data.Subscription
import com.filaindiana.network.ShopsResponse.Shop
import com.filaindiana.utils.default
import com.filaindiana.utils.filterOpen
import com.filaindiana.utils.filterSubscribed
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */

class MapState {
    private lateinit var subscriptions: List<Subscription>

    private var allShops: MutableList<Shop> = mutableListOf()
    private val fetchedLocations: MutableSet<LatLng> = mutableSetOf()

    val filters = MutableLiveData<ShopFilters>().default(ShopFilters.defaultState())
    val shopsFiltered = MutableLiveData<List<Shop>>().default(shopsFiltered())

    fun setSubscriptions(subscriptions: List<Subscription>) {
        this.subscriptions = subscriptions
        this.shopsFiltered.value = shopsFiltered()
    }

    fun addShops(shops: List<Shop>) {
        this.allShops.addAll(shops)
        this.shopsFiltered.value = shopsFiltered()
    }

    fun shopsAll(): List<Shop> {
        return allShops
    }

    fun toogleOpened() {
        this.filters.value = filters.value!!.copy(isOpened = !this.filters.value!!.isOpened)
        this.shopsFiltered.value = shopsFiltered()
    }

    fun toogleSubscribed() {
        this.filters.value = filters.value!!.copy(isSubscribed = !this.filters.value!!.isSubscribed)
        this.shopsFiltered.value = shopsFiltered()
    }

    fun addNewFetchedLocation(location: LatLng) {
        fetchedLocations.add(location)
    }

    fun closestLocationDistance(location: LatLng): Double {
        return fetchedLocations.map { SphericalUtil.computeDistanceBetween(location, it) }.min()
            ?: Double.MAX_VALUE
    }

    private fun shopsFiltered(): List<Shop> {
        var shops = shopsAll()
        if (filters.value!!.isOpened) {
            shops = shops.filterOpen()
        }
        if (filters.value!!.isSubscribed) {
            shops = shops.filterSubscribed(subscriptions)
        }
        return shops
    }
}