package com.filaindiana.map

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.filaindiana.R
import com.filaindiana.data.KEY_SUBSCRIPTON_LOCATION
import com.filaindiana.favourites.FavouritesActivity
import com.filaindiana.utils.*
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
const val RC_FAVORITES = 2

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private lateinit var mapHelper: MapHelper
    private var subscriptionLocation: LatLng? = null
    private var isSubscriptionsAvailable: Boolean = false

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

    fun invalidateMenu(shouldShowMenu: Boolean) {
        isSubscriptionsAvailable = shouldShowMenu
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isSubscriptionsAvailable) menuInflater.inflate(R.menu.menu_map_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_favorites -> {
                val intent = Intent(this, FavouritesActivity::class.java)
                startActivityForResult(intent, RC_FAVORITES)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        when (requestCode) {
            DEFAULT_SETTINGS_REQ_CODE -> {
                requestLocationSearch()
                Firebase.analytics(this).logPermissionsAccepted()
            }
            RC_FAVORITES -> {
                subscriptionLocation = data?.extras?.getParcelable(KEY_SUBSCRIPTON_LOCATION)
                if (subscriptionLocation != null) {
                    logDebug { "onActivityResult: $subscriptionLocation" }
                    askLocationPermissions()
                }
            }
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
                layout_filters_container.show()
                setupOpenNowSwitch()
                setupFavoritesButton()
                OnboardingManager.startOnlyClosedOnboarding(this@MapsActivity)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFavoritesButton() {
        layout_favorites.isChecked = PrefsUtils.isFavoritesFilter()
        layout_favorites.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            true
        }
        layout_favorites.setOnCheckedChangeListener { _, _ -> mapHelper.onShowSubscribedClick() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOpenNowSwitch() {
        layout_open_now.show()
        layout_open_now.isChecked = PrefsUtils.isOpenNowFilter()
        layout_open_now.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            true
        }
        layout_open_now.setOnCheckedChangeListener { _, _ -> mapHelper.onShowOpenedClick() }
    }
}
