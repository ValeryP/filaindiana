package com.filaindiana.map

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.filaindiana.R
import com.filaindiana.utils.*
import com.filaindiana.utils.NotificationBuilder.KEY_SUBSCRIPTON_LOCATION
import com.filaindiana.worker.NotificationWorker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import io.nlopez.smartlocation.SmartLocation
import kotlinx.android.synthetic.main.activity_maps.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE
import pub.devrel.easypermissions.EasyPermissions


const val RC_PERMISSIONS_LOCATION = 1

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private lateinit var mapHelper: MapHelper
    private var subscriptionLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscriptionLocation = intent?.extras?.getParcelable(KEY_SUBSCRIPTON_LOCATION)
        logDebug { "onCreate: $subscriptionLocation" }
        setContentView(R.layout.activity_maps)
        NotificationWorker.enqueue(this)
        (supportFragmentManager.findFragmentById(R.id.layout_map) as SupportMapFragment)
            .getMapAsync(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        subscriptionLocation = intent?.extras?.getParcelable(KEY_SUBSCRIPTON_LOCATION)
        logDebug { "onNewIntent: $subscriptionLocation" }
        askLocationPermissions()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapHelper = MapHelper(this, googleMap)
        askLocationPermissions()
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
            Firebase.analytics(this).logRequestPermissions()
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.app_requires_location_permission),
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
            Firebase.analytics(this).logPermissionsDeniedPermanently()
            DialogProvider.showPermissionRequiredDialog(this)
        } else {
            Firebase.analytics(this).logPermissionsDenied()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEFAULT_SETTINGS_REQ_CODE) {
            requestLocationSearch()
            Firebase.analytics(this).logPermissionsAccepted()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        requestLocationSearch()
        Firebase.analytics(this).logPermissionsAccepted()
    }

    override fun onRationaleAccepted(requestCode: Int) {
        requestLocationSearch()
        Firebase.analytics(this).logPermissionsRationaleAccepted()
    }

    override fun onRationaleDenied(requestCode: Int) {
        Firebase.analytics(this).logPermissionsDenied()
        DialogProvider.showPermissionRequiredDialog(this)
    }

    private fun requestLocationSearch() {
        Firebase.analytics(this).logLocationSearchStarted()
        mapHelper.startLocationSearch { userLocation ->
            val zoomLocation = subscriptionLocation ?: userLocation
            mapHelper.focusMap(zoomLocation) {
                subscriptionLocation = null
                setupClosedShopsSwitch()
                setupSubscribedButton()
                OnboardingManager.startOnlyClosedOnboarding(this@MapsActivity)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSubscribedButton() {
        layout_show_subscribed.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            true
        }
        layout_show_subscribed.setOnClickListener { mapHelper.onShowSubscribedClick() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClosedShopsSwitch() {
        layout_hide_closed.show()
        layout_hide_closed.isChecked = PrefsUtils.isOpenedFilter()
        layout_hide_closed.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            true
        }
        layout_hide_closed.setOnCheckedChangeListener { _, _ -> mapHelper.onShowOpenedClick() }
    }
}
