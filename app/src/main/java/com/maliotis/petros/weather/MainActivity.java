package com.maliotis.petros.weather;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ExpandableListView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    double mLatitude;
    Location mLocation;
    double mLongitude;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Geocoder mGeocoder;
    List<Address> addresses;
    String name;
    String code;
    Handler mHandler;
    boolean atLeastOnce;
    Day[] mDays;
    int i;
    Hour[] mHours;
    Hour[] mHoursForToday;
    List<Hour[]> hourListForDays;
    NestedScrollView scrollView;
    CardView cardView;

    ExpandableListView mListView;
    Hour[][] hoursArray;
    Call[] call;
    boolean[] finished;
    boolean finallyComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start of variable declaration//
        cardView = findViewById(R.id.cardView);
        mTemperatureLabel = findViewById(R.id.temperatureLabel);
        mHumidityValue = findViewById(R.id.humidityValue);
        mPrecipValue = findViewById(R.id.precipValue);
        mSummaryLabel = findViewById(R.id.summaryLabel);
        mIconImageView = findViewById(R.id.iconImageView);
        mLayout = findViewById(R.id.ConstraintLayout);
        mLocationLabel = findViewById(R.id.locationLabel);
        mSwipeRefreshLayout = findViewById(R.id.refreshLayout);
        mListView = findViewById(android.R.id.list);
        mGeocoder = new Geocoder(this);
        mHandler = new Handler();
        hourListForDays = new ArrayList<>();
        addresses = new ArrayList<>();
        mDays = new Day[7];
        mHours = new Hour[48];
        mHoursForToday = new Hour[48];
        scrollView = findViewById(R.id.scroll);
        //End of variable declaration//

        mSwipeRefreshLayout.setProgressViewOffset(false, -150, 5);

        hoursArray = new Hour[8][24];
        finished = new boolean[8];
        call = new Call[8];

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ifSimplified(1);
            }
        });

        ifSimplified(0);

        mLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                ViewGroup.LayoutParams params = mListView.getLayoutParams();
                if (mListView.isGroupExpanded(i)) {
                    params.height = 1152;
                    mListView.setLayoutParams(params);
                    mListView.requestFocus();
                    mListView.clearFocus();
                } else {
                    params.height = 1540;
                    mListView.setLayoutParams(params);
                    mListView.requestFocus();
                    mListView.clearFocus();
                }
                return false;
            }
        });


    }//onCreate!!

    private void ifSimplified(int i) {
        if (isNetworkAvailable() && isGPSEnabled()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GPS();
                    getForecast(mLatitude, mLongitude);
                    //getForecastDayHour(mLatitude,mLongitude);
                    geoLocation();
                    atLeastOnce = true;
                }
            }, 150);
        } else {
            checkForAlerts();
        }
        if (i == 1) {
            stopRefresh();
        }
    }

    private void geoLocation() {
        try {
            addresses = mGeocoder.getFromLocation(mLatitude, mLongitude, 4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            name = addresses.get(0).getLocality();
            code = addresses.get(0).getCountryName();
            Log.d("COUNTRY", name);
        }

        if ((name == null || name.equals("")) && (code == null || code.equals(""))) {
            name = "-";
            code = "-";
        }
    }

    private void getForecast(double latitude, double longitude) {
        String ApiKey = "a69a377194dd481e775182e1702fa70b";
        String locale = Locale.getDefault().getLanguage();
        if (latitude == 0 && longitude == 0) {
            latitude = 37.9667;//37.8267;
            longitude = -122.4233;
        }
        String ForecastUrl =
                "https://api.darksky.net/forecast/" + ApiKey + "/" + latitude + "," + longitude + "?" +"lang="+ locale;

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
                            alertUserAboutError();
                            //Log for on Failure
                        }
                    });

                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            mDays = mForecast.getDailyForecast();
                            mHoursForToday = mForecast.getHourlyForecast();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                    animate();
                                    //stopRefresh();
                                }
                            });
                            fetchDailyData();
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Exception cuaght: ", e);
                    }
                }
            });
        }

    }

    private void fetchDailyData() {
        for (i = 0; i < mDays.length; i++) {
            getForecastDayHour(mLatitude, mLongitude, mDays[i], i);
        }
    }

    public void getForecastDayHour(double latitude, double longitude, final Day days, final int i) {
        String ApiKey = "a69a377194dd481e775182e1702fa70b";
        String locale = Locale.getDefault().getLanguage();
        if (latitude == 0 && longitude == 0) {
            latitude = 37.9667;//37.8267;
            longitude = -122.4233;
        }
        String ForecastUrl =
                "https://api.darksky.net/forecast/" + ApiKey + "/" + latitude + "," + longitude + "," + days.getTime() + "?" + "lang=" + locale;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(ForecastUrl).build();
        call[i] = client.newCall(request);
        call[i].enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        mHours = getDayHourByHourForecast(jsonData);
                        Log.v("TAG", "it Worked");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDays[i].setDayHourByHour(mHours);
                                Log.v("TAG", mDays[i].getDayHourByHour().length + "");
                                Log.v("RUN", i + "");
                                finished[i] = true;
                                for (int j = 0; j < 8; j++) {
                                    if (finished[j]) {
                                        finallyComplete = true;
                                    } else {
                                        finallyComplete = false;
                                        break;
                                    }
                                }
                                if (finallyComplete) {
                                    fillHoursArray();
                                    listAdapter();
                                }
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    Log.e("Days", "Exception caught : ", e);
                }
            }
        });
    }


    private Hour[] getDayHourByHourForecast(String jsonData) throws JSONException {
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

    public void fillHoursArray() {
        int g;
        for (int i = 0; i < mDays.length; i++) {
            g = 0;
            for (int j = 0; j < mDays[i].getDayHourByHour().length; j = j + 3) {
                Hour hour;
                hour = mDays[i].getDayHourByHour()[j];
                hoursArray[i][g] = hour;
                g++;
            }
        }
        //reset array for completed days got from server
        for (int i = 0; i < 8; i++) {
            finished[i] = false;
        }
        finallyComplete = false;
    }

    private void listAdapter() {
        ExpDayAdapter expDayAdapter = new ExpDayAdapter(MainActivity.this, mDays, hoursArray);
        mListView.setAdapter(expDayAdapter);
    }

    private void stopRefresh() {
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);

    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();

        double humidity = current.getHumidity() * 100;
        int humidityI = (int) humidity;

        mTemperatureLabel.setText(current.getTemperature() + "");
        mHumidityValue.setText(humidityI + "%");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());
        mLocationLabel.setText(current.getTimeZone());
        mLocationLabel.setText(name + "," + code);

        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
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
            day.setTemperatureMin(jsonDay.getDouble("temperatureLow"));

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

    public boolean isNetworkAvailable() {
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

    public void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    public void alertUserAboutNetwork() {
        NetworkAlertDialog dialog = new NetworkAlertDialog();
        dialog.show(getFragmentManager(), "AlertAboutNetwork");
    }

    public void alertUserAboutGPS() {
        GPSAlertDialog dialog = new GPSAlertDialog();
        dialog.show(getFragmentManager(), "AlertUSerAboutGPS");
    }

    public void alertUserAboutBoth() {
        AlertDialogNetGPS dialod = new AlertDialogNetGPS();
        dialod.show(getFragmentManager(), "AlertUserAboutNetGPS");
    }

    public void informUser() {
        InformUserToGetData dialog = new InformUserToGetData();
        dialog.show(getFragmentManager(), "informUser");
    }

    private void GPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTAG", "Something went wrong");

        } else {
            //mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            mLocation = getLastLocation();
            if (mLocation == null) {
                Log.v("Location", "Location was null");
            } else {
                mLatitude = mLocation.getLatitude();
                mLongitude = mLocation.getLongitude();
            }
        }
    }


    public void checkForAlerts() {
        if (!isNetworkAvailable() && isGPSEnabled()) {
            alertUserAboutNetwork();
        } else if (!isGPSEnabled() && isNetworkAvailable()) {
            alertUserAboutGPS();
        } else {
            alertUserAboutBoth();
        }
    }

    private Location getLastLocation() {
        Location bLocation = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTAG", "Something went wrong");

        } else {
            List<String> providers = locationManager.getProviders(true);
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bLocation == null || l.getAccuracy() < bLocation.getAccuracy()) {
                    bLocation = l;
                }
            }

        }

        return bLocation;
    }

    public boolean isGPSEnabled() {
        boolean tf;
        boolean ft;
        boolean ret = true;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        tf = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        ft = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!tf && !ft && (locationProviders == null || locationProviders.equals(""))) {
            ret = false;
        }

        return ret;

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void animate() {
        FadeAnimation fadeAnimation = new FadeAnimation();
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
