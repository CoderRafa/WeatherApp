package rafa.khach.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings.Global.putString
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val API: String = "8a9f017984e7d7bcc9f8cc337143df49"

    var userLocation: String? = null
    var search: Button? = null
    var curLoc: Button? = null
    var lati: String? = null
    var longi: String? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fetchLocation()

        val sharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        lati = sharedPreferences.getString("LATI", null)
        longi = sharedPreferences.getString("LONGI", null)

        var url = "https://api.openweathermap.org/data/2.5/weather?lat=$lati&lon=$longi&units=metric&appid=$API"

        results(url)

        var curLoc = findViewById<ImageView>(R.id.currrentLocation)

        curLoc?.setOnClickListener() {
            results(url)
        }

        var userLocation = findViewById<EditText>(R.id.userLocation)
        var search = findViewById<ImageView>(R.id.searchLocation)


        search.setOnClickListener() {
            if (userLocation?.text?.toString()?.trim()?.equals("")!!)
                Toast.makeText(applicationContext, "Please enter a location", Toast.LENGTH_SHORT).show()
            else {
                var city: String = userLocation?.text.toString()
                var url: String = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$API"

                results(url)
            }
        }
    }

    @SuppressLint("WrongViewCast")
    private fun results(url: String) {
        doAsync {

            try{

            val apiResponse = URL(url).readText()
            val jsonObj = JSONObject(apiResponse)
            val weather = jsonObj.getJSONArray("weather")
            val sys = jsonObj.getJSONObject("sys")
            val desc = weather.getJSONObject(0).getString("description")
            val main = jsonObj.getJSONObject("main")
            val tempDec = main.getString("temp").toDouble()
            val temp = BigDecimal(tempDec).setScale(0, RoundingMode.HALF_EVEN).toString() + "°C"
            val feelDec = main.getString("feels_like")
            val feel = BigDecimal(feelDec).setScale(0, RoundingMode.HALF_EVEN).toString() + "°C"
            val address = jsonObj.getString("name") + ", " + sys.getString("country")
            val humidity = main.getString("humidity")
            val updatedAt = jsonObj.getLong("dt")
            val sunrise: Long = sys.getLong("sunrise")
            val sunset: Long = sys.getLong("sunset")
            val wind = jsonObj.getJSONObject("wind")
            val windSpeed = wind.getString("speed")
            val pressure = main.getString("pressure")
            val updatedAtText =
                    "Updated at: " + SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                            Date()
                    )

            findViewById<TextView>(R.id.temp).text = temp
            findViewById<TextView>(R.id.address).text = address
            findViewById<TextView>(R.id.feelsLike).text = "RealFeel  $feel"
            findViewById<TextView>(R.id.humidity1).text = "Humidity $humidity %"
            findViewById<TextView>(R.id.status).text = desc.capitalize()
            findViewById<TextView>(R.id.updated_at).text = updatedAtText
            findViewById<TextView>(R.id.wind).text =      "Wind        $windSpeed m/sec"
            findViewById<TextView>(R.id.pressure).text =  "Pressure $pressure hPA"

            if(updatedAt > sunrise && updatedAt < sunset){
                    Log.d("Text", "Day")
                val mealLayout = findViewById<RelativeLayout>(R.id.base)
                mealLayout.setBackgroundResource(R.drawable.gradientbg_day)
                    when (desc) {
                        "clear sky" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.sunny)
                        "few clouds" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.cloudy)
                        "scattered clouds" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.scattered_clouds_day)
                        "broken clouds", "overcast clouds" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.broken_clouds_d_n)
                        "shower rain", "rain", "light rain" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.shower_rain_d_n)
                        "thunderstorm" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.thunderstorm)
                        "snow", "shower sleet", "light snow" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.snow)
                        else -> {
                            findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.cloudy)
                        }
                    }
            }
                else {
                    Log.d("Text", "Night")
                    when (desc) {
                        "clear sky" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.clear_sky_night)
                        "few clouds" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.cloudy_night)
                        "scattered clouds" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.scattered_clouds_night)
                        "broken clouds", "overcast clouds" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.broken_clouds_night)
                        "shower rain", "rain", "light rain" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.shower_rain_d_n)
                        "thunderstorm" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.thunderstorm_night)
                        "snow", "shower sleet", "light snow" -> findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.snow_night)
                        else -> {
                            findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.cloudy_night)
                        }
                    }
                val mealLayout = findViewById<RelativeLayout>(R.id.base)
                mealLayout.setBackgroundResource(R.drawable.gradientbg)
                }

            }catch (e: Exception){
                if(e.toString() == "android.view.ViewRootImpl" +"$"+"CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.") {
                    Log.d("Error", e.toString())
                }else{
                    uiThread { longToast("Please enter an existing city") }
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation

        if (PermissionChecker.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    101
            )
            return
        }

        task.addOnSuccessListener {
            if (it!= null) {

//                findViewById<TextView>(R.id.lati).text = it.latitude.toString()
//                findViewById<TextView>(R.id.longi).text = it.longitude.toString()

                val sharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.apply(){
                    putString("LATI", it.latitude.toString())
                    putString("LONGI", it.longitude.toString())
                }.apply()
            }
        }
    }
}

