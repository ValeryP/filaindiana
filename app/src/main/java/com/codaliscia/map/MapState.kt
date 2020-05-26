package com.codaliscia.map

import androidx.lifecycle.MutableLiveData
import com.codaliscia.data.Subscription
import com.codaliscia.network.ShopsResponse
import com.codaliscia.network.ShopsResponse.Shop
import com.codaliscia.utils.PrefsUtils
import com.codaliscia.utils.default
import com.codaliscia.utils.filterOpen
import com.codaliscia.utils.filterSubscribed
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */

class MapState {
    private lateinit var subscriptions: List<Subscription>

    private var allShops: MutableList<Shop>? = null
    private val fetchedLocations: MutableMap<LatLng, List<Shop>> = mutableMapOf()

    val filters = MutableLiveData<ShopFilters>().default(ShopFilters.defaultState())
    val shopsFiltered = MutableLiveData<List<Shop>>().default(shopsFiltered())

    fun setSubscriptions(subscriptions: List<Subscription>) {
        this.subscriptions = subscriptions
        this.shopsFiltered.value = shopsFiltered()
    }

    fun shopsAll(): MutableList<Shop>? = allShops

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
        if (this.allShops == null) this.allShops = mutableListOf()
        this.allShops?.addAll(shops)
        this.shopsFiltered.value = shopsFiltered()
    }

    private fun shopsFiltered(): List<Shop> {
        return shopsAll()?.let {
            var result = it.toMutableList()
            if (filters.value!!.isOnlyOpened) {
                result = result.filterOpen().toMutableList()
            }
            if (filters.value!!.isSubscribed && ::subscriptions.isInitialized) {
                result = result.filterSubscribed(subscriptions).toMutableList()
            }
            result
        }?.toList() ?: emptyList()
    }
}