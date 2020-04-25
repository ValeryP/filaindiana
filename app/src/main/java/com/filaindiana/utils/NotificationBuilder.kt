package com.filaindiana.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import com.filaindiana.R
import com.filaindiana.map.MapsActivity
import com.filaindiana.network.ShopsResponse
import com.filaindiana.worker.CHANNEL_ID

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
object NotificationBuilder {

    internal fun showNotification(subscriptions: List<ShopsResponse.Shop>, con: Context) {
        if (subscriptions.isEmpty()) return

        createNotificationChannel(con)
        val activityIntent = Intent(con, MapsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentOpenApp: PendingIntent =
            PendingIntent.getActivity(con, 0, activityIntent, 0)
        val content =
            "${subscriptions.joinToString(", ") { it.shopData.name }} have a queue < 15 min"
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
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setAutoCancel(true)
            .setContentIntent(pendingIntentOpenApp)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        with(NotificationManagerCompat.from(con)) {
            notify(101010, builder.build())
        }
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