

package com.example.android.cse594project;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

/*
This class allows a user to add a note. They can enter the note through the Android keyboard or by
pressing the voice to text key. When they are done they can save the note to the database.
 */

public class AddNote extends AppCompatActivity {
    DBHandler dbHandler;
    EditText noteField;
    ListView noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_note);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "baskerville_old_face.ttf");
        Button myButton = (Button) findViewById(R.id.savebutton);
        Button voiceButton = (Button) findViewById(R.id.voicebutton);
        EditText myEditText = (EditText) findViewById(R.id.notetext);
        myButton.setTypeface(myTypeface);
        myEditText.setTypeface(myTypeface);
        voiceButton.setTypeface(myTypeface);
        dbHandler = new DBHandler(this, null, null, 1);
        noteField = (EditText) findViewById(R.id.notetext);
        noteList = (ListView) findViewById(R.id.list);
    }

    // Saves the note the user entered into the database.
    public void addNote(View view) {
        String n = noteField.getText().toString();
        if(!n.equals("")){
        String encryptedNote = Crypt.encrypt(n);
        dbHandler.addNote(encryptedNote);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("noteinfo", "Note Added");
        setResult(Activity.RESULT_OK, resultIntent);
        }
        finish();
    }

/*  This function prompts the user to speak and will call the built in android speech recognizer.
    When the speech recognizer ends, it will return with a activity result of 100 and a string consisting
    of what the user said.
 */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    //Button to initiate voice to text.
    public void speak(View view) {
        promptSpeechInput();
    }
    /* When the built in text to speech finishes this function is called with a string array consisting
    of the words the user said. This string is then entered into the note text field so the user can save the data.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    noteField.setText(result.get(0));
                    noteField.setSelection(noteField.getText().length());
                }
                break;
            }
        }
    }
}
