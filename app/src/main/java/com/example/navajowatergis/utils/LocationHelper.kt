package com.example.navajowatergis.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices

class LocationHelper(private val context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(onResult: (Location?) -> Unit) {
        client.lastLocation.addOnSuccessListener { loc ->
            onResult(loc)
        }.addOnFailureListener {
            onResult(null)
        }
    }
}
