package com.maliotis.petros.weather

import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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

    //lateinit var server: MockWebServer

//    @Before
//    fun setup() {
//        server = MockWebServer()
//    }

    @Test
    fun testFetchForecast() {
        val server = MockWebServer()
        val inputStream = activityRule.activity.applicationContext.assets.open("bodyFromDarkSky.json")
        val size = inputStream.available()
        val byteArray = ByteArray(size)
        inputStream.read(byteArray)
        inputStream.close()
        val bodyFromDarkSky = String(byteArray, Charsets.UTF_8)
        val url = "/test"
        server.enqueue(MockResponse().setBody(bodyFromDarkSky).setResponseCode(200))
        server.start()
        val baseUrl = server.url(url)
        val responseString = createOkHttpClient(baseUrl.toString())
        val req = server.takeRequest()
        assertEquals(responseString, bodyFromDarkSky)
        server.shutdown()

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

//    @After
//    fun tearDown() {
//        server.shutdown()
//    }
}