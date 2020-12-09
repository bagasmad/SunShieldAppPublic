package com.example.sunshieldapp;
import java.io.IOException;
import java.util.Calendar;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.ArrayList;
import java.util.Date;

public class DebugActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    public Location presentLocation;
    //Tidak akan menanyakan kembali permission ketika sudah di acc sebelumnya
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createNotificationChannel();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        Log.i("chronometer", "Hello");
        callGPS();
        //callAPI(Double.toString(presentLocation.getLatitude()), Double.toString(presentLocation.getLongitude()) ,this);
        Date currentTime = Calendar.getInstance().getTime();
        TextView jam = findViewById(R.id.textJam);
        jam.setText(currentTime.toString());
    }

    //Memanggil GPS
    public void callGPS()
    {
        setContentView(R.layout.activity_debug);
        locationManager = (LocationManager) this.getSystemService(getApplicationContext().LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                presentLocation = location;
                //textView.setText(presentLocation.getLatitude()+","+ presentLocation.getLongitude());
                //callAPI(Double.toString(presentLocation.getLatitude()), Double.toString(presentLocation.getLongitude()) ,textView);
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

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            presentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        //textView.setText(presentLocation.getLatitude()+","+ presentLocation.getLongitude());

    }



    public void callAPI(String a,String b, final Activity activity)
    {
        //Initialization
        final TextView responseKota = activity.findViewById(R.id.kotaResponse);
        final TextView textCuaca = activity.findViewById(R.id.textCuaca);
        final TextView textSuhu = activity.findViewById(R.id.textSuhu);
        final TextView textUv = activity.findViewById(R.id.uvResponse);
        final TextView textUvMax = activity.findViewById(R.id.uvMax);
        final TextView saran = activity.findViewById(R.id.saranResponse);
        final Double[] uvMax = new Double[1];
        final String[] deskripsi = new String[1];
        final String[] suhu = new String[1];
        final Double[] codeUV = new Double[1];
        String uvUrl = "https://api.openuv.io/api/v1/uv?lat="+a+"&lng="+b;
        String urlGoogle="https://maps.googleapis.com/maps/api/geocode/json?latlng="+ a +","+ b +"&key=AIzaSyBmSKL6T0nKvFck1pnjcDt6sSHFO79cP4M";
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat="+a+"&"+"lon="+b+"&exclude=minutely,hourly,daily,alerts&lang=id&units=metric&appid=84458e6a9721248d598d4f5e775694d9";

        //CallOpenWeather for weather information
        new AsyncHttpClient().get(url, new AsyncHttpResponseHandler() {@Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String stringJSON = new String(responseBody);
            try {
                JSONObject reader = new JSONObject(stringJSON);
                JSONObject current = reader.getJSONObject("current");
                JSONArray cuacaArray = current.getJSONArray("weather");
                deskripsi[0] = cuacaArray.getJSONObject(0).getString("description");
                suhu[0] = current.getString("temp");
                textCuaca.setText(deskripsi[0]);
                textSuhu.setText(suhu[0] + " celsius");


            }
            catch (Exception e)
            {
                Log.i("Terjadi Error","Error");
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

        Log.i("request",request.toString());
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
                    final String maxUV = result.getString("uv_max");
                    codeUV[0] = Double.parseDouble(uv);
                    uvMax[0]= Double.parseDouble(maxUV);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textUv.setText(uv);
                            textUvMax.setText(maxUV);
                            if(codeUV[0]<5)
                            {
                                saran.setText("Tidak perlu menggunakan tabir surya");
                            }
                            else if(codeUV[0]>=5&&codeUV[0]<6)
                            {
                                saran.setText("Resiko rendah terkena paparan UV, gunakan tabir surya atau kurangi kegiatan luar ruangan");
                            }
                            else if(codeUV[0]>=6&&codeUV[0]<=10)
                            {
                                saran.setText("Resiko sedang terkena paparan UV, gunakan tabir surya atau kurangi kegiatan luar ruangan");
                            }
                            else
                            {
                                saran.setText("Resiko tinggi terkena paparan UV, gunakan tabir surya atau tetap diam di tempat teduh");

                            }


                        }
                    });


                }
                catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("uv", "kesalahan dalam pemanggilan API openUV");
                }

            }
        });

        //Call Geocoding API
        new AsyncHttpClient().get(urlGoogle, new AsyncHttpResponseHandler() {@Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String stringJSON = new String(responseBody);
            ArrayList<String> kota = new ArrayList<>();
            try {
                JSONObject reader = new JSONObject(stringJSON);
                JSONArray results = reader.getJSONArray("results");
                JSONArray components = results.getJSONObject(0).getJSONArray("address_components");
                Log.i("components",components.toString());
                for (int i=0;i<components.length();i++)
                {
                    String selected = components.getJSONObject(i).getJSONArray("types").get(0).toString();
                    if(selected.equals("administrative_area_level_4")||selected.equals("administrative_area_level_2"))
                    {
                        kota.add(components.getJSONObject(i).getString("long_name"));
                    }
                    else
                    {
                        Log.i("hehe",components.getJSONObject(i).getJSONArray("types").get(0).toString());
                        //responseKota.setText("masi error");
//                            String kota = components.getJSONObject(i).getString("long_name");
//                            Log.i("kota",kota);
//                            responseKota.setText(kota);
                    }
                }

            }
            catch (Exception e)
            {
                Log.i("Terjadi Error","Error");
                e.printStackTrace();
            }
            responseKota.setText(kota.get(0)+", "+kota.get(1));
            //responseKota.setText(stringJSON);

        }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("APIkota","fail");
            }
        });

    }

    public void alarm(View view)
    {
        Toast.makeText(DebugActivity.this,"Reminder Set", Toast.LENGTH_LONG).show();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long timeAtButtonClick = System.currentTimeMillis();
        long secondsInMillis = 2*60*60*1000;
        Intent intent = new Intent(DebugActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(DebugActivity.this,0,intent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick+secondsInMillis, pendingIntent);
    }

    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE){
            CharSequence name = "RemindMe";
            String description = "Channel for Reminder Timer";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyMe", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void Homepage(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
