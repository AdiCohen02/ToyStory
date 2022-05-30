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

    public static SettingsAndBluetooth s = new SettingsAndBluetooth(); //todo: check memory leak
    public static final String PREFERENCES = "preferences";
    String[] items = {"זיהוי דיבור","זיהוי סף"};

    public static final String DEFAULT_VALUE = "true";
    public static final String CHOSE_DEFAULT = "false";
    public static boolean defaultSaf = true;
    public static boolean askedDefault = false;


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
                if (!askedDefault ){ startDefaultDialog();}
                else{
                    if (defaultSaf) { startActivity(new Intent(homePage.this, safRecognition.class)); }
                    else { startActivity(new Intent(homePage.this, voiceRecognition.class)); }
                }
            }
        });
    }

    private void startDefaultDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(homePage.this);
        alertDialog.setTitle("הגדר ברירת מחדל");
        int checkedItem = 1;
        alertDialog.setNegativeButton("הגדר",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        startDefaultActivity(defaultSaf);
                        askedDefault = true;
                        Toast.makeText(homePage.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("1111: which activity" + which);
                defaultSaf = (which == 1);
                saveData();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }


    private void startDefaultActivity(boolean shouldStartSaf) {
        if (!shouldStartSaf){
            startActivity(new Intent(this, voiceRecognition.class));
        } else {
            System.out.println("1111: start safRecognition");
            startActivity(new Intent(this, safRecognition.class)); }
    }
    
    //todo: finish :
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DEFAULT_VALUE, defaultSaf);
        editor.putBoolean(CHOSE_DEFAULT, true);
        editor.apply();
    }

    // todo : decide
//    public void loadData() {
//        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
//        defaultSaf = sharedPreferences.getBoolean(DEFAULT_VALUE, true);
//        askedDefault = sharedPreferences.getBoolean(CHOSE_DEFAULT, false);
//    }


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
            case R.id.home:
                startActivity(new Intent(this, homePage.class));
            case R.id.nav_avg_sound:
                Toast.makeText(this, "עובר לזיהוי פשוט", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, safRecognition.class));
                return true;
            case R.id.nav_Bluetooth2Led:
                Toast.makeText(this, "התחברות לבלוטות'", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SettingsAndBluetooth.class));
                return true;
            case R.id.nav_info:
                info();
                return true;
            case R.id.disable_bluetooth:
                Toast.makeText(this, "התנתקות מהבלוטות'", Toast.LENGTH_SHORT).show();
                BluetoothActions.shutDown();
                return true;
            case R.id.nav_voice_recognition:
                Toast.makeText(this, "עובר לזיהוי דיבור", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, voiceRecognition.class));
            default:
                return true;
        }
    }


    private void info() {
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(homePage.this);
        alertDialog.setTitle("דף הבית");
        alertDialog.setMessage("משחק עם הבובה דורש בלוטוס.\n זיהוי פשוט - זיהוי צלילים וקולות פשוט, בהתאם לסף שיוגדר. \nזיהוי דיבור - זיהוי מילה אוטומטי של גוגל, דורש אינטרנט ומזהה אך ורק מילים.\nמשחק מהנה :)");

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



