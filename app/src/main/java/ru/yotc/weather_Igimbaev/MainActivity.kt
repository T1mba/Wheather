package ru.yotc.weather_Igimbaev

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import org.json.JSONObject
import org.w3c.dom.Text
import ru.yotc.myapplication.HTTP
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    lateinit var textView:TextView
    lateinit var secview:TextView
    lateinit var tempView:TextView
    lateinit var lastview:TextView
    lateinit var predlastview:TextView
    private val token = "d4c9eea0d00fb43230b479793d6aa78f"
    private var callback: (result: String?, error: String)->Unit =  {result, error ->
        if(result != null) {
            val json = JSONObject(result)
            val wheather = json.getJSONArray("weather")
            val icoName = wheather.getJSONObject(0).getString("icon")
            val wind = json.getJSONObject("wind")
            val mains = json.getJSONObject("main")
            val secmain = json.getJSONObject("main")


            runOnUiThread {
                textView.text = json.getString("name")
                secview.text = wind.getDouble("speed").toString()
                tempView.text= mains.getDouble("temp").toString()
                lastview.text = secmain.getDouble("humidity").toString()
                predlastview.text = wind.getDouble("deg").toString()

            }
            val splash = findViewById<ImageView>(R.id.splash)
            splash.elevation = 0F
            HTTP.getImage("https://openweathermap.org/img/w/${icoName}.png") {
                bitmap, error ->
                if(bitmap !=null)
                {

                    var imageView = findViewById<ImageView>(R.id.ico)
                    runOnUiThread{
                        imageView.setImageBitmap(bitmap)
                    }
                }

            }
        }

        else
        {
            runOnUiThread{
                textView.text = error
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        textView = findViewById<TextView>(R.id.header)
        secview = findViewById<TextView>(R.id.headesr)
        tempView = findViewById<TextView>(R.id.temp)
        lastview = findViewById<TextView>(R.id.vlaj)
        predlastview = findViewById<TextView>(R.id.napr)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        checkPermission()




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
            val url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=metric&appid=${token}&lang=RU"
            HTTP.requestGET(url, callback)
        }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null)
        {
            return
        }
        val name = data.getStringExtra("cityName")
        val url = " https://api.openweathermap.org/data/2.5/weather?q=${name}&units=metric&appid=${token}"
        HTTP.requestGET(url, callback)


    }

    fun selectCity(view: View) {
        startActivityForResult(Intent(this, CityListActivity::class.java),
                1)

    }
    /*
    val cityName = intent.getStringExtra("city_name")
    fun onGetSec(q:String)
    {

        val url = " https://api.openweathermap.org/data/2.5/weather?q=${cityName}&units=metric&appid=${token}"
        HTTP.requestGET(url, callback)
    }

     */






}