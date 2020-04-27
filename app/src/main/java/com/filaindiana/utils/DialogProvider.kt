package com.filaindiana.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff.Mode.MULTIPLY
import android.net.Uri
import android.provider.Settings
import androidx.core.content.res.ResourcesCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.filaindiana.R
import kotlinx.android.synthetic.main.dialog_marker_details.view.*

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 22.04.2020
 */
object DialogProvider {
    fun showGpsRequiredDialog(ctx: Context) {
        MaterialDialog(ctx).show {
            title(text = ctx.getString(R.string.permission_required))
            message(text = ctx.getString(R.string.enable_gps))
            cancelable(false)
            positiveButton(text = ctx.getString(R.string.enable)) {
                (ctx as Activity).startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    1
                )
            }
            negativeButton(text = ctx.getString(R.string.exit)) {
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
        isOpened: Boolean,
        isSubscribed: Boolean,
        onSubscribeClicked: () -> Unit
    ) {
        MaterialDialog(ctx).show { customView(R.layout.dialog_marker_details) }.let { dialog ->
            dialog.getCustomView().apply {
                layout_dialogMarkerDetails_img.setImageResource(iconRes)
                layout_dialogMarkerDetails_name.text = name
                layout_dialogMarkerDetails_address.text = address
                layout_dialogMarkerDetails_openHours.text =
                    ctx.getString(R.string.open_hours, openHours)
                if (isOpened) {
                    layout_dialogMarkerDetails_queue.text =
                        ctx.getString(R.string.queue, queueSizePeople, queueWaitMinutes)
                    layout_dialogMarkerDetails_queue.setTextColor(
                        ResourcesCompat.getColor(
                            ctx.resources,
                            R.color.colorMarkerGreen,
                            null
                        )
                    )
                } else {
                    layout_dialogMarkerDetails_queue.text = ctx.getString(R.string.closed)
                    layout_dialogMarkerDetails_queue.setTextColor(
                        ResourcesCompat.getColor(
                            ctx.resources,
                            R.color.colorTextDark,
                            null
                        )
                    )
                }
                layout_dialogMarkerDetails_update.text = if (lastUpdateTime != null) ctx.getString(
                    R.string.last_reported,
                    lastUpdateTime
                ) else ""
                if (isSubscribed) {
                    layout_dialogMarkerDetails_button.text = ctx.getString(R.string.unsubscribe)
                    layout_dialogMarkerDetails_button.background.setColorFilter(
                        ResourcesCompat.getColor(
                            ctx.resources,
                            R.color.colorButtonGrey,
                            null
                        ), MULTIPLY
                    )
                    layout_dialogMarkerDetails_button.setCompoundDrawablesWithIntrinsicBounds(
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
                    layout_dialogMarkerDetails_button.text =
                        ctx.getString(R.string.subscribe_for_updates)
                    layout_dialogMarkerDetails_button.background.setColorFilter(
                        ResourcesCompat.getColor(
                            ctx.resources,
                            R.color.colorAccent,
                            null
                        ), MULTIPLY
                    )
                    layout_dialogMarkerDetails_button.setCompoundDrawablesWithIntrinsicBounds(
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
                layout_dialogMarkerDetails_button.setOnClickListener {
                    dialog.dismiss()
                    onSubscribeClicked()
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
        MaterialDialog(ctx).show {
            title(text = ctx.getString(R.string.permission_required))
            message(text = ctx.getString(R.string.requires_location_permission_details))
            cancelable(false)
            positiveButton(text = ctx.getString(R.string.enable)) {
                (ctx as Activity).startActivityForResult(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", ctx.packageName, null)
                }, 1)
            }
            negativeButton(text = ctx.getString(R.string.exit)) {
                (ctx as Activity).finishAndRemoveTask()
            }
        }
    }
}