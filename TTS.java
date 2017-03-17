package com.example.android.cse594project;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

/*
This class implements the androids built in text to speech. It says a string from the user and then
outputs that string as speech through the users speakers.
 */
public  class TTS extends Service implements TextToSpeech.OnInitListener{
    private TextToSpeech tts;
    private String spokenText;
    public static String NOTIFICATION = "notification";

    //This function is called when the class starts and is used to grab the string and initialize the text to speech.
    @Override
    public void onStart(Intent intent, int startId) {
        if(intent != null){
            spokenText = intent.getStringExtra(NOTIFICATION);
            tts = new TextToSpeech(this, this);
        }
    }

    /*
    This function is called when the text to speech initializes it check text to speech is ready and
    using the correct language. It will then perform the text to speech operation.
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    //This function stops the text to speech service.
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}