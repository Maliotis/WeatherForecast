package com.maliotis.petros.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maliotis.petros.weather.weather.Day;
import com.maliotis.petros.weather.weather.Hour;


public class ExpDayAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private Day[] mDays;
    private Hour[][] mHours;

    public ExpDayAdapter(Context context, Day[] days, Hour[][] hours){
        mContext = context;
        mDays = days;
        mHours = hours;
    }

    @Override
    public int getGroupCount() {
        return mDays.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 8;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mDays[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mHours[groupPosition][childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
        //return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
        //return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderG viewHolderG;

        if(convertView == null){
            //brand new

            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item,null);
            viewHolderG = new ViewHolderG();
            viewHolderG.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            viewHolderG.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            viewHolderG.dayLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);

            convertView.setTag(viewHolderG);
        }
        else{
            viewHolderG = (ViewHolderG) convertView.getTag();
        }

        Day day = mDays[groupPosition];
        viewHolderG.iconImageView.setImageResource(day.getIconId());
        String temp = day.getTemperatureMax()+"";
        viewHolderG.temperatureLabel.setText(temp);
        if(groupPosition == 0){
            viewHolderG.dayLabel.setText(R.string.Today);
        }
        else {
            viewHolderG.dayLabel.setText(day.getDayOfTheWeek());
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderChild holderChild;

        if(convertView == null){
            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.hourly_list_item,null);
            holderChild = new ViewHolderChild();
            holderChild.mTimeLabel = (TextView) convertView.findViewById(R.id.timeLabel);
            holderChild.mSummaryLabel = (TextView) convertView.findViewById(R.id.summaryLabel);
            holderChild.mTemperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holderChild.mIconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holderChild.mHourDay = (TextView) convertView.findViewById(R.id.hourDayLabel);
            holderChild.mDegree = (TextView) convertView.findViewById(R.id.degreeLabel);
            convertView.setTag(holderChild);

        }
        else {
            holderChild = (ViewHolderChild) convertView.getTag();
        }

        Hour hours = mHours[groupPosition][childPosition];
        holderChild.mTimeLabel.setText(hours.getHour());
        holderChild.mSummaryLabel.setText(hours.getSummary());
        holderChild.mTemperatureLabel.setText(hours.getTemperature()+"");
        holderChild.mIconImageView.setImageResource(hours.getIconId());
        holderChild.mHourDay.setText("");
        holderChild.mDegree.setText("o");


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private static class ViewHolderG{
        ImageView iconImageView;
        TextView temperatureLabel;
        TextView dayLabel;
    }

    private static class ViewHolderChild{

        TextView mTimeLabel;
        TextView mSummaryLabel;
        TextView mTemperatureLabel;
        ImageView mIconImageView;
        TextView mHourDay;
        TextView mDegree;
    }
}
