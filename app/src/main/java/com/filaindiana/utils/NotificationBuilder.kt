package com.filaindiana.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.*
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import com.filaindiana.R
import com.filaindiana.map.MapsActivity
import com.filaindiana.network.ShopsResponse
import com.filaindiana.worker.CHANNEL_ID
import com.filaindiana.worker.UnsubscribeActionReceiver

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
object NotificationBuilder {
    const val KEY_SUBSCRIPTON_LOCATION = "SUBSCRIPTON_LOCATION"
    const val NOTIFICATION_ID = 101010

    internal fun showNotification(subscriptions: List<ShopsResponse.Shop>, con: Context) {
        if (subscriptions.isEmpty()) return

        createNotificationChannel(con)
        val activityIntent = Intent(con, MapsActivity::class.java).apply {
            flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_MAIN
            addCategory(CATEGORY_LAUNCHER)
            putExtra(KEY_SUBSCRIPTON_LOCATION, subscriptions.first().shopData.getLocation())
        }
        val unsubscribeIntent = Intent(con, UnsubscribeActionReceiver::class.java)
        val pendingIntentUnsubscribeIntent =
            PendingIntent.getBroadcast(con, 0, unsubscribeIntent, FLAG_UPDATE_CURRENT)
        val pendingIntentOpenApp: PendingIntent =
            PendingIntent.getActivity(con, 0, activityIntent, FLAG_UPDATE_CURRENT)
        val content = "${subscriptions.size} shops have a queue < 15 min"
        val subscriptionShops =
            subscriptions.joinToString(", ") { "${it.shopData.name} (${it.shopData.address})" }
        val contentBig = "$subscriptionShops have a queue < 15 min"
        val largeIcon = ResourcesCompat.getDrawable(
            con.resources,
            subscriptions.first().shopData.getImgResId(),
            null
        )!!.toBitmapScaled(256).makeSquare()
        val builder = NotificationCompat.Builder(con, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
            .setLargeIcon(largeIcon)
            .setContentTitle("Time to shop!")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentBig))
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notifications_off_black_24dp,
                "Unsubscribe all",
                pendingIntentUnsubscribeIntent
            )
            .setContentIntent(pendingIntentOpenApp)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        with(NotificationManagerCompat.from(con)) { notify(NOTIFICATION_ID, builder.build()) }
    }

    private fun createNotificationChannel(con: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance).apply {
                description = "Notification when the supermarket has a queue < 15 min"
            }
            val notificationManager: NotificationManager =
                con.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}