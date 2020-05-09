package com.codaliscia.map

import com.codaliscia.utils.PrefsUtils

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
data class ShopFilters(var isOnlyOpened: Boolean, var isSubscribed: Boolean) {
    companion object {
        fun defaultState() = ShopFilters(
            isOnlyOpened = PrefsUtils.isOpenNowFilter(),
            isSubscribed = PrefsUtils.isFavoritesFilter()
        )
    }
}