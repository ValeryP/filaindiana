package com.filaindiana.map

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
data class ShopFilters(var isOnlyOpened: Boolean, var isSubscribed: Boolean) {
    companion object {
        fun defaultState() = ShopFilters(isOnlyOpened = false, isSubscribed = false)
    }
}