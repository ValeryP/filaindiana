package com.filaindiana.map

import android.util.Log
import android.widget.Toast
import com.filaindiana.DialogProvider
import com.filaindiana.network.RestClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
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
class MapHelper(private val activity: MapsActivity, val mMap: GoogleMap) {
    private val mapJobs = mutableListOf<Job>()
    private val fetchedLocations = mutableListOf<LatLng>()

    init {
        mMap.apply {
            uiSettings.apply {
                isRotateGesturesEnabled = false
                isScrollGesturesEnabled = false
                isTiltGesturesEnabled = false
                isZoomGesturesEnabled = false
            }
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

    fun fetchNewShops() {
        val mapLocation = mMap.cameraPosition.target
        val shouldFetchLocation = fetchedLocations.isEmpty() || !fetchedLocations.any {
            SphericalUtil.computeDistanceBetween(mapLocation, it) < 1000
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
            for (shop in shops) {
                activity.layout_footer_text.text = shop.supermarket.name

                val position = shop.supermarket.getLocation()
                val iconBitmap = MapMarkerProvider(activity)
                    .buildMarkerViewAsync(shop)
                val icon = BitmapDescriptorFactory.fromBitmap(iconBitmap)
                val marker = MarkerOptions().position(position).icon(icon)
                mMap.addMarker(marker)

                activity.layout_footer_view.removeAllViews()
            }
            fetchedLocations.add(location)
        })
    }

    private fun launchSmartLocator() {
        Toasty.info(activity, "Looking for your location...", Toast.LENGTH_LONG, true).show()
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
                    mMap.setOnCameraIdleListener(activity)
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
                        Toast.LENGTH_LONG,
                        true
                    ).show()
                }

                override fun onCancel() {}
            })
    }
}