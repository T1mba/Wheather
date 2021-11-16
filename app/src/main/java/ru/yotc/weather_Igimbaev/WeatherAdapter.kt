package ru.yotc.weather_Igimbaev

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.yotc.myapplication.HTTP
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter (
    private val values: ArrayList<Weather>,
    private val activity: Activity
): RecyclerView.Adapter<WeatherAdapter.ViewHolder>(){

    // обработчик клика по элементу списка (лямбда выражение), может быть не задан
    private var itemClickListener: ((Weather) -> Unit)? = null

    fun setItemClickListener(itemClickListener: (Weather) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    // Метод onCreateViewHolder вызывается при создании визуального элемента
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // грузим layout, который содержит вёрстку элемента списка (нарисуйте сами)
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.weather_item,
                parent,
                false)

        // создаем на его основе ViewHolder
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = values.size

    // заполняет визуальный элемент данными
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tempTextView.text = "${values[position].mainTemp} C"
            holder.timeView.text = values[position].dtTxt

            // onIconLoad.invoke(holder.iconImageView, values[position].weatherIcon)

            holder.container.setOnClickListener {
                //кликнули на элемент списка
                itemClickListener?.invoke(values[position])
            }

            HTTP.getImage("https://openweathermap.org/img/w/${values[position].weatherIcon}.png") { bitmap, error ->
                if (bitmap != null) {
                    activity.runOnUiThread {
                        try {
                            holder.iconImageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {

                        }
                    }
                } else
                    Log.d("KEILOG", error)
            }
    }

    //Реализация класса ViewHolder, хранящего ссылки на виджеты.
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var iconImageView: ImageView = itemView.findViewById(R.id.weather_icon)
        var tempTextView: TextView = itemView.findViewById(R.id.weather_temp)
        var container: LinearLayout = itemView.findViewById(R.id.container)
        var timeView: TextView = itemView.findViewById(R.id.weather_time)

    }
}