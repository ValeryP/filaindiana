package com.filaindiana.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 24.04.2020
 */
class SubscriptionRepository private constructor(private val subscriptionsDao: SubscriptionDao) {

    fun getSubscriptions() = subscriptionsDao.getAll()
    suspend fun getSubscriptionsSync() = subscriptionsDao.getAllSync()
    fun getSubscription(shopId: String) = subscriptionsDao.get(shopId)
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
        subscriptionsDao.insert(
            Subscription(shopId, shopName, shopAddress, shopBrand, lat, lng, true)
        )
    }

    @WorkerThread
    suspend fun deleteSubscription(shopId: String) = withContext(Dispatchers.IO) {
        subscriptionsDao.delete(shopId)
    }

    @WorkerThread
    suspend fun deleteSubscriptions() = withContext(Dispatchers.IO) {
        subscriptionsDao.deleteAll()
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