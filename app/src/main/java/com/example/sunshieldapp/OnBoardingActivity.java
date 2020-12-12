package com.example.sunshieldapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

public class OnBoardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        final ViewFlipper flipper = findViewById(R.id.ViewFlipper);
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        flipper.setInAnimation(in);
        flipper.setOutAnimation(out);
        Button buttonNext = findViewById(R.id.buttonNext);
        Button buttonPrevs = findViewById(R.id.buttonPrevs);
        Button buttonMulai = findViewById(R.id.buttonMulai);
        buttonMulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flipper.getDisplayedChild()<2)
                {
                    flipper.showNext();
                }
            }
        });

        buttonPrevs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flipper.getDisplayedChild()>0)
                {
                    flipper.showPrevious();
                }
            }
        });
    }
}