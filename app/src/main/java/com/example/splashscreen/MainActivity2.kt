package com.example.splashscreen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splashscreen.adapter.RvAdapter
import com.example.splashscreen.data.forecastmodels.Forecast
import com.example.splashscreen.data.forecastmodels.ForecastData

import com.example.splashscreen.databinding.ActivityMain2Binding
import com.example.splashscreen.databinding.ActivityMainBinding
import com.example.splashscreen.utils.RetrofitInstance
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale


class MainActivity2 : AppCompatActivity() , ItemclickInterface{

    private lateinit var binding: ActivityMain2Binding
    private lateinit var rvAdapter: RvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)

        setContentView(binding.root)
        getForecast()

        var button2 = findViewById<ImageButton>(R.id.backButton)
        button2.setOnClickListener {
            val intent2 = Intent(this, MainActivity::class.java)
            startActivity(intent2)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    private fun getForecast() {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.api.getForecast(
                    "madurai",
                    "metric",
                    "4699776300bc505eb0dd3c4643b2e3dc"
                )
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            } catch (e: retrofit2.HttpException) {
                Toast.makeText(applicationContext, "http error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val forecast: Forecast = response.body()!!

                    val forecastList: ArrayList<ForecastData>? =
                        forecast.list as? ArrayList<ForecastData>

                    forecastList?.let { list ->
                        val nonNullableList: ArrayList<ForecastData> = ArrayList(list)
                        rvAdapter = RvAdapter(nonNullableList, this@MainActivity2)
                        binding.recyclerview2.apply {
                            val spanCount = 2
                            val gridLayoutManager = GridLayoutManager(this@MainActivity2, spanCount)
                            adapter = rvAdapter
                            layoutManager = gridLayoutManager
                        }
                    }

                }
            }
        }
    }

    override fun onItemClick(forecastData: ForecastData) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Selected day")
        val message = "You selected on ${forecastData.weather[0].description}\nTemperature: ${forecastData.main.temp.toInt()}\n  ${displayTime(forecastData.dt_txt)}"
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_bg)

    }
    private fun displayTime(dtTxt: String): CharSequence? {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dtTxt)
        return outputFormat.format(date)
    }
}