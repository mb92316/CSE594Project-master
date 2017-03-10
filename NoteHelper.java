package com.example.android.cse594project;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NoteHelper extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    EditText noteField;
    Button b_pick;
    DBHandler dbHandler;
    int id;
    String noteText;
    Button notificationButton;
    Button voiceButton;
    Crypt crypt;
    String dateString;
    int day, month, year, hour, minute;
    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;
    int choice;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int alarmID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_helper);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "baskerville_old_face.ttf");
        EditText myEditText = (EditText) findViewById(R.id.updatenotetext);
        Button myDeleteButton = (Button) findViewById(R.id.deletebutton);
        Button myAlarmButton = (Button) findViewById(R.id.alarmbutton);
        Button mySaveButton = (Button) findViewById(R.id.savebutton);
        myEditText.setTypeface(myTypeface);
        myDeleteButton.setTypeface(myTypeface);
        myAlarmButton.setTypeface(myTypeface);
        mySaveButton.setTypeface(myTypeface);

        Bundle extras = getIntent().getExtras();
        id = extras.getInt("id");
        noteText = extras.getString("notetext");
        dbHandler = new DBHandler(this, null, null, 1);
        crypt = new Crypt();
        noteField = (EditText) findViewById(R.id.updatenotetext);
        noteField.setText(noteText);
        noteField.setSelection(noteField.getText().length());
        getDate();
    }

    public void deleteNote(View view) {

        AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
        a_builder.setMessage("Are you sure you want to delete this note?");
        a_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHandler.deleteNote(id);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("noteinfo", "Note Deleted");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
        a_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = a_builder.create();
        alert.setTitle("WARNING");
        alert.show();

    }

    public void updateNote(View view) {
        String n = noteField.getText().toString();
        String encryptedNote = Crypt.encrypt(n);
        dbHandler.updateNote(id, encryptedNote );
        Intent resultIntent = new Intent();
        resultIntent.putExtra("noteinfo", "Note Updated");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public  void alarm (View view){

        registerForContextMenu(view);
            openContextMenu(view);
        /*
        PopupMenu popupMenu = new PopupMenu(this, view);

        popupMenu.inflate(R.menu.alarm_menu);

        MenuItem menuItem =

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.alarmbutton) {
                    item.title
                    //set notification alarm
                }
                else if (item.getItemId() == R.id.voicebutton) {
                    //set voice alarm
                }
                else if (item.getItemId() == R.id.cancelbutton) {
                    //cancel alarm
                }
                return false;
            }
        });

        popupMenu.show();
        */
    }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
            super.onCreateContextMenu(menu, view, menuInfo);
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.alarm_menu, menu);

            MenuItem menuItem = menu.findItem(R.id.cancelbutton);
        }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        Calendar c = Calendar.getInstance();

        switch (item.getItemId()){
            case R.id.alarmbutton: {
                choice = 1;
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);
                datePickerDialog.show();
                return true;
            }
            case R.id.voicebutton: {
                choice = 2;
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);
                datePickerDialog.show();
                return true;
            }
            case R.id.cancelbutton: {
                int currentAlarmID = dbHandler.getAlarm(id);
                int alarmType = dbHandler.getAlarmType(id);
                if (alarmID != -1) {
                    cancel(currentAlarmID, alarmType);
                }
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    public void onDateSet(DatePicker view, int i, int i1, int i2) {
        yearFinal = i;
        monthFinal = i1 + 1;
        dayFinal = i2;
        Calendar c = Calendar.getInstance();
        hour=c.get(Calendar.HOUR_OF_DAY);
        minute=c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, this,
                hour, minute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    public void onTimeSet(TimePicker view, int i, int i1) {
        hourFinal   = i;
        minuteFinal = i1;
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm");
        String test = yearFinal + "-" + monthFinal + "-" + dayFinal + " " + hourFinal + ":" + minuteFinal;
        Date date = new Date();
        Date d1 = new Date();
        int alarmID = dbHandler.getAlarm(id);
        int alarmType = dbHandler.getAlarmType(id);
        if(alarmID != -1) {
            cancel(alarmID, alarmType);
        }
        try {
            d1 = ft.parse(test);
        }catch (ParseException e) {
            System.out.println("Unparseable using " + ft);
        }
        long diff = d1.getTime() - date.getTime();
        dateString = ft.format(d1);
        System.out.println(diff);
        if(diff > 0) {
            if (choice == 1) {
                scheduleNoteNotification(diff);
            } else if (choice == 2) {
                scheduleVoiceNotification(diff);
            } else {
                Toast.makeText(this, "something broke", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(this, "Set a date/time in the future", Toast.LENGTH_LONG).show();
        }
    }

    private void scheduleNoteNotification(long delay) {
        editor = pref.edit();
        alarmID++;
        editor.putInt("AlarmID", alarmID);
        editor.commit();
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), alarmID, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        dbHandler.updateDate(id, dateString );
        dbHandler.updateAlarm(id, alarmID);
        dbHandler.updateAlarmType(id, 1);
        Toast.makeText(getApplicationContext(), "Notification Alarm Set", Toast.LENGTH_SHORT).show();
        finish();
    }


    private void scheduleVoiceNotification(long delay) {
        editor = pref.edit();
        alarmID++;
        editor.putInt("AlarmID", alarmID);
        editor.commit();
        Intent notificationIntent = new Intent(this, AlarmPublisher.class);
        notificationIntent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), alarmID, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        dbHandler.updateDate(id, dateString );
        dbHandler.updateAlarm(id, alarmID);
        dbHandler.updateAlarmType(id, 2);
        Toast.makeText(getApplicationContext(), "Voice Alarm Set", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void cancelAlarm(View view) {


    }

    public void cancel(int currentAlarmID, int alarmType){
        if (alarmType == 1){
            Intent notificationIntent = new Intent(this, NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 20);
            notificationIntent.putExtra("id", id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), alarmID, notificationIntent, 0);
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            Toast.makeText(getApplicationContext(), "Alarm Canceled", Toast.LENGTH_SHORT).show();
            finish();
        }
        else if(alarmType == 2) {
            Intent notificationIntent = new Intent(this, AlarmPublisher.class);
            notificationIntent.putExtra(AlarmPublisher.NOTIFICATION_ID, 20);
            notificationIntent.putExtra("id", id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), currentAlarmID, notificationIntent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            finish();
        }
        else{
            Toast.makeText(this, "No alarm set", Toast.LENGTH_LONG).show();
        }
        dbHandler.updateDate(id, "null");
        dbHandler.updateAlarmType(id, -1);
        dbHandler.updateAlarm(id, -1);
        finish();
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //      POP UP ALARM ABOVE IS NOT FUNCTIONAL. REGULAR ALARM BELOW IS FUNCTIONAL
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
    public void alarm(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("notetext", noteText);
        bundle.putInt("id", id);
        Intent intent = new Intent(getApplicationContext(), Alarm.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }
*/
    public void getDate() {
       TextView alarmField = (TextView) findViewById(R.id.alarmfield);
        String date =  dbHandler.getDate(id);
        if(!date.equals("null")) {
            alarmField.setText("Alarm: " + date);
        }
        else {
            alarmField.setText("Alarm: ");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        getDate();
    }
}

