package com.monsieur.cloy.speedometerwearos

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PermissionInfoCompat
import com.google.android.gms.location.*
import com.monsieur.cloy.speedometerwearos.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.Permission

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 111
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val UPDATE_INTERVAL_IN_MILLISECONDS = 500L
    val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onStart() {
        super.onStart()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_FINE_LOCATION)
        }
        else{
            startLocationTracking()
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful || task.result == null) {
                        Log.w(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.isWaitForAccurateLocation = true
        locationRequest.smallestDisplacement = 1f
        return locationRequest
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var allowed = false
        val currentPer = permissions[0]
        allowed = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if(allowed){
            startLocationTracking()
            return
        }
    }

    private fun startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(createLocationRequest(), MyLocationCallback(), mainLooper)
            getLastLocation()
        }

    }

    private inner class MyLocationCallback(): LocationCallback(){
        @SuppressLint("SetTextI18n")
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            var speed = locationResult.lastLocation.speed*3.6
            binding.speed.text = round(speed, 1).toString()
        }
    }

    fun round(value: Double, places: Int): Double {
        if (java.lang.Double.isNaN(value))
            return 0.0
        require(places >= 0)
        if (value == 0.0 || value.isInfinite())
            return 0.0
        var bd = BigDecimal(value.toString())
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }
}