package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.cardview.widget.CardView;

import com.example.myapplication.arduino2Bluetooth.SettingsAndBluetooth;
import com.example.myapplication.voiceEditor.BluetoothActions;
import com.example.myapplication.voiceEditor.safRecognition;

public class homePage extends AppCompatActivity {

    public static SettingsAndBluetooth s = new SettingsAndBluetooth();
    public int firstTime =0, whichActivity;
    public boolean rememberMyChoice;
    public static final String PREFERENCES = "preferences";
    String[] items = {"זיהוי דיבור","זיהוי סף"};

    public static final String DEFAULT_VALUE = "saf";
    public static final String CHOSE_DEFAULT = "false";
    public static boolean onStart = true;
    public boolean defaultSaf = true;
    public boolean askedDefault = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        CardView playWith = (CardView)findViewById(R.id.cardPlayWith);
        CardView playWithout = (CardView)findViewById(R.id.cardPlayWithout);

        playWith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(homePage.this, "משחק עם הבובה", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(homePage.this, SettingsAndBluetooth.class));
            }
        });

        playWithout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onStart){ startDialog();}
                else{
                    if (defaultSaf) { startActivity(new Intent(homePage.this, safRecognition.class)); }
                    else { startActivity(new Intent(homePage.this, gamePage.class)); }
                }
            }
        });
    }

    private void startDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(homePage.this);
        alertDialog.setTitle("הגדר מסך ברירת מחדל");
        int checkedItem = 1;
        alertDialog.setNegativeButton("קבע מסך כברירת מחדל",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(homePage.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                whichActivity = which;
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }


    private void defaultActivity(int whichActivity) {
        //TODO: start the selected activity - DONE!
        //TODO: set the activity as default:)
        if (whichActivity ==0){
            startActivity(new Intent(this, gamePage.class));
        } else {
            startActivity(new Intent(this, safRecognition.class)); }

    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(SILENCE_SWITCH, environmentSwitch.isChecked());
        editor.apply();
    }

    public void updateViews() {
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
//        defaultSaf = sharedPreferences.getInt(DEFAULT_VALUE, "saf");
//        askedDefault = sharedPreferences.getBoolean(DE, false);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_avg_sound:
                Toast.makeText(this, "עובר לזיהוי פשוט", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, safRecognition.class));
                return true;
            case R.id.nav_Bluetooth2Led:
                Toast.makeText(this, "התחברות לבלוטות'", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SettingsAndBluetooth.class));
                return true;
            case R.id.nav_info:
                Toast.makeText(this, "הדרכה", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(homePage.this, info_activity.class));
                info();
                return true;
            case R.id.disable_bluetooth:
                Toast.makeText(this, "התנתקות מהבלוטות'", Toast.LENGTH_SHORT).show();
                BluetoothActions.shutDown();
                return true;
            case R.id.nav_voice_recognition:
                Toast.makeText(this, "עובר לזיהוי דיבור", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, gamePage.class));
            default:
                return true;
        }
    }


    private void info() {
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(homePage.this);
        alertDialog.setTitle("הדרכה לזיהוי פשוט: על מנת להשתמש בדף זה עלייך...");
        alertDialog.setNegativeButton("OK",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(homePage.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        androidx.appcompat.app.AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
}



