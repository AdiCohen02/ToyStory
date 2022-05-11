package com.example.myapplication.arduino2Bluetooth;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.gamePage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class SettingsAndBluetooth extends Activity {
    private static final int REQUEST_ENABLE_BT = 88;
    TextView bluetoothstatus, bluetoothPaired;
    Button enableLedButton, btnshut;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    boolean status;
    ArrayList<String> devicesList;
    ArrayList<BluetoothDevice> ListDevices;
    ArrayAdapter<String> adapter;
    InputStream taInput;
    OutputStream taOut;
    BluetoothDevice pairedBluetoothDevice = null;
    BluetoothSocket blsocket = null;
    ListView listt;
    private int MY_PERMISSIONS_REQUEST_CODE = 123;
    private Switch toyswitch;
    private Button startBtn2;

    public static final String PREFERENCES = "preferences";
    public static final String TOY_SWITCH = "switch";

    private boolean savedswitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_and_bluetooth);

        bluetoothstatus = (TextView) findViewById(R.id.bluetooth_state);
        bluetoothPaired = (TextView) findViewById(R.id.bluetooth_paired);
        enableLedButton = (Button) findViewById(R.id.buttonlightup);
        btnshut = (Button) findViewById(R.id.buttonShut);
        listt = (ListView) findViewById(R.id.mylist);

        ListDevices = new ArrayList<BluetoothDevice>();
        devicesList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, R.id.txtlist, devicesList);
        listt.setAdapter(adapter);

        toyswitch = (Switch) findViewById(R.id.switchToyBtn);
        startBtn2 = findViewById(R.id.startBtn2);

        loadData();
        updateViews();

        btnshut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blsocket != null && blsocket.isConnected()) {
                    send2Bluetooth(48);
                }
            }
        });

        startBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
                Intent intent1 = new Intent(SettingsAndBluetooth.this, gamePage.class);
                intent1.putExtra("toyexist", toyswitch.getShowText());
                startActivity(intent1);
            }
        });

        enableLedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blsocket != null && blsocket.isConnected()) {
                    send2Bluetooth(49);
                }
            }
        });


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        listt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("1111: start onItemClick");
                Toast.makeText(getApplicationContext(), "item with address: " + devicesList.get(i) + " clicked", Toast.LENGTH_LONG).show();
                try {
                    System.out.println("1111: trying connect to led " + ListDevices.get(i).getName());
                    connect2LED(ListDevices.get(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("1111: start");
        System.out.println("1111: Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + " Build.VERSION_CODES.M: " + Build.VERSION_CODES.M);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            System.out.println("1111: good version");
        } else {
            System.out.println("1111: bad version");
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
                bluetoothstatus.setText("NOT ENABLED");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                System.out.println("1111: requested");
                queryPairedDevices();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        System.out.println("1111: onActivityResult");
        if (requestCode == RESULT_OK){
            bluetoothstatus.setText("ENABLED");
            queryPairedDevices();
            System.out.println("1111: there is bluetooth");
        }
        if (requestCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "כדי להתחבר לבובה יש צורך בבלוטוס.", Toast.LENGTH_LONG).show();
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void queryPairedDevices(){
        System.out.println("1111: started query");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        System.out.println("1111: queryPairedDevices: num is " + pairedDevices.size());
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                devicesList.add(deviceName);
                System.out.println("1111: device" + deviceName + " @" + deviceHardwareAddress);
                ListDevices.add(device);
                }
            }
    }

    @SuppressLint("MissingPermission")
    void connect2LED(BluetoothDevice device) throws IOException {
        System.out.println("1111: start connect2LED");
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        if (!hasPermissions()) {
            my_request();
        }
        blsocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        System.out.println("1111: createInsecureRfcommSocketToServiceRecord");
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            blsocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            System.out.println("1111: Unable to connect; close the socket and return." + connectException);
            bluetoothPaired.setText("couldn't connect: " + device.getName());
            try {
                blsocket.close();
            } catch (IOException closeException) {
                System.out.println("1111: Could not close the client socket" + closeException);
            }
            return;
        }
        System.out.println("1111: connected");
        pairedBluetoothDevice = device;
        bluetoothPaired.setText("CONNECTED: " + device.getName());
        bluetoothPaired.setTextColor(getResources().getColor(R.color.purple_200));
        Toast.makeText(getApplicationContext(), "Device connected successfully!", Toast.LENGTH_LONG).show();
    }

    void send2Bluetooth(int status) {
        System.out.println("1111: start send2Bluetooth");
        //make sure there is a paired device
        if (pairedBluetoothDevice != null && blsocket != null) {
            try {
                taOut = blsocket.getOutputStream();
                taOut.write(status);
                taOut.flush();
                System.out.println("1111: flushed " + status);
            } catch (IOException ioe) {
                Log.e("app>", "Could not open a output stream " + ioe);
                System.out.println("1111: Could not open a output stream");
            }
        }
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(TOY_SWITCH, toyswitch.isChecked());
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        savedswitch = sharedPreferences.getBoolean(TOY_SWITCH, false);
    }

    public void updateViews() {
        toyswitch.setChecked(savedswitch);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (blsocket != null){ blsocket.close(); }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                devicesList.add(device.getName() + " @" + device.getAddress());
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

//    @Override
//    public void onReceive(Context context, Intent intent) {
//
//    }
}


