package com.example.navajowatergis.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.navajowatergis.R
import com.example.navajowatergis.data.entities.WellWithAnalytes
import com.example.navajowatergis.utils.LocationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var gMap: GoogleMap
    private val viewModel: WellViewModel by viewModels()
    private val locationHelper by lazy { LocationHelper(this) }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableMyLocation()
                centerOnUserLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        gMap.setOnMarkerClickListener(this)

        viewModel.wellsLive.observe(this) { wells ->
            gMap.clear()
            if (wells.isNotEmpty()) {
                wells.forEach { addMarker(it) }
                val first = wells.first().well
                val pos = LatLng(first.latitude, first.longitude)
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 8f))
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
            centerOnUserLocation()
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun addMarker(wellWithAnalytes: WellWithAnalytes) {
        val w = wellWithAnalytes.well
        val analyteSummary = if (wellWithAnalytes.analytes.isEmpty()) "No analytes" else wellWithAnalytes.analytes.joinToString(", ") { it.analyteName }
        val pos = LatLng(w.latitude, w.longitude)
        val marker = gMap.addMarker(MarkerOptions().position(pos).title(w.name).snippet(analyteSummary))
        // tag the marker with the well id so we can retrieve it on click
        marker?.tag = w.id
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val tag = marker.tag
        if (tag is Long) {
            val i = Intent(this, WellDetailActivity::class.java)
            i.putExtra(WellDetailActivity.EXTRA_WELL_ID, tag)
            startActivity(i)
            return true
        }
        return false
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                gMap.isMyLocationEnabled = true
            } catch (_: Exception) {}
        }
    }

    private fun centerOnUserLocation() {
        locationHelper.getLastKnownLocation { loc: Location? ->
            loc?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
            }
        }
    }
}
