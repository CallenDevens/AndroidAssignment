package com.example.aya.myapplication;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


public class HoldParty extends AppCompatActivity {
    public static final int INTENT_ADD_MEMBER = 1;
    public static final int INTENT_HOLD_PARTY = 2;
    public static final int INTENT_SELECT_VENUE = 3;
    ArrayList<String> invitee_names = new ArrayList<String>();
    ArrayList<String> invitee_phones = new ArrayList<String>();

    Calendar cal;
    String partyAddress = "";
    double partyLatitude;
    double partyLongitude;
    String movieId = "";

    private BroadcastReceiver sendBroadcastReceiver = null;
    private BroadcastReceiver deliveryBroadcastReceiver = null;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_party);

        Intent i = getIntent();
        movieId = i.getStringExtra("movieId");
       // Toast.makeText(HoldParty.this, movieId, Toast.LENGTH_SHORT).show();

        DatePicker dtPicker = (DatePicker) findViewById(R.id.datePicker);
        dtPicker.setMinDate(System.currentTimeMillis() - 1000);

        Button btnAddMem = (Button)findViewById(R.id.btnAddMember);
        btnAddMem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HoldParty.this, PickUpContacts.class);
                startActivityForResult(i, INTENT_ADD_MEMBER);
            }
        });

        Button btnHolfParty = (Button)findViewById(R.id.btnHoldTheParty);
        btnHolfParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (invitee_names.isEmpty()) {
                    Toast.makeText(HoldParty.this, "Please invite your friends.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(partyAddress.isEmpty()){
                    Toast.makeText(HoldParty.this, "Please select your party venue.",Toast.LENGTH_SHORT).show();
                }
                else {
                    holdParty();
                }
            }
        });

        Button btnTestMap = (Button) findViewById(R.id.testMap);
        btnTestMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HoldParty.this, PickupVenue.class);
                startActivityForResult(i, INTENT_SELECT_VENUE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == INTENT_ADD_MEMBER && resultCode == PickUpContacts.UPDATE_CONTACT)
        {
            Bundle bundle = data.getExtras();
            invitee_names = bundle.getStringArrayList("names");
            invitee_phones = bundle.getStringArrayList("phones");
        }

        if(requestCode == INTENT_SELECT_VENUE && resultCode == PickupVenue.RETURN_PARTY_VENUE){
            partyAddress = data.getStringExtra("address");
            partyLatitude = data.getDoubleExtra("latitude", 0.0);
            partyLongitude = data.getDoubleExtra("longitude", 0.0);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hold_party, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void sendMessage(String phone, String message){
        String SENT = "sms_sent";
        String DELIVERED = "sms_delivered";

        PendingIntent sentPI = PendingIntent.getActivity(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getActivity(this, 0, new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone, null, message, sentPI, deliveredPI);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if( sendBroadcastReceiver== null && deliveryBroadcastReceiver ==null){
            sendBroadcastReceiver = new BroadcastReceiver()
            {

                public void onReceive(Context arg0, Intent arg1)
                {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS Sent", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

            deliveryBroadcastReceiver = new BroadcastReceiver()
            {
                public void onReceive(Context arg0, Intent arg1)
                {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS Delivered", Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

            registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));
            registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));
        }
    }
    @Override
    protected void onDestroy()
    {
        if(sendBroadcastReceiver!= null && deliveryBroadcastReceiver!=null) {
            unregisterReceiver(sendBroadcastReceiver);
            unregisterReceiver(deliveryBroadcastReceiver);
        }
        super.onDestroy();
    }

    private void holdParty(){
        Date date = getDateFromDatePicker();

        cal=Calendar.getInstance();
        cal.setTime(date);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        String datetime = month_name + " ,"+cal.get(Calendar.DAY_OF_MONTH)+" ," + cal.get(Calendar.YEAR);

        String message = "Dear friend, we will hold a movie party at "+partyAddress+" on " + datetime +", hope you join us.";
        for (String phonenumber : invitee_phones) {
            if(!phonenumber.equals("")) {
                phonenumber = phonenumber.replaceAll(" +", "");
        //        sendMessage(phonenumber, message);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                setNotification(cal);
                insertPartyInfo();
            }
        }).start();

        finish();

    }

    private void setNotification(Calendar cal){
        Intent myIntent = new Intent(HoldParty.this, PartyNotificationService.class);
        myIntent.putExtra("latitude", partyLatitude);
        myIntent.putExtra("longitude", partyLongitude);
        myIntent.putExtra("party_time", cal.getTimeInMillis());
        myIntent.putExtra("party_address", partyAddress);

        int pending_request_code = getRequestCode();

        myIntent.putExtra("request_code", pending_request_code);

        pendingIntent = PendingIntent.getService(HoldParty.this,pending_request_code, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        //start service 2 hours before the party, redo every half hour
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()-AlarmManager.INTERVAL_HOUR*2 ,
                AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
        Log.e("TEST", "cal.getTimeinMillis:" + cal.getTimeInMillis());
    }

    private int getRequestCode(){
        ContentResolver resolver = getContentResolver();
        int request_code = 999;
        Cursor request_cursor = resolver.query(AppContentProvider.PENDING_CODE_URI, null, null, null, null);
        if(request_cursor!=null && request_cursor.moveToFirst()){
            request_code = request_cursor.getInt(request_cursor.getColumnIndex(PendingCodeDB.KEY_PENDING_CODE));
        }

        ContentValues values = new ContentValues();
        if(request_code > 100){
            values.put(PendingCodeDB.KEY_PENDING_CODE, 0);
        }
        else
        {
            int next_requestcode = request_code+1;
            values.put(PendingCodeDB.KEY_PENDING_CODE,next_requestcode);
        }
        resolver.update(AppContentProvider.PENDING_CODE_URI,values, PendingCodeDB.KEY_PENDING_CODE + " = ?", new String[]{ request_code+"" });
        return request_code;
    }
    private void insertPartyInfo(){
        Date date = getDateFromDatePicker();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");;

        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PartiesDB.KEY_MOVIE_ID, movieId);
        contentValues.put(PartiesDB.KEY_DATETIME, dateFormat.format(date));
        contentValues.put(PartiesDB.COLUMN_LATITUDE, partyLatitude);
        contentValues.put(PartiesDB.COLUMN_LONGITUDE, partyLongitude);
        contentValues.put(PartiesDB.KEY_VENUE, partyAddress);
        resolver.insert(AppContentProvider.PARTY_URI, contentValues);
        contentValues.clear();

        for(int i = 0; i < invitee_phones.size(); i++){
            contentValues.put(ContactsDB.KEY_PARTY_DATETIME, dateFormat.format(date));
            contentValues.put(ContactsDB.KEY_PARTY_MOVIE_ID, movieId);
            contentValues.put(ContactsDB.KEY_PARTY_VENUE, partyAddress);
            contentValues.put(ContactsDB.KEY_PHONE_NUMBER, invitee_phones.get(i));
            contentValues.put(ContactsDB.COLUMN_NAME, invitee_names.get(i));
            resolver.insert(AppContentProvider.PARTY_CONTACTS_URI, contentValues);
            contentValues.clear();
        }
    }


    public Date getDateFromDatePicker(){
        DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        return calendar.getTime();
    }
}