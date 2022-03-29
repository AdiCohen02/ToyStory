package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;


public class settingsPage extends AppCompatActivity {

    private TextView time_text, volume_text;
    private SeekBar volume, time;
    private Button startBtn2;
    private String searchFor;
    private Button homeBtn;
    private Switch toyswitch;

    public static final String PREFERENCES = "preferences";
    public static final String DURIATION_VALUE = "duriation";
    public static final String VOLUME_VALUE = "volume";
    public static final String TOY_SWITCH = "switch";

    private Integer savedVolume, savedDuriation;
    private boolean savedswitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        startBtn2 = findViewById(R.id.startBtn2);
        homeBtn = findViewById(R.id.homeBtn);
        toyswitch = (Switch) findViewById(R.id.switchToyBtn);
        time = findViewById(R.id.time);
        time_text= findViewById(R.id.time_text);
        volume_text = findViewById(R.id.volume_text);
        volume = findViewById(R.id.volume);

        if (volume != null) {
            volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
                    saveData();
                }
            });
        }

        if (time != null) {
            time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
                    saveData();
                }
            });
        }

        loadData();
        updateViews();

        startBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
                Intent intent1 = new Intent(settingsPage.this, gamePage.class);
                intent1.putExtra("volume", volume.getProgress());
                intent1.putExtra("time", time.getProgress());
                intent1.putExtra("toyexist", toyswitch.getShowText());
                intent1.putExtra("search for", searchFor);
                startActivity(intent1);
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                startActivity(new Intent(settingsPage.this, homePage.class));
            }
        });
    }



    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VOLUME_VALUE, volume.getProgress());
        editor.putInt(DURIATION_VALUE, time.getProgress());
        editor.putBoolean(TOY_SWITCH, toyswitch.isChecked());
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        savedVolume = sharedPreferences.getInt(VOLUME_VALUE, 1);
        savedDuriation = sharedPreferences.getInt(DURIATION_VALUE, 1);
        savedswitch = sharedPreferences.getBoolean(TOY_SWITCH, false);
    }

    public void updateViews() {
        volume.setProgress(savedVolume);
        time.setProgress(savedDuriation);
        toyswitch.setChecked(savedswitch);
    }

    public int getVolume(){
        return volume.getProgress();
    }

    public int getDuriation(){
        return time.getProgress();
    }

    public boolean getToyExist(){
        return toyswitch.isChecked();
    }

}