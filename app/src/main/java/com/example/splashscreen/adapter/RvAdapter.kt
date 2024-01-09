package com.example.splashscreen.adapter

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.splashscreen.ItemclickInterface
import com.example.splashscreen.data.forecastmodels.ForecastData
import com.example.splashscreen.databinding.DailyweatherBinding
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class RvAdapter(
    private val forecastArray: ArrayList<ForecastData>,
    private val itemClickInterface: ItemclickInterface
) : RecyclerView.Adapter<RvAdapter.ViewHolderClass>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding = DailyweatherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolderClass(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = forecastArray[position]
        holder.bind(currentItem)

        holder.itemView.setOnClickListener {
            itemClickInterface.onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return forecastArray.size
    }

    class ViewHolderClass(private val binding: DailyweatherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(forecastData: ForecastData) {
            val imageIcon = forecastData.weather[0].icon
            val imageUrl = "https://openweathermap.org/img/wn/$imageIcon.png"
            Picasso.get().load(imageUrl).into(binding.dayimage)
            binding.title.text = forecastData.weather[0].description
            binding.lal.text =forecastData.main.humidity.toString()
            binding.lon.text = displayTime(forecastData.dt_txt)
            binding.day.text = "${forecastData.main.temp.toInt()}"
        }
    }
}

private fun displayTime(dtTxt: String): CharSequence? {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val date = inputFormat.parse(dtTxt)
    return outputFormat.format(date)
}