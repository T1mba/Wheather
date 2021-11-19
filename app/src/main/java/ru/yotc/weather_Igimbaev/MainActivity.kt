package ru.yotc.weather_Igimbaev

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import org.json.JSONObject
import org.w3c.dom.Text
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import ru.yotc.myapplication.HTTP
import java.io.StringReader
import java.lang.Exception
import java.nio.file.WatchEvent
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.XMLFormatter
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    var counter = 0
    private var ready = false
    var cityName = ""
    var topTag = ""
    var subTag = ""
    var dt_txt = ""
    var description: String = ""
    var icon: String = ""
    var windSpeed: Double = 0.0
    var temp: Double = 0.0
    var deg: Int = 0
    var humi: Int = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    lateinit var textView: TextView
    lateinit var secview: TextView
    lateinit var tempView: TextView
    lateinit var lastview: TextView
    private lateinit var getServer:(result:String, error: String) -> Unit
    lateinit var predlastview: TextView
    private lateinit var callback: (result: String?, error: String) -> Unit
    private lateinit var dailyInfoRecyclerView: RecyclerView
    lateinit var desview: TextView
    private val weatherList = ArrayList<Weather>()
    private val token = "d4c9eea0d00fb43230b479793d6aa78f"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
            getServer = {result, error ->
                //Тут должен быть запрос на сервер
            }
        callback = { result, error ->
            if (result != null) {
// перед заполнением очищаем список
                try{
                weatherList.clear()

                val json = JSONObject(result)
                val list = json.getJSONArray("list")


// перебираем json массив
                for (i in 0 until list.length()) {
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
                    showDetailsInfo(weatherList[0])
                    textView.text = json.getJSONObject("city").getString("name")
                    secview.text = weatherList[0].windSpeed.toString()
                    tempView.text = weatherList[0].mainTemp.toString()
                    lastview.text = weatherList[0].mainHumidity.toString()

                    desview.text = weatherList[0].weatherDescription
// уведомляем визуальный элемент, что данные изменились
                    dailyInfoRecyclerView.adapter?.notifyDataSetChanged()


                }

            }
                catch (e: Exception){
                    AlertDialog.Builder(this)
                            .setTitle("Ошибка работы с Json")
                            .setMessage(e.toString())
                            .setPositiveButton("OK", null)
                            .create()
                            .show()
                }
            }
            else
            {
                AlertDialog.Builder(this)
                        .setTitle("Ошибка запроса с инернета")
                        .setMessage(error)
                        .setPositiveButton("OK", null)
                        .create()
                        .show()
            }


        }


        object : CountDownTimer(5000, 1000) {
            val splash = findViewById<ImageView>(R.id.splash)

            override fun onTick(millisUntilFinished: Long) {

                counter++
                if (counter > 3 && ready) {
                    splash.elevation = 0F
                    this.cancel()
                }
            }

            override fun onFinish() {
                ready = true
                splash.elevation = 0F

            }
        }.start()
        textView = findViewById<TextView>(R.id.header)
        secview = findViewById<TextView>(R.id.headesr)
        tempView = findViewById<TextView>(R.id.temp)
        lastview = findViewById<TextView>(R.id.vlaj)

        desview = findViewById<TextView>(R.id.desc)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        checkPermission()
        dailyInfoRecyclerView = findViewById(R.id.dailyInfoRecyclerView)
        dailyInfoRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val weatherAdapter = WeatherAdapter(weatherList, this)
        weatherAdapter.setItemClickListener {
            runOnUiThread {
                showDetailsInfo(it)
            }
        }
        dailyInfoRecyclerView.adapter = weatherAdapter


    }

    private fun showDetailsInfo(weather: Weather) {
        //TODO("Not yet implemented")
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val permission = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permission, 0)
        } else {
            mLocationRequest = LocationRequest()
            mLocationRequest.interval = 1000
            mLocationRequest.fastestInterval = 1000
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        }

    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.locations.isNotEmpty()) {
                val iocIndex = locationResult.locations.size - 1
                val lon = locationResult.locations[iocIndex].longitude
                val lat = locationResult.locations[iocIndex].latitude
                onGetCoordinate(lat, lon)

            }
        }

    }

    fun onGetCoordinate(lat: Double, lon: Double) {
        try {
            fusedLocationClient.removeLocationUpdates(mLocationCallback)
            // val url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=metric&appid=${token}&lang=RU"
            //HTTP.requestGET(url, callback)
            val url = "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&units=metric&appid=${token}&lang=RU"
            HTTP.requestGET(url, callback)
        }
        catch (e: Exception){
            AlertDialog.Builder(this)
                    .setTitle("Ошибка запроса по координатам")
                    .setMessage(e.toString())
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }

        var name = data.getStringExtra("cityName")
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=${name}&mode=xml&appid=${token}&lang=ru&units=metric"
        HTTP.requestGET(url) { result, error ->
            if (result != null) {
                try{
                weatherList.clear()

                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val parser = factory.newPullParser()
                parser.setInput(StringReader(result))
                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "location", "forecast" -> topTag = parser.name
                                "name" -> {
                                    if (topTag == "location") cityName = parser.nextText()
                                    name = parser.getAttributeValue(null, "name")
                                }
                                "time" -> {
                                    if (topTag == "forecast") {
                                        subTag = parser.name
                                        dt_txt = parser.getAttributeValue(null, "from").toString()
                                    }
                                }
                                "symbol" -> {
                                    if (subTag == "time") {
                                        description = parser.getAttributeValue(null, "name").toString()
                                        icon = parser.getAttributeValue(null, "var").toString()
                                    }
                                }
                                "temperature" -> {


                                    temp = parser.getAttributeValue(null, "value").toDouble()


                                }
                                "windSpeed" -> {
                                    windSpeed = parser.getAttributeValue(null, "mps").toDouble()
                                }
                                "windDirection" -> {
                                    deg = parser.getAttributeValue(null, "deg").toInt()
                                }
                                "humidity" -> {
                                    humi = parser.getAttributeValue(null, "value").toInt()
                                }

                            }
                        }
                        XmlPullParser.END_TAG -> {
                            when (parser.name) {
                                "time" -> {
                                    subTag = ""
                                    weatherList.add(
                                            Weather(
                                                    0,
                                                    temp,
                                                    humi,
                                                    icon,
                                                    description,
                                                    windSpeed,
                                                    deg,
                                                    dt_txt
                                            )
                                    )
                                }
                            }
                        }
                    }
                    parser.next()
                }
                runOnUiThread {
                    showDetailsInfo(weatherList[0])
                    textView.text = cityName
                    secview.text = weatherList[0].windSpeed.toString()
                    tempView.text = weatherList[0].mainTemp.toString()
                    desview.text = weatherList[0].weatherDescription
                    lastview.text = weatherList[0].mainHumidity.toString()
// уведомляем визуальный элемент, что данные изменились
                    dailyInfoRecyclerView.adapter?.notifyDataSetChanged()


                }



            }
                catch (e: Exception){
                    AlertDialog.Builder(this)
                            .setTitle("Ошибка разбора XML")
                            .setMessage(e.toString())
                            .setPositiveButton("OK", null)
                            .create()
                            .show()
                }
            }
            else
            {
                AlertDialog.Builder(this)
                        .setTitle("Ошибка работы с интернетом")
                        .setMessage(error)
                        .setPositiveButton("OK", null)
                        .create()
                        .show()
            }



        }



    }

    fun selectCity(view: View) {
        startActivityForResult(Intent(this, CityListActivity::class.java),
                1)
    }

    fun select(view: View) {
        MyCustomDialog().show(supportFragmentManager, "Dialog")
    }

}








