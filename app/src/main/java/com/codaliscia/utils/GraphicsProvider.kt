package com.codaliscia.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.PixelCopy
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.codaliscia.R
import com.codaliscia.map.MapsActivity
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
object GraphicsProvider {
    fun getColoredIcon(con: Context, @DrawableRes r: Int, @ColorRes col: Int): Drawable? {
        return ResourcesCompat.getDrawable(con.resources, r, null)?.let {
            val drawableWrapped = DrawableCompat.wrap(it)
            val color = ResourcesCompat.getColor(con.resources, col, null)
            DrawableCompat.setTint(drawableWrapped, color)
            drawableWrapped
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getBitmapFromViewO(view: View, activity: MapsActivity): Bitmap =
        suspendCoroutine { cont ->
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, ARGB_8888)
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val rect = Rect(
                location[0],
                location[1],
                location[0] + view.measuredWidth,
                location[1] + view.measuredHeight
            )
            PixelCopy.request(
                activity.window,
                rect,
                bitmap, {
                    if (it == PixelCopy.SUCCESS) {
                        cont.resume(bitmap)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }

    fun getBitmapFromView(view: View, activity: MapsActivity): Bitmap {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        @Suppress("DEPRECATION")
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun getShopImgResId(brand: String): Int {
        return when (brand) {
            "auchan" -> R.drawable.auchan
            "bennet" -> R.drawable.bennet
            "carrefour" -> R.drawable.carrefour
            "coop" -> R.drawable.coop
            "crai" -> R.drawable.crai
            "despar" -> R.drawable.despar
            "eataly" -> R.drawable.eataly
            "ekom" -> R.drawable.ekom
            "esselunga" -> R.drawable.esselunga
            "eurospin" -> R.drawable.eurospin
            "famila" -> R.drawable.famila
            "galassia" -> R.drawable.galassia
            "gigante" -> R.drawable.gigante
            "iper" -> R.drawable.iper
            "iperal" -> R.drawable.iperal
            "lidl" -> R.drawable.lidl
            "md" -> R.drawable.md
            "naturasì" -> R.drawable.naturasi
            "pam" -> R.drawable.pam
            "penny" -> R.drawable.penny
            "simply" -> R.drawable.simply
            "superc" -> R.drawable.superc
            "tigros" -> R.drawable.tigros
            "unes" -> R.drawable.unes
            else -> R.drawable.generic
        }
    }
}