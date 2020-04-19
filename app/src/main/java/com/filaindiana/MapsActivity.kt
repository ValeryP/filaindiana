package com.filaindiana

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import es.dmoral.toasty.Toasty
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationParams.NAVIGATION
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


const val RC_PERMISSIONS_LOCATION = 1

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap.apply {
            uiSettings.apply {
                isRotateGesturesEnabled = false
                isScrollGesturesEnabled = false
                isTiltGesturesEnabled = false
                isZoomGesturesEnabled = false
            }
        }
        askLocationPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized) {
            askLocationPermissions()
        }
    }

    private fun startLocationSearch() {
        Log.e("xxx", "SmartLocation.with()")
        val isGpsEnabled = SmartLocation.with(this).location().state().isGpsAvailable
        if (isGpsEnabled) {
            launchSmartLocator()
        } else {
            showGpsRequiredDialog(this)
        }
    }

    private fun launchSmartLocator() {
        Toasty.info(this, "Looking for your location...", LENGTH_LONG, true).show()
        SmartLocation.with(this).location().apply { config(NAVIGATION) }.oneFix().start {
            val location = LatLng(it.latitude, it.longitude)
            Log.e("xxx", "Location: $location")
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(location, 16f),
                object : CancelableCallback {
                    override fun onFinish() {
                        mMap.apply {
                            isMyLocationEnabled = true
                            uiSettings.apply {
                                isMyLocationButtonEnabled = true
                                isScrollGesturesEnabled = true
                                isZoomGesturesEnabled = true
                            }
                        }
                        Toasty.success(
                            this@MapsActivity,
                            "Location successfully found!",
                            LENGTH_LONG,
                            true
                        ).show()
                    }

                    override fun onCancel() {}
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SmartLocation.with(this).location().stop()
    }

    @AfterPermissionGranted(RC_PERMISSIONS_LOCATION)
    private fun askLocationPermissions() {
        val perms = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            startLocationSearch()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "The app requires location permission",
                RC_PERMISSIONS_LOCATION,
                *perms
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults, this
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            showPermissionRequiredDialog(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            startLocationSearch()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        startLocationSearch()
    }

    override fun onRationaleDenied(requestCode: Int) {
        showPermissionRequiredDialog(this)
    }

    override fun onRationaleAccepted(requestCode: Int) {
        startLocationSearch()
    }

    private fun showGpsRequiredDialog(ctx: Context) {
        MaterialDialog(ctx).show {
            title(text = "Permission required")
            message(text = "Enable GPS to find closest supermarkets")
            cancelable(false)
            positiveButton(text = "Enable") {
                (ctx as Activity).startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    1
                )
            }
            negativeButton(text = "Exit") {
                (ctx as Activity).finishAndRemoveTask()
            }
        }
    }

    private fun showPermissionRequiredDialog(ctx: Context) {
        MaterialDialog(ctx).show {
            title(text = "Permission required")
            message(
                text = "The app requires location permission to find closest shops. " +
                        "Click \"Enable\" to open an application settings screen and grant permissions."
            )
            cancelable(false)
            positiveButton(text = "Enable") {
                (ctx as Activity).startActivityForResult(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", ctx.packageName, null)
                }, 1)
            }
            negativeButton(text = "Exit") {
                (ctx as Activity).finishAndRemoveTask()
            }
        }
    }
}
