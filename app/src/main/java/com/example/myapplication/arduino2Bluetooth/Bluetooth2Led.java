package com.example.myapplication.arduino2Bluetooth;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Bluetooth2Led extends Activity {
    TextView bluetoothstatus, bluetoothPaired;
    Button enableLedButton, btndisconnect, btnshut;
    BluetoothAdapter myBluetooth;
    boolean status;
    ArrayList<String> devicesList;
    ArrayList<BluetoothDevice> ListDevices;
    ArrayAdapter<String> adapter;
    InputStream taInput;
    OutputStream taOut;
    BluetoothDevice pairedBluetoothDevice = null;
    BluetoothSocket blsocket = null;
    ListView listt;
    private int BluetoothRequestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_tryments);

        bluetoothstatus = (TextView) findViewById(R.id.bluetooth_state);
        bluetoothPaired = (TextView) findViewById(R.id.bluetooth_paired);
        enableLedButton = (Button) findViewById(R.id.buttonlightup);
        btnshut = (Button) findViewById(R.id.buttonShut);
        btndisconnect = (Button) findViewById(R.id.buttondisconnect);
        listt = (ListView) findViewById(R.id.mylist);

        ListDevices = new ArrayList<BluetoothDevice>();
        devicesList = new ArrayList<String>();
//        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, R.id.txtlist,  devicesList);
        listt.setAdapter(adapter);

        btnshut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blsocket != null && blsocket.isConnected()) {
                    send2Bluetooth(13, 13);
                }
            }
        });

        enableLedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blsocket != null && blsocket.isConnected()) {
                    send2Bluetooth(44, 45);
                }
            }
        });

        btndisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blsocket != null && blsocket.isConnected()) {
                    try {
                        blsocket.close();
                        Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_LONG).show();
                        bluetoothPaired.setText("DISCONNECTED");
                        bluetoothPaired.setTextColor(getResources().getColor(R.color.purple_200));

                    } catch (IOException ioe) {
                        Log.e("app>", "Cannot close socket");
                        pairedBluetoothDevice = null;
                        Toast.makeText(getApplicationContext(), "Could not disconnect", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        listt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "item with address: " + devicesList.get(i) + " clicked", Toast.LENGTH_LONG).show();

                connect2LED(ListDevices.get(i));
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
//        client.connect();

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        status = myBluetooth.isEnabled();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("1111: no permission");
            checkPermission();
        }
        System.out.println("1111: has permission");
        myBluetooth.startDiscovery();
        if (status) {
            bluetoothstatus.setText("ENABLED");
            registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        } else {
            bluetoothstatus.setText("NOT READY");
        }
    }

    void connect2LED(BluetoothDevice device) {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                checkPermission();
            }
            blsocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            blsocket.connect();
            pairedBluetoothDevice = device;
            bluetoothPaired.setText("PAIRED: " + device.getName());
            bluetoothPaired.setTextColor(getResources().getColor(R.color.white));

            Toast.makeText(getApplicationContext(), "Device paired successfully!", Toast.LENGTH_LONG).show();
        } catch (IOException ioe) {
            Log.e("taha>", "cannot connect to device :( " + ioe);
            Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_LONG).show();
            pairedBluetoothDevice = null;
        }
    }

    void send2Bluetooth(int led, int brightness) {
        //make sure there is a paired device
        if (pairedBluetoothDevice != null && blsocket != null) {
            try {
                taOut = blsocket.getOutputStream();
                taOut.write(led + brightness);

                taOut.flush();
            } catch (IOException ioe) {
                Log.e("app>", "Could not open a output stream " + ioe);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {

            Log.i("app>", "broadcast received");
            String action = intent.getAction();


            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                devicesList.add(device.getName() + " @" + device.getAddress());
                ListDevices.add(device);

                adapter.notifyDataSetChanged();
            }
        }
    };


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            System.out.println("1111: checking");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BluetoothRequestCode);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, BluetoothRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("1111: got permission results");
        if (requestCode == BluetoothRequestCode && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

}
