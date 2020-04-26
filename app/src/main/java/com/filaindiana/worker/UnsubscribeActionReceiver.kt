package com.filaindiana.worker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.util.Log
import com.filaindiana.data.AppDB
import com.filaindiana.data.SubscriptionRepository
import com.filaindiana.utils.NotificationBuilder.NOTIFICATION_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 26.04.2020
 */
class UnsubscribeActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(IO).launch {
            val subscriptionsDao = AppDB.getDatabase(context).subscriptionDao()
            val repo = SubscriptionRepository.getInstance(subscriptionsDao)
            repo.deleteSubscriptions()

            val nManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(NOTIFICATION_ID)
        }
    }
}