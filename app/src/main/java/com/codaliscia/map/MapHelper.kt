package com.codaliscia.map

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.widget.Toast.LENGTH_LONG
import androidx.lifecycle.Observer
import com.codaliscia.R
import com.codaliscia.data.AppDB
import com.codaliscia.data.Subscription
import com.codaliscia.data.SubscriptionRepository
import com.codaliscia.network.RestClient
import com.codaliscia.network.ShopsResponse.Shop
import com.codaliscia.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
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
        repo.getSubscriptions().observe(activity, Observer {
            activity.invalidateMenu(it.isNotEmpty())
            state.setSubscriptions(it)
        })
        state.shopsFiltered.observe(activity, Observer { shops ->
            CoroutineScope(Main).launch {
                invalidateMap(shops)
            }
        })
        state.filters.observe(activity, Observer { invalidateViews(it) })
    }

    private fun invalidateViews(filters: ShopFilters) {
        if (filters.isSubscribed && !PrefsUtils.isOnboardingShownSubsctiptionFilter()) {
            Toasty.info(activity, activity.getString(R.string.show_favorite_supermarkets)).show()
            PrefsUtils.setOnboardingShownSubsctiptionFilter()
        }
        if (filters.isOnlyOpened && !PrefsUtils.isOnboardingShownOpenedFilter()) {
            Toasty.info(activity, activity.getString(R.string.show_open_supermarkets)).show()
            PrefsUtils.setOnboardingShownOpenedFilter()
        }
    }

    private suspend fun invalidateMap(pointsToShow: List<Shop>) {
        val pointsAll = state.shopsAll()
        if (pointsAll != null) {
            val pointsOpen = pointsAll.filterOpen()
            val subscriptions = repo.getSubscriptionsSync()
            val pointsSubscribed = pointsToShow.filterSubscribed(subscriptions)
            val isShowOpenActive = PrefsUtils.isOpenNowFilter()
            val isShowSubscribtionsActive = PrefsUtils.isFavoritesFilter()
            if (pointsToShow.isEmpty() && pointsAll.isEmpty()) {
                Toasty.info(activity, activity.getString(R.string.no_shops)).show()
            } else if (pointsToShow.isEmpty() && pointsAll.isNotEmpty()) {
                Timer().schedule(1000) {
                    CoroutineScope(Main).launch {
                        if (isShowSubscribtionsActive) {
                            state.toogleSubscribed()
                        } else if (isShowOpenActive) {
                            state.toogleOpened()
                        }
                    }
                }
            } else {
                freezeMap()
                mMap.clear()
                addShopsOnTheMap(pointsToShow, subscriptions)
                if (pointsSubscribed.isNotEmpty()) {
                    activity.layout_favorites.show()
                    OnboardingManager.startFavouritesOnboarding(activity)
                } else {
                    activity.layout_favorites.hide()
                }
                val isOpenVisible = pointsOpen.isNotEmpty()
                if (isOpenVisible) {
                    activity.layout_open_now.show()
                } else {
                    activity.layout_open_now.hide()
                }
                defreezeMap()
            }
        }
    }

    private fun freezeMap() {
        activity.layout_favorites.isEnabled = false
        activity.layout_open_now.isEnabled = false
        activity.layout_favorites.isClickable = false
        activity.layout_open_now.isClickable = false
        mMap.uiSettings.apply {
            isRotateGesturesEnabled = false
            isScrollGesturesEnabled = false
            isTiltGesturesEnabled = false
            isZoomGesturesEnabled = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun defreezeMap() {
        activity.layout_favorites.isEnabled = true
        activity.layout_open_now.isEnabled = true
        activity.layout_favorites.isClickable = true
        activity.layout_open_now.isClickable = true
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
        logInfo { "SmartLocation.with()" }
        val isGpsEnabled = SmartLocation.with(activity).location().state().isGpsAvailable
        if (isGpsEnabled) {
            launchSmartLocator(callback)
        } else {
            DialogProvider.showGpsRequiredDialog(activity)
        }
    }

    fun onShowOpenedClick() {
        Firebase.analytics(activity).logClickShowOpenedOnly(!PrefsUtils.isOpenNowFilter())
        state.toogleOpened()
    }

    fun onShowSubscribedClick() {
        Firebase.analytics(activity).logClickShowSubscribedOnly(!PrefsUtils.isFavoritesFilter())
        state.toogleSubscribed()
    }

    private fun tryToFetchNewShops() {
        val mapLocation = mMap.cameraPosition.target
        Firebase.analytics(activity).logMapMoved(mapLocation)
        if (state.closestLocationDistance(mapLocation) > 500) {
            Firebase.analytics(activity).logFetchingNewPoints(mapLocation)
            mapJobs.forEach { it.cancel() }
            mapJobs.add(CoroutineScope(Main).launch {
                val shops = RestClient.getShops(mapLocation.latitude, mapLocation.longitude)
                state.addNewFetchedLocation(mapLocation, shops)
            })
        }
    }

    private suspend fun addShopsOnTheMap(shops: List<Shop>, subscriptions: List<Subscription>) {
        val subscribedShops = state.shopsAll()?.filterSubscribed(subscriptions)?.toList()
        activity.layout_footer_view.show()
        try {
            for (shop in shops) {
                try {
                    val position = shop.shopData.getLocation()
                    val markerProvider = MapMarkerProvider(activity)
                    val isSubscribed = subscribedShops?.contains(shop) ?: false
                    val bitmap = markerProvider.buildMarkerViewAsync(shop, isSubscribed)
                    val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
                    val markerOptions = MarkerOptions().position(position).icon(icon)
                    mMap.addMarker(markerOptions).apply { tag = shop.shopData.marketId }
                } catch (e: Exception) {
                    logException(e, "Can't add marker, skip")
                }
                activity.layout_footer_view.removeAllViews()
            }
        } catch (e: ConcurrentModificationException) {
            logException(e, "Concurrent list modification")
        }
        activity.layout_footer_view.hide()
    }

    private fun launchSmartLocator(callback: ((location: LatLng) -> Unit)? = null) {
        Toasty.info(activity, activity.getString(R.string.looking_for_location), LENGTH_LONG, true)
            .show()
        SmartLocation.with(activity).location().apply { config(LocationParams.NAVIGATION) }.oneFix()
            .start {
                val userLocation = LatLng(it.latitude, it.longitude)
                logInfo { "Location: $userLocation" }
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
            val shop = state.shopsAll()?.firstOrNull { item -> item.shopData.marketId == shopId }
            shop?.let {
                val name = shop.shopData.name
                val address = "${shop.shopData.address}, ${shop.shopData.city}"
                CoroutineScope(IO).launch {
                    val subscription = repo.getSubscriptionSync(shop.shopData.marketId)
                    CoroutineScope(Main).launch {
                        DialogProvider.showShopDetails(activity, shop, subscription != null) {
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
                                    Firebase.analytics(activity).logSavedSubscription(shop)
                                    Toasty.success(
                                        activity,
                                        activity.getString(R.string.subscribed_details),
                                        LENGTH_LONG,
                                        true
                                    ).show()
                                    Firebase.analytics(activity).logShowYouAreSubcribedDialog(shop)
                                } else {
                                    repo.deleteSubscription(subscription.shopId)
                                    Toasty.success(
                                        activity,
                                        activity.getString(R.string.no_updates, name),
                                        LENGTH_LONG,
                                        true
                                    ).show()
                                    Firebase.analytics(activity)
                                        .logShowYouAreUnsubcribedDialog(shop)
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