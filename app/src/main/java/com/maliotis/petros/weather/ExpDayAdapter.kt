package com.maliotis.petros.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.maliotis.petros.weather.weather.Day
import com.maliotis.petros.weather.weather.Hour

class ExpDayAdapter(private val mContext: Context, private val mDays: Array<Day?>, private val mHours: Array<Array<Hour?>>) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return mDays.size - 1
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 8
    }

    override fun getGroup(groupPosition: Int): Day? {
        return mDays[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any? {
        return mHours[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return 0
        //return groupPosition;
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return 0
        //return childPosition;
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convView: View?, parent: ViewGroup): View {
        var convertView = convView
        val viewHolderG: ViewHolderG
        if (convertView == null) {
            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null)
            viewHolderG = ViewHolderG()
            viewHolderG.iconImageView = convertView.findViewById<View>(R.id.iconImageView) as ImageView
            viewHolderG.temperatureLabel = convertView.findViewById<View>(R.id.temperatureLabel) as TextView
            viewHolderG.minTempLabel = convertView.findViewById(R.id.minTempLabel)
            viewHolderG.dayLabel = convertView.findViewById<View>(R.id.dayNameLabel) as TextView
            convertView.tag = viewHolderG
        } else {
            viewHolderG = convertView.tag as ViewHolderG
        }
        val day = mDays[groupPosition]
        day?.iconId?.run {
            viewHolderG.iconImageView!!.setImageResource(this)
        }

        val temp = day?.temperatureMax.toString() + "°C"
        val minTemp = day?.temperatureMin.toString() + "°C"
        viewHolderG.temperatureLabel!!.text = temp
        viewHolderG.minTempLabel!!.text = minTemp
        if (groupPosition == 0) {
            viewHolderG.dayLabel!!.setText(R.string.Today)
        } else {
            viewHolderG.dayLabel!!.text = day?.dayOfTheWeek
        }
        return convertView!!
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convView: View?, parent: ViewGroup): View {
        var convertView = convView
        val holderChild: ViewHolderChild
        if (convertView == null) {
            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.hourly_list_item, null)
            holderChild = ViewHolderChild()
            holderChild.mTimeLabel = convertView.findViewById<View>(R.id.timeLabel) as TextView
            holderChild.mSummaryLabel = convertView.findViewById<View>(R.id.summaryLabel) as TextView
            holderChild.mTemperatureLabel = convertView.findViewById<View>(R.id.temperatureLabel) as TextView
            holderChild.mIconImageView = convertView.findViewById<View>(R.id.iconImageView) as ImageView
            holderChild.mHourDay = convertView.findViewById<View>(R.id.hourDayLabel) as TextView
            holderChild.mDegree = convertView.findViewById<View>(R.id.degreeLabel) as TextView
            convertView.tag = holderChild
        } else {
            holderChild = convertView.tag as ViewHolderChild
        }
        val hours = mHours[groupPosition][childPosition]
        holderChild.mTimeLabel!!.text = hours?.hour
        holderChild.mSummaryLabel!!.text = hours?.summary
        holderChild.mTemperatureLabel!!.text = hours?.temperature.toString() + ""
        hours?.iconId?.run {
            holderChild.mIconImageView!!.setImageResource(this)
        }
        holderChild.mHourDay!!.text = ""
        holderChild.mDegree!!.text = "o"
        return convertView!!
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    private class ViewHolderG {
        var iconImageView: ImageView? = null
        var temperatureLabel: TextView? = null
        var dayLabel: TextView? = null
        var minTempLabel: TextView? = null
    }

    private class ViewHolderChild {
        var mTimeLabel: TextView? = null
        var mSummaryLabel: TextView? = null
        var mTemperatureLabel: TextView? = null
        var mIconImageView: ImageView? = null
        var mHourDay: TextView? = null
        var mDegree: TextView? = null
    }

}