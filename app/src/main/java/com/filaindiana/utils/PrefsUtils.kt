package com.filaindiana.utils

import com.pixplicity.easyprefs.library.Prefs
import kotlin.random.Random

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 26.04.2020
 */
object PrefsUtils {
    private const val isOnboardingShownSubsctiptionFilter =
        "IS_ONBOARDING_SHOWN_SUBSCTIPTION_FILTER"
    private const val isOnboardingShownOpenedFilter = "IS_ONBOARDING_SHOWN_OPENED_FILTER"
    private const val isSubsctiptionFilter = "IS_SUBSCTIPTION_FILTER"
    private const val isOpenedFilter = "IS_OPENED_FILTER"
    private const val userId = "USER_ID"

    fun generateUserId() {
        val template = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
        val numbers = (0..9).map { it.toString() }
        val chars = 'a'..'e'
        val id = template.map {
            when (it) {
                'x', 'y' -> if (Random.nextBoolean()) numbers.random() else chars.random()
                else -> it
            }
        }.joinToString("")
        Prefs.putString(userId, id)
    }

    fun getUserId() = Prefs.getString(userId, null)

    fun isSubsctiptionFilter() = Prefs.getBoolean(isSubsctiptionFilter, false)
    fun setSubsctiptionFilter(value: Boolean) = Prefs.putBoolean(isSubsctiptionFilter, value)

    fun isOpenedFilter() = Prefs.getBoolean(isOpenedFilter, false)
    fun setOpenedFilter(value: Boolean) = Prefs.putBoolean(isOpenedFilter, value)

    fun isOnboardingShownSubsctiptionFilter() =
        Prefs.getBoolean(isOnboardingShownSubsctiptionFilter, false)

    fun setOnboardingShownSubsctiptionFilter() =
        Prefs.putBoolean(isOnboardingShownSubsctiptionFilter, true)

    fun isOnboardingShownOpenedFilter() =
        Prefs.getBoolean(isOnboardingShownOpenedFilter, false)

    fun setOnboardingShownOpenedFilter() =
        Prefs.putBoolean(isOnboardingShownOpenedFilter, true)
}