package com.codaliscia.utils

import android.content.Context
import android.os.Bundle
import com.codaliscia.data.Subscription
import com.codaliscia.network.ShopsResponse.Shop
import com.codaliscia.utils.Firebase.Companion.Events.*
import com.codaliscia.utils.Firebase.Companion.Params.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 29.04.2020
 */
class Firebase {
    companion object {
        enum class Events {
            SUBSCRIPTION_CREATED,
            REQUEST_LOCATION_SEARCH,
            REQUEST_PERMISSIONS,
            REQUEST_PERMISSIONS_DENIED,
            REQUEST_PERMISSIONS_DENIED_PERMANENTLY,
            SHOW_PERMISSIONS_RATIONALE_DIALOG,
            ACCEPTED_PERMISSIONS_RATIONALE,
            ACCEPTED_PERMISSIONS,
            WORKER_STARTED,
            WORKER_NOTIFICATION_TRIGGERED,
            SUBSCRIPTION_CANCELED,
            SHOW_YOU_ARE_SUBCRIBED_DIALOG,
            SHOW_YOU_ARE_UNSUBCRIBED_DIALOG,
            CLICK_SEND_REPORT,
            SELECT_QUEUE_SIZE,
            SELECT_QUEUE_TIME,
            SHOW_REPORT_DIALOG,
            SHOW_DETAILS_DIALOG,
            MAP_MOVED,
            FETCHING_NEW_POINTS,
            CLICK_SHOW_OPENED_ONLY,
            CLICK_SHOW_SUBSCRIBED_ONLY,
            SHOW_GPS_REQUIRED_DIALOG,
        }

        enum class Params {
            SHOP_BRAND,
            SHOP_CITY,
            SHOP_ID,
            USER_LOCATION,
            QUEUE_SIZE,
            QUEUE_TIME,
            IS_SHOP_SUBSCRIBED,
            STATE,
        }

        class Analytics(context: Context) {
            private val fa = FirebaseAnalytics.getInstance(context)

            fun setupUser() = fa.setUserId(PrefsUtils.getUserId())
            fun logSavedSubscription(shop: Shop) {
                log(
                    Event(SUBSCRIPTION_CREATED)
                        .with(SHOP_BRAND, shop.shopData.brand)
                        .with(SHOP_CITY, shop.shopData.city)
                        .with(SHOP_ID, shop.shopData.marketId)
                )
            }

            fun logShowYouAreSubcribedDialog(shop: Shop) {
                log(
                    Event(SHOW_YOU_ARE_SUBCRIBED_DIALOG)
                        .with(SHOP_BRAND, shop.shopData.brand)
                        .with(SHOP_CITY, shop.shopData.city)
                        .with(SHOP_ID, shop.shopData.marketId)
                )
            }

            fun logShowYouAreUnsubcribedDialog(shop: Shop) {
                log(
                    Event(SHOW_YOU_ARE_UNSUBCRIBED_DIALOG)
                        .with(SHOP_BRAND, shop.shopData.brand)
                        .with(SHOP_CITY, shop.shopData.city)
                        .with(SHOP_ID, shop.shopData.marketId)
                )
            }

            fun logLocationSearchStarted() = log(Event(REQUEST_LOCATION_SEARCH))
            fun logRequestPermissions() = log(Event(REQUEST_PERMISSIONS))
            fun logPermissionsDenied() = log(Event(REQUEST_PERMISSIONS_DENIED))
            fun logPermissionsDeniedPermanently() =
                log(Event(REQUEST_PERMISSIONS_DENIED_PERMANENTLY))

            fun logShowPermissionsRationaleDialog() = log(Event(SHOW_PERMISSIONS_RATIONALE_DIALOG))
            fun logPermissionsAccepted() = log(Event(ACCEPTED_PERMISSIONS))
            fun logPermissionsRationaleAccepted() = log(Event(ACCEPTED_PERMISSIONS_RATIONALE))
            fun logWorkerStarted() = log(Event(WORKER_STARTED))
            fun logSubscriptionsCanceled() = log(Event(SUBSCRIPTION_CANCELED))
            fun logWorkerNotificationTriggered(triggeredSubscription: List<Subscription>) {
                triggeredSubscription.forEach {
                    val city = try {
                        it.shopAddress.split(",").last().trim()
                    } catch (e: Exception) {
                        null
                    }
                    log(
                        Event(WORKER_NOTIFICATION_TRIGGERED)
                            .with(SHOP_BRAND, it.shopBrand)
                            .with(SHOP_ID, it.shopId)
                            .apply { if (city != null) this.with(SHOP_CITY, city) }
                    )
                }
            }

            fun logClickSendReport(latLng: LatLng, shopId: String, queueSize: Int, queueTime: Int) =
                log(
                    Event(CLICK_SEND_REPORT)
                        .with(USER_LOCATION, latLng.toString())
                        .with(SHOP_ID, shopId)
                        .with(QUEUE_SIZE, queueSize)
                        .with(QUEUE_TIME, queueTime)
                )

            fun logSelectQueueSize(queueSize: Int) =
                log(Event(SELECT_QUEUE_SIZE).with(QUEUE_SIZE, queueSize))

            fun logSelectQueueTime(queueTime: Int) =
                log(Event(SELECT_QUEUE_TIME).with(QUEUE_TIME, queueTime))

            fun logShowReportDialog(shop: Shop) {
                log(
                    Event(SHOW_REPORT_DIALOG)
                        .with(SHOP_BRAND, shop.shopData.brand)
                        .with(SHOP_CITY, shop.shopData.city)
                        .with(SHOP_ID, shop.shopData.marketId)
                )
            }

            fun logShowShopDetailsDialog(shop: Shop, subscribed: Boolean) {
                log(
                    Event(SHOW_DETAILS_DIALOG)
                        .with(SHOP_BRAND, shop.shopData.brand)
                        .with(SHOP_CITY, shop.shopData.city)
                        .with(SHOP_ID, shop.shopData.marketId)
                        .with(IS_SHOP_SUBSCRIBED, subscribed)
                )
            }


            fun logMapMoved(location: LatLng) =
                log(Event(MAP_MOVED).with(USER_LOCATION, location.toString()))

            fun logFetchingNewPoints(location: LatLng) =
                log(Event(FETCHING_NEW_POINTS).with(USER_LOCATION, location.toString()))

            fun logClickShowOpenedOnly(state: Boolean) = log(
                Event(CLICK_SHOW_OPENED_ONLY)
                    .with(STATE, state)
            )

            fun logClickShowSubscribedOnly(state: Boolean) = log(
                Event(CLICK_SHOW_SUBSCRIBED_ONLY)
                    .with(STATE, state)
            )

            fun logShowGpsRequiredDialog() = log(Event(SHOW_GPS_REQUIRED_DIALOG))

            private fun log(event: Event) = fa.logEvent(event.eventName.name, event.bundle)

            @Suppress("unused")
            private class Event(val eventName: Events) {
                internal val bundle = Bundle()
                fun with(key: Params, value: Boolean): Event {
                    bundle.putBoolean(key.name, value)
                    return this
                }

                fun with(key: Params, value: Int): Event {
                    bundle.putInt(key.name, value)
                    return this
                }

                fun with(key: Params, value: Float): Event {
                    bundle.putFloat(key.name, value)
                    return this
                }

                fun with(key: Params, value: String): Event {
                    bundle.putString(key.name, value)
                    return this
                }
            }
        }

        class Crashlytics {
            private val fc = FirebaseCrashlytics.getInstance()
            fun setupUser() = fc.setUserId(PrefsUtils.getUserId())
            fun log(data: String) = fc.log(data)
            fun silentCrash(exception: Throwable) = fc.recordException(exception)
        }

        fun analytics(context: Context) = Analytics(context)
        fun crashlytics() = Crashlytics()
    }
}