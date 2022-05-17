package com.example.myapplication.voiceEditor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class BluetoothActions {
    public static BluetoothSocket blsocket = null;
    public static BluetoothDevice pairedBluetoothDevice = null;
    public BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;
    public static OutputStream taOut;

    public static void send2Bluetooth(int status) {
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

    public static void shutDown(){
        try {
            if (blsocket != null){ blsocket.close(); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dog_reaction(){
        if (blsocket != null && blsocket.isConnected()) {
            send2Bluetooth(48);
            System.out.println("1111: reaction!!!");
        }
    }


    //todo: what should i do with onRequestPermissionsResult

}
