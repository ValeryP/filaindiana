package com.filaindiana.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color.WHITE
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import com.filaindiana.data.Subscription
import com.filaindiana.network.ShopsResponse
import kotlin.math.max


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.pxToSp: Int
    get() = (this / Resources.getSystem().displayMetrics.scaledDensity).toInt()
val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.dpToSp: Int
    get() = (this.dpToPx / Resources.getSystem().displayMetrics.scaledDensity).toInt()
val Int.spToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()

fun List<ShopsResponse.Shop>.filterOpen(): List<ShopsResponse.Shop> {
    return this.filter { it.shopShopState != null && it.shopData.isOpen }
}

fun List<ShopsResponse.Shop>.filterSubscribed(subscriptions: List<Subscription>): List<ShopsResponse.Shop> {
    val subscriptionIds = subscriptions.map { it.shopId }
    return this.filter { subscriptionIds.contains(it.shopData.marketId) }
}

fun Drawable.toBitmapScaled(width: Int): Bitmap {
    Canvas()
    val maxSide = max(this.intrinsicWidth, this.intrinsicHeight)
    val scale = width.toFloat() / maxSide
    return this.toBitmap(
        (this.intrinsicWidth * scale).toInt(),
        (this.intrinsicHeight * scale).toInt()
    )
}

fun Bitmap.makeSquare(): Bitmap {
    val dim = max(this.width, this.height)
    val dstBmp = Bitmap.createBitmap(dim, dim, ARGB_8888)
    val canvas = Canvas(dstBmp)
    canvas.drawColor(WHITE)
    canvas.drawBitmap(
        this,
        (dim - this.width) / 2.toFloat(),
        (dim - this.height) / 2.toFloat(),
        null
    )
    return dstBmp
}