package com.example.aya.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class NotificationMapActivity extends AppCompatActivity {
    GoogleMap mMap;
    double party_latitude;
    double party_longitude;
    String partyAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_map);

        setUpMapIfNeed();

    }
    private void setUpMapIfNeed(){
        SupportMapFragment sMapFragment;
        if(mMap == null) {
            sMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.noifyMap);
            if(sMapFragment == null){
                Log.e("TEST", "getChildrenManager().findFragmentByIf fail..");
            }
            else{
                mMap = sMapFragment.getMap();
            }
        }
        if(mMap != null) {
            setUpMap();
        }
    }

    private void setUpMap(){
        mMap.clear();
        Intent intent = getIntent();
        party_latitude = intent.getDoubleExtra("party_latitude", 0.0);
        party_longitude = intent.getDoubleExtra("party_longitude", 0.0);
        partyAddress = intent.getStringExtra("party_address");
        String duration = intent.getStringExtra("duration");
        int request_code = intent.getIntExtra("request_code", 0);

        LatLng latLng = new LatLng(party_latitude, party_longitude);
        Marker positionMaker = mMap.addMarker(new MarkerOptions()
                        .title(partyAddress)
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        positionMaker.showInfoWindow();

        Intent myIntent = new Intent(this, PartyNotificationService.class);

        //Cancel the alarm
        PendingIntent pendingIntent = PendingIntent.getService(this,request_code, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendingIntent);
        Toast.makeText(NotificationMapActivity.this, "You are " + duration + " away from an upcoming party", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification_map, menu);
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
