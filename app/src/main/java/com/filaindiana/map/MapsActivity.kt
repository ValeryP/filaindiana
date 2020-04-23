package com.filaindiana.map

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.filaindiana.DialogProvider
import com.filaindiana.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import io.nlopez.smartlocation.SmartLocation
import kotlinx.android.synthetic.main.activity_maps.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE
import pub.devrel.easypermissions.EasyPermissions


const val RC_PERMISSIONS_LOCATION = 1

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private lateinit var mapHelper: MapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        (supportFragmentManager.findFragmentById(R.id.layout_map) as SupportMapFragment)
            .getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mapHelper = MapHelper(this, googleMap)
        askLocationPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (::mapHelper.isInitialized) askLocationPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        SmartLocation.with(this).location().stop()
    }

    @AfterPermissionGranted(RC_PERMISSIONS_LOCATION)
    private fun askLocationPermissions() {
        val perms = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            requestLocationSearch()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "The app requires location permission",
                RC_PERMISSIONS_LOCATION,
                *perms
            )
        }
    }

    override fun onRequestPermissionsResult(rc: Int, perms: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(rc, perms, results)
        EasyPermissions.onRequestPermissionsResult(rc, perms, results, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            DialogProvider.showPermissionRequiredDialog(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEFAULT_SETTINGS_REQ_CODE) {
            requestLocationSearch()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        requestLocationSearch()
    }

    override fun onRationaleDenied(requestCode: Int) {
        DialogProvider.showPermissionRequiredDialog(this)
    }

    override fun onRationaleAccepted(requestCode: Int) {
        requestLocationSearch()
    }

    private fun requestLocationSearch() {
        mapHelper.startLocationSearch {
            layout_hide_closed.visibility = VISIBLE
            layout_hide_closed.setOnTouchListener { v, event ->
                v.onTouchEvent(event)
                true
            }
            layout_hide_closed.setOnCheckedChangeListener { _, isOnlyOpened ->
                layout_hide_closed.isEnabled = false
                layout_hide_closed.isClickable = false
                Log.v("xxx", "1")
                mapHelper.invalidateMarkers(isOnlyOpened) {
                    Log.v("xxx", "2")
                    layout_hide_closed.isEnabled = true
                    layout_hide_closed.isClickable = true
                }
            }
        }
    }
}
