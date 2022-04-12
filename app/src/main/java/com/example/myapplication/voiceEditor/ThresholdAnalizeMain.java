package com.example.myapplication.voiceEditor;

import static com.example.myapplication.gamePage.RecordAudioRequestCode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;


public class ThresholdAnalizeMain extends AppCompatActivity {
    private static final String LOG_TAG = ThresholdAnalizeMain.class.getSimpleName();
    private RecordingThread mRecordingThread;
    private PlaybackThread mPlaybackThread;
    private static final int REQUEST_RECORD_AUDIO = 13;
    private Button recordBtn;
    float X_SEC = 4;
    float Y_SEC = 0.5F;
    float threshold; // SHOULD WORK WITH SUITABLE SETTINGS

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avg_sound_level);
        recordBtn = findViewById(R.id.btnStatusRecord);

        mRecordingThread = new RecordingThread(new AudioDataReceivedListener() {
            @Override
            public void onAudioDataReceived(short[] data) {
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRecordingThread.recording()) {
                    startAudioRecordingSafe();
                    recordBtn.setText("RECORDING");
                } else {
                    mRecordingThread.stopRecording();
                    recordBtn.setText("STOPPED");
                }
            }
        });

        }

    @Override
    protected void onStart(){
        super.onStart();
        startAudioRecordingSafe();
        try {
            short [] ret = getAudioSample();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            mainLoop();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//    protected void mainLoop() throws IOException {
//        // the main loop of the data analyst.
//        float x_duriation = X_SEC;
//        float y_duriation = Y_SEC;
//        while (shouldKeepGoing()) {
//            float x_avg = calcAvgSound(x_duriation);
//            float y_avg = calcAvgSound(y_duriation);
//            Boolean passedThreshold = passedThreshold(x_avg, y_avg, threshold);
//            if (passedThreshold){
//                reaction();
//            }
//        short [] ret = getAudioSample();
//        }


    protected void reaction(){
        // for now, just color the screen.
    }

    protected Boolean shouldKeepGoing(){
        // should return if we want to keep analaysing data, or not according the the user state.
        return true;
    }

    @Override
    protected void onStop(){
        super.onStop();
        mRecordingThread.stopRecording();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startAudioRecordingSafe() {
        // called on start.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            mRecordingThread.startRecording();
        } else {
            requestMicrophonePermission();
        }
    }

    private short[] getAudioSample() throws IOException {
        // should return an array of the samples
        Log.v(LOG_TAG, "IN GET AUDIO SAMPLE");
        InputStream is = getResources().openRawResource(R.raw.jinglebells);
        //todo: get audio sample.
        byte[] data;
        try {
            data = IOUtils.toByteArray(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        Log.v(LOG_TAG, String.valueOf(samples));
        return samples;
    }

//    private short[] getXsecondsRawData (short[] raw_data, float seconds){
//        // recieves the data and returns the raw data of the last X minutes.
//
//
//    }

//    private float calcAvgSound(float seconds) {
//        // todo: should have a thread
//        // return avarage of the raw_data of the last seconds.
//        // should use:
//        // getAudioSample() -> short[] ->
//        // getXsecondsRawData -> short[] ->
//        // after it, according to the seconds number and return the avarage volume in it.
//
//    }

//    private Boolean passedThreshold(float x_seconds_avg, float y_seconds_avg, float threshold){
//        // x > y
//        // if y_seconds_avg/x_seconds_avg >= thershold - means we passed the needed threshold
//        // and we should return true, and the reaction will start. returns False otherwise.
//    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

    private void requestMicrophonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            // Show dialog explaining why we need record audio
            ActivityCompat.requestPermissions(ThresholdAnalizeMain.this, new String[]{
                            android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                }
        else {
            ActivityCompat.requestPermissions(ThresholdAnalizeMain.this, new String[]{
                    android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mRecordingThread.stopRecording();
        }
    }

}



