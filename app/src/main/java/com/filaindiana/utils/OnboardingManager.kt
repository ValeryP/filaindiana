package com.filaindiana.utils

import androidx.core.content.res.ResourcesCompat
import com.filaindiana.R
import com.filaindiana.map.MapsActivity
import com.wooplr.spotlight.SpotlightConfig
import com.wooplr.spotlight.utils.SpotlightSequence
import kotlinx.android.synthetic.main.activity_maps.*


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 26.04.2020
 */
object OnboardingManager {
    fun startOnboarding(a: MapsActivity) {
        SpotlightSequence.getInstance(a, config(a))
            .addSpotlight(
                a.layout_hide_closed_container,
                "Opened only",
                "Show only supermarkets which are currently opened",
                "17"
            )
            .addSpotlight(
                a.layout_show_subscribed,
                "Show subscriptions",
                "Show only supermarkets which you are currently subscribed for updates",
                "18"
            )
            .startSequence()
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