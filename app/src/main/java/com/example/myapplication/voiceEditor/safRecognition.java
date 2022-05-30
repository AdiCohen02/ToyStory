package com.example.myapplication.voiceEditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.R;
import com.example.myapplication.arduino2Bluetooth.SettingsAndBluetooth;
import com.example.myapplication.homePage;
import com.example.myapplication.voiceRecognition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class safRecognition extends AppCompatActivity {

    public double noiseLevel = 1000;
    public double koliutLevel = 5000;
    public int delayMillis = 10;
    public int DOG_ACTION_DURIATION = 2000; //should be in millis
    private MediaRecorder mRecorder;
    private Handler mHandler = new Handler();
    private MediaPlayer mp;


    private SeekBar childLevel;
    public static final String PREFERENCES = "preferences";
    public static final String VOLUME_VALUE = "volume";
    public static final String SILENCE_SWITCH = "switch";

    private Integer savedVolume;
    private boolean savedswitch;

    public List<Double> samples_list = new ArrayList<Double>();
    double s_avg = 0;


    private AudioRecord recorder;
    private int sampleRate = 16000; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;
    public Button btnStart, btnTresh;
    private boolean is_on = false;
    public boolean threadStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saf_recognition);

        Button btnTresh = (Button) findViewById(R.id.threshold);
        Button btnStart = (Button) findViewById(R.id.start_recording1);
        TextView currSeek = (TextView) findViewById(R.id.volume_curr_value);
        TextView explainThresh = (TextView) findViewById(R.id.check_yout_volume);
        ImageButton autoDogReaction = (ImageButton) findViewById(R.id.playRecord2);
        childLevel = findViewById(R.id.child_level);
        mp = MediaPlayer.create(this, R.raw.bark);

        autoDogReaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!BluetoothActions.dog_reaction() && is_on) {
                    Toast.makeText(safRecognition.this, "בלוטוס לא זמין, בדוק חיבור", Toast.LENGTH_SHORT).show();
                    mp.start();
                }
            }
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);


        btnStart.setText("לחץ להתחלת המשחק");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File file = new File(Environment.getExternalStorageDirectory() + "/Demo");
        if (!file.exists())
            file.mkdir();

        mRecorder.setOutputFile(getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/demoAudio.mp3");

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            mRecorder = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mRecorder = null;
        }

        Thread thread = new Thread(mPollTask);
        loadData();
        updateViews();
        childLevel.setRotation(180);
        currSeek.setText("העוצמה הנוכחית היא " + savedVolume * 5 + "dB");


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_on) {
                    koliutLevel = childLevel.getProgress() * 5;
                    saveData();
                    thread.start();
                    threadStarted = true;
                    btnStart.setText("מקשיב... לחץ כדי להפסיק.");
                    is_on = true;
                    btnTresh.setBackgroundColor(Color.WHITE);
                    explainThresh.setText("");

                }
                else {
                    is_on = false;
                    btnStart.setText("לחץ להתחלת המשחק");
                }
            }
        });


        btnTresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTresh.setText("בהקלטה: שהמטופל ישמיע צליל AAA");
                delayMillis = 3000;
                double threshLevel1 = getAmplitude();
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        double x = getAmplitude();
                        koliutLevel = 37 * Math.log10(x / 700);
                        if (koliutLevel < 5) {
                            koliutLevel = 5;
                        }
                        childLevel.setProgress(((int) koliutLevel / 5));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTresh.setText("העוצמה הותאמה, לחץ לניסיון חוזר");
                            }
                        });
                    }
                };
                timer.schedule(task, 3000);
                btnTresh.setText("דבר בעוצמה נמוכה");
            }
        });

        if (childLevel != null) {
            childLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                    int val = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
                    currSeek.setText(progress * 5 + "dB");
                    currSeek.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    currSeek.setY(280);
                    currSeek.setTextSize(28);
                    koliutLevel = progress * 5;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
                    is_on = false;
                    saveData();
                    loadData();
                }
            });
        }
    }


    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VOLUME_VALUE, childLevel.getProgress());
        editor.apply();
    }

    public void updateViews() {
        childLevel.setProgress(savedVolume);
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        savedVolume = sharedPreferences.getInt(VOLUME_VALUE, 1);
        savedswitch = sharedPreferences.getBoolean(SILENCE_SWITCH, false);
    }

    // todo: try using this method in case that microphone premmision isnt available
//    public static boolean isMicrophoneAvailable() {
//        System.out.println("1111: getSystemService " + Context.AUDIO_SERVICE);
//        AudioManager audioManager = (AudioManager) MyApp.getAppContext().getSystemService(Context.AUDIO_SERVICE);
//        System.out.println("1111: isMicrophoneAvailable: " + (audioManager.getMode() == MODE_NORMAL));
//        return audioManager.getMode() == MODE_NORMAL;
//    }

    public int getAmplitude() {
        // returns the max amplitude measured since the last time the function getMaxAmplitude was called.
        if (mRecorder != null) {
            int x = mRecorder.getMaxAmplitude();
            if (x > 0) { return x; }
        }
        return 0;
    }

    public double getAvarage(Integer sampleNum, List<Double> samples) {
        // calculating the avg on the audio
        double sum = 0;
        for (int i = 0; i < sampleNum; i++) {
            if (samples.get(i) > 0) {
                sum = sum + samples.get(i);
            }
        }
        return sum / sampleNum; // the list containd zero at half of the places, multiplying by 2 fixes this.
    }


    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = getAmplitude(); //called every 10 millis because of mHandler.postDelayed
            samples_list.add(amp);
            // todo: delete prints
            if (samples_list.size() > 20) { // getting avg of last 3 seconds
                System.out.println("1111: sample list: " + samples_list.size() +" "+ samples_list);
                s_avg = getAvarage(samples_list.size(), samples_list);
                samples_list.clear();
                System.out.println("1111: sample list:" + samples_list.size());
                s_avg = 37 * Math.log10(s_avg / 700);
                System.out.println("1111: avg in db:" + s_avg);
                System.out.println("1111: up " + s_avg + ">= 0.7 * " + koliutLevel);
                if (s_avg >= 0.7 * koliutLevel && s_avg > 5 && is_on) {
                    if (!BluetoothActions.dog_reaction()) {
                        Toast.makeText(safRecognition.this, "בלוטוס לא זמין, בדוק חיבור", Toast.LENGTH_SHORT).show();
                        System.out.println("1111: should play woof");
                        mp.start();
                        try {
                            Thread.sleep(1900);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("1111: FINISHED THREAD");
                    }
                }
                s_avg = 0;

            }
            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, 50);
        }
    };



    @Override
    //menu with 3 dots
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
                //startActivity(new Intent(homePage.this, info_activity.class));
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
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(safRecognition.this);
        alertDialog.setTitle("זיהוי פשוט");
        alertDialog.setMessage("זיהוי צלילים וקולות פשוט, בהתאם לסף שיוגדר. יתקבל פידבק חיובי עבור כל צליל בעוצמה שווה או גבוהה מהסף שיוגדר.\nלחץ על הכלב להפעלה אוטומטית של התגובה");
        alertDialog.setNegativeButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(safRecognition.this, "מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        androidx.appcompat.app.AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        alert.getWindow().setLayout(1000,800);
    }


    @SuppressLint("RestrictedApi")
    @Override
    // menu with 3 dots
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }
}
