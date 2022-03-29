package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class helper extends AppCompatActivity {

    private EditText name;
    private Button start_recording, stop_recording, play_recording, submit_recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        name = findViewById(R.id.name);
        start_recording = findViewById(R.id.start_recording);
        stop_recording = findViewById(R.id.stop_recording);
        play_recording = findViewById(R.id.play_recording);
        submit_recording = findViewById(R.id.submit_recording);



        submit_recording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(helper.this, settingsPage.class);
            }
        });
    }
}