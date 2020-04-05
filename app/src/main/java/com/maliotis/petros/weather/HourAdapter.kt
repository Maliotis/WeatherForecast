package com.maliotis.petros.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.maliotis.petros.weather.HourAdapter.HourViewHolder
import com.maliotis.petros.weather.weather.Day
import com.maliotis.petros.weather.weather.Hour

class HourAdapter(private val mContext: Context, private val mHours: Array<Hour>, private val mDays: Array<Day>) : RecyclerView.Adapter<HourViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hourly_list_item, parent, false)
        return HourViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        holder.bindHour(mHours[position])
    }

    override fun getItemCount(): Int {
        return mHours.size
    }

    inner class HourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var mTimeLabel: TextView
        var mSummaryLabel: TextView
        var mTemperatureLabel: TextView
        var mIconImageView: ImageView
        var mHourDay: TextView
        var mDegree: TextView
        fun bindHour(hour: Hour) {
            if (hour.hour == "0:00") {
                mHourDay.text = ""
                mTimeLabel.text = ""
                mSummaryLabel.text = ""
                mTemperatureLabel.text = ""
                mIconImageView.setImageResource(android.R.color.transparent)
                mHourDay.text = ""
                mDegree.text = ""
                //setting the day of the week.
                mHourDay.text = hour.dayOfTheWeekH
            } else {
                mHourDay.text = ""
                mDegree.text = "o"
                mTimeLabel.text = hour.hour
                mSummaryLabel.text = hour.summary
                mTemperatureLabel.text = hour.temperature.toString() + ""
                mIconImageView.setImageResource(hour.iconId)
            }
        }

        override fun onClick(v: View) {
            val time = mTimeLabel.text.toString()
            val temperature = mSummaryLabel.text.toString()
            val summary = mSummaryLabel.text.toString()
            val message = String.format("At %s it will be %s and %s", time, temperature, summary)
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
        }

        init {
            mTimeLabel = itemView.findViewById<View>(R.id.timeLabel) as TextView
            mSummaryLabel = itemView.findViewById<View>(R.id.summaryLabel) as TextView
            mTemperatureLabel = itemView.findViewById<View>(R.id.temperatureLabel) as TextView
            mIconImageView = itemView.findViewById<View>(R.id.iconImageView) as ImageView
            mHourDay = itemView.findViewById<View>(R.id.hourDayLabel) as TextView
            mDegree = itemView.findViewById<View>(R.id.degreeLabel) as TextView
            itemView.setOnClickListener(this)
        }
    }

}