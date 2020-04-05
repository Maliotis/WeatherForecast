package com.maliotis.petros.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.maliotis.petros.weather.weather.Day

class DayAdapter internal constructor(private val mContext: Context, private val mDays: Array<Day>) : BaseAdapter() {
    override fun getCount(): Int {
        return mDays.size
    }

    override fun getItem(position: Int): Any {
        return mDays[position]
    }

    override fun getItemId(position: Int): Long {
        return 0 // We are not going to use this. Tag items for easy reference
    }

    override fun getView(position: Int, convView: View?, parent: ViewGroup): View {
        var convertView = convView
        val holder: ViewHolder
        if (convertView == null) {
            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null)
            holder = ViewHolder()
            holder.iconImageView = convertView.findViewById<View>(R.id.iconImageView) as ImageView
            holder.temmperatureLabel = convertView.findViewById<View>(R.id.temperatureLabel) as TextView
            holder.dayLabel = convertView.findViewById<View>(R.id.dayNameLabel) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val day = mDays[position]
        holder.iconImageView!!.setImageResource(day.iconId)
        val temp = day.temperatureMax.toString() + "°C"
        holder.temmperatureLabel!!.text = temp
        if (position == 0) {
            holder.dayLabel!!.setText(R.string.Today)
        } else {
            holder.dayLabel!!.text = day.dayOfTheWeek
        }
        return convertView!!
    }

    private class ViewHolder {
        var iconImageView: ImageView? = null
        var temmperatureLabel: TextView? = null
        var dayLabel: TextView? = null
    }

}