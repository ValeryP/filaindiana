package com.filaindiana.map

import androidx.lifecycle.MutableLiveData
import com.filaindiana.data.Subscription
import com.filaindiana.network.ShopsResponse
import com.filaindiana.network.ShopsResponse.Shop
import com.filaindiana.utils.PrefsUtils
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
    private val fetchedLocations: MutableMap<LatLng, List<Shop>> = mutableMapOf()

    val filters = MutableLiveData<ShopFilters>().default(ShopFilters.defaultState())
    val shopsFiltered = MutableLiveData<List<Shop>>().default(shopsFiltered())

    fun setSubscriptions(subscriptions: List<Subscription>) {
        this.subscriptions = subscriptions
        this.shopsFiltered.value = shopsFiltered()
    }

    fun shopsAll(): List<Shop> {
        return allShops
    }

    fun toogleOpened() {
        val isOnlyOpenedNew = !this.filters.value!!.isOnlyOpened
        PrefsUtils.setOpenedFilter(isOnlyOpenedNew)
        this.filters.value = filters.value!!.copy(isOnlyOpened = isOnlyOpenedNew)
        this.shopsFiltered.value = shopsFiltered()
    }

    fun toogleSubscribed() {
        val isSubscribedNew = !this.filters.value!!.isSubscribed
        PrefsUtils.setSubsctiptionFilter(isSubscribedNew)
        this.filters.value = filters.value!!.copy(isSubscribed = isSubscribedNew)
        this.shopsFiltered.value = shopsFiltered()
    }

    fun addNewFetchedLocation(location: LatLng, shops: ShopsResponse) {
        fetchedLocations[location] = shops
        addShops(shops)
    }

    fun closestLocationDistance(location: LatLng): Double {
        return fetchedLocations.keys.map { SphericalUtil.computeDistanceBetween(location, it) }
            .min()
            ?: Double.MAX_VALUE
    }

    fun getShopClusterLocation(shop: Shop): LatLng {
        return fetchedLocations.filter { it.value.contains(shop) }.keys.firstOrNull()!!
    }

    private fun addShops(shops: List<Shop>) {
        this.allShops.addAll(shops)
        this.shopsFiltered.value = shopsFiltered()
    }

    private fun shopsFiltered(): List<Shop> {
        var shops = shopsAll()
        if (filters.value!!.isOnlyOpened) {
            shops = shops.filterOpen()
        }
        if (filters.value!!.isSubscribed && ::subscriptions.isInitialized) {
            shops = shops.filterSubscribed(subscriptions)
        }
        return shops
    }
}