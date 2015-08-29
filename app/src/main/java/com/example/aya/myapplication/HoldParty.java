package com.example.aya.myapplication;
import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

public class HoldParty extends AppCompatActivity {
    private final int INTENT_ADD_MEMBER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_party);

        DatePicker dtPicker = (DatePicker) findViewById(R.id.datePicker);
        dtPicker.setMinDate(System.currentTimeMillis() - 1000);

        Button btnAddMem = (Button)findViewById(R.id.btnAddMember);
        btnAddMem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HoldParty.this, PickUpContacts.class);
                startActivityForResult(i,INTENT_ADD_MEMBER);
            }
        });


        /*
        Button btnSetVenue = (Button) findViewById(R.id.btnPickVenue);
        btnSetVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HoldParty.this, VenueActivity.class);
                startActivity(i);
            }
        });
        */
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
}
