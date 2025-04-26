package com.example.uniqueweatherapp

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniqueweatherapp.ui.theme.UniqueWeatherAppTheme
import com.example.uniqueweatherapp.util.checkPermissionsAndStartService
import com.example.uniqueweatherapp.util.handlePermissionResult
import com.example.uniqueweatherapp.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest

class MainActivity : ComponentActivity() {

    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationWeatherService.WEATHER_UPDATE_ACTION) {
                    val location = intent.getStringExtra("location") ?: return
                    weatherViewModel.fetchWeather(location, "a1160aed969479de39cbe27819c9de63")
                }
            }
        }

        val filter = IntentFilter(LocationWeatherService.WEATHER_UPDATE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(receiver, filter)
        }


        checkPermissionsAndStartService(this)

        enableEdgeToEdge()
        setContent {
            UniqueWeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(
                        modifier = Modifier.padding(innerPadding),
                        weatherViewModel = weatherViewModel,
                        onRequestPermissionsAndNotify = {
                            checkPermissionsAndStartService(this@MainActivity)
                            requestNotificationPermissionAndShow(this@MainActivity)
                        }
                    )
                }
            }
        }

    }

    private fun requestNotificationPermissionAndShow(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            } else {
                showNotification(context)
            }
        } else {
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
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
            .setSmallIcon(R.drawable.sun) // Your app icon
            .setContentTitle("Weather App")
            .setContentText("Location-based weather update activated!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            NotificationManagerCompat.from(context).notify(1002, notification)
        }
    }




    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handlePermissionResult(requestCode, permissions, grantResults, this)
    }
}

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    weatherViewModel: WeatherViewModel,
    onRequestPermissionsAndNotify: () -> Unit
) {
    val weather by weatherViewModel.weather.observeAsState()
    val errorMessage by weatherViewModel.errorMessage.observeAsState()
    val context = LocalContext.current
    var zipCode by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
            weatherViewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row (
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row (
            modifier = modifier.align(Alignment.CenterHorizontally)
        ) {
            IconButton(
                onClick = {
                    checkPermissionsAndStartService(context as Activity)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.my_location),
                    modifier = Modifier.size(52.dp)
                )
            }
            OutlinedTextField(
                value = zipCode,
                onValueChange = {
                    if (it.length <= 5 && it.all(Char::isDigit)) zipCode = it
                },
                label = {
                    Text(stringResource(R.string.enter_zip_code))},
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row (
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ){
            Button(
                onClick = {
                    if (zipCode.length == 5) {
                        weatherViewModel.fetchWeather(zipCode, "a1160aed969479de39cbe27819c9de63")
                    } else {
                        Toast.makeText(context, context.getString(R.string.invalid_zip), Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Text(text = stringResource(R.string.get_weather))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    if (zipCode.length == 5) {
                        val intent = Intent(context, com.example.uniqueweatherapp.ui.ForecastActivity::class.java)
                        intent.putExtra("ZIP_CODE", zipCode)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, context.getString(R.string.invalid_zip), Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Text(text = stringResource(R.string.forecast_label))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        weather?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = it.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.temp_format, it.main.temp),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(24.dp))
                Image(
                    painter = painterResource(id = R.drawable.sun),
                    contentDescription = stringResource(R.string.sunny_icon),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.feels_like_format, it.main.feelsLike),
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(text = stringResource(R.string.humidity_format, it.main.humidity),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(text = stringResource(R.string.pressure_format, it.main.pressure),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
            )
        }
    }
}
