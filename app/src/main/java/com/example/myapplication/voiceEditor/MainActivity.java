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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.R;
import com.example.myapplication.arduino2Bluetooth.SettingsAndBluetooth;
import com.example.myapplication.gamePage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public boolean startedThred = false;
    public double noiseLevel = 1000;
    public double koliutLevel = 5000;
    public int delayMillis = 10;
    public int DOG_ACTION_DURIATION = 2000; //should be in millis
    private MediaRecorder mRecorder;
    private Handler mHandler = new Handler();


    private SeekBar childLevel;
    private Switch environmentSwitch;
    public static final String PREFERENCES = "preferences";
    public static final String VOLUME_VALUE = "volume";
    public static final String SILENCE_SWITCH = "switch";

    private Integer savedVolume;
    private boolean savedswitch;

    public List<Double> list=new ArrayList<Double>();


    private AudioRecord recorder;
    private int sampleRate = 16000; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;
    public Button btnStart, btnTresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saf_recognition);

        Button btnTresh = (Button) findViewById(R.id.threshold);
        Button btnStart = (Button) findViewById(R.id.start_recording1);
        TextView currSeek = (TextView) findViewById(R.id.volume_curr_value);
        ImageButton playRecord = (ImageButton) findViewById(R.id.playRecord2);
        childLevel = findViewById(R.id.child_level);
        environmentSwitch = (Switch) findViewById(R.id.switchEnvironment);

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.bark);
        playRecord.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                mp.start();
            }
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        //                if(!isMicrophoneAvailable()) {
//                    ((TextView)btnStart).setText("Mic not available");
//                    return;
//                }
//                } //todo: check we indeed have access

        btnStart.setText("לחץ להתחלה");
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

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                koliutLevel = childLevel.getProgress();
                saveData();
                thread.start();
                btnStart.setText("כל הכבוד!");
                btnStart.setBackgroundColor(Color.WHITE);
                startedThred = true;
            }
        });


        btnTresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTresh.setText("בהקלטה: שהמטופל ישמיע צליל AAA ");
                delayMillis = 3000;
                double threshLevel1 = getAmplitude();
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        koliutLevel = getAmplitude();
                        //System.out.println("1111: tresh level is" + ((int) koliutLevel / 10) * 10);
                        childLevel.setProgress(((int) koliutLevel / 10));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTresh.setText("העוצמה היא " + koliutLevel + " dB");
                            }
                        });
                    }
                };
                timer.schedule(task, 3000);
                btnTresh.setText("המתן");
            }
        });

        // todo: add function - when the switch of noisy environment is on, set dellayMillis to 400, otherwise to 10.
//        if (environmentSwitch != null) {
//            environmentSwitch.setChecked(boolean checked() {
//            }


        if (childLevel != null) {
            childLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                    int val = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
                    currSeek.setText(progress * 10 + "dB");
                    currSeek.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    currSeek.setY(280); //just added a value set this properly using screen with height aspect ratio , if you do not set it by default it will be there below seek bar
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
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
        editor.putBoolean(SILENCE_SWITCH, environmentSwitch.isChecked());
        editor.apply();
    }

    public void updateViews() {
        childLevel.setProgress(savedVolume);
        environmentSwitch.setChecked(savedswitch);
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        savedVolume = sharedPreferences.getInt(VOLUME_VALUE, 1);
        savedswitch = sharedPreferences.getBoolean(SILENCE_SWITCH, false);
    }

    // todo: delete?
//    public static boolean isMicrophoneAvailable() {
//        System.out.println("1111: getSystemService " + Context.AUDIO_SERVICE);
//        AudioManager audioManager = (AudioManager) MyApp.getAppContext().getSystemService(Context.AUDIO_SERVICE);
//        System.out.println("1111: isMicrophoneAvailable: " + (audioManager.getMode() == MODE_NORMAL));
//        return audioManager.getMode() == MODE_NORMAL;
//    }

    public int getAmplitude() {
        if (mRecorder != null) {
            int x = (int) (37 * Math.log10(mRecorder.getMaxAmplitude() / 700));
            if (x > 0) {
                return x;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

//    public double getAvarage(Integer sampleNum, List<Double> samples){
//        double sum = 0;
//        for (int i=0; i < sampleNum; i++){
//            if ( samples[i])
//        }
//    }

    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = getAmplitude(); //todo: check if db scale is ok...
            list.add(amp);
            double avg = 0;
//            if (list.size() > 300){
//                getAvarage(list.size(),list);
//            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (amp >= 0.7 * koliutLevel) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Button btnStart = (Button) findViewById(R.id.start_recording1);
                                btnStart.setBackgroundColor(Color.BLUE);
                                BluetoothActions.dog_reaction();

                                Timer timer = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //todo: call boolean checked
                                                System.out.println("1111: calling reaction");
                                                btnStart.setBackgroundColor(Color.WHITE);
                                            }
                                        });
                                    }
                                };
                                timer.schedule(task, DOG_ACTION_DURIATION); // This is the time it takes for the dog
                            }
                        });
                    }
                }
            });
            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, 10);
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_avg_sound:
                Toast.makeText(this, "עובר לזיהוי פשוט", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
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
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("הדרכה לזיהוי פשוט: על מנת להשתמש בדף זה עלייך...");
            alertDialog.setNegativeButton("OK",
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            Toast.makeText(MainActivity.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }
}
