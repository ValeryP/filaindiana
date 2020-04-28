package com.filaindiana.utils

import androidx.core.content.res.ResourcesCompat
import com.filaindiana.R
import com.filaindiana.map.MapsActivity
import com.wooplr.spotlight.SpotlightConfig
import com.wooplr.spotlight.SpotlightView
import kotlinx.android.synthetic.main.activity_maps.*


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 26.04.2020
 */
object OnboardingManager {
    fun startOnlyClosedOnboarding(a: MapsActivity) {
        val onlyOpened = a.getString(R.string.only_opened)
        SpotlightView.Builder(a)
            .setConfiguration(config(a))
            .target(a.layout_hide_closed_container)
            .headingTvText(onlyOpened)
            .subHeadingTvText(a.getString(R.string.only_shops_currently_opened))
            .usageId(onlyOpened)
            .show()
    }

    fun startOnlySubscribtionOnboarding(a: MapsActivity) {
        val onlySubscribed = a.getString(R.string.show_subscriptions)
        SpotlightView.Builder(a)
            .setConfiguration(config(a))
            .target(a.layout_show_subscribed)
            .headingTvText(onlySubscribed)
            .subHeadingTvText(a.getString(R.string.only_shops_currently_subscribed))
            .usageId(onlySubscribed)
            .show()
    }

    private fun config(a: MapsActivity): SpotlightConfig = SpotlightConfig().apply {
        introAnimationDuration = 300
        isRevealAnimationEnabled = true
        fadingTextDuration = 300
        headingTvColor = ResourcesCompat.getColor(a.resources, R.color.colorTextWhite, null)
        headingTvSize = 32
        subHeadingTvColor = ResourcesCompat.getColor(a.resources, R.color.colorTextWhite, null)
        subHeadingTvSize = 16
        maskColor = ResourcesCompat.getColor(a.resources, R.color.colorOnboardingBg, null)
        lineAnimationDuration = 150
        lineAndArcColor = ResourcesCompat.getColor(a.resources, R.color.colorAccent, null)
        isDismissOnTouch = true
        isDismissOnBackpress = true
    }
}