package com.maliotis.petros.weather.weather

import android.os.Parcel
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.*

class Hour : Parcelable, Comparable<Hour?> {
    var time: Long = 0
    var summary: String? = null
    private var mTemperature = 0.0
    var iCon: String? = null
    var timezone: String? = null

    val temperature: Int
        get() = Math.round(mTemperature).toInt()

    fun setTemperature(temperature: Double) {
        mTemperature = temperature
        mTemperature = (mTemperature - 32) / 1.8
    }

    val iconId: Int
        get() = Forecast.getIconId(iCon)

    val hour: String
        get() {
            val formatter = SimpleDateFormat("H:mm")
            val date = Date(time * 1000)
            return formatter.format(date)
        }

    val dayOfTheWeekH: String
        get() {
            val formatter = SimpleDateFormat("EEEE")
            formatter.timeZone = TimeZone.getTimeZone(timezone)
            val dateTime = Date(time * 1000)
            return formatter.format(dateTime)
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(time)
        dest.writeString(summary)
        dest.writeDouble(mTemperature)
        dest.writeString(iCon)
        dest.writeString(timezone)
    }

    private constructor(`in`: Parcel) {
        time = `in`.readLong()
        summary = `in`.readString()
        mTemperature = `in`.readDouble()
        iCon = `in`.readString()
        timezone = `in`.readString()
    }

    constructor() {}

    override fun compareTo(other: Hour?): Int {
        return if (other != null)
            this.hour.compareTo(other.hour)
        else 0
    }

    companion object {
        val CREATOR: Parcelable.Creator<Hour> = object : Parcelable.Creator<Hour> {
            override fun createFromParcel(source: Parcel): Hour? {
                return Hour(source)
            }

            override fun newArray(size: Int): Array<Hour?> {
                return arrayOfNulls(size)
            }
        }
    }
}