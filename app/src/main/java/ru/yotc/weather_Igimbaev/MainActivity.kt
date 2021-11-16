package ru.yotc.weather_Igimbaev

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import org.json.JSONObject
import org.w3c.dom.Text
import ru.yotc.myapplication.HTTP
import java.lang.Exception
import java.nio.file.WatchEvent


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    lateinit var textView:TextView
    lateinit var secview:TextView
    lateinit var tempView:TextView
    lateinit var lastview:TextView
    lateinit var predlastview:TextView
    private lateinit var callback: (result: String?, error: String)->Unit
    private lateinit var dailyInfoRecyclerView: RecyclerView
    lateinit var desview: TextView
    private val weatherList = ArrayList<Weather>()
    private val token = "d4c9eea0d00fb43230b479793d6aa78f"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
        callback = { result, error ->
            if(result != null) {
// перед заполнением очищаем список
                weatherList.clear()

                val json = JSONObject(result)
                val list = json.getJSONArray("list")

// перебираем json массив
                for(i in 0 until list.length()){
                    val item = list.getJSONObject(i)
                    val weather = item.getJSONArray("weather").getJSONObject(0)

// добавляем в список новый элемент
                    weatherList.add(
                            Weather(
                                    item.getInt("dt"),
                                    item.getJSONObject("main").getDouble("temp"),
                                    item.getJSONObject("main").getInt("humidity"),
                                    weather.getString("icon"),
                                    weather.getString("description"),
                                    item.getJSONObject("wind").getDouble("speed"),
                                    item.getJSONObject("wind").getInt("deg"),
                                    item.getString("dt_txt")

                            )
                    )

                }

                runOnUiThread {

// уведомляем визуальный элемент, что данные изменились
                    dailyInfoRecyclerView.adapter?.notifyDataSetChanged()

                }
            }
            else
                Log.d("KEILOG", error)

        }

        textView = findViewById<TextView>(R.id.header)
        secview = findViewById<TextView>(R.id.headesr)
        tempView = findViewById<TextView>(R.id.temp)
        lastview = findViewById<TextView>(R.id.vlaj)
        predlastview = findViewById<TextView>(R.id.napr)
        desview = findViewById<TextView>(R.id.desc)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        checkPermission()
                dailyInfoRecyclerView = findViewById(R.id.dailyInfoRecyclerView)
        dailyInfoRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val weatherAdapter = WeatherAdapter(weatherList, this)
        weatherAdapter.setItemClickListener {
            weather ->
        }
        dailyInfoRecyclerView.adapter = weatherAdapter


    }
    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            val permission = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permission, 0)
        }
        else
        {
            mLocationRequest = LocationRequest()
            mLocationRequest.interval = 1000
            mLocationRequest.fastestInterval = 1000
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        }

    }
    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult)
        {
            if(locationResult.locations.isNotEmpty())
            {
                val iocIndex = locationResult.locations.size - 1
                val lon = locationResult.locations[iocIndex].longitude
                val lat = locationResult.locations[iocIndex].latitude
                onGetCoordinate(lat, lon)

            }
        }
        
    }
        fun onGetCoordinate(lat: Double, lon: Double)
        {
            fusedLocationClient.removeLocationUpdates(mLocationCallback)
           // val url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=metric&appid=${token}&lang=RU"
            //HTTP.requestGET(url, callback)
            val url = "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&units=metric&appid=${token}&lang=RU"
            HTTP.requestGET(url, callback)
        }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null)
        {
            return
        }
        val name = data.getStringExtra("cityName")
        val url = " https://api.openweathermap.org/data/2.5/forecast?q=${name}&units=metric&appid=${token}&lang=ru"
        HTTP.requestGET(url, callback)


    }

    fun selectCity(view: View) {
        startActivityForResult(Intent(this, CityListActivity::class.java),
                1)

    }

}








