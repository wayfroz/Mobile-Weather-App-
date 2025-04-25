package com.example.uniqueweatherapp.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.uniqueweatherapp.LocationWeatherService

fun checkPermissionsAndStartService(activity: Activity) {
    val permissions = mutableListOf<String>()

    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.FOREGROUND_SERVICE)
        != PackageManager.PERMISSION_GRANTED) {
        permissions.add(Manifest.permission.FOREGROUND_SERVICE)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        ContextCompat.checkSelfPermission(activity, Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
        permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    if (permissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), 1001)
    } else {
        startLocationService(activity)
    }
}

fun startLocationService(activity: Activity) {
    val intent = Intent(activity, LocationWeatherService::class.java)
    ContextCompat.startForegroundService(activity, intent)
}

fun handlePermissionResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    activity: Activity
) {
    if (requestCode == 1001 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
        startLocationService(activity)
    } else {
        Toast.makeText(activity, "Location and foreground permissions are required.", Toast.LENGTH_SHORT).show()
    }
}
