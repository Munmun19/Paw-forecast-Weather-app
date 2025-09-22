package com.example.pawforecast

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pawforecast.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        fetchWeatherData("Agartala")
        SearchCity()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun SearchCity() {
        val searchView= binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityName:String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(APIinterface::class.java)
        val response = retrofit.getWeatherData(city = cityName, appid = "e8b0c4d4111fc29e73254daeff440c88", units = "metric")
        response.enqueue(object : Callback<WeatherApp>{
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toString()
                    val feelsLike = responseBody.main.feels_like
                    val humidity = responseBody.main.humidity
                    val visibility = responseBody.visibility
                    val windSpeed = responseBody.wind.speed
                    val seaLevel = responseBody.main.pressure
                    val minTemp = responseBody.main.temp_min
                    val maxTemp = responseBody.main.temp_max
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val condition= responseBody.weather.firstOrNull()?.main?:"unknown"

                    binding.tmp.text= "$temperature °C"
                    binding.weather.text= condition
                    binding.feels.text= "Feels like $feelsLike °C"
                    binding.humidity.text= "Humidity: $humidity %"
                    binding.visible.text= "Visibility: $visibility m"
                    binding.windspeed.text= "$windSpeed kmPh"
                    binding.sea.text= "$seaLevel mb"
                    binding.maxtemp.text= "$maxTemp °C"
                    binding.mintemp.text= "$minTemp °C"
                    binding.rise.text= "${time(sunRise)}"
                    binding.set.text= "${time(sunSet)}"
                    binding.day.text=dayName(System.currentTimeMillis())
                        binding.date.text= date()
                        binding.location.text= "$cityName"

                    changeimgcondition(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun changeimgcondition(conditions: String) {
        when(conditions){
            "Clear Sky", "Sunny", "Clear" ->{
                binding.root.setBackgroundResource(R.drawable.gradientsunny)
                binding.lottieAnimationView.setAnimation(R.raw.sunnyanim)
            }
            "Haze", "Mist", "Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.gradienthaze)
                binding.lottieAnimationView.setAnimation(R.raw.haze)
            }
            "Party Clouds", "Clouds", "Overcast" ->{
                binding.root.setBackgroundResource(R.drawable.gradientrain)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain", "Drizzle", "Moderate Rain" ->{
                binding.root.setBackgroundResource(R.drawable.gradientrain)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Showers", "Heavy Rain", "Rain" ->{
                binding.root.setBackgroundResource(R.drawable.gradientrain)
                binding.lottieAnimationView.setAnimation(R.raw.heavyrain)
            }
            "Storm", "Thunderstorm" ->{
                binding.root.setBackgroundResource(R.drawable.gradientrain)
                binding.lottieAnimationView.setAnimation(R.raw.thunder)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.gradientsnow)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_bg)
                binding.lottieAnimationView.setAnimation(R.raw.sunnyanim)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }
    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }

    fun dayName(timestamp: Long):String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date(timestamp)))
    }
}