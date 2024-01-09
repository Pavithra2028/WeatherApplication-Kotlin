package com.example.splashscreen
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.splashscreen.adapter.RvAdapter
import com.example.splashscreen.databinding.ActivityMainBinding
import com.example.splashscreen.utils.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var rvAdapter: RvAdapter
    private var city: String = "madurai"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val textView: TextView = findViewById(R.id.textView)
        val calendar = Calendar.getInstance()
        val currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)
        textView.text = currentDate
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        binding.searchview.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    city = query
                }
                getCurrentWeather(city)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        //fetchLocation()
        getCurrentWeather(city)
        binding.tvLocation.setOnClickListener {
            fetchLocation()
        }
//        getForecast()

        var button1 = findViewById<Button>(R.id.next7days)
        button1.setOnClickListener {
            val intent1 = Intent(this, MainActivity2::class.java)
            startActivity(intent1)
        }

    }

    private fun fetchLocation() {
        val task: Task<Location> = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101
            )

            return
        }

        task.addOnSuccessListener { location ->
            location?.let {
                val geocoder = Geocoder(this, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(it.latitude, it.longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            processAddresses(addresses)
                        }
                    })
                } else {
                    try {
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        processAddresses(addresses)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } ?: run {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processAddresses(addresses: List<Address>?) {
        if (addresses.isNullOrEmpty()) {
            return
        }

        val address = addresses.firstOrNull()
        address?.let {
            city = it.locality ?: "Default City"
            getCurrentWeather(city)
        } ?: run {
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun getCurrentWeather(city:String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getCurrentWeather(
                    city,
                    "metric",
                    "4699776300bc505eb0dd3c4643b2e3dc"
                )
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        val data = response.body()!!
                        val iconId = data.weather[0].icon
                        val imgUrl = "https://openweathermap.org/img/wn/$iconId@4x.png"
                        Picasso.get().load(imgUrl).into(binding.imageviewrain)
                        Picasso.get().load(imgUrl).into(binding.imagewind)
                        Picasso.get().load(imgUrl).into(binding.imagehumidity)
                        Picasso.get().load(imgUrl).into(binding.imageviewsun)
                        binding.tvTemp.text = data.main.temp.toString()
                        binding.sunrise.text = dateFormatConverter(data.sys.sunset.toLong())
                        binding.sunset.text = dateFormatConverter(data.sys.sunrise.toLong())
                        binding.tvLocation.text = data.name
                        binding.feelslike.text = data.main.feels_like.toInt().toString()
                        binding.pressure.text=data.main.pressure.toString()
                        binding.min.text=data.main.temp_min.toString()
                        binding.maximum.text=data.main.temp_max.toString()
                        binding.Textview.text=data.weather[0].description.toUpperCase()


                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Network issue: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun dateFormatConverter(date: Long): CharSequence? {
        return SimpleDateFormat(
            "hh:mm a",
            Locale.ENGLISH
        ).format(Date(date * 1000))
    }

}

