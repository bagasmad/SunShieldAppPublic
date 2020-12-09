package com.example.sunshieldapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    SeekBar seekBar;
    TextView chronometer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent intent = getIntent();
        Log.i("chronometer", "Hello");
        seekBar=findViewById(R.id.chronometerSeekbar);
        chronometer = findViewById(R.id.chronometerSettings);
        seekBar.setMax(24);
        seekBar.setMin(0);
        seekBar.setProgress(MainActivity.time/(60000*15));
        int hours = (MainActivity.time)/3600000;
        int minutes = ((MainActivity.time)%3600000)/60000;
        updateViews(hours,minutes);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saveTime(seekBar.getProgress()*60000*15);
                Log.i("Time", Integer.toString(seekBar.getProgress()*60000*15));
                int hours = (progress*60000*15)/3600000;
                int minutes = ((progress*60000*15)%3600000)/60000;
                updateViews(hours,minutes);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        seekBar.setProgress(MainActivity.time/(60000*15));
        int hours = (MainActivity.time)/3600000;
        int minutes = ((MainActivity.time)%3600000)/60000;
        updateViews(hours,minutes);
    }

    public void saveTime(int time)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(UserClass.SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(UserClass.TIME,time);
        editor.apply();
    }

    public void updateViews(int hours, int minutes)
    {
        if(hours>=10)
        {
            if(minutes<10)
            {
                chronometer.setText(hours+":"+"0"+minutes);
            }
            else
            {
                chronometer.setText(hours+":"+minutes);
            }
        }
        else
        {
            if(minutes<10)
            {
                chronometer.setText("0"+hours+":"+"0"+minutes);
            }
            else
            {
                chronometer.setText("0"+hours+":"+minutes);
            }

        }
    }
}