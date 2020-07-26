package com.maliotis.petros.weather

import android.app.ExpandableListActivity
import android.os.SystemClock
import android.util.Log
import android.widget.ExpandableListView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.zipWith
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.anything
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * Created by petrosmaliotis on 04/04/2020.
 */
@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class MainActivityTestTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java)

    lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
    }

    @Test
    fun testFetchForecast() {
        val bodyFromDarkSky = readLocalDataFromDarkSky()
        val url = "/test"
        server.enqueue(MockResponse().setBody(bodyFromDarkSky).setResponseCode(200))
        server.start()
        val baseUrl = server.url(url)
        val responseString = createOkHttpClient(baseUrl.toString())
        val req = server.takeRequest()
        assertEquals(responseString, bodyFromDarkSky)
        server.shutdown()

    }

    private fun readLocalDataFromDarkSky(): String {
        val inputStream = activityRule.activity.applicationContext.assets.open("bodyFromDarkSky.json")
        val size = inputStream.available()
        val byteArray = ByteArray(size)
        inputStream.read(byteArray)
        inputStream.close()
        val bodyFromDarkSky = String(byteArray, Charsets.UTF_8)
        return bodyFromDarkSky
    }

    fun createOkHttpClient(url: String): String? {
        val request = Request.Builder().url(url).build()
        val okHttpClient =  OkHttpClient.Builder()
                .hostnameVerifier(object : HostnameVerifier {
                    override fun verify(hostname: String?, session: SSLSession?): Boolean {
                        return true
                    }

                })
                .build()
        val call = okHttpClient.newCall(request)
        val response = call.execute()
        return response.body?.string()
    }


    @Test
    fun testGpsAndNetwork_Success() {
        val gpsObservable = activityRule.activity.gpsObservable
        val networkObservable = activityRule.activity.networkObservable
        val gpsAndNetworkObservable = gpsObservable.zipWith(networkObservable) { gps, net ->
            Pair<Boolean, Boolean>(gps, net)
        }
        val gpsAndNetworkDisposable = gpsAndNetworkObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    assertEquals(true, it.first)
                    assertEquals(true, it.second)
                }

        gpsObservable.onNext(true)
        networkObservable.onNext(true)
    }

    @Test
    fun testGpsAndNetwork_FailureGPS() {
        val gpsObservable = activityRule.activity.gpsObservable
        val networkObservable = activityRule.activity.networkObservable
        val gpsAndNetworkObservable = gpsObservable.zipWith(networkObservable) { gps, net ->
            Pair<Boolean, Boolean>(gps, net)
        }
        val gpsAndNetworkDisposable = gpsAndNetworkObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    assertEquals(false, it.first)
                    assertEquals(true, it.second)
                }

        gpsObservable.onNext(false)
        networkObservable.onNext(true)
    }

    @Test
    fun testGpsAndNetwork_FailureNetwork() {
        val gpsObservable = activityRule.activity.gpsObservable
        val networkObservable = activityRule.activity.networkObservable
        val gpsAndNetworkObservable = gpsObservable.zipWith(networkObservable) { gps, net ->
            Pair<Boolean, Boolean>(gps, net)
        }
        val gpsAndNetworkDisposable = gpsAndNetworkObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    assertEquals(true, it.first)
                    assertEquals(false, it.second)
                }

        gpsObservable.onNext(true)
        networkObservable.onNext(false)
    }

    @Test
    fun testGpsAndNetwork_FailureBoth() {
        val gpsObservable = activityRule.activity.gpsObservable
        val networkObservable = activityRule.activity.networkObservable
        val gpsAndNetworkObservable = gpsObservable.zipWith(networkObservable) { gps, net ->
            Pair<Boolean, Boolean>(gps, net)
        }
        val gpsAndNetworkDisposable = gpsAndNetworkObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    assertEquals(false, it.first)
                    assertEquals(false, it.second)
                }

        gpsObservable.onNext(false)
        networkObservable.onNext(false)
    }


    @Test
    fun test_ExpandableList() {

        // Check list is displayed
        onView(withId(R.id.expandableList1)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Artificial delay to fetch the data
        SystemClock.sleep(1000)
        // Check list expands
        onData(anything())
                .inAdapterView(withId(R.id.expandableList1))
                .atPosition(0)
                .perform(click())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }
}