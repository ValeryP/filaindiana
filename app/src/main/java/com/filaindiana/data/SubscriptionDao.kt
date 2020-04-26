package com.filaindiana.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 24.04.2020
 */
@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun getAll(): LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions")
    suspend fun getAllSync(): List<Subscription>

    @Query("SELECT * FROM subscriptions WHERE shopId = :shopId")
    fun get(shopId: String): LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE shopId = :shopId")
    suspend fun getSync(shopId: String): List<Subscription>

    @Insert
    suspend fun insert(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE shopId = :shopId")
    suspend fun delete(shopId: String)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}