package com.example.android.cse594project;

import android.Manifest;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Locale;
import javax.crypto.KeyGenerator;
import static com.example.android.cse594project.R.id.pinpaditem;

/*
This class acts as a hub for the entire project. This class is used to show the notes the user entered. It
also allows the user to go to other activities such as adding a note and editing a note. The user can also set
their lock screen preferences.
 */
public class MainActivity extends AppCompatActivity {
    KeyguardManager mKeyguardManager;
    public Context mcontext;
    EditText noteField;
    ListView noteList;
    static DBHandler dbHandler;
    FingerprintManager fingerprintManager;
    String KEY_NAME = "note_key";
    String PIN_KEY = "pin_key";
    //This Boolean is used to determine whether or not to show the notes based upon the users lockscreen preferences.
    Boolean showBool = false;
    int pinBool;
    int fingerBool;
    private static final byte[] SECRET_BYTE_ARRAY = new byte[] {1, 2, 3, 4, 5, 6};
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mcontext = this.getApplicationContext();
        setContentView(R.layout.activity_main);
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "baskerville_old_face.ttf");
        Button mySettingsButton = (Button) findViewById(R.id.settings);
        Button myCloudNotesButton = (Button) findViewById(R.id.cloudNotes);
        Button myAddNoteButton = (Button) findViewById(R.id.addNote);
        Button voiceButton = (Button) findViewById(R.id.voicecommand);
        mySettingsButton.setTypeface(myTypeface);
        myCloudNotesButton.setTypeface(myTypeface);
        myAddNoteButton.setTypeface(myTypeface);
        voiceButton.setTypeface(myTypeface);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        dbHandler = new DBHandler(this, null, null, 1);
        noteField = (EditText) findViewById(R.id.notetext);
        noteList = (ListView) findViewById(R.id.list);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        pinBool = pref.getInt("pinpadInt", 0);
        fingerBool = pref.getInt("fingerInt", 0);
        keyCheck();
        if(pinBool == 1 && fingerBool == 1) {
            fingerprintwithpin();
        }
        else if(pinBool == 1) {
            pinAuthenticate();
        }
        else if(fingerBool == 1) {
           fingerprint();
        }
        else {
            showBool = true;
        }
        showNotes();
    }

    /*
    This class is called if the user has enabled a fingerprint lockscreen and will call the fingerprint activity.
    If the user does not have fingerprints registered, the class will turn off the fingerprint lock.
     */
    public void fingerprint() {
        editor = pref.edit();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Fingerprint authentication permission not enabled", Toast.LENGTH_LONG).show();
        }
        if (fingerprintManager.hasEnrolledFingerprints()){

            Intent intent = new Intent(this, FingerPrint.class);
            startActivityForResult(intent, 2);
        }
        else{
            editor.putInt("fingerInt", 0);
            editor.commit();
            Toast.makeText(this, "No enrolled fingerprints, fingerprint unlock disabled", Toast.LENGTH_LONG).show();
        }
    }

    /*
    This class is called if a user has both fingerprint and lockscreen enabled.
     */
    public void fingerprintwithpin() {

        editor = pref.edit();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Fingerprint authentication permission not enabled", Toast.LENGTH_LONG).show();
        }
        if (fingerprintManager.hasEnrolledFingerprints()){

            Intent intent = new Intent(this, FingerPrint.class);
            startActivityForResult(intent, 3);
        }
        else{
            editor.putInt("fingerInt", 0);
            editor.commit();
            Toast.makeText(this, "No enrolled fingerprints, fingerprint unlock disabled", Toast.LENGTH_LONG).show();
        }
    }

    /*
    This is used to check if the keys needed to encrypt notes or unlock the screen have been created.
    If not, this will call the respective generate keys.
     */
    public void keyCheck() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(KEY_NAME, null);
            if (entry == null) {
                createKey();
            }

            if (mKeyguardManager.isKeyguardSecure()) {
                createPinKey();
            }
        } catch ( KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new RuntimeException(e);
        }
    }

    //Create the key to encrypt notes
    private void createKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setRandomizedEncryptionRequired(false)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException | KeyStoreException
                | CertificateException | IOException e) {
            Toast.makeText(this, "Failed to create a symmetric key for note encryption", Toast.LENGTH_LONG).show();
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }

    //Create the key to unlock screen
    private void createPinKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(PIN_KEY,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(60)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException | KeyStoreException
                | CertificateException | IOException e) {
            Toast.makeText(this, "Failed to create a symmetric key for pinpad", Toast.LENGTH_LONG).show();
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }

    /*
    This class will show the authentication screen. If the user and after they authenticate the notes can be shown
    and showBool is set to true.
     */
    private void pinAuthenticate() {
        showAuthenticationScreen();
        showBool = true;
        /*
        If you want to check if user has authenticated recently
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(PIN_KEY, null);
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            cipher.doFinal(SECRET_BYTE_ARRAY);
            showBool = true;
        } catch (UserNotAuthenticatedException e) {
            showAuthenticationScreen();
        } catch (KeyPermanentlyInvalidatedException e) {
        } catch (BadPaddingException | IllegalBlockSizeException | KeyStoreException |
                CertificateException | UnrecoverableKeyException | IOException
                | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        */
    }

    //Calls the Confirm credential API to show the Pinpad login
    private void showAuthenticationScreen() {
        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        if (intent != null) {
            startActivityForResult(intent, 2);
        }
    }

    //This class is called by the voice button and allows for voice to text.
    public void command(View view) {
        promptSpeechInput();
    }

    /*  This function prompts the user to speak and will call the built in android speech recognizer.
    When the speech recognizer ends, it will return with a activity result of 4 and a string consisting
    of what the user said.
 */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, 4);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Voice Failed", Toast.LENGTH_SHORT).show();
        }
    }

    //This button will open a context menu allowing the users to set their lockscreen preferences.
    public void settings(View view){
        registerForContextMenu(view);
        openContextMenu(view);
    }

    /*
    This function is used to create a menu allowing the user the create set lockscreen preferences.
    */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);
        pinBool = pref.getInt("pinpadInt", 0);
        fingerBool = pref.getInt("fingerInt", 0);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        MenuItem pinItem =  menu.findItem(R.id.pinpaditem);
        MenuItem fingerprintItem = menu.findItem(R.id.fingerprintitem);
        if(pinBool == 1) {
            pinItem.setChecked(true);
        }
        else{
            pinItem.setChecked(false);
        }
        if(fingerBool == 1) {

            fingerprintItem.setChecked(true);
        }
        else {
            fingerprintItem.setChecked(false);
        }

    }

    /*
    This function is called when a user selects an item from the menu. It makes use of android prefences.
    If the user selects a pin lockscreen, the integer pinBool will be set to 1, if they disable the lockscreen it is set to 0.
    If the user select a fingerprint lockscreen, fingerBool is set to 1, if they disable the finger lock screen it is set to 0.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item){
        editor = pref.edit();
        switch (item.getItemId()){
            case pinpaditem:
                if(pinBool == 0) {
                    if (mKeyguardManager.isKeyguardSecure()) {
                        editor.putInt("pinpadInt", 1);
                        editor.commit();
                        Toast.makeText(this, "Pin lock screen set", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Please set a lock screen", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                else {
                    editor.putInt("pinpadInt", 0);
                    editor.commit();
                    Toast.makeText(this, "Pin lock screen disabled", Toast.LENGTH_LONG).show();
                    return true;
                }
            case R.id.fingerprintitem:
                if(fingerBool == 0) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Fingerprint authentication permission not enabled", Toast.LENGTH_LONG).show();
                        return true;
                    }

                    if (fingerprintManager.hasEnrolledFingerprints()) {
                        editor.putInt("fingerInt", 1);
                        editor.commit();
                        Toast.makeText(this, "Fingerprint lock screen set", Toast.LENGTH_LONG).show();
                        return true;
                    } else {
                        Toast.makeText(this, "Please enroll a fingerprint", Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
                else{
                    editor.putInt("fingerInt", 0);
                    editor.commit();
                }
        }
        return super.onContextItemSelected(item);
    }

    //Starts newNote activity when add note button is pressed.
    public void newNote(View view) {
        Intent intent = new Intent(this, AddNote.class);
        //startActivityForResult is a callback. When
        startActivityForResult(intent, 1);
    }

    /*
    This function is called when another activity finishes. If the requestCOde is 1, the user has successfully added,
    deleted, or edited a note, and the result is displayed to the user. If the request code is 2, a user has successfully
    authenticated with their fingerprints and showBool is now true. If the request code is 3, the user has successfully
    authenticated with their fingerprints and the pinpad unlock is now called. If the request code is 4, the voice to text
    has finished and what the user said is displayed as a string.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(data != null) {
                String newText = data.getStringExtra("noteinfo");
                Toast.makeText(this, newText, Toast.LENGTH_LONG).show();
            }
            showNotes();
        }
        if (requestCode == 2){
            if(data != null) {
                showBool = true;
                showNotes();
            }
            else {
                fingerprint();
            }
        }
        if (requestCode == 3){
            if(data != null) {
                showBool = true;
                pinAuthenticate();
            }
            else {
                fingerprint();
            }
        }
        if (requestCode == 4) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Toast.makeText(this, result.get(0), Toast.LENGTH_LONG).show();
                if(result.get(0).equals("add")){
                    Intent intent = new Intent(this, AddNote.class);
                    startActivityForResult(intent, 1);
                }
            }
        }
    }

    /*
    This function is used to show the notes that the user has entered. It will only show the notes if the
    user has not enabled a lockscreen or if they have successfully authenticated in some manner.
     */
    public void showNotes() {
        if(showBool == true) {
            Cursor cursor = dbHandler.getNotes();
            if (cursor != null) {
                noteCursor c = new noteCursor(this, cursor);
                noteList.setAdapter(c);
                noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adaptView, View view, int newInt,
                                            long newLong) {
                        LinearLayout parent = (LinearLayout) view;
                        LinearLayout child = (LinearLayout) parent.getChildAt(0);
                        TextView m = (TextView) child.getChildAt(1);
                        TextView k = (TextView) child.getChildAt(0);
                        String noteText = k.getText().toString();
                        Bundle bundle = new Bundle();
                        int id = Integer.parseInt(m.getText().toString());
                        bundle.putInt("id", id);
                        bundle.putString("notetext", noteText);
                        Intent intent = new Intent(getApplicationContext(), NoteHelper.class);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, 1);
                    }
                });
                noteList.setOnTouchListener(new OnSwipeTouchListener(this, mcontext,
                        noteList));
            }
        }
    }


    /* This function is called when a user swipes to delete a note. If side is 1, the user has swiped right.
     If side is 2, the user has swiped left. A popup menu is called verifying the deletion. If the user enters
    yes, the note is deleted and an animation is called moving the note offscreen. If they click no, nothing happens.
     */
    public void delete(final int pos, final int side){
        AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
        a_builder.setMessage("Are you sure you want to delete this note?");
        a_builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                View g = noteList.getAdapter().getView(pos, null, noteList);
                Animation animation = null;
                if(side == 1){
                    float direction = 1;
                    animation = deleteAnimation(direction);
                    noteList.getChildAt(pos).startAnimation(animation);
                }
                else if(side == 2) {
                    float direction = -1;
                    animation = deleteAnimation(direction);
                    noteList.getChildAt(pos).startAnimation(animation);
                }
                animation.setAnimationListener(new Animation.AnimationListener(){
                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }
                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        showNotes();
                    }
                });
                LinearLayout parent = (LinearLayout) g;
                LinearLayout child = (LinearLayout) parent.getChildAt(0);
                TextView m = (TextView) child.getChildAt(1);
                int id = Integer.parseInt(m.getText().toString());
                dbHandler.deleteNote(id);
                //Toast.makeText(this, "Note deleted", Toast.LENGTH_LONG).show();
            }
        });
        a_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        a_builder.show();

    }

    //This function is an animaiton that moves the note in a direction offscreen.
    private Animation deleteAnimation(float direction) {
        int duration = 200;
        Animation movement = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, direction,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        movement.setDuration(duration);
        movement.setInterpolator(new AccelerateInterpolator(1));
        return movement;
    }
}
