package com.example.myapplication;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.arduino2Bluetooth.SettingsAndBluetooth;
import com.newventuresoftware.waveform.WaveformView;

import java.util.ArrayList;
import java.util.Locale;

@SuppressWarnings("ALL")
public class gamePage extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    private static SpeechRecognizer speechRecognizer;
    public static final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private TextView textView;
    private ImageView micButton;
    private Button recognizeSettingBtn;
    private boolean is_on;
    private WaveformView mRealtimeWaveformView;
    private static final int REQUEST_RECORD_AUDIO = 13;
    public String chosenWord = "";
    public int recStatus = 0; // 0 -  אך ורק זיהוי דיבור, 1 - זיהוי סף וזיהוי דיבור.
    private Button chooseWordBtn;
    ImageButton playRecord;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // home & helper & mic button & text are created
        // creating speech recognition
        // permissiom is handled
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        System.out.println("1111: hi");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }

        textView = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        recognizeSettingBtn = findViewById(R.id.recognizeSettingBtn);
        chooseWordBtn = findViewById(R.id.chooseWordBtn);
        playRecord = findViewById(R.id.playRecord);

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.bark);
        playRecord.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                mp.start();
            }
        });


        chooseWordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecSettingsDialog();
            }
        });

        recognizeSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                showRecSettingsDialog();
            }
        });
        try {
            // creating speech recognition object
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            // all of these to make sure hebrew is added
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he");
            speechRecognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"he"});

            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            System.out.println("1111: created speechRecognizerIntent");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("1111: error");
        }

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            private Context context = null;

            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                textView.setText("");
                textView.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {
                textView.setHint("onRmsChanged...");

            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                System.out.println("1111: onBufferReceived");
                textView.setHint("onBufferReceived...");
            }

            @Override
            public void onEndOfSpeech() {
                textView.setHint("onEndOfSpeech...");
            }

            @Override
            public void onError(int error) {
                System.out.println("1111: on error");
                String mError = "";
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        textView.setHint("network timeout");
                        System.out.println("1111: on error network timeout");
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        textView.setHint("network, Please check data bundle or network settings");
                        System.out.println("1111: network, Please check data bundle or network settings");
                        return;
                    case SpeechRecognizer.ERROR_AUDIO:
                        textView.setHint("ERROR_AUDIO");
                        System.out.println("1111: ERROR_AUDIO");
                        return;
                    case SpeechRecognizer.ERROR_SERVER:
                        mError = "server";
                        System.out.println("1111:" +mError);
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        mError = " client";
                        System.out.println("1111:" +mError);
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        mError = " speech time out" ;
                        System.out.println("1111:" +mError);
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        mError = " no match" ;
                        System.out.println("1111:" +mError);
                        speechRecognizer.startListening(speechRecognizerIntent);
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        mError = " recogniser busy" ;
                        System.out.println("1111:" +mError);
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        mError = " insufficient permissions" ;
                        System.out.println("1111:" +mError);
                        break;
                }
            }

            @Override
            public void onResults(Bundle bundle) {
                textView.setHint("onResults...");
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                textView.setText(data.get(0));
                if (chosenWord.isEmpty()){
                    if (data.get(0).equals(chosenWord)) {
                        try {
                            shouldReact();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } }
                }else{
                    if (!data.get(0).equals("")) {
                        try {
                            shouldReact();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } } }

                speechRecognizer.startListening(speechRecognizerIntent);

            }

//            @Override
//            public void onActivityResult(int requestCode, int resultCode, Intent data) {
//                // the resulting text is in the getExtras:
//                Bundle bundle = data.getExtras();
//                ArrayList<String> matches = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
//                // the recording url is in getData:
//                Uri audioUri = data.getData();
//                ContentResolver contentResolver = getContentResolver();
//                try {
//                    InputStream filestream = contentResolver.openInputStream(audioUri);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                // TODO: read audio file from inputstream
//            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String word = (String) data.get(data.size() - 1);
                textView.setText(word);
                if (!data.get(0).equals("")) {
                    try {
                        shouldReact();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }} }

            @Override
            public void onEvent(int i, Bundle bundle) {
                textView.setText("onEvent");
            }
        });
    }

    private void showChooseWordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(gamePage.this);
        alertDialog.setTitle("לבחור מילה");
        String[] items = {"בוא", "עוד","הב","שב"};
        int checkedItem = 1;
        alertDialog.setNegativeButton("בחרתי",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(gamePage.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        chosenWord = items[0];
                        break;
                    case 1:
                        chosenWord = items[1];
                        break;
                    case 2:
                        chosenWord = items[2];
                        break;
                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void showRecSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(gamePage.this);
        alertDialog.setTitle("בחר הגדרות זיהוי");
        String[] items = {"זיהוי דיבור","זיהוי סף"};
        int checkedItem = 1;
        alertDialog.setNegativeButton("בחרתי",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(gamePage.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recStatus = which;
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

//    private void showRecSettingsDialog() {
//        AlertDialog.Builder alertDialogSettings = new AlertDialog.Builder(gamePage.this);
//        alertDialogSettings.setTitle("בחר אופציה לזיהוי");
//        String[] items = {"זיהוי דיבור","זיהוי סף קול"};
//        int checkedItem = 1;
//        alertDialogSettings.setNegativeButton("",
//                new DialogInterface.OnClickListener()
//                {
//                    public void onClick(DialogInterface dialog, int id)
//                    {
//                        Toast.makeText(gamePage.this,"מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
//                        dialog.cancel();
//                    }
//                });
//        alertDialogSettings.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                recStatus = which;
//
//                }
//            }
//        });
//        AlertDialog alert = alertDialogSettings.create();
//        alert.alertDialogSettings(false);
//        alert.alertDialogSettings();
//    }


    private void shouldReact() throws InterruptedException {

        changeColor();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                returnColor();
            }
        }).start();
    }

    private void returnColor() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout bgElement = (RelativeLayout) findViewById(R.id.game);
                bgElement.setBackgroundColor(Color.WHITE);
            }
        });
    }


    private void changeColor() {
        RelativeLayout bgElement = (RelativeLayout) findViewById(R.id.game);
        bgElement.setBackgroundColor(Color.BLUE);
    }


    @Override
    protected void onStart(){
        super.onStart();
        speechRecognizer.stopListening();

        is_on = false;
        micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
        textView.setHint("Tap to Speak...");

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_on == true){
                    micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    speechRecognizer.stopListening();
                    onStop();
                    textView.setHint("Tap to Speak...");
                    speechRecognizer.cancel();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    is_on = false;
                }
                else{
                    micButton.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                    is_on = true;
                }
            }


        });

//        startAudioRecordingSafe();
//
//        short[] samples = new short[] {0, 4, 5};
//        try {
//            samples = getAudioSample();
//        } catch (IOException ioException) {
//            ioException.printStackTrace();
//        }
    }

    protected void onStop(){
        //todo: make sure the listening thread stops when we leave the page and get back
        super.onStop();
        speechRecognizer.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

//    final WaveformView mPlaybackView = (WaveformView) findViewById(R.id.rawDataView);

//    private void startAudioRecordingSafe() {
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
//                == PackageManager.PERMISSION_GRANTED) {
//            mRecordingThread.startRecording();
//        } else {
//            checkPermission();
//        }
//    }

//    private short[] getAudioSample() throws IOException{
//        InputStream is = getResources().openRawResource(R.raw.jinglebells);
//        byte[] data;
//        try {
//            data = IOUtils.toByteArray(is);
//        } finally {
//            if (is != null) {
//                is.close();
//            }
//        }
//
//        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
//        short[] samples = new short[sb.limit()];
//        sb.get(samples);
//        return samples;
//    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
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
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            /*
            case R.id.nav_avg_sound:
                Toast.makeText(this, "לזיהוי פשוט", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                return true;*/
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
            case R.id.home:
                Toast.makeText(this, "למסך הבית", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, homePage.class));
                return true;
            default:
                return true;
        } }
    private void info() {
    }
    private void disableBluetooth() {
    }

}
