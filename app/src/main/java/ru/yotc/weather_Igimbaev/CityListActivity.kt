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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_list)
         var names = arrayOf(
            "Moscow",
            "Yoshkar-Ola",
            "Kazan"
        )


        list = findViewById<ListView>(R.id.cityList)
       list.adapter = ArrayAdapter(this,R.layout.city_list_item,names)

        list.setOnItemClickListener { parent, view, position, id ->
            val mainIntent = Intent(this, MainActivity::class.java)
            val cityName = names[id.toInt()]

            // запоминаем выбранное название города
            mainIntent.putExtra("city_name", cityName)

            // возвращаемся на основной экран (Activity)
            startActivity( mainIntent )
        }

    }



    }



