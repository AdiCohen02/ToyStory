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
import com.example.myapplication.voiceEditor.BluetoothActions;
import com.example.myapplication.voiceEditor.safRecognition;

import java.util.ArrayList;
import java.util.Locale;

@SuppressWarnings("ALL")
public class voiceRecognition<audio> extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    private Integer DOG_ACTION_DURIATION = 2000;
    private static SpeechRecognizer speechRecognizer;
    public static final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private TextView textView;
    private ImageView micButton;
    private boolean is_on = false;
    private MediaPlayer mp;
    private static final int REQUEST_RECORD_AUDIO = 13;
    public String chosenWord = null;
    private Button chooseWordBtn;
    public RelativeLayout bgElement;
    public ImageButton autoDogReaction;

    public SettingsAndBluetooth s = new SettingsAndBluetooth();
    public String[] words = {"בוא", "עוד", "שב", "ללא מילה"}; // it is possible to add words to this list, but you should update showChooseWordDialog as well

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // creating speech recognition
        // permissiom is hundle here
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }

        textView = findViewById(R.id.text); // text table of - לחץ כדי להתחיל
        micButton = findViewById(R.id.button); // button of starting and stoping google recognition
        chooseWordBtn = findViewById(R.id.chooseWordBtn);
        autoDogReaction = findViewById(R.id.playRecord);

        RelativeLayout bgElement = (RelativeLayout) findViewById(R.id.game);
        mp = MediaPlayer.create(this, R.raw.bark); // dog record, used just when playong without a doll


        // the purpose is that when the dog picture is pressed there will be a reaction - of the doll or a bark.
        autoDogReaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!BluetoothActions.dog_reaction()) {
                    Toast.makeText(voiceRecognition.this, "בלוטוס לא זמין, בדוק חיבור", Toast.LENGTH_SHORT).show();
                    mp.start();
                }
            }
        });

        //todo: should silence noise from the google recognition enviroment
        //audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        chooseWordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooseWordDialog();
            }
        });

        // creating the google recognition object, defining hebrew as language
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

        // all speechRecognizer object events are here. hints are here for debuging if needed.
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            private Context context = null;

            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                textView.setHint("מקשיב...");
            }

            @Override
            public void onRmsChanged(float v) {
//                textView.setHint("onRmsChanged...");
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
//                textView.setHint("onBufferReceived...");
            }

            @Override
            public void onEndOfSpeech() {
//                textView.setHint("onEndOfSpeech...");
            }

            @Override
            public void onError(int error) {
                // is on - true if listening is on, false otherwise
                if (!is_on) { //this if fixes a bug that happens when error is reached while stopListening
                    speechRecognizer.stopListening();
                    speechRecognizer.cancel();
                    textView.setHint("לחץ כדי להתחיל...");
                    return;
                }
                // printing errors here for debuging, is needed
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        textView.setHint("network timeout");
//                        System.out.println("1111: on error network timeout");
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        textView.setHint("עלייך להיות מחובר לאינטרנט.");
//                        System.out.println("1111: network, Please check data bundle or network settings");
                        return;
                    case SpeechRecognizer.ERROR_AUDIO:
                        textView.setHint("ERROR_AUDIO");
//                        System.out.println("1111: ERROR_AUDIO");
                        return;
                    case SpeechRecognizer.ERROR_SERVER:
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        // no text mathces the sound. start listening again.
                        speechRecognizer.startListening(speechRecognizerIntent);
                        is_on = true;
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        break;
                }
            }

            @Override

            public void onResults(Bundle bundle) {
                // this event happens on the end of speech, the data contains guessed sentences.
//                textView.setHint("onResults...");
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                textView.setText(data.get(0)); //displays text
                System.out.println("1111: on results");
                if ((chosenWord == null & !data.get(0).equals("")) | (chosenWord != null & data.contains(chosenWord))) {
                    shouldReact(); // dog or application reaction
                }
                speechRecognizer.startListening(speechRecognizerIntent);
                is_on = true; // start listening becuse on results stops speechRecognizer.
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // this event happens while speaking.
                System.out.println("1111: on partial results");
                ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String word = (String) data.get(data.size() - 1);
                textView.setText(word); // showing results
                if ((chosenWord == null & !data.get(0).equals("")) | (chosenWord != null & data.contains(chosenWord))) {
                    shouldReact();// dog or application reaction
                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            } //do not delete this method

        });
    }

    private void showChooseWordDialog() {
        // choose word dialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(voiceRecognition.this);
        alertDialog.setTitle("לבחור מילה");
        int checkedItem = 1;
        alertDialog.setNegativeButton("בחרתי",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(voiceRecognition.this, "מעולה, בואו נתחיל!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        alertDialog.setSingleChoiceItems(words, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        chosenWord = words[0];
                        break;
                    case 1:
                        chosenWord = words[1];
                        break;
                    case 2:
                        chosenWord = words[2];
                        break;
                    case 3:
                        chosenWord = null;
                        break;
                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void shouldReact() {
        // dog reaction
        bgElement = (RelativeLayout) findViewById(R.id.game);
        bgElement.setBackgroundColor(Color.BLUE);
        if (!BluetoothActions.dog_reaction()) {
            // in case the doll is not connected, the up will play sound.
            Toast.makeText(this, "בלוטוס לא זמין, בדוק חיבור", Toast.LENGTH_SHORT).show();
            mp.start();
        }
        bgElement.setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        speechRecognizer.stopListening();

        is_on = false;
        micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
        textView.setHint("לחץ כדי להתחיל...");

        micButton.setOnClickListener(new View.OnClickListener() {
            // starting and stoping listening button
            @Override
            public void onClick(View view) {
                if (is_on == true) {
                    is_on = false;
                    micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    speechRecognizer.stopListening();
                    textView.setHint("לחץ כדי להתחיל...");
                    speechRecognizer.cancel();
                } else {
                    micButton.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                    is_on = true;
                }
            }


        });

    }

    protected void onStop() {
        super.onStop();
        speechRecognizer.stopListening();
        is_on = false;
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
    // requesting all needed premissions, check here in a case of problem
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    // 3 dots of above menu set up
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    // 3 dots of above menu set up
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                startActivity(new Intent(this, homePage.class));
            case R.id.nav_avg_sound:
                Toast.makeText(this, "עובר לזיהוי פשוט", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, safRecognition.class));
                return true;
            case R.id.nav_Bluetooth2Led:
                Toast.makeText(this, "התחברות לבלוטוס", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SettingsAndBluetooth.class));
                return true;
            case R.id.nav_info:
                info();
                return true;
            case R.id.disable_bluetooth:
                Toast.makeText(this, "התנתקות מהבלוטוס", Toast.LENGTH_SHORT).show();
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
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(voiceRecognition.this);
        alertDialog.setMessage("על מנת להפעיל אוטומטית, לחץ על הכלב. לזיהוי דיבור, לחצה על המיקרופון.\nוודא.י שיש לך חיבור לאינטרנט.");
        alertDialog.setNegativeButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        androidx.appcompat.app.AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
}
