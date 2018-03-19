package com.maliotis.petros.weather;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.maliotis.petros.weather.weather.Day;
import com.maliotis.petros.weather.weather.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DailyForecastActivity extends AppCompatActivity {

    private Day[] mDays;
    public ExpandableListView mListView;
    public TextView mTextView;
    public TextView locationLabel;
    double mLongitude;
    double mLatitude;
    String timezone;
    String name;
    String code;
    Hour[] mHours;
    Hour[][] hoursArray;
    int i;
    Call call[];
    boolean finished[];
    boolean finallyComplete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        mListView = (ExpandableListView) findViewById(android.R.id.list);
        mTextView = (TextView) findViewById(android.R.id.empty);
        locationLabel = (TextView) findViewById(R.id.locationLabel);
        mHours = new Hour[24];
        hoursArray = new Hour[8][24];
        finished = new boolean[8];
        call = new Call[8];

        Intent intent  = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables , parcelables.length , Day[].class);

        timezone = intent.getStringExtra("timezone");
        name = intent.getStringExtra("name");
        code  = intent.getStringExtra("code");
        mLatitude = intent.getDoubleExtra("latitude",0);
        mLongitude = intent.getDoubleExtra("longitude",0);
        locationLabel.setText(name+","+code);


        for (i = 0; i < mDays.length; i++) {
            getForecastDayHour(mLatitude, mLongitude, mDays[i], i);
        }


//        DayAdapter adapter = new DayAdapter(this, mDays);
//        mListView.setAdapter(adapter);
//        mListView.setEmptyView(mTextView);
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                String dayOfTheWeek = mDays[position].getDayOfTheWeek();
//                String conditions  = mDays[position].getSummary();
//                String highTemp = mDays[position].getTemperatureMax()+"";
//                String message = String.format("On %s high will be %s and  it will be %s",dayOfTheWeek,highTemp,conditions);
//                Toast.makeText(DailyForecastActivity.this,message,Toast.LENGTH_LONG).show();
//            }
//        });

    }

    public void getForecastDayHour(double latitude, double longitude, final Day days,final int i){
        String ApiKey = "a69a377194dd481e775182e1702fa70b";
            if (latitude ==0 && longitude == 0){
                latitude = 37.9667;//37.8267;
                longitude = -122.4233;
            }
            String ForecastUrl = "https://api.darksky.net/forecast/" + ApiKey + "/" + latitude + "," + longitude + "," + days.getTime();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(ForecastUrl).build();
            call[i] = client.newCall(request);
            call[i].enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        String jsonData = response.body().string();
                        //Log.v("DAYS",jsonData);
                        if(response.isSuccessful()){
                            mHours = getDayHourByHourForecast(jsonData);
                            Log.v("TAG","it Worked");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mDays[i].setDayHourByHour(mHours);
                                    Log.v("TAG",mDays[i].getDayHourByHour().length+"");
                                    Log.v("RUN",i+"");
                                    finished[i] = true;
                                    for (int j =0 ; j < 8 ; j++){
                                        if(finished[j]){
                                            finallyComplete = true;
                                        }
                                        else{
                                            finallyComplete = false;
                                            break;
                                        }
                                    }
                                    if (finallyComplete){
                                        fillHoursArray();
                                        listAdapter();
                                    }
                                }
                            });
                        }
                    }
                    catch (IOException | JSONException e){
                        Log.e("Days","Exception caught : ", e);
                    }
                }
            });
    }


    private Hour[] getDayHourByHourForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");
        Hour[] hours = new Hour[data.length()];

        for (int i = 0; i < data.length(); i++){
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setICon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);

            hours[i] = hour;
        }

        return hours;
    }

    public void fillHoursArray(){
        int g;
        for(int i = 0 ; i < mDays.length ; i++){
            g=0;
            for(int j = 0 ; j < mDays[i].getDayHourByHour().length ; j=j+3){
                Hour hour ;
                hour = mDays[i].getDayHourByHour()[j];
                hoursArray[i][g] = hour;
                g++;
            }
        }
    }

    private void listAdapter() {
        ExpDayAdapter expDayAdapter = new ExpDayAdapter(DailyForecastActivity.this,mDays,hoursArray);
        mListView.setAdapter(expDayAdapter);
        mListView.setEmptyView(mTextView);
    }
}
