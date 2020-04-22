@file:Suppress("unused")

package com.filaindiana

import android.app.Application
import coil.ImageLoader
import coil.util.CoilUtils
import net.danlew.android.joda.JodaTimeAndroid
import okhttp3.OkHttpClient


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 18.04.2020
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
        ImageLoader(this) {
            allowHardware(false)
            okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(this@App))
                    .build()
            }
        }
    }
}