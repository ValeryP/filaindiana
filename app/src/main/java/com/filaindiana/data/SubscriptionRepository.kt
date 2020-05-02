package com.filaindiana.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 24.04.2020
 */
class SubscriptionRepository private constructor(private val subscriptionsDao: SubscriptionDao) {

    fun getSubscriptions() = subscriptionsDao.getAll()
    suspend fun getSubscriptionsSync() = subscriptionsDao.getAllSync()
    suspend fun getSubscriptionSync(shopId: String) = subscriptionsDao.getSync(shopId).firstOrNull()

    @WorkerThread
    suspend fun saveSubscription(
        shopId: String,
        shopName: String,
        shopAddress: String,
        shopBrand: String,
        lat: Double,
        lng: Double
    ) = withContext(Dispatchers.IO) {
        subscriptionsDao.insert(Subscription(shopId, shopName, shopAddress, shopBrand, lat, lng))
    }

    @WorkerThread
    suspend fun deleteSubscription(shopId: String) = withContext(Dispatchers.IO) {
        subscriptionsDao.delete(shopId)
    }

    @WorkerThread
    suspend fun deleteSubscriptions(ids: ArrayList<String>?) = withContext(Dispatchers.IO) {
        ids?.forEach { subscriptionId -> deleteSubscription(subscriptionId) }
    }

    companion object {
        @Volatile
        private var instance: SubscriptionRepository? = null

        fun getInstance(subscriptionsDao: SubscriptionDao) =
            instance ?: synchronized(this) {
                instance ?: SubscriptionRepository(subscriptionsDao).also { instance = it }
            }
    }
}