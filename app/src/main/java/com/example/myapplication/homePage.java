package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.arduino2Bluetooth.SettingsAndBluetooth;
import com.example.myapplication.voiceEditor.MainActivity;

public class homePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        Button startBtn = (Button)findViewById(R.id.btnStart);
        Button BluetoothBtn = (Button)findViewById(R.id.btnBluetooth);
        Button voiceBtn = (Button)findViewById(R.id.btnSimple);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(homePage.this, gamePage.class));
            }
        });

        BluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(homePage.this, SettingsAndBluetooth.class));
            }
        });

        voiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(homePage.this, MainActivity.class));
            }
        });

    }
}



