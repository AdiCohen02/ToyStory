package com.example.myapplication.voiceEditor;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothActions {
    public static BluetoothSocket blsosket_action = null;
    public static BluetoothDevice pairedBluetoothDevice_action = null;
    public BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;

    public static OutputStream taOut;

    @SuppressLint("MissingPermission")
    public static boolean connect2device(BluetoothDevice device) throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        blsosket_action = device.createInsecureRfcommSocketToServiceRecord(uuid);

        System.out.println("1111: createInsecureRfcommSocketToServiceRecord");
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            blsosket_action.connect();
            pairedBluetoothDevice_action = device;
            return true;
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            System.out.println("1111: UNABLE::: " + connectException);
            //bluetoothPaired.setText("couldn't connect: " + device.getName());
            //listt.getChildAt(index).setBackgroundColor(Color.RED);
            try {
                blsosket_action.close();
            } catch (IOException closeException) {
                System.out.println("1111: Could not close the client socket" + closeException);
            }
            return false;
        }
    }

    public static void send2Bluetooth(int status) {
        System.out.println("1111: start send2Bluetooth");
        //make sure there is a paired device
        if (pairedBluetoothDevice_action != null && blsosket_action != null) {
            try {
                taOut = blsosket_action.getOutputStream();
                taOut.write(status);
                taOut.flush();
            } catch (IOException ioe) {
                Log.e("app>", "Could not open a output stream " + ioe);
                System.out.println("1111: Could not open a output stream");
            }
        }
    }

    public static void shutDown(){
        try {
            if (blsosket_action != null){ blsosket_action.close(); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean dog_reaction() {
        // returns true if the reaction started
        System.out.println("1111: in dog reaction");
        if (blsosket_action != null) {
            if (blsosket_action.isConnected()) {
                send2Bluetooth(49);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            send2Bluetooth(48);
            return true;
        }
        return false;
    }


    //todo: what should i do with onRequestPermissionsResult

}
