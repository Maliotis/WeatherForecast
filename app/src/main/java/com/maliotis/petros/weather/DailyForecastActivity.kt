package com.maliotis.petros.weather

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.maliotis.petros.weather.weather.Day
import com.maliotis.petros.weather.weather.Hour
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class DailyForecastActivity : AppCompatActivity() {
    private lateinit var mDays: Array<Day?>
    var mListView: ExpandableListView? = null
    var mTextView: TextView? = null
    var locationLabel: TextView? = null
    var mLongitude = 0.0
    var mLatitude = 0.0
    var timezone: String? = null
    var name: String? = null
    var code: String? = null
    lateinit var mHours: Array<Hour?>
    lateinit var hoursArray: Array<Array<Hour?>>
    var i = 0
    lateinit var call: Array<Call?>
    lateinit var finished: BooleanArray
    var finallyComplete = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_forecast)
        mListView = findViewById<View>(android.R.id.list) as ExpandableListView
        mTextView = findViewById<View>(android.R.id.empty) as TextView
        locationLabel = findViewById<View>(R.id.locationLabel) as TextView
        mHours = arrayOfNulls(24)
        hoursArray = Array(8) { arrayOfNulls<Hour?>(24) }
        finished = BooleanArray(8)
        call = arrayOfNulls(8)
        val intent = intent
        val parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST)
        mDays = Arrays.copyOf(parcelables, parcelables.size, Array<Day>::class.java)
        timezone = intent.getStringExtra("timezone")
        name = intent.getStringExtra("name")
        code = intent.getStringExtra("code")
        mLatitude = intent.getDoubleExtra("latitude", 0.0)
        mLongitude = intent.getDoubleExtra("longitude", 0.0)
        locationLabel!!.text = "$name,$code"
        i = 0
        while (i < mDays.size) {
            getForecastDayHour(mLatitude, mLongitude, mDays[i], i)
            i++
        }

    }

    fun getForecastDayHour(latitude: Double, longitude: Double, days: Day?, i: Int) {
        var latitude = latitude
        var longitude = longitude
        val ApiKey = "a69a377194dd481e775182e1702fa70b"
        if (latitude == 0.0 && longitude == 0.0) {
            latitude = 37.9667 //37.8267;
            longitude = -122.4233
        }
        val ForecastUrl = "https://api.darksky.net/forecast/" + ApiKey + "/" + latitude + "," + longitude + "," + days!!.time
        val client = OkHttpClient()
        val request = Request.Builder().url(ForecastUrl).build()
        call[i] = client.newCall(request)
        call[i]!!.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonData = response.body!!.string()
                    //Log.v("DAYS",jsonData);
                    if (response.isSuccessful) {
                        mHours = getDayHourByHourForecast(jsonData)
                        Log.v("TAG", "it Worked")
                        runOnUiThread {
                            mDays[i]!!.dayHourByHour = mHours
                            Log.v("TAG", mDays[i]!!.dayHourByHour.size.toString() + "")
                            Log.v("RUN", i.toString() + "")
                            finished[i] = true
                            for (j in 0..7) {
                                if (finished[j]) {
                                    finallyComplete = true
                                } else {
                                    finallyComplete = false
                                    break
                                }
                            }
                            if (finallyComplete) {
                                fillHoursArray()
                                listAdapter()
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.e("Days", "Exception caught : ", e)
                } catch (e: JSONException) {
                    Log.e("Days", "Exception caught : ", e)
                }
            }
        })
    }

    @Throws(JSONException::class)
    private fun getDayHourByHourForecast(jsonData: String): Array<Hour?> {
        val forecast = JSONObject(jsonData)
        val timezone = forecast.getString("timezone")
        val hourly = forecast.getJSONObject("hourly")
        val data = hourly.getJSONArray("data")
        val hours = arrayOfNulls<Hour>(data.length())
        for (i in 0 until data.length()) {
            val jsonHour = data.getJSONObject(i)
            val hour = Hour()
            hour.summary = jsonHour.getString("summary")
            hour.setTemperature(jsonHour.getDouble("temperature"))
            hour.iCon = jsonHour.getString("icon")
            hour.time = jsonHour.getLong("time")
            hour.timezone = timezone
            hours[i] = hour
        }
        return hours
    }

    fun fillHoursArray() {
        var g: Int
        for (i in mDays.indices) {
            g = 0
            var j = 0
            while (j < mDays[i]!!.dayHourByHour.size) {
                var hour: Hour?
                hour = mDays[i]!!.dayHourByHour[j]
                hoursArray[i][g] = hour
                g++
                j = j + 3
            }
        }
    }

    private fun listAdapter() {
        val expDayAdapter = ExpDayAdapter(this@DailyForecastActivity, mDays, hoursArray)
        mListView!!.setAdapter(expDayAdapter)
        mListView!!.emptyView = mTextView
    }
}