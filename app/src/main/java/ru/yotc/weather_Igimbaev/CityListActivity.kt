package ru.yotc.weather_Igimbaev

import android.R.layout.test_list_item
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView

class CityListActivity : AppCompatActivity() {
  lateinit var  list: ListView
    private var names = arrayOf(
        "Moscow",
        "Yoshkar-Ola",
        "Kazan"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_list)

        list = findViewById<ListView>(R.id.cityList)
       list.adapter = ArrayAdapter(this,R.layout.city_list_item,names)


    }






}
