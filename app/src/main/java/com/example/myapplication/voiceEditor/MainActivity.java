package com.example.myapplication.voiceEditor;

import static android.media.AudioManager.MODE_NORMAL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

class Helper extends TimerTask {
    public static int i = 1;

    // TimerTask.run() method will be used to perform the action of the task

    public void run() {
        System.out.println("This is called " + i++ + " time");
    }
}

public class MainActivity extends AppCompatActivity {

    public boolean startedThred = false;
    public double noiseLevel = 1000;
    public double koliutLevel = 5000;
    public int delayMillis = 10;
    public int DOG_ACTION_DURIATION = 1000; //should be in millis
    private MediaRecorder mRecorder;
    private Handler mHandler = new Handler();

    private SeekBar childLevel;
    private Switch environmentSwitch;
    public static final String PREFERENCES = "preferences";
    public static final String VOLUME_VALUE = "volume";
    public static final String SILENCE_SWITCH = "switch";

    private Integer savedVolume;
    private boolean savedswitch;


    private AudioRecord recorder;
    private int sampleRate = 16000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;
    public Button btnStart, btnTresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saf_recognition);

        Button btnTresh = (Button)findViewById(R.id.threshold);
        Button btnStart = (Button)findViewById(R.id.start_recording1);
        TextView currSeek = (TextView) findViewById(R.id.volume_curr_value);
        childLevel = findViewById(R.id.child_level);
        environmentSwitch = (Switch) findViewById(R.id.switchEnvironment);


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        //                if(!isMicrophoneAvailable()) {
//                    ((TextView)btnStart).setText("Mic not available");
//                    return;
//                }
//                } //todo: check we indeed have access

        btnStart.setText("Mic available");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        System.out.println("1111: here1");
        File file = new File(Environment.getExternalStorageDirectory() + "/Demo");
        if (!file.exists())
            file.mkdir();

        mRecorder.setOutputFile(getExternalFilesDir(Environment.DIRECTORY_MUSIC)  + "/demoAudio.mp3");

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("1111: error");
            mRecorder = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            System.out.println("1111: error");
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
                System.out.println("1111: started btnKoliut");
                btnTresh.setText("בהקלטה: שהמטופל ישמיע צליל AAA ");
                delayMillis = 3000;
                double threshLevel1 = getAmplitude();
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        koliutLevel = getAmplitude();
                        childLevel.setProgress((int)koliutLevel/100);
                        System.out.println("1111: tresh level is" + koliutLevel);
                        // todo: change button theme after finished
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTresh.setText("העוצמה היא " + koliutLevel+ " dB");
                                //todo: set scale
                            }
                        });
                    }
                };
                timer.schedule(task, 3000);
                btnTresh.setText("המתן");
            }
        });

        if (childLevel != null) {
            childLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                    //todo: add value in db
                    int val = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
                    currSeek.setText(progress * 5 + "dB");
                    currSeek.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    currSeek.setY(250); //just added a value set this properly using screen with height aspect ratio , if you do not set it by default it will be there below seek bar

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
                    saveData();
                }
            });
        }

        // todo: add function - when the switch of noisy environment is on, set dellayMillis to 400, otherwise to 10.
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VOLUME_VALUE, childLevel.getProgress());
        System.out.println("1111: saved volume is" + savedVolume);
        editor.putBoolean(SILENCE_SWITCH, environmentSwitch.isChecked());
        editor.apply();
        System.out.println("1111: saved volume is" + savedVolume);
    }

    public void updateViews() {
        System.out.println("1111: saved volume is" + savedVolume);
        childLevel.setProgress(savedVolume);
        environmentSwitch.setChecked(savedswitch);
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        savedVolume = sharedPreferences.getInt(VOLUME_VALUE, 1);
        savedswitch = sharedPreferences.getBoolean(SILENCE_SWITCH, false);
    }

    public static boolean isMicrophoneAvailable() {
        System.out.println("1111: getSystemService " + Context.AUDIO_SERVICE);
        AudioManager audioManager = (AudioManager) MyApp.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        System.out.println("1111: isMicrophoneAvailable: " + (audioManager.getMode() == MODE_NORMAL));
        return audioManager.getMode() == MODE_NORMAL;
    }

    public int getAmplitude() {
        if (mRecorder != null) {
            int x = (int)(50 * Math.log10(mRecorder.getMaxAmplitude() / 1000));
            if (x>0){
                return x;
            }
            else {
                return 0;
            }
        }
        else { return 0; }
    }

    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = getAmplitude(); //todo: check if db scale is ok...
            //Log.i("Noise", "runnable mPollTask");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (amp >= 0.7 * koliutLevel) {
                        System.out.println("1111 volume passed. value is: "+ amp);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Button btnStart = (Button)findViewById(R.id.start_recording1);;
                                btnStart.setBackgroundColor(Color.BLUE);
                                Timer timer = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
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
            mHandler.postDelayed(mPollTask, delayMillis);
        }
    };
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_avg_sound:
                Toast.makeText(this, "לזיהוי פשוט", Toast.LENGTH_SHORT).show();
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
                disableBluetooth();
                return true;
            default:
                return true;
        } }

    private void info() {
    }
    private void disableBluetooth() {
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
