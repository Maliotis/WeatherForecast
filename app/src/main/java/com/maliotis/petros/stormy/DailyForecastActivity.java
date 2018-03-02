package com.maliotis.petros.stormy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.maliotis.petros.stormy.weather.Day;

import java.util.Arrays;

public class DailyForecastActivity extends AppCompatActivity {

    private Day[] mDays;
    public ListView mListView;
    public TextView mTextView;
    public TextView locationLabel;
    String timezone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        mListView = (ListView) findViewById(android.R.id.list);
        mTextView = (TextView) findViewById(android.R.id.empty);
        locationLabel = (TextView) findViewById(R.id.locationLabel);

        Intent intent  = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables , parcelables.length , Day[].class);

        timezone = intent.getStringExtra("timezone");
        locationLabel.setText(timezone);

        DayAdapter adapter = new DayAdapter(this, mDays);
        mListView.setAdapter(adapter);
        mListView.setEmptyView(mTextView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String dayOfTheWeek = mDays[position].getDayOfTheWeek();
                String conditions  = mDays[position].getSummary();
                String highTemp = mDays[position].getTemperatureMax()+"";
                String message = String.format("On %s high will be %s and  it will be %s",dayOfTheWeek,highTemp,conditions);
                Toast.makeText(DailyForecastActivity.this,message,Toast.LENGTH_LONG).show();
            }
        });

    }
}
