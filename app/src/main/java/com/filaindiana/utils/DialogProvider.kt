package com.filaindiana.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff.Mode.MULTIPLY
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.res.ResourcesCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.filaindiana.R
import com.filaindiana.network.RestClient
import com.filaindiana.network.ShopsResponse.Shop
import com.google.android.gms.maps.model.LatLng
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import es.dmoral.toasty.Toasty
import io.nlopez.smartlocation.SmartLocation
import kotlinx.android.synthetic.main.dialog_marker_details.view.*
import kotlinx.android.synthetic.main.dialog_report.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 22.04.2020
 */
object DialogProvider {
    fun showGpsRequiredDialog(ctx: Context) {
        Firebase.analytics(ctx).logShowGpsRequiredDialog()
        MaterialDialog(ctx).show {
            title(text = ctx.getString(R.string.permission_required))
            message(text = ctx.getString(R.string.enable_gps))
            cancelable(false)
            positiveButton(text = ctx.getString(R.string.enable)) {
                (ctx as Activity).startActivityForResult(Intent(ACTION_LOCATION_SOURCE_SETTINGS), 1)
            }
            negativeButton(text = ctx.getString(R.string.exit)) {
                (ctx as Activity).finishAndRemoveTask()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun showShopDetails(
        ctx: Context,
        shop: Shop,
        isSubscribed: Boolean,
        onSubscribeClicked: () -> Unit
    ) {
        val openHours = shop.shopData.getOpeningHoursFormatted()
        val queueSizePeople = shop.shopShopState?.queueSizePeople ?: 0
        val queueWaitMinutes = shop.shopShopState?.queueWaitMinutes ?: 0
        Firebase.analytics(ctx).logShowShopDetailsDialog(shop, isSubscribed)
        MaterialDialog(ctx).show { customView(R.layout.dialog_marker_details) }.let { dialog ->
            dialog.getCustomView().apply {
                layout_dialogMarkerDetails_img.setImageResource(shop.shopData.getImgResId())
                layout_dialogMarkerDetails_name.text = shop.shopData.name
                layout_dialogMarkerDetails_address.text =
                    "${shop.shopData.address}, ${shop.shopData.city}"
                layout_dialogMarkerDetails_openHours.text =
                    ctx.getString(R.string.open_hours, openHours)
                when {
                    shop.isReportingRequired() -> {
                        layout_dialogMarkerDetails_queue.text =
                            context.getString(R.string.report_required)
                        layout_dialogMarkerDetails_queue.textSize = 20F
                    }
                    shop.shopData.isOpen -> {
                        layout_dialogMarkerDetails_queue.text =
                            ctx.getString(R.string.queue, queueSizePeople, queueWaitMinutes)
                        layout_dialogMarkerDetails_queue.setTextColor(
                            ResourcesCompat.getColor(
                                ctx.resources,
                                R.color.colorMarkerGreen,
                                null
                            )
                        )
                    }
                    else -> {
                        layout_dialogMarkerDetails_queue.text = ctx.getString(R.string.closed)
                        layout_dialogMarkerDetails_queue.setTextColor(
                            ResourcesCompat.getColor(
                                ctx.resources,
                                R.color.colorTextDark,
                                null
                            )
                        )
                    }
                }
                val lastUpdate = shop.shopShopState?.getLastUpdate()
                if (lastUpdate == null) {
                    layout_dialogMarkerDetails_update.hide()
                } else {
                    layout_dialogMarkerDetails_update.text =
                        ctx.getString(R.string.last_reported, lastUpdate)
                }
                if (isSubscribed) {
                    layout_dialogMarkerDetails_button_subscribe.text =
                        ctx.getString(R.string.unsubscribe)
                    layout_dialogMarkerDetails_button_subscribe.background.setColorFilter(
                        ResourcesCompat.getColor(
                            ctx.resources,
                            R.color.colorButtonGrey,
                            null
                        ), MULTIPLY
                    )
                    layout_dialogMarkerDetails_button_subscribe.setCompoundDrawablesWithIntrinsicBounds(
                        GraphicsProvider.getColoredIcon(
                            ctx,
                            R.drawable.ic_notifications_off_black_24dp,
                            R.color.colorTextWhite
                        ),
                        null,
                        null,
                        null
                    )
                } else {
                    layout_dialogMarkerDetails_button_subscribe.text =
                        ctx.getString(R.string.subscribe_for_updates)
                    layout_dialogMarkerDetails_button_subscribe.background.setColorFilter(
                        ResourcesCompat.getColor(
                            ctx.resources,
                            R.color.colorAccent,
                            null
                        ), MULTIPLY
                    )
                    layout_dialogMarkerDetails_button_subscribe.setCompoundDrawablesWithIntrinsicBounds(
                        GraphicsProvider.getColoredIcon(
                            ctx,
                            R.drawable.ic_notifications_active_black_24dp,
                            R.color.colorTextWhite
                        ),
                        null,
                        null,
                        null
                    )
                }
                layout_dialogMarkerDetails_button_subscribe.setOnClickListener {
                    dialog.dismiss()
                    onSubscribeClicked()
                }
                layout_dialogMarkerDetails_button_report.setOnClickListener {
                    dialog.dismiss()
                    showReportDialog(ctx, shop)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showReportDialog(context: Context, shop: Shop) {
        Firebase.analytics(context).logShowReportDialog(shop)
        MaterialDialog(context).show { customView(R.layout.dialog_report) }.let { dialog ->
            dialog.getCustomView().apply {
                layout_dialogReport_img.setImageResource(shop.shopData.getImgResId())
                layout_dialogReport_name.text = shop.shopData.name
                layout_dialogReport_address.text =
                    "${shop.shopData.address}, ${shop.shopData.city}"
                layout_dialogReport_openHours.text =
                    context.getString(R.string.open_hours, shop.shopData.getOpeningHoursFormatted())
                layout_dialogReport_queueSizeSeekbar.setIndicatorTextFormat("\${PROGRESS} people")
                layout_dialogReport_queueSizeSeekbar.onSeekChangeListener =
                    object : OnSeekChangeListener {
                        override fun onSeeking(seekParams: SeekParams) {
                            logDebug { seekParams.progress.toString() }
                        }

                        override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                            seekBar?.let {
                                Firebase.analytics(context).logSelectQueueSize(it.progress)
                            }
                        }
                    }
                layout_dialogReport_queueTimeSeekbar.setIndicatorTextFormat("\${PROGRESS} min")
                layout_dialogReport_queueTimeSeekbar.onSeekChangeListener =
                    object : OnSeekChangeListener {
                        override fun onSeeking(seekParams: SeekParams) {
                            logDebug { seekParams.progress.toString() }
                        }

                        override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                            seekBar?.let {
                                Firebase.analytics(context).logSelectQueueTime(it.progress)
                            }
                        }
                    }
                layout_dialogReport_button_report.setOnClickListener {
                    logDebug { "Click: send report" }
                    val lastLocation = SmartLocation.with(context)
                        .location().lastLocation.let { LatLng(it!!.latitude, it.longitude) }
                    val shopId = shop.shopData.marketId
                    val queueSize = layout_dialogReport_queueSizeSeekbar.progress
                    val queueTime = layout_dialogReport_queueTimeSeekbar.progress
                    Firebase.analytics(context)
                        .logClickSendReport(lastLocation, shopId, queueSize, queueTime)
                    CoroutineScope(IO).launch {
                        RestClient.report(lastLocation, shopId, queueSize, queueTime)
                    }
                    dialog.dismiss()
                    Toasty.success(
                        context,
                        context.getString(R.string.report_sent),
                        LENGTH_LONG,
                        true
                    ).show()
                }
            }
        }
    }

    fun showSubscribedDialog(ctx: Context, imgResId: Int) {
        MaterialDialog(ctx).show {
            title(text = ctx.getString(R.string.subscribed))
            icon(imgResId)
            message(text = ctx.getString(R.string.subscribed_details))
            positiveButton(text = ctx.getString(android.R.string.ok))
        }
    }

    fun showUnsubscribedDialog(ctx: Context, name: String, imgResId: Int) {
        MaterialDialog(ctx).show {
            title(text = ctx.getString(R.string.unsubscribed))
            icon(imgResId)
            message(text = ctx.getString(R.string.no_updates, name))
            positiveButton(text = ctx.getString(android.R.string.ok))
        }
    }

    fun showPermissionRequiredDialog(ctx: Context) {
        Firebase.analytics(ctx).logShowPermissionsRationaleDialog()
        MaterialDialog(ctx).show {
            title(text = ctx.getString(R.string.permission_required))
            message(text = ctx.getString(R.string.requires_location_permission_details))
            cancelable(false)
            positiveButton(text = ctx.getString(R.string.enable)) {
                (ctx as Activity).startActivityForResult(Intent().apply {
                    action = ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", ctx.packageName, null)
                }, 1)
            }
            negativeButton(text = ctx.getString(R.string.exit)) {
                (ctx as Activity).finishAndRemoveTask()
            }
        }
    }
}