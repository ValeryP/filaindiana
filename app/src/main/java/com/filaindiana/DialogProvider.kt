package com.filaindiana

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.dialog_marker_details.view.*

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 22.04.2020
 */
object DialogProvider {
    fun showGpsRequiredDialog(ctx: Context) {
        MaterialDialog(ctx).show {
            title(text = "Permission required")
            message(text = "Enable GPS to find closest supermarkets")
            cancelable(false)
            positiveButton(text = "Enable") {
                (ctx as Activity).startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    1
                )
            }
            negativeButton(text = "Exit") {
                (ctx as Activity).finishAndRemoveTask()
            }
        }
    }

    fun showMarkerDetails(
        ctx: Context,
        iconRes: Int,
        name: String,
        address: String,
        openHours: String,
        queueSizePeople: Int,
        queueWaitMinutes: Int,
        lastUpdateTime: String?,
        onSubscribeClicked: () -> Unit
    ) {
        MaterialDialog(ctx).show { customView(R.layout.dialog_marker_details) }.let {
            it.getCustomView().apply {
                layout_dialogMarkerDetails_img.setImageResource(iconRes)
                layout_dialogMarkerDetails_name.text = name
                layout_dialogMarkerDetails_address.text = address
                layout_dialogMarkerDetails_openHours.text = "Open: $openHours"
                layout_dialogMarkerDetails_queue.text =
                    "$queueSizePeople pers / $queueWaitMinutes min"
                layout_dialogMarkerDetails_update.text =
                    if (lastUpdateTime != null) "Last reported: $lastUpdateTime" else ""
                layout_dialogMarkerDetails_button.setOnClickListener { onSubscribeClicked() }
            }
        }
    }

    fun showPermissionRequiredDialog(ctx: Context) {
        MaterialDialog(ctx).show {
            title(text = "Permission required")
            message(
                text = "The app requires location permission to find closest shops. " +
                        "Click \"Enable\" to open an application settings screen and grant permissions."
            )
            cancelable(false)
            positiveButton(text = "Enable") {
                (ctx as Activity).startActivityForResult(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", ctx.packageName, null)
                }, 1)
            }
            negativeButton(text = "Exit") {
                (ctx as Activity).finishAndRemoveTask()
            }
        }
    }
}