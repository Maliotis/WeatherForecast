package com.maliotis.petros.weather

import android.widget.ExpandableListView
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.zipWith
import junit.framework.TestCase.assertEquals
import org.hamcrest.core.Is.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Created by petrosmaliotis on 04/04/2020.
 */
@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class KotlinInstrumentedTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testGpsAndNetwork() {
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

        gpsObservable.onNext(false)
        networkObservable.onNext(true)
    }



    @Test
    fun viewToTest() {

        val expandableListView = activityRule.activity.findViewById<ExpandableListView>(R.id.expandableList1)
        assertThat(expandableListView.count, `is`(0))
    }

}