package com.codaliscia.utils

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
import com.codaliscia.R
import com.codaliscia.data.KEY_SUBSCRIPTONS_ID
import com.codaliscia.data.KEY_SUBSCRIPTON_LOCATION
import com.codaliscia.data.Subscription
import com.codaliscia.map.MapsActivity
import com.codaliscia.worker.CHANNEL_ID
import com.codaliscia.worker.UnsubscribeActionReceiver
import java.util.ArrayList

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
object NotificationBuilder {
    const val NOTIFICATION_ID = 101010

    internal fun showNotification(subscriptions: List<Subscription>, con: Context) {
        if (subscriptions.isEmpty()) return

        createNotificationChannel(con)
        val activityIntent = Intent(con, MapsActivity::class.java).apply {
            flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_MAIN
            addCategory(CATEGORY_LAUNCHER)
            putExtra(KEY_SUBSCRIPTON_LOCATION, subscriptions.first().getLocation())
        }
        val unsubscribeIntent = Intent(con, UnsubscribeActionReceiver::class.java).apply {
            putStringArrayListExtra(KEY_SUBSCRIPTONS_ID, subscriptions.map { it.shopId } as ArrayList<String>)
        }
        val pendingIntentUnsubscribeIntent =
            PendingIntent.getBroadcast(con, 0, unsubscribeIntent, FLAG_UPDATE_CURRENT)
        val pendingIntentOpenApp: PendingIntent =
            PendingIntent.getActivity(con, 0, activityIntent, FLAG_UPDATE_CURRENT)
        val content = con.getString(R.string.queue_details, subscriptions.size.toString())
        val subscriptionShops =
            subscriptions.joinToString(", ") { "${it.shopName} (${it.shopAddress})" }
        val contentBig = con.getString(R.string.queue_details, subscriptionShops)
        val largeIcon = ResourcesCompat.getDrawable(
            con.resources,
            GraphicsProvider.getShopImgResId(subscriptions.first().shopBrand),
            null
        )!!.toBitmapScaled(256).makeSquare()
        val builder = NotificationCompat.Builder(con, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(con.getString(R.string.time_to_shop))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentBig))
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_star_outline_24px,
                con.getString(R.string.unsibscribe_all),
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
                description = con.getString(R.string.notification_channel_description)
            }
            val notificationManager: NotificationManager =
                con.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}