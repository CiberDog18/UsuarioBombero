package com.example.usuario.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.usuario.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoBomberosActivity extends AppCompatActivity {

    private CircleImageView mCircleImageBack;
    String url = "https://goo.gl/maps/Z4FPiopaZa2hFXeX7";
    CircleImageView RedirecUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_bomberos);

        mCircleImageBack = findViewById(R.id.circleImageBack2);
        RedirecUrl = findViewById(R.id.circleImageMap);

        RedirecUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri _link = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW,_link);
                startActivity(intent);

            }
        });

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}