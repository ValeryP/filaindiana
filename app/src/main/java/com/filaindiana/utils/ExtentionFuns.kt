package com.filaindiana.utils

import androidx.lifecycle.MutableLiveData
import com.filaindiana.data.Subscription
import com.filaindiana.network.ShopsResponse

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }

fun List<ShopsResponse.Shop>.filterOpen(): List<ShopsResponse.Shop> {
    return this.filter { it.shopShopState != null && it.shopData.isOpen }
}

fun List<ShopsResponse.Shop>.filterSubscribed(subscriptions: List<Subscription>): List<ShopsResponse.Shop> {
    val subscriptionIds = subscriptions.map { it.shopId }
    return this.filter { subscriptionIds.contains(it.shopData.marketId) }
}
