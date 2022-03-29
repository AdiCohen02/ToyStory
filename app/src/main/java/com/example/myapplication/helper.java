package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class helper extends AppCompatActivity {
    ImageButton playRecord;
    private Button getHomeBtn;
    private Button gameBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helper);

         playRecord = findViewById(R.id.playRecord);
         gameBtn=findViewById(R.id.gameBtn);
         getHomeBtn=findViewById(R.id.getBackHomeBtn_inHelper);
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.bark);
        playRecord.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                mp.start();
            }
        });

        getHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                startActivity(new Intent(helper.this, homePage.class));
            }
        });
        gameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                startActivity(new Intent(helper.this, gamePage.class));
            }
        });
    }
}