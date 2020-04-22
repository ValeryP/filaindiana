package com.filaindiana.map

import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast.LENGTH_LONG
import com.filaindiana.DialogProvider
import com.filaindiana.network.RestClient
import com.filaindiana.network.ShopsResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import es.dmoral.toasty.Toasty
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationParams
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 22.04.2020
 */
class MapHelper(private val activity: MapsActivity, val mMap: GoogleMap) :
    GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {
    private val mapJobs = mutableListOf<Job>()
    private val fetchedShops = mutableMapOf<LatLng, ShopsResponse>()

    init {
        mMap.apply {
            uiSettings.apply {
                isRotateGesturesEnabled = false
                isScrollGesturesEnabled = false
                isTiltGesturesEnabled = false
                isZoomGesturesEnabled = false
            }
            setOnMarkerClickListener(this@MapHelper)
        }
    }

    fun startLocationSearch() {
        Log.v("xxx", "SmartLocation.with()")
        val isGpsEnabled = SmartLocation.with(activity).location().state().isGpsAvailable
        if (isGpsEnabled) {
            launchSmartLocator()
        } else {
            DialogProvider.showGpsRequiredDialog(activity)
        }
    }

    private fun fetchNewShops() {
        val mapLocation = mMap.cameraPosition.target
        val shouldFetchLocation = fetchedShops.isEmpty() || !fetchedShops.keys.any {
            SphericalUtil.computeDistanceBetween(mapLocation, it) < 500
        }
        if (shouldFetchLocation) {
            Log.v("xxx", "distanceDiff: $shouldFetchLocation")
            showShopsMarkers(mapLocation)
        }
    }

    private fun showShopsMarkers(location: LatLng) {
        mapJobs.forEach { it.cancel() }
        mapJobs.add(CoroutineScope(Dispatchers.Main).launch {
            val shops = RestClient.build().getShopsLocations(location.latitude, location.longitude)
            Log.v("xxx", "showShopsMarkers: $location - ${shops.count()}")
            activity.layout_footer_root.visibility = VISIBLE
            for (shop in shops) {
                activity.layout_footer_text.text = shop.supermarket.name

                val position = shop.supermarket.getLocation()
                val iconBitmap = MapMarkerProvider(activity).buildMarkerViewAsync(shop)
                val icon = BitmapDescriptorFactory.fromBitmap(iconBitmap)
                val markerOptions = MarkerOptions().position(position).icon(icon)
                mMap.addMarker(markerOptions).apply { tag = shop.supermarket.marketId }

                activity.layout_footer_view.removeAllViews()
            }
            activity.layout_footer_root.visibility = GONE
            fetchedShops[location] = shops
        })
    }

    private fun launchSmartLocator() {
        Toasty.info(activity, "Looking for your location...", LENGTH_LONG, true).show()
        SmartLocation.with(activity).location().apply { config(LocationParams.NAVIGATION) }.oneFix()
            .start {
                val userLocation = LatLng(it.latitude, it.longitude)
                Log.v("xxx", "Location: $userLocation")
                focusMapUserLocation(userLocation)
            }
    }

    private fun focusMapUserLocation(location: LatLng) {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(location, 15f),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    mMap.setOnCameraIdleListener(this@MapHelper)
                    mMap.apply {
                        isMyLocationEnabled = true
                        uiSettings.apply {
                            isMyLocationButtonEnabled = true
                            isScrollGesturesEnabled = true
                            isZoomGesturesEnabled = true
                        }
                    }
                    Toasty.success(
                        activity,
                        "Location successfully found!",
                        LENGTH_LONG,
                        true
                    ).show()
                }

                override fun onCancel() {}
            })
    }

    override fun onCameraIdle() = fetchNewShops()
    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let { it ->
            val shopId = it.tag
            val shop = fetchedShops.values.flatten()
                .firstOrNull { item -> item.supermarket.marketId == shopId }
            shop?.let {
                val iconRes = shop.supermarket.getImgResId()
                val name = shop.supermarket.name
                val address = "${shop.supermarket.address}, ${shop.supermarket.city}"
                val openHours =
                    shop.supermarket.getOpeningHours().let { "${it.first} - ${it.second}" }
                val queueSizePeople = shop.state?.queueSizePeople ?: 0
                val queueWaitMinutes = shop.state?.queueWaitMinutes ?: 0
                val lastUpdateTime = shop.state?.getLastUpdate()
                DialogProvider.showMarkerDetails(
                    activity,
                    iconRes,
                    name,
                    address,
                    openHours,
                    queueSizePeople,
                    queueWaitMinutes,
                    lastUpdateTime
                ) {
                    Log.v("xxx", "Click: $shopId")
                }
            }
        }
        return false
    }
}