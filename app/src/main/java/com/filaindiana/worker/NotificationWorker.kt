@file:Suppress("PrivatePropertyName")

package com.filaindiana.worker

import android.app.Notification.FLAG_AUTO_CANCEL
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType.CONNECTED
import com.filaindiana.R
import com.filaindiana.data.AppDB
import com.filaindiana.data.Subscription
import com.filaindiana.data.SubscriptionRepository
import com.filaindiana.map.MapsActivity
import com.filaindiana.network.RestClient
import java.util.concurrent.TimeUnit.MINUTES

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */

const val CHANNEL_ID = "Fila Indiana Notifications"

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    private val subscriptionsDao = AppDB.getDatabase(appContext).subscriptionDao()
    private val repo = SubscriptionRepository.getInstance(subscriptionsDao)
    private val client = RestClient.build()

    companion object {
        fun enqueue(context: Context) {
            Log.v("xxx", "NotificationWorker scheduled")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(CONNECTED)
                .build()
            val work = PeriodicWorkRequestBuilder<NotificationWorker>(5, MINUTES)
                .setConstraints(constraints)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(CHANNEL_ID, KEEP, work)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val triggeredSubscription = mutableListOf<Subscription>()
            val subscriptions = repo.getSubscriptionsSync()
            val shops = subscriptions.map { client.getShops(it.lat, it.lng) }.flatten()
            for (s in subscriptions) {
                shops.firstOrNull { it.shopData.marketId == s.shopId }?.let { shop ->
                    if ((shop.shopShopState?.queueWaitMinutes ?: Int.MAX_VALUE) < 15) {
                        triggeredSubscription.add(s)
                    }
                }
            }
            Log.v(
                "xxx",
                "${subscriptions.size} subscription, ${shops.size} shops, ${triggeredSubscription.size} alerts"
            )
            showNotification(triggeredSubscription)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(triggeredSubscription: MutableList<Subscription>) {
        createNotificationChannel()
        val activityIntent = Intent(applicationContext, MapsActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentOpenApp: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, activityIntent, 0)
        val content =
            "${triggeredSubscription.joinToString(", ") { it.shopName }} have a queue < 15 min"
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
            .setContentTitle("Time to shop!")
            .setContentText(content)
            .setAutoCancel(true)
            .setContentIntent(pendingIntentOpenApp)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(101010, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val importance = IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance).apply {
                description = "Notification when the supermarket has a queue < 15 min"
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}