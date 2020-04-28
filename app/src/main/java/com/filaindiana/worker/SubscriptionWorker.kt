@file:Suppress("PrivatePropertyName")

package com.filaindiana.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType.CONNECTED
import com.filaindiana.data.AppDB
import com.filaindiana.data.SubscriptionRepository
import com.filaindiana.network.RestClient
import com.filaindiana.utils.NotificationBuilder
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

    companion object {
        fun enqueue(context: Context) {
            Log.v("xxx", "NotificationWorker scheduled")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(CONNECTED)
                .build()
            val work = PeriodicWorkRequestBuilder<NotificationWorker>(15, MINUTES)
                .setConstraints(constraints)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(CHANNEL_ID, KEEP, work)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val subscriptions = repo.getSubscriptionsSync()
            val shops = subscriptions.map { RestClient.getShops(it.lat, it.lng) }.flatten()
            val triggeredSubscription = subscriptions.filter { s ->
                val shop = shops.firstOrNull { it.shopData.marketId == s.shopId }
                shop != null && shop.shopData.isOpen && (shop.shopShopState?.queueWaitMinutes ?: Int.MAX_VALUE) < 15
            }
            Log.v(
                "xxx",
                "${subscriptions.size} subscription, ${shops.size} shops, ${triggeredSubscription.size} alerts"
            )
            NotificationBuilder.showNotification(triggeredSubscription, applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

}