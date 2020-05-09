package com.codaliscia.utils

import android.util.Log

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 30.04.2020
 */
inline fun logDebug(tag: String = "xxx", msg: () -> String) {
    Log.d(tag, msg())
}

inline fun logInfo(tag: String = "xxx", msg: () -> String) {
    val message = msg()
    Log.i(tag, message)
    Firebase.crashlytics().log(message)
}

inline fun logWarn(tag: String = "xxx", msg: () -> String) {
    val message = msg()
    Log.w(tag, message)
    Firebase.crashlytics().log(message)
}

inline fun logError(tag: String = "xxx", msg: () -> String) {
    val message = msg()
    Log.e(tag, message)
    Firebase.crashlytics().log(message)
}

fun logException(error: Throwable, tag: String = "xxx") {
    Log.e(tag, error.toString(), error)
    Firebase.crashlytics().log(error.toString())
    Firebase.crashlytics().silentCrash(error)
}