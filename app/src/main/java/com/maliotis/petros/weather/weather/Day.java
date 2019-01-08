package com.maliotis.petros.weather.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Day implements Parcelable{

    private long mTime;
    private String mSummary;
    private double mTemperatureMax;
    private double mTemperatureMin;
    private String mICon;
    private String mTimezone;
    private Hour[] mDayHourByHour;

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public int getTemperatureMax() {

        return (int) Math.round(mTemperatureMax);
    }

    public void setTemperatureMin(double temperatureMin) {
        mTemperatureMin = temperatureMin;
        mTemperatureMin = (mTemperatureMin - 32) / 1.8;
    }

    public void setTemperatureMax(double temperatureMax) {
        mTemperatureMax = temperatureMax;
        mTemperatureMax = (mTemperatureMax - 32) / 1.8;
    }

    public String getICon() {
        return mICon;
    }

    public void setICon(String ICon) {
        mICon = ICon;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }

    public int getIconId(){
        return Forecast.getIconId(mICon);
    }

    public String getDayOfTheWeek(){
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public Hour[] getDayHourByHour(){
        return mDayHourByHour;
    }

    public void setDayHourByHour(Hour[] dayHourByHour){
        mDayHourByHour = dayHourByHour;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTime);
        dest.writeString(mSummary);
        dest.writeDouble(mTemperatureMax);
        dest.writeString(mICon);
        dest.writeString(mTimezone);
    }

    private Day(Parcel in){
        mTime = in.readLong();
        mSummary = in.readString();
        mTemperatureMax = in.readDouble();
        mICon = in.readString();
        mTimezone = in.readString();
    }

    public Day(){

    }

    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel source) {
            return new Day(source);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };

    public int getTemperatureMin() {
        return (int) Math.round(mTemperatureMin);
    }
}
