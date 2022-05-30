package com.example.myapplication.arduino2Bluetooth;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.voiceRecognition;
import com.example.myapplication.homePage;
import com.example.myapplication.voiceEditor.BluetoothActions;
import com.example.myapplication.voiceEditor.safRecognition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class SettingsAndBluetooth extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 88;
    Button enableLedButton;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> devicesStrings;
    ArrayList<BluetoothDevice> ListDevices;
    ArrayAdapter<String> adapter;
    BluetoothDevice pairedBluetoothDevice = null;
    BluetoothSocket blsocket = null;
    ListView listt;
    private int MY_PERMISSIONS_REQUEST_CODE = 123;
    private Button startBtn2;

    public boolean rememberMyChoice;
    public static final String PREFERENCES = "preferences";
    String[] options = {"זיהוי דיבור","זיהוי סף"};

    public static final String DEFAULT_VALUE = "true";
    public static final String CHOSE_DEFAULT = "false";
    public static boolean defaultSaf = true;
    public static boolean askedDefault = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_and_bluetooth);

        enableLedButton = (Button) findViewById(R.id.buttonlightup);
        listt = (ListView) findViewById(R.id.mylist);
        startBtn2 = findViewById(R.id.startBtn2);

        ListDevices = new ArrayList<BluetoothDevice>();
        devicesStrings = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, R.id.txtlist, devicesStrings);
        listt.setAdapter(adapter);


        startBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // starting default activity when pressing "התחל"
                if (!askedDefault ){ startDefaultDialog();}
                else{
                    if (defaultSaf) { startActivity(new Intent(SettingsAndBluetooth.this, safRecognition.class)); }
                    else { startActivity(new Intent(SettingsAndBluetooth.this, voiceRecognition.class)); }
                }
            }
        });

        enableLedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BluetoothActions.dog_reaction()){
                    Toast.makeText(getApplicationContext(), "problem with bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        listt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // adding paired devices to list
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "item with address: " + devicesStrings.get(i) + " clicked", Toast.LENGTH_SHORT).show();
                try {
                    //todo: connect object
                    System.out.println("1111: trying connect to led " + ListDevices.get(i).getName());
                    connect2LED(ListDevices.get(i), i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startDefaultDialog() {
        // dialog for staring saf or voice recognition according to the user choice
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsAndBluetooth.this);
        alertDialog.setTitle("הגדר מסך ברירת מחדל");
        int checkedItem = 1;
        alertDialog.setNegativeButton("הגדר",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        startDefaultActivity(defaultSaf);
                        askedDefault = true;
                        Toast.makeText(SettingsAndBluetooth.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        alertDialog.setSingleChoiceItems(options, checkedItem, new DialogInterface.OnClickListener() {
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
        // staring saf or voice recognition according to the user choice
        if (!shouldStartSaf){
            startActivity(new Intent(this, voiceRecognition.class));
        } else {
            startActivity(new Intent(this, safRecognition.class)); }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("1111: Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + " Build.VERSION_CODES.M: " + Build.VERSION_CODES.M);
        // the application needs a specific SDK version, this if is here for debuging.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            System.out.println("1111: good app version");
        } else {
            System.out.println("1111: bad app version");
        }
        if (!hasPermissions()) {
            my_request();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            bluetoothManager = getSystemService(BluetoothManager.class);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                System.out.println("1111: Device doesn't support Bluetooth");
                onStop();
            }
            if (!bluetoothAdapter.isEnabled()) {
                //bluetoothstatus.setText("NOT ENABLED");
                //textView.setText("אין מכשירים זמינים");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                queryPairedDevices();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // finished requesting bluetooth permissions.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            //bluetoothstatus.setText("ENABLED");
            queryPairedDevices();
        }
        if (requestCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "כדי להתחבר לבובה יש צורך בבלוטוס.", Toast.LENGTH_LONG).show();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void defaultActivity(boolean rememberMyChoice, int whichActivity) {
        // setting saf recognition or voice recognition as default.
        if (whichActivity ==0){
            startActivity(new Intent(this, voiceRecognition.class));
        } else {
            startActivity(new Intent(this, safRecognition.class)); }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        //savedswitch = sharedPreferences.getBoolean(TOY_SWITCH, false);
    }

    @SuppressLint("MissingPermission")
    private void queryPairedDevices(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                devicesStrings.add(deviceName);
                ListDevices.add(device);
                }
            }
    }

    @SuppressLint("MissingPermission")
    void connect2LED(BluetoothDevice device, int index) throws IOException {
        // connecting to the device if the permissions are suitable.
        if (!hasPermissions()) {
            my_request(); //requesting premmision if they dont exist
        }
        if (BluetoothActions.connect2device(device)){
            Toast.makeText(getApplicationContext(), "Device connected successfully!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "couldn't connect: " + device.getName(), Toast.LENGTH_LONG).show();
        }
    }


    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DEFAULT_VALUE, defaultSaf);
        editor.putBoolean(CHOSE_DEFAULT, true);
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothActions.shutDown();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            System.out.println("1111: start broadcast");

            Log.i("app>", "broadcast received");
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                devicesStrings.add(device.getName() + " @" + device.getAddress());
                System.out.println("1111: device" + device.getName() + " @" + device.getAddress());
                ListDevices.add(device);
                adapter.notifyDataSetChanged();
            }
        }
    };

    private boolean hasPermissions() {
        int result_b = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        int result_s = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        int result_a = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE);
        return result_b == PackageManager.PERMISSION_GRANTED && result_s == PackageManager.PERMISSION_GRANTED && result_a == PackageManager.PERMISSION_GRANTED;
    }

    private void my_request() {
        String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE};
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_CODE);
        System.out.println("1111: asking for perm");
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CODE){
            // When request is cancelled, the results array are empty
            System.out.println("1111: SCAN :" + (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED));
            System.out.println("1111: BLUETOOTH :" + (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED));
//            System.out.println("1111: grantResults[1] :" + (grantResults[1] == PackageManager.PERMISSION_GRANTED));
            if((grantResults.length >0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                // Permissions are granted
                System.out.println("1111: Permission Granted");
            }else {
                // Permissions are denied
                System.out.println("1111: Permission denied");
            }
            return;
            }
        else{
            System.out.println("1111: default :( ");
        }
    }

    // -------------------------------------------------- menu: --------------------------------------------

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
                onStop();
                return true;
            case R.id.nav_voice_recognition:
                Toast.makeText(this, "עובר לזיהוי דיבור", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, voiceRecognition.class));
            default:
                return true;
        }
    }


    private void info() {
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(SettingsAndBluetooth.this);
        alertDialog.setTitle("הגדרות בלוטוס");
        alertDialog.setMessage("לבהתחברות לבובה, בצע התחברות למכשיר HC_05 בהגדרות ולאחר מכן באפליקציה. תוכל ללחוץ על הכפתור 'לחץ לבדיקת החיבור לבובה' כדי לוודא חיבור לבובה." );
        alertDialog.setNegativeButton("OK",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(SettingsAndBluetooth.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        androidx.appcompat.app.AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
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

}


