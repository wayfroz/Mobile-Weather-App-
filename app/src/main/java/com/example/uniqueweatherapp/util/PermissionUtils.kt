package com.example.uniqueweatherapp.util

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.uniqueweatherapp.LocationWeatherService
import com.example.uniqueweatherapp.R

private const val LOCATION_NOTIFICATION_REQUEST_CODE = 1001
private const val WEATHER_NOTIFICATION_ID = 1002

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
        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), LOCATION_NOTIFICATION_REQUEST_CODE)
    } else {
        startLocationService(activity)
        showNotification(activity)
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
    if (requestCode == LOCATION_NOTIFICATION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
        startLocationService(activity)
        showNotification(activity)
    } else {
        Toast.makeText(activity, "Location and notification permissions are required.", Toast.LENGTH_SHORT).show()
    }
}

private fun showNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    val channelId = "weather_notification_channel"
    val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Weather Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager?.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.sun)
        .setContentTitle("Weather App")
        .setContentText("Location-based weather update activated!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    NotificationManagerCompat.from(context).notify(WEATHER_NOTIFICATION_ID, notification)
}
