package com.filaindiana.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import coil.api.load
import com.filaindiana.R
import com.filaindiana.network.ShopsResponse
import com.filaindiana.utils.GraphicsProvider
import com.filaindiana.utils.hide
import com.filaindiana.utils.show
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
    @SuppressLint("InflateParams")
    suspend fun buildMarkerViewAsync(shop: ShopsResponse.Shop, isSubscribed: Boolean): Bitmap =
        withContext(CoroutineScope(Dispatchers.Main).coroutineContext) {
            val marker: View =
                LayoutInflater.from(activity).inflate(R.layout.view_marker, null).apply {
                    this.view_img.load(shop.shopData.getImgResId())
                    if (isSubscribed) {
                        this.view_subscription.show()
                    } else {
                        this.view_subscription.hide()
                    }
                    if (shop.isReportingRequired()) {
                        this.view_text_bg.setBackgroundResource(R.drawable.bg_rounded_blue)
                        this.view_text_number.text = ""
                        this.view_text_min.text = activity.getString(R.string.report)
                    } else if (shop.shopShopState == null || !shop.shopData.isOpen) {
                        this.view_text_bg.setBackgroundResource(R.drawable.bg_rounded_grey)
                        this.view_text_number.text = ""
                        this.view_text_min.text = activity.getString(R.string.closed)
                    } else {
                        this.view_text_bg.setBackgroundResource(shop.shopShopState.getStatusColor())
                        this.view_text_min.text =
                            if (shop.shopShopState.queueWaitMinutes >= 0) activity.getString(R.string.min) else ""
                        this.view_text_number.text = shop.shopShopState.queueWaitMinutes.toString()
                        this.view_text_bg.alpha = shop.shopShopState.getUpdateFreshness()
                    }
                }
            addMarkerView(marker)
            markerToBitmap(marker)
        }

    private suspend fun markerToBitmap(marker: View): Bitmap {
        return if (VERSION.SDK_INT >= VERSION_CODES.O) {
            GraphicsProvider.getBitmapFromViewO(marker, activity)
        } else {
            GraphicsProvider.getBitmapFromView(marker, activity)
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
}