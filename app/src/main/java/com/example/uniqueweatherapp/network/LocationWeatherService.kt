package com.example.uniqueweatherapp

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class LocationWeatherService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val WEATHER_UPDATE_ACTION = "com.example.uniqueweatherapp.WEATHER_UPDATE"
        private const val TAG = "LocationWeatherService"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        showNotification("Loading", "--°", "Fetching...")

        fetchLocationAndNotify()
    }

    private fun fetchLocationAndNotify() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                serviceScope.launch {
                    val condition = "Clear"
                    val temperature = "75°F"
                    var locationName = "Unknown"

                    try {
                        val geocoder = Geocoder(this@LocationWeatherService, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        locationName = addresses?.firstOrNull()?.postalCode ?: "10001"
                    } catch (e: Exception) {
                        Log.e(TAG, "Geocoder failed: ${e.message}", e)
                        locationName = "10001" // fallback zip code
                    }

                    launch(Dispatchers.Main) {
                        showNotification(condition, temperature, locationName)
                        sendWeatherBroadcast(temperature, condition, locationName)
                    }
                }
            } ?: stopSelf()
        }
    }

    private fun sendWeatherBroadcast(temp: String, condition: String, location: String) {
        val broadcastIntent = Intent(WEATHER_UPDATE_ACTION).apply {
            putExtra("temp", temp)
            putExtra("condition", condition)
            putExtra("location", location)
        }
        sendBroadcast(broadcastIntent)
    }

    private fun showNotification(condition: String, temp: String, location: String) {
        val channelId = "weather_channel"
        val channelName = "Weather Updates"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(condition)
            .setContentText("$temp in $location")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}