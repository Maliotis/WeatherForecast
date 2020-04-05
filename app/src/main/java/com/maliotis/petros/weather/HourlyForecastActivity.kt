package com.maliotis.petros.weather

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maliotis.petros.weather.weather.Day
import com.maliotis.petros.weather.weather.Hour
import java.util.*

class HourlyForecastActivity : AppCompatActivity() {
    private lateinit var mhours: Array<Hour>
    private lateinit var mDays: Array<Day>
    var recyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hourly_forecast)
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        val intent = intent
        val parcelables = intent.getParcelableArrayExtra(MainActivity.HOURLY_FORECAST)
        mhours = Arrays.copyOf(parcelables, parcelables.size, Array<Hour>::class.java)
        val parcelables1 = intent.getParcelableArrayExtra(MainActivity.HOURLY_DAY_FORECAST)
        mDays = Arrays.copyOf(parcelables1, parcelables1.size, Array<Day>::class.java)
        val hourAdapter = HourAdapter(this, mhours, mDays)
        recyclerView!!.adapter = hourAdapter
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.setHasFixedSize(true)
    }
}