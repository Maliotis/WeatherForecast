package com.maliotis.petros.stormy;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.maliotis.petros.stormy.weather.Day;
import com.maliotis.petros.stormy.weather.Hour;

import java.util.Arrays;

public class HourlyForecastActivity extends AppCompatActivity {

    private Hour[] mhours;
    private Day[]  mDays;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.HOURLY_FORECAST);
        mhours = Arrays.copyOf(parcelables, parcelables.length, Hour[].class);

        Parcelable[] parcelables1 = intent.getParcelableArrayExtra(MainActivity.HOURLY_DAY_FORECAST);
        mDays = Arrays.copyOf(parcelables1,parcelables1.length,Day[].class);
        HourAdapter hourAdapter = new HourAdapter(this,mhours,mDays);
        recyclerView.setAdapter(hourAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);
    }
}
