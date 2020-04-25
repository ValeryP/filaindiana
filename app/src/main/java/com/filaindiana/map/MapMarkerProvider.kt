package com.filaindiana.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.PixelCopy.SUCCESS
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import coil.api.load
import com.filaindiana.R
import com.filaindiana.network.ShopsResponse
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.view_marker.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 22.04.2020
 */
class MapMarkerProvider(private val activity: MapsActivity) {
    suspend fun buildMarkerViewAsync(
        shop: ShopsResponse.Shop,
        isSubscriptionEnabled: Boolean
    ): Bitmap? =
        withContext(CoroutineScope(Dispatchers.Main).coroutineContext) {
            val layoutInflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val marker: View = layoutInflater.inflate(R.layout.view_marker, null).apply {
                this.view_img.load(shop.shopData.getImgResId())
                this.view_subscription.visibility = if (isSubscriptionEnabled) VISIBLE else GONE
                if (shop.shopShopState == null || !shop.shopData.isOpen) {
                    this.view_text_bg.setBackgroundResource(R.drawable.bg_rounded_grey)
                    this.view_text_number.text = ""
                    this.view_text_min.text = "Closed"
                } else {
                    this.view_text_bg.setBackgroundResource(shop.shopShopState.getStatusColor())
                    this.view_text_min.text =
                        if (shop.shopShopState.queueWaitMinutes >= 0) "min" else ""
                    this.view_text_number.text = shop.shopShopState.queueWaitMinutes.toString()
                    this.view_text_bg.alpha = shop.shopShopState.getUpdateFreshness()
                }
            }
            addMarkerView(marker)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getBitmapFromViewO(marker)
            } else {
                getBitmapFromView(marker)
            }
        }

    private suspend fun addMarkerView(marker: View): Boolean = suspendCoroutine { cont ->
        activity.layout_footer_view.let {
            it.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    cont.resume(true)
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
        activity.layout_footer_view.addView(marker)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getBitmapFromViewO(view: View): Bitmap? =
        suspendCoroutine { cont ->
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            val bitmap = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
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
                    if (it == SUCCESS) {
                        cont.resume(bitmap)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }

    private fun getBitmapFromView(view: View): Bitmap? {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        @Suppress("DEPRECATION")
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}