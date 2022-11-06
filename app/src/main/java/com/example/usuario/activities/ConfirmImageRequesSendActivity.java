package com.example.usuario.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;

import com.example.usuario.R;
import com.example.usuario.adapters.OptionsPagerAdapter;
import com.example.usuario.utils.ShadowTransformer;

import java.util.ArrayList;

public class ConfirmImageRequesSendActivity extends AppCompatActivity {

    ViewPager mViewPager;
    ArrayList<String> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_image_reques_send);
        mViewPager = findViewById(R.id.viewPager2);
        data = getIntent().getStringArrayListExtra("data");


        OptionsPagerAdapter pagerAdapter = new OptionsPagerAdapter(
                getApplicationContext(),
                getSupportFragmentManager(),
                dpToPixels2(2, this),
                data
        );

        ShadowTransformer transformer = new ShadowTransformer(mViewPager, pagerAdapter);
        transformer.enableScaling(true);

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setPageTransformer(false, transformer);
    }

    public static float dpToPixels2(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }
}