package com.example.uniqueweatherapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniqueweatherapp.ui.theme.UniqueWeatherAppTheme
import com.example.uniqueweatherapp.viewmodel.WeatherViewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniqueWeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(modifier: Modifier = Modifier) {
    val viewModel: WeatherViewModel = viewModel()
    val weather by viewModel.weather.observeAsState()
    val context = LocalContext.current
    var zipCode by remember { mutableStateOf("") }
    val errorMessage by viewModel.errorMessage.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(12.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = zipCode,
            onValueChange = {
                if (it.length <= 5 && it.all(Char::isDigit)) zipCode = it
            },
            label = { Text(stringResource(R.string.enter_zip_code)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (zipCode.length == 5) {
                    viewModel.fetchWeather(zipCode, "a1160aed969479de39cbe27819c9de63")
                } else {
                    Toast.makeText(context, context.getString(R.string.invalid_zip), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.get_weather))
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.forecast_label))
        }

        Spacer(modifier = Modifier.height(24.dp))

        weather?.let {
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
            Column {
                Text(text = stringResource(R.string.humidity_format, it.main.humidity))
                Text(text = stringResource(R.string.pressure_format, it.main.pressure))
            }
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
