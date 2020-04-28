package com.filaindiana.map

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.filaindiana.R
import com.filaindiana.data.AppDB
import com.filaindiana.data.Subscription
import com.filaindiana.data.SubscriptionRepository
import com.filaindiana.network.RestClient
import com.filaindiana.network.ShopsResponse.Shop
import com.filaindiana.utils.DialogProvider
import com.filaindiana.utils.OnboardingManager
import com.filaindiana.utils.PrefsUtils
import com.filaindiana.utils.filterSubscribed
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.FirebaseAnalytics.Event.GENERATE_LEAD
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME
import es.dmoral.toasty.Toasty
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationParams
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import kotlin.concurrent.schedule

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 22.04.2020
 */
class MapHelper(private val activity: MapsActivity, val mMap: GoogleMap) :
    GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {
    private var state: MapState
    private var repo: SubscriptionRepository
    private val mapJobs = mutableListOf<Job>()

    init {
        freezeMap()
        mMap.setOnMarkerClickListener(this@MapHelper)
        state = MapState()
        repo = SubscriptionRepository.getInstance(AppDB.getDatabase(activity).subscriptionDao())
        repo.getSubscriptions().observe(activity, Observer { state.setSubscriptions(it) })
        state.shopsFiltered.observe(activity, Observer { shops -> invalidateMap(shops) })
        state.filters.observe(activity, Observer { invalidateViews(it) })
    }

    private fun invalidateViews(filters: ShopFilters) {
        val color = ResourcesCompat.getColor(
            activity.resources,
            if (filters.isSubscribed) R.color.colorMarkerRed else R.color.colorMarkerGrey,
            null
        )
        activity.layout_show_subscribed.drawable.setTint(color)
        activity.layout_show_subscribed.alpha = if (filters.isSubscribed) 1f else 0.5f
        if (filters.isSubscribed && !PrefsUtils.isOnboardingShownSubsctiptionFilter()) {
            Toasty.info(activity, activity.getString(R.string.filter_shops_subscribed)).show()
            PrefsUtils.setOnboardingShownSubsctiptionFilter()
        }
        if (filters.isOnlyOpened && !PrefsUtils.isOnboardingShownOpenedFilter()) {
            Toasty.info(activity, activity.getString(R.string.filter_shops_opened)).show()
            PrefsUtils.setOnboardingShownOpenedFilter()
        }
    }

    private fun invalidateMap(points: List<Shop>) {
        if (points.isEmpty() && state.shopsAll().isNotEmpty()) {
            Toasty.info(activity, activity.getString(R.string.no_subscribed_shops)).show()
            Timer().schedule(1000) { CoroutineScope(Main).launch { state.toogleSubscribed() } }
        } else {
            CoroutineScope(Main).launch {
                mMap.clear()
                freezeMap()
                val subscriptions = repo.getSubscriptionsSync()
                addShopsOnTheMap(points, subscriptions)
                val isSubscribtionsVisible = points.filterSubscribed(subscriptions).isNotEmpty()
                if (isSubscribtionsVisible) {
                    OnboardingManager.startOnlySubscribtionOnboarding(activity)
                    activity.layout_show_subscribed.visibility = VISIBLE
                } else {
                    activity.layout_show_subscribed.visibility = GONE
                }
                defreezeMap()
            }
        }
    }

    private fun freezeMap() {
        activity.layout_show_subscribed.isEnabled = false
        activity.layout_show_subscribed.isClickable = false
        activity.layout_hide_closed.isEnabled = false
        activity.layout_hide_closed.isClickable = false
        mMap.uiSettings.apply {
            isRotateGesturesEnabled = false
            isScrollGesturesEnabled = false
            isTiltGesturesEnabled = false
            isZoomGesturesEnabled = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun defreezeMap() {
        activity.layout_show_subscribed.isEnabled = true
        activity.layout_show_subscribed.isClickable = true
        activity.layout_hide_closed.isEnabled = true
        activity.layout_hide_closed.isClickable = true
        mMap.apply {
            if (EasyPermissions.hasPermissions(
                    activity,
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                )
            ) {
                isMyLocationEnabled = true
            }
            uiSettings.apply {
                isMyLocationButtonEnabled = false
                isScrollGesturesEnabled = true
                isZoomGesturesEnabled = true
            }
        }
    }

    fun startLocationSearch(callback: ((location: LatLng) -> Unit)? = null) {
        Log.v("xxx", "SmartLocation.with()")
        val isGpsEnabled = SmartLocation.with(activity).location().state().isGpsAvailable
        if (isGpsEnabled) {
            launchSmartLocator(callback)
        } else {
            DialogProvider.showGpsRequiredDialog(activity)
        }
    }

    fun onShowOpenedClick() = state.toogleOpened()

    fun onShowSubscribedClick() = state.toogleSubscribed()

    private fun tryToFetchNewShops() {
        val mapLocation = mMap.cameraPosition.target
        if (state.closestLocationDistance(mapLocation) > 500) {
            mapJobs.forEach { it.cancel() }
            mapJobs.add(CoroutineScope(Main).launch {
                val shops = RestClient.getShops(mapLocation.latitude, mapLocation.longitude)
                state.addNewFetchedLocation(mapLocation, shops)
            })
        }
    }

    private suspend fun addShopsOnTheMap(shops: List<Shop>, subscriptions: List<Subscription>) {
        val subscribedShops = state.shopsAll().filterSubscribed(subscriptions)
        activity.layout_footer_view.visibility = VISIBLE
        for (shop in shops) {
            val position = shop.shopData.getLocation()
            val iconBitmap = MapMarkerProvider(activity).buildMarkerViewAsync(
                shop,
                subscribedShops.contains(shop)
            )
            val icon = BitmapDescriptorFactory.fromBitmap(iconBitmap)
            val markerOptions = MarkerOptions().position(position).icon(icon)
            mMap.addMarker(markerOptions).apply { tag = shop.shopData.marketId }
            activity.layout_footer_view.removeAllViews()
        }
        activity.layout_footer_view.visibility = GONE
    }

    private fun launchSmartLocator(callback: ((location: LatLng) -> Unit)? = null) {
        Toasty.info(activity, activity.getString(R.string.looking_for_location), LENGTH_LONG, true)
            .show()
        SmartLocation.with(activity).location().apply { config(LocationParams.NAVIGATION) }.oneFix()
            .start {
                val userLocation = LatLng(it.latitude, it.longitude)
                Log.v("xxx", "Location: $userLocation")
                callback?.invoke(userLocation)
            }
    }

    fun focusMap(location: LatLng, callback: (() -> Unit)? = null) {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(location, 15f),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    mMap.setOnCameraIdleListener(this@MapHelper)
                    defreezeMap()
                    Toasty.success(
                        activity,
                        activity.getString(R.string.location_found),
                        LENGTH_LONG,
                        true
                    ).show()
                    if (callback != null) Timer().schedule(5000) {
                        CoroutineScope(Main).launch {
                            callback()
                        }
                    }
                }

                override fun onCancel() {}
            })
    }

    override fun onCameraIdle() = tryToFetchNewShops()
    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let { it ->
            val shopId = it.tag.toString()
            val shop = state.shopsAll().firstOrNull { item -> item.shopData.marketId == shopId }
            shop?.let {
                val name = shop.shopData.name
                val address = "${shop.shopData.address}, ${shop.shopData.city}"
                CoroutineScope(IO).launch {
                    val subscription = repo.getSubscriptionSync(shop.shopData.marketId)
                    CoroutineScope(Main).launch {
                        DialogProvider.showMarkerDetails(
                            activity,
                            shop.shopData.getImgResId(),
                            name,
                            address,
                            shop.shopData.getOpeningHours().let {
                                if (it.size == 2) {
                                    "${it[0]} - ${it[1]}"
                                } else {
                                    "${it[0]} - ${it[1]}, ${it[2]} - ${it[3]}"
                                }
                            },
                            shop.shopShopState?.queueSizePeople ?: 0,
                            shop.shopShopState?.queueWaitMinutes ?: 0,
                            shop.shopShopState?.getLastUpdate(),
                            shop.shopData.isOpen,
                            subscription != null
                        ) {
                            CoroutineScope(Main).launch {
                                if (subscription == null) {
                                    repo.saveSubscription(
                                        shop.shopData.marketId,
                                        name,
                                        address,
                                        shop.shopData.brand,
                                        state.getShopClusterLocation(shop).latitude,
                                        state.getShopClusterLocation(shop).longitude
                                    )
                                    DialogProvider.showSubscribedDialog(
                                        activity,
                                        shop.shopData.getImgResId()
                                    )
                                    activity.fa.logEvent(
                                        GENERATE_LEAD,
                                        Bundle().apply {
                                            this.putString(ITEM_NAME, shop.shopData.brand)
                                        })
                                } else {
                                    repo.deleteSubscription(subscription.shopId)
                                    DialogProvider.showUnsubscribedDialog(
                                        activity,
                                        name,
                                        shop.shopData.getImgResId()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return true
    }
}