package com.maliotis.petros.weather

import android.Manifest
import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.maliotis.petros.weather.animations.FadeAnimation
import com.maliotis.petros.weather.weather.Current
import com.maliotis.petros.weather.weather.Day
import com.maliotis.petros.weather.weather.Forecast
import com.maliotis.petros.weather.weather.Hour
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // Views
    lateinit var mLocationLabel: TextView
    lateinit var mTemperatureLabel: TextView
    lateinit var mHumidityValue: TextView
    lateinit var mPrecipValue: TextView
    lateinit var mSummaryLabel: TextView
    lateinit var mIconImageView: ImageView
    lateinit var mLayout: RelativeLayout
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    var scrollView: NestedScrollView? = null
    var cardView: CardView? = null
    lateinit var mListView: ExpandableListView

    // Forecast
    private var mForecast: Forecast? = null
    lateinit var mDays: Array<Day?>
    lateinit var mHours: Array<Hour?>
    lateinit var mHoursForToday: Array<Hour?>
    var hourListForDays: List<Array<Hour>>? = null
    lateinit var hoursArray: Array<Array<Hour?>>

    // Location
    var mLocation: Location? = null
    var locationManager: LocationManager? = null
    var mLatitude = 0.0
    var mLongitude = 0.0
    private var mGeocoder: Geocoder? = null
    var addresses: List<Address>? = null
    var name: String? = null
    var code: String? = null

    // Rx components
    lateinit var gpsAndNetworkDisposable: Disposable
    lateinit var gpsAndNetworkObservable: Observable<Pair<Boolean, Boolean>>
    lateinit var gpsObservable: Subject<Boolean>
    lateinit var networkObservable: Subject<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        // Swipe refresh layout
        mSwipeRefreshLayout.setProgressViewOffset(false, -150, 10)
        mSwipeRefreshLayout.setOnRefreshListener { getLatestForecastDetails() }

        mLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        // expandable list view listeners
        mListView.setOnGroupClickListener { expandableListView, view, i, l ->
            val params = mListView.layoutParams
            val scale: Float = resources.displayMetrics.density
            if (mListView.isGroupExpanded(i)) {
                val pixels = (350 * scale + 0.5f).toInt()
                params.height = pixels
                mListView.layoutParams = params
                mListView.requestFocus()
                mListView.clearFocus()
            } else {
                val pixels = (550 * scale + 0.5f).toInt()
                params.height = pixels
                mListView.layoutParams = params
                mListView.requestFocus()
                mListView.clearFocus()
            }
            false
        }

        hourListForDays = ArrayList()
        addresses = ArrayList()
        hoursArray = Array(7) { arrayOfNulls<Hour?>(8) }
        mDays = arrayOfNulls(7)
        mHours = arrayOfNulls(48)
        mHoursForToday = arrayOfNulls(48)

        mGeocoder = Geocoder(this)
    }

    override fun onStart() {
        super.onStart()
        gpsObservable = PublishSubject.create()
        networkObservable = PublishSubject.create()
        observeGpsAndNetworkChecks()
        getLatestForecastDetails()
    }

    override fun onStop() {
        super.onStop()
        if (!gpsAndNetworkDisposable.isDisposed)
            gpsAndNetworkDisposable
    }

    /**
     * Observing changes in network and gps to get the latest forecast
     */
    fun observeGpsAndNetworkChecks(): Disposable {
        gpsAndNetworkObservable = gpsObservable.zipWith(networkObservable) { gps, net ->
            Pair<Boolean, Boolean>(gps, net)
        }
        gpsAndNetworkDisposable = gpsAndNetworkObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.first && it.second) {
                        GPS()
                        getForecastRx(mLatitude, mLongitude)
                        geoUserLocation()
                        stopRefresh()
                    } else {
                        checkForAlerts(it)
                    }
                }
        return gpsAndNetworkDisposable
    }

    /**
     * It will then trigger the observeGpsAndNetworkChecks() method reactively
     * @see observeGpsAndNetworkChecks
     */
    private fun getLatestForecastDetails() {
        isGPSEnabled()
        isNetworkAvailable()
    }

    private fun bindViews() {
        cardView = findViewById(R.id.cardView)
        mTemperatureLabel = findViewById(R.id.temperatureLabel)
        mHumidityValue = findViewById(R.id.humidityValue)
        mPrecipValue = findViewById(R.id.precipValue)
        mSummaryLabel = findViewById(R.id.summaryLabel)
        mIconImageView = findViewById(R.id.iconImageView)
        mLayout = findViewById(R.id.ConstraintLayout)
        mLocationLabel = findViewById(R.id.locationLabel)
        mSwipeRefreshLayout = findViewById(R.id.refreshLayout)
        mListView = findViewById(R.id.expandableList1)
        scrollView = findViewById(R.id.scroll)
    }

    /**
     * This method will get the locality and countryName based on the users last location
     */
    private fun geoUserLocation() {
        try {
            addresses = mGeocoder!!.getFromLocation(mLatitude, mLongitude, 4)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (addresses?.isNotEmpty() == true) {
            name = addresses!![0].locality
            code = addresses!![0].countryName
            Log.d("COUNTRY", "$name")
        }
        if ((name == null || name == "") && (code == null || code == "")) {
            name = "-"
            code = "-"
        }
    }

    /**
     * This method creates an Http request to fetch the latest forecast
     *
     */
    private fun getForecastRx(lat: Double, long: Double) {

        val v = Observable.create<Response> { emitter ->
            var latitude = lat
            var longitude = long
            val ApiKey = "a69a377194dd481e775182e1702fa70b"
            val locale = Locale.getDefault().language
            if (latitude == 0.0 && longitude == 0.0) {
                latitude = 37.9667 //37.8267;
                longitude = -122.4233
            }
            val forecastUrl = "https://api.darksky.net/forecast/$ApiKey/$latitude,$longitude?lang=$locale&extend=hourly"
            val request = Request.Builder().url(forecastUrl).build()
            val client = OkHttpClient()
            val call = client.newCall(request)
            call.enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    emitter.onError(e)
                }
                override fun onResponse(call: Call, response: Response) {
                    emitter.onNext(response)
                }
            })

        }
                .subscribeOn(Schedulers.io())
                .map { response ->
                    try {
                        val jsonData = response.body!!.string()
                        Log.v(TAG, jsonData)
                        if (response.isSuccessful) {
                            mForecast = parseForecastDetails(jsonData)
                            mHoursForToday = mForecast!!.hourlyForecast
                            runOnUiThread {

                            }
                        } else {
                            alertUserAboutError()
                        }
                        response
                    } catch (e: IOException) {
                        Log.e(TAG, "Exception cuaght: ", e)
                        response
                    } catch (e: JSONException) {
                        Log.e(TAG, "Exception cuaght: ", e)
                        response
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ ->
                    updateDisplay()
                    listAdapter(mListView)
                    animate()
                }


    }

    private fun fillHoursArray() {
        var g: Int
        for (i in mDays.indices) {
            g = 0
            var j = 0
            if (mDays[i]?.dayHourByHour != null) {
                while (j < mDays[i]!!.dayHourByHour.size) {
                    val hour: Hour? = mDays[i]!!.dayHourByHour[j]
                    hoursArray[i][g] = hour
                    g++
                    j += 3
                }
            }
        }
        // sort every row
        for (hArray in hoursArray) {
            if (!hArray.contains(null))
                hArray.sort()
        }
    }

    fun listAdapter(listView: ExpandableListView) {
        Log.d("TAG", "listAdapter: thread = " + Thread.currentThread().name)
        val expDayAdapter = ExpDayAdapter(this@MainActivity, mDays, hoursArray)
        listView.setAdapter(expDayAdapter)
    }

    private fun stopRefresh() {
        mSwipeRefreshLayout.postDelayed({
            if (mSwipeRefreshLayout.isRefreshing)
                mSwipeRefreshLayout.isRefreshing = false
        }, 500)
    }

    /**
     * This method will update the views with the latest information
     */
    private fun updateDisplay() {
        val current = mForecast!!.current
        val humidity = current.humidity * 100
        val humidityI = humidity.toInt()
        mTemperatureLabel.text = current.temperature.toString()
        mHumidityValue.text = "$humidityI%"
        mPrecipValue.text = current.precipChance.toString() + "%"
        mSummaryLabel.text = current.summary
        mLocationLabel.text = current.timeZone
        mLocationLabel.text = "$name,$code"
        val drawable = resources.getDrawable(current.iconId)
        mIconImageView.setImageDrawable(drawable)
    }

    @Throws(JSONException::class)
    fun parseForecastDetails(jsonData: String): Forecast {
        val forecast = Forecast()
        forecast.current = getCurrentDetails(jsonData)
        forecast.dailyForecast = getDailyForecast(jsonData)
        mDays = forecast.dailyForecast
        forecast.hourlyForecast = getHourlyForecast(jsonData)
        return forecast
    }

    @Throws(JSONException::class)
    private fun getDailyForecast(jsonData: String): Array<Day?> {
        val forecast = JSONObject(jsonData)
        val timezone = forecast.getString("timezone")
        val daily = forecast.getJSONObject("daily")
        val data = daily.getJSONArray("data")
        val days = arrayOfNulls<Day>(data.length())
        for (i in 0 until data.length() - 1) {
            val jsonDay = data.getJSONObject(i)
            val day = Day()
            day.summary = jsonDay.getString("summary")
            day.setTemperatureMax(jsonDay.getDouble("temperatureHigh"))
            day.iCon = jsonDay.getString("icon")
            day.time = jsonDay.getLong("time")
            day.timezone = timezone
            day.setTemperatureMin(jsonDay.getDouble("temperatureLow"))
            days[i] = day
        }
        return days
    }

    @Throws(JSONException::class)
    private fun getHourlyForecast(jsonData: String): Array<Hour?> {
        val forecast = JSONObject(jsonData)
        val timezone = forecast.getString("timezone")
        val hourly = forecast.getJSONObject("hourly")
        val data = hourly.getJSONArray("data")
        val hours = arrayOfNulls<Hour>(24)

        for (i in 0 until 24) {
            val jsonHour = data.getJSONObject(i)
            val hour = Hour()
            hour.summary = jsonHour.getString("summary")
            hour.setTemperature(jsonHour.getDouble("temperature"))
            hour.iCon = jsonHour.getString("icon")
            hour.time = jsonHour.getLong("time")
            hour.timezone = timezone
            hours[i] = hour
        }

        val hoursH: ArrayList<Hour> = ArrayList()

        for (i in 0 until data.length()) {
            val jsonHour = data.getJSONObject(i)
            val hour = Hour()
            hour.summary = jsonHour.getString("summary")
            hour.setTemperature(jsonHour.getDouble("temperature"))
            hour.iCon = jsonHour.getString("icon")
            hour.time = jsonHour.getLong("time")
            hour.timezone = timezone

            hoursH.add(hour)

            if (hoursH.size == 24) {
                mDays[i / 24 ]?.dayHourByHour = hoursH.toTypedArray()
                hoursH.clear()
            }
        }

        fillHoursArray()

        return hours
    }

    @Throws(JSONException::class)
    private fun getCurrentDetails(jsonData: String): Current {
        val forecast = JSONObject(jsonData)
        val timezone = forecast.getString("timezone")
        Log.i(TAG, "JSON : $timezone")
        val currently = forecast.getJSONObject("currently")
        val current = Current()
        current.humidity = currently.getDouble("humidity")
        current.time = currently.getLong("time")
        current.icon = currently.getString("icon")
        current.setPrecipChance(currently.getDouble("precipProbability"))
        current.summary = currently.getString("summary")
        current.setTemperature(currently.getDouble("temperature"))
        current.timeZone = timezone
        Log.d(TAG, current.formatedTime)
        return current
    }

    fun isGPSEnabled() {
        val tf: Boolean
        val ft: Boolean
        var isAvailable = true
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        tf = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        ft = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val locationProviders = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        if (!tf && !ft && (locationProviders == null || locationProviders == "")) {
            isAvailable = false
        }
        gpsObservable.onNext(isAvailable)
    }

    fun isNetworkAvailable() {
            val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var networkInfo: NetworkInfo? = null
            if (manager != null) {
                networkInfo = manager.activeNetworkInfo
            }
            var isAvailable = false
            if (networkInfo != null && networkInfo.isConnected) {
                isAvailable = true
            }
            networkObservable.onNext(isAvailable)
        }

    private fun alertUserAboutError() {
        val dialog = AlertDialogFragment()
        dialog.show(supportFragmentManager, "error_dialog")
    }

    fun alertUserAboutNetwork() {
        val dialog = NetworkAlertDialog()
        dialog.show(supportFragmentManager, "AlertAboutNetwork")
    }

    fun alertUserAboutGPS() {
        val dialog = GPSAlertDialog()
        dialog.show(supportFragmentManager, "AlertUSerAboutGPS")
    }

    fun alertUserAboutBoth() {
        val dialod = AlertDialogNetGPS()
        dialod.show(supportFragmentManager, "AlertUserAboutNetGPS")
    }

    fun informUser() {
        val dialog = InformUserToGetData()
        dialog.show(supportFragmentManager, "informUser")
    }

    private fun GPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTAG", "Something went wrong")
        } else {
            //mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            mLocation = lastLocation
            if (mLocation == null) {
                Log.v("Location", "Location was null")
            } else {
                mLatitude = mLocation!!.latitude
                mLongitude = mLocation!!.longitude
            }
        }
    }

    fun checkForAlerts(pair: Pair<Boolean, Boolean>) {
        if (!pair.second && pair.first) {
            alertUserAboutNetwork()
        } else if (!pair.first && pair.second) {
            alertUserAboutGPS()
        } else {
            alertUserAboutBoth()
        }
    }

    private val lastLocation: Location?
        get() {
            var bLocation: Location? = null
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MYTAG", "Something went wrong")
            } else {
                val providers = locationManager!!.getProviders(true)
                for (provider in providers) {
                    val l = locationManager!!.getLastKnownLocation(provider) ?: continue
                    if (bLocation == null || l.accuracy < bLocation.accuracy) {
                        bLocation = l
                    }
                }
            }
            return bLocation
        }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun animate() {
        val fadeAnimation = FadeAnimation()
        fadeAnimation.add(scrollView)
        val set = fadeAnimation.animatorSet
        set.start()
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
        const val DAILY_FORECAST = "DAILY_FORECAST"
        const val HOURLY_FORECAST = "HOURLY_FORECAST"
        const val HOURLY_DAY_FORECAST = "HOURLY_DAY_FORECAST"
    }
}