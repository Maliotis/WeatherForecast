package com.maliotis.petros.weather;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.maliotis.petros.weather.animations.FadeAnimation;
import com.maliotis.petros.weather.weather.Current;
import com.maliotis.petros.weather.weather.Day;
import com.maliotis.petros.weather.weather.Forecast;
import com.maliotis.petros.weather.weather.Hour;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    public static final String HOURLY_DAY_FORECAST = "HOURLY_DAY_FORECAST";
    private Forecast mForecast;

    TextView mLocationLabel;
    TextView mTemperatureLabel;
    TextView mHumidityValue;
    TextView mPrecipValue;
    TextView mSummaryLabel;
    ImageView mIconImageView;
    ConstraintLayout mLayout;
    LocationManager locationManager;
    Button dailyButton;
    Button hourlyButton;
    double mLatitude;
    Location mLocation;
    double mLongitude;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Geocoder mGeocoder;
    List<Address> addresses;
    String name;
    String code;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTemperatureLabel = (TextView) findViewById(R.id.temperatureLabel);
        mHumidityValue = (TextView) findViewById(R.id.humidityValue);
        mPrecipValue = (TextView) findViewById(R.id.precipValue);
        mSummaryLabel = (TextView) findViewById(R.id.summaryLabel);
        mIconImageView = (ImageView) findViewById(R.id.iconImageView);
        mLayout = (ConstraintLayout) findViewById(R.id.ConstraintLayout);
        mLocationLabel = (TextView) findViewById(R.id.locationLabel);
        dailyButton = (Button) findViewById(R.id.dailyButton);
        hourlyButton = (Button) findViewById(R.id.hourlyButton);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mGeocoder = new Geocoder(this);

        mSwipeRefreshLayout.setProgressViewOffset(false,-150,5);
        GPS();

        dailyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DailyForecastActivity.class);
                intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
                intent.putExtra("timezone", mForecast.getCurrent().getTimeZone());
                startActivity(intent);
            }
        });

        hourlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HourlyForecastActivity.class);
                intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
                intent.putExtra(HOURLY_DAY_FORECAST,mForecast.getDailyForecast());
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GPS();
                getForecast(mLatitude,mLongitude);
                geoLocation();
            }
        });

        getForecast(mLatitude,mLongitude);

        geoLocation();
    }//onCreate!!

    private void geoLocation() {
        try {
            addresses =  mGeocoder.getFromLocation(mLatitude,mLongitude,4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0;i<addresses.size();i++) {
            name = addresses.get(0).getLocality();
            code = addresses.get(0).getCountryName();
            Log.d("COUNTRY",name);
        }
    }

    private void getForecast(double latitude, double longitude) {
        String ApiKey = "a69a377194dd481e775182e1702fa70b";
        if (latitude == 0 && longitude == 0) {
            latitude = 37.9667;//37.8267;
            longitude = -122.4233;
        }
        String ForecastUrl = "https://api.darksky.net/forecast/" + ApiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(ForecastUrl).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log for on Failure
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log for on response
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetailes(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                    animate();
                                    stopRefresh();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Exception cuaght: ", e);
                    }
                }
            });
        } else {
            alertUserAboutNetwork();
        }
    }

    private void stopRefresh(){
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        },500);

    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();

        double humidity =  current.getHumidity()*100;
        int humidityI = (int) humidity;

        mTemperatureLabel.setText(current.getTemperature() + "");
        mHumidityValue.setText(humidityI+"%");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());
        mLocationLabel.setText(current.getTimeZone());
        mLocationLabel.setText(code+"/"+name);

        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private Forecast parseForecastDetailes(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();
        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();
            day.setSummary(jsonDay.getString("summary"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureHigh"));
            day.setICon(jsonDay.getString("icon"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");
        Hour[] hours = new Hour[data.length()];

        for (int i = 0; i < data.length(); i++) {
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

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "JSON : " + timezone);
        JSONObject currently = forecast.getJSONObject("currently");
        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.d(TAG, current.getFormatedTime());
        return current;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
        }
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private void alertUserAboutNetwork() {
        NetworkAlertDialog dialog = new NetworkAlertDialog();
        dialog.show(getFragmentManager(),"AlertAboutNetwork");
    }

    private void GPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTAG", "Something went wrong");

        } else {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, this);

                if(isNetworkAvailable()) {
                    mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    mLatitude = mLocation.getLatitude();
                    mLongitude = mLocation.getLongitude();
                }
            }
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void animate() {

        FadeAnimation fadeAnimation = new FadeAnimation();
        fadeAnimation.add(hourlyButton);
        fadeAnimation.add(dailyButton);
        fadeAnimation.add(mTemperatureLabel);
        fadeAnimation.add(mPrecipValue);
        fadeAnimation.add(mHumidityValue);
        fadeAnimation.add(mIconImageView);
        fadeAnimation.add(mLocationLabel);
        fadeAnimation.add(mSummaryLabel);
        fadeAnimation.add(findViewById(R.id.degreeImageView));
        fadeAnimation.add(findViewById(R.id.humidityLabel));
        fadeAnimation.add(findViewById(R.id.precipLabel));
        AnimatorSet set = fadeAnimation.getAnimatorSet();
        set.start();
    }


}
