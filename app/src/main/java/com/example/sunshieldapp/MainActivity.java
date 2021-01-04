package com.example.sunshieldapp;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    public MediaPlayer mediaPlayer;
    private LocationManager locationManager;
    private LocationListener locationListener;
    public Location presentLocation;
    public static int time;

    //Tidak akan menanyakan kembali permission ketika sudah di acc sebelumnya
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                callGPS();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createNotificationChannel();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            callGPS();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            callAPI(Double.toString(presentLocation.getLatitude()), Double.toString(presentLocation.getLongitude()) ,this);
        }
        Date currentTime = Calendar.getInstance().getTime();
        String date = DateFormat.getDateInstance().format(currentTime);
        TextView tanggal = findViewById(R.id.teksTanggal);
        tanggal.setText(date);
        LoadTime();
        LinearLayout buttonIngatkan = findViewById(R.id.buttonIngatkan);
        buttonIngatkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ingatkan(v);
            }
        });
        Log.i("Test", UserClass.SHARED_PREFS);
        OnBoarding();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadTime();
    }

    //Memanggil GPS
    public void callGPS() {
        setContentView(R.layout.homescreen);
        locationManager = (LocationManager) this.getSystemService(getApplicationContext().LOCATION_SERVICE);
        locationListener = new LocationListener() {
            //Trigger yang akan berubah ketika lokasi berganti
            @Override
            public void onLocationChanged(Location location) {
                presentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            presentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    public void callAPI(String a, String b, final Activity activity) {
        //Initialization
        final TextView teksLokasi = activity.findViewById(R.id.teksLokasi);
        final TextView teksCuaca = activity.findViewById(R.id.teksPrediksi);
        final TextView teksSuhu = activity.findViewById(R.id.teksSuhu);
        final TextView teksUV = activity.findViewById(R.id.teksIndexUV);
        final String[] deskripsi = new String[1];
        final String[] suhu = new String[1];
        String uvUrl = "https://api.openuv.io/api/v1/uv?lat=" + a + "&lng=" + b;
        String urlGoogle = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + a + "," + b + "&key=[API KEY]"; //Insert googlemaps API key here
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat=" + a + "&" + "lon=" + b + "&exclude=minutely,hourly,alerts&lang=id&units=metric&appid=bf7fb2daf9967369f55ff97981b61a34";

        //CallOpenWeather for weather information
        new AsyncHttpClient().get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String stringJSON = new String(responseBody);
                try {
                    JSONObject reader = new JSONObject(stringJSON);
                    JSONObject current = reader.getJSONObject("current");
                    JSONArray cuacaArray = current.getJSONArray("weather");
                    final int cuacaHariIni = reader.getJSONArray("daily").getJSONObject(0).getJSONArray("weather").getJSONObject(0).getInt("id");
                    final int cuacaCurrent = cuacaArray.getJSONObject(0).getInt("id");
                    deskripsi[0] = cuacaArray.getJSONObject(0).getString("description");
                    suhu[0] = current.getString("temp");
                    teksCuaca.setText(deskripsi[0]);
                    teksSuhu.setText(suhu[0] + "°" + "C");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WeatherClass.setKodeCuacaForecast(cuacaHariIni, activity);
                            WeatherClass.setKodeCuacaCurrent(cuacaCurrent, activity);
                        }
                    });


                } catch (Exception e) {
                    Log.i("Terjadi Error", "Error");
                    e.printStackTrace();
                }
                Log.i("ResponseAPICuaca", stringJSON);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("ResponseAPICuaca", "Failure");
            }
        });

        //Call UVIndex API
        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(uvUrl)
                .get()
                .addHeader("x-access-token", "3ea48624ebec42f85fe979340611e64e")
                .build();

        Log.i("request", request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String uvResponse = response.body().string();
                try {
                    Log.i("uv", uvResponse);
                    JSONObject reader = new JSONObject(uvResponse);
                    JSONObject result = reader.getJSONObject("result");
                    final String uv = result.getString("uv");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            teksUV.setText(uv);
                            WeatherClass.setIndexUV(Double.parseDouble(uv),activity);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("uv", "kesalahan dalam pemanggilan API openUV");
                }

            }
        });

        //Call Geocoding API
        new AsyncHttpClient().get(urlGoogle, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String stringJSON = new String(responseBody);
                ArrayList<String> kota = new ArrayList<>();
                try {
                    JSONObject reader = new JSONObject(stringJSON);
                    JSONArray results = reader.getJSONArray("results");
                    JSONArray components = results.getJSONObject(0).getJSONArray("address_components");
                    Log.i("components", components.toString());
                    for (int i = 0; i < components.length(); i++) {
                        String selected = components.getJSONObject(i).getJSONArray("types").get(0).toString();
                        if (selected.equals("administrative_area_level_4") || selected.equals("administrative_area_level_2")) {
                            kota.add(components.getJSONObject(i).getString("long_name"));
                        } else {
                            Log.i("hehe", components.getJSONObject(i).getJSONArray("types").get(0).toString());
                        }
                    }

                } catch (Exception e) {
                    Log.i("Terjadi Error", "Error");
                    e.printStackTrace();
                }
                teksLokasi.setText(kota.get(0) + ", " + kota.get(1));
                //responseKota.setText(stringJSON);

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("APIkota", "fail");
            }
        });

    }

    public void pushNotification() {
        Toast.makeText(MainActivity.this, "Reminder Set", Toast.LENGTH_LONG).show();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long timeAtTrigger = System.currentTimeMillis();
        Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtTrigger, pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            CharSequence name = "SunShieldApp";
            String description = "Channel for SunShieldApp";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("SunShieldApp", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void Debug(View view) {
        Intent intent = new Intent(this, DebugActivity.class);
        startActivity(intent);
    }

    public void Ingatkan(final View view) {
        LinearLayout timerLayout = findViewById(R.id.LinearLayoutChronometer);
        timerLayout.setVisibility(View.VISIBLE);
        LinearLayout linearLayout = (LinearLayout) view;
        LoadTime();
        final int millisInFuture = time;
        int hours = millisInFuture / 3600000;
        int minutes = (millisInFuture % 3600000) / 60000;
        Log.i("hours", Integer.toString(hours));
        Log.i("minutes", Integer.toString(minutes));
        changeTimer(hours,minutes);
        final CountDownTimer timer = new CountDownTimer(millisInFuture, 60000) {
            void setText(long millisUntilFinished)
            {
                Log.i("Millis", Long.toString(millisUntilFinished));
                int hours = (int) (millisUntilFinished / 3600000);
                Log.i("Modulo", Long.toString(millisUntilFinished % 3600000));
                int minutes = (int) Math.round(millisUntilFinished % 3600000 / 60000.0);
                if (minutes >= 60) {
                    hours++;
                    minutes = 0;
                }
                changeTimer(hours,minutes);
            }

            @Override
            public void onTick(long millisUntilFinished) {
                setText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                setText(0);
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm);
                mediaPlayer.start();
                pushNotification();

            }

        }.start();
        linearLayout.setBackground(getDrawable(R.drawable.ic_hentikan));
        TextView textView = linearLayout.findViewById(R.id.teksIngatkan);
        textView.setText("Hentikan");
        LinearLayout buttonIngatkan = findViewById(R.id.buttonIngatkan);
        buttonIngatkan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Batalkan(v);
                timer.cancel();
            }
        });
    }

    public void Batalkan(View view)
    {
        LinearLayout timerLayout = findViewById(R.id.LinearLayoutChronometer);
        timerLayout.setVisibility(GONE);
        LinearLayout linearLayout = (LinearLayout) view;
        linearLayout.setBackground(getDrawable(R.drawable.ic_buttoningatkanorange));
        TextView textView = linearLayout.findViewById(R.id.teksIngatkan);
        textView.setText("Ingatkan");
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ingatkan(v);
            }
        });
    }

    public void changeTimer(int hours, int minutes)
    {
        final TextView chronometer = findViewById(R.id.chronometer);
        if (hours >= 10) {
            if (minutes < 10) {
                chronometer.setText(hours + ":" + "0" + minutes);
            } else {
                chronometer.setText(hours + ":" + minutes);
            }
        } else {
            if (minutes < 10) {
                chronometer.setText("0" + hours + ":" + "0" + minutes);
            } else {
                chronometer.setText("0" + hours + ":" + minutes);
            }

        }
    }

    public void LoadTime() {
        SharedPreferences sharedPreferences = getSharedPreferences(UserClass.SHARED_PREFS, MODE_PRIVATE);
        time = sharedPreferences.getInt(UserClass.TIME, 7200000);
    }

    public void Settings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void OnBoarding() {
        Intent intent = new Intent(this, OnBoardingActivity.class);
        startActivity(intent);
    }

    public void Diagnose(View view) {
        Intent intent = new Intent(this, DiagnoseActivity.class);
        startActivity(intent);
    }


}

