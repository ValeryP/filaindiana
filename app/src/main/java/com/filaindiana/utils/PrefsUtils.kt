package com.filaindiana.utils

import com.pixplicity.easyprefs.library.Prefs

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 26.04.2020
 */
object PrefsUtils {
    private const val isOnboardingShownSubsctiptionFilter =
        "IS_ONBOARDING_SHOWN_SUBSCTIPTION_FILTER"
    private const val isOnboardingShownOpenedFilter = "IS_ONBOARDING_SHOWN_OPENED_FILTER"

    fun isOnboardingShownSubsctiptionFilter() =
        Prefs.getBoolean(isOnboardingShownSubsctiptionFilter, false)

    fun setOnboardingShownSubsctiptionFilter() =
        Prefs.putBoolean(isOnboardingShownSubsctiptionFilter, true)

    fun isOnboardingShownOpenedFilter() =
        Prefs.getBoolean(isOnboardingShownOpenedFilter, false)

    fun setOnboardingShownOpenedFilter() =
        Prefs.putBoolean(isOnboardingShownOpenedFilter, true)
}