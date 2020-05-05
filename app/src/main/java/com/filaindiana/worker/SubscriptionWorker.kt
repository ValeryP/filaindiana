@file:Suppress("PrivatePropertyName")

package com.filaindiana.worker

import android.content.Context
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import com.filaindiana.data.AppDB
import com.filaindiana.data.SubscriptionRepository
import com.filaindiana.network.RestClient
import com.filaindiana.utils.Firebase
import com.filaindiana.utils.NotificationBuilder
import com.filaindiana.utils.logInfo
import java.util.concurrent.TimeUnit.MINUTES

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */

const val CHANNEL_ID = "FilaIndiana Notifications"

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    private val subscriptionsDao = AppDB.getDatabase(appContext).subscriptionDao()
    private val repo = SubscriptionRepository.getInstance(subscriptionsDao)

    companion object {
        fun enqueue(context: Context) {
            logInfo { "NotificationWorker scheduled" }
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(CONNECTED)
                .build()
            val work = PeriodicWorkRequestBuilder<NotificationWorker>(15, MINUTES)
                .setConstraints(constraints)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(CHANNEL_ID, REPLACE, work)
            Firebase.analytics(context).logWorkerStarted()
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val subscriptions = repo.getSubscriptionsSync()
            val shops = subscriptions.map { RestClient.getShops(it.lat, it.lng) }.flatten()
            val triggered = subscriptions.filter { subscription ->
                val shop = shops.firstOrNull { it.shopData.marketId == subscription.shopId }
                val isOpen = shop?.isOpen() ?: false
                val isStateUpdated =
                    shop?.shopShopState?.getUpdateTime()?.isAfter(subscription.getTime()) ?: false
                val hasSmallerQueue = (shop?.shopShopState?.queueWaitMinutes ?: Int.MAX_VALUE) <= 15
                isOpen && isStateUpdated && hasSmallerQueue
            }
            logInfo { "${subscriptions.size} subscription, ${shops.size} shops, ${triggered.size} alerts" }
            NotificationBuilder.showNotification(triggered, applicationContext)
            Firebase.analytics(this.applicationContext).logWorkerNotificationTriggered(triggered)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

}