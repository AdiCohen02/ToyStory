package com.example.myapplication;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import java.util.Locale;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.util.ArrayList;

@SuppressWarnings("ALL")
public class gamePage extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    private static SpeechRecognizer speechRecognizer;
    public static final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private EditText editText;
    private ImageView micButton;
    private Button getHomeBtn;
    private Button helpBtn;
    private boolean is_on;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // home & helper & mic button & text are created
        // creating speech recognition
        // permissiom is handled
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }

        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        getHomeBtn = findViewById(R.id.getBackHomeBtn);
        helpBtn = findViewById(R.id.helpBtn);

        getHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                startActivity(new Intent(gamePage.this, homePage.class));
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                startActivity(new Intent(gamePage.this, helper.class));
            }
        });

        // creating speech recognition object
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // all of these to make sure hebrew is added
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he");
        speechRecognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"he"});

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {
                                                                                                    editText.setHint("onRmsChanged...");
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                editText.setHint("onBufferReceived...");
            }

            @Override
            public void onEndOfSpeech() {
                editText.setHint("onEndOfSpeech...");
            }

//            @Override
//            public void onError(int i) {
//
//            }

            @Override
            public void onError(int error) {
                String mError = "";
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        editText.setHint("network timeout");
//                        speechRecognizer.startListening(speechRecognizerIntent);
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        editText.setHint("network, Please check data bundle or network settings");
                        return;
                    case SpeechRecognizer.ERROR_AUDIO:
                        editText.setHint("ERROR_AUDIO");
                        return;
                    case SpeechRecognizer.ERROR_SERVER:
                        mError = " server";
//                        speechRecognizer.startListening(speechRecognizerIntent);
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        mError = " client";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        mError = " speech time out" ;
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        mError = " no match" ;
                        speechRecognizer.startListening(speechRecognizerIntent);
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        mError = " recogniser busy" ;
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        mError = " insufficient permissions" ;
                        break;
                }
            }

        @Override
            public void onResults(Bundle bundle) {
                editText.setHint("onResults...");
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                editText.setText(data.get(0));
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String word = (String) data.get(data.size() - 1);
                editText.setText(word);

            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                editText.setText("onEvent");
            }
        });
    }



    @Override
    protected void onStart(){
        super.onStart();

        is_on = false;
        micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
        editText.setHint("Tap to Speak...");

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_on == true){
                    micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
//                    speechRecognizer.stopListening();
                    editText.setText("Tap to Speak...");
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
}
