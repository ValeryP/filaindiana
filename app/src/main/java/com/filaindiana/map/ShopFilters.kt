package com.filaindiana.map

import com.filaindiana.data.Subscription
import com.filaindiana.network.ShopsResponse

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
data class ShopFilters(var isOpened: Boolean, var isSubscribed: Boolean) {
    companion object {
        fun defaultState() = ShopFilters(isOpened = true, isSubscribed = false)
    }
}