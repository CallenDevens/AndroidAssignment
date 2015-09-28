package com.example.aya.myapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PartyListActivity extends AppCompatActivity {

    private String movieId;
        ContentResolver resolver;
    ArrayList<Party> partyArrayList = new ArrayList<Party>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patry_list);

        resolver = getContentResolver();
        Intent i = getIntent();
        movieId = i.getStringExtra("movieId");
        initListView(movieId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patry_list, menu);
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

    private void initListView(String movieId){
        resolver = getContentResolver();
        Cursor party_cursor = resolver.query(AppContentProvider.PARTY_URI, null, PartiesDB.KEY_MOVIE_ID + " = ?", new String[]{movieId}, null);
        if(party_cursor!= null && party_cursor.moveToFirst()){
            do {

                String datetime = party_cursor.getString(party_cursor.getColumnIndex(PartiesDB.KEY_DATETIME));
                String venue = party_cursor.getString(party_cursor.getColumnIndex(PartiesDB.KEY_VENUE));
                double longitude = party_cursor.getDouble(party_cursor.getColumnIndex(PartiesDB.COLUMN_LONGITUDE));
                double latitude = party_cursor.getDouble(party_cursor.getColumnIndex(PartiesDB.COLUMN_LATITUDE));
                Calendar cal = getCalfromString(datetime);

                Party party = new Party(cal, venue, longitude, latitude);

                Cursor contact_cursor = resolver.query(AppContentProvider.PARTY_CONTACTS_URI, null,
                        ContactsDB.KEY_PARTY_VENUE + "= ? AND "+
                                ContactsDB.KEY_PARTY_DATETIME +" = ? AND "+
                                ContactsDB.KEY_PARTY_MOVIE_ID + " = ?",
                        new String[]{venue, "datetime(" + datetime+ ")", movieId}, null);
                if(contact_cursor != null && contact_cursor.moveToFirst()){
                    do{
                        String contact_name = contact_cursor.getString(contact_cursor.getColumnIndex(ContactsDB.COLUMN_NAME));
                        String contact_phone = contact_cursor.getString(contact_cursor.getColumnIndex(ContactsDB.KEY_PHONE_NUMBER));
                        party.addContact(new Contact(contact_name,contact_phone));

                    }while (contact_cursor.moveToNext());
                    contact_cursor.close();
                }

                partyArrayList.add(party);

            }while (party_cursor.moveToNext());
            party_cursor.close();
        }

        PartyAdapter adapter = new PartyAdapter(partyArrayList, this);
        ListView party_list = (ListView)findViewById(R.id.party_list);
        party_list.setAdapter(adapter);
    }

    private Calendar getCalfromString(String datetime){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date = format.parse(datetime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }catch (ParseException e){
            return Calendar.getInstance();
        }
    }
}
