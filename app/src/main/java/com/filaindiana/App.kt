@file:Suppress("unused")

package com.filaindiana

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 18.04.2020
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }
}