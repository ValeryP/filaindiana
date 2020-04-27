package com.filaindiana.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.filaindiana.R

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 24.04.2020
 */
@Database(entities = [Subscription::class], version = 1, exportSchema = false)
abstract class AppDB : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        fun getDatabase(context: Context): AppDB = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(ctx: Context) =
            Room.databaseBuilder(
                ctx.applicationContext,
                AppDB::class.java,
                ctx.getString(R.string.db_name)
            ).build()
    }
}