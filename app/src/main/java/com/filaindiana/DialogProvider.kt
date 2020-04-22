package com.filaindiana

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.afollestad.materialdialogs.MaterialDialog

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