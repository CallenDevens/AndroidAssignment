package com.example.aya.myapplication;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClubActivity extends AppCompatActivity {

    private ContentResolver resolver;
    private GoogleMap mMap;

    private ArrayList<Marker> availableClubsList = new ArrayList<Marker>();
    private ArrayList<Club> clubsList = new ArrayList<Club>();

    Cursor club_cursor;
    Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);

        myLocation = getLastKnownLocation();
        resolver = getContentResolver();
        club_cursor = resolver.query(AppContentProvider.CLUB_URI, null, null, null, null);
        setUpMap();
    }

    private void setUpMap(){
        SupportMapFragment sMapFragment;
        if(mMap == null) {
            sMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.clubMap);
            if(sMapFragment == null){
                Log.e("TEST", "getChildrenManager().findFragmentByIf fail..");
            }
            else {
                mMap = sMapFragment.getMap();
            }
        }
        addMyLocationMarker(myLocation);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 14));
        new setClubsMark().execute(null,null);
    }

    private void bindMarkerListener(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (availableClubsList.contains(marker)) {
                    new AlertDialog.Builder(ClubActivity.this)
                            .setTitle("Join club")
                            .setMessage("Join this club?")
                            .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = marker.getTitle();
                                    ContentValues values = new ContentValues();
                                    values.put(ClubsDB.COLUMN_JOINED, 1);
                                    resolver.update(AppContentProvider.CLUB_URI, values, ClubsDB.KEY_CLUB_NAME + " = ?", new String[]{name});
                                    availableClubsList.remove(marker);
                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                }
                            })
                            .create()
                            .show();
                }
                else{
                    Toast.makeText(ClubActivity.this, "You cannot join the club.", Toast.LENGTH_SHORT);
                }
                return false;
            }
        });
    }
    private void loadClubs(){
        Location myLocation = getLastKnownLocation();
        addMyLocationMarker(myLocation);


        LatLng myLocationLatlng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        String club_address;
        String club_name;
        double club_latitude;
        double club_longitude;
        int club_range = Integer.MAX_VALUE;
        int club_joined;

        if(club_cursor!= null && club_cursor.moveToFirst()){
            club_name = club_cursor.getString(club_cursor.getColumnIndex(ClubsDB.KEY_CLUB_NAME));
            club_address = club_cursor.getString(club_cursor.getColumnIndex(ClubsDB.KEY_ADDRESS));
            club_latitude = club_cursor.getDouble(club_cursor.getColumnIndex(ClubsDB.COLUMN_LATITUDE));
            club_longitude = club_cursor.getDouble(club_cursor.getColumnIndex(ClubsDB.COLUMN_LONGITUDE));
            club_joined = club_cursor.getInt(club_cursor.getColumnIndex(ClubsDB.COLUMN_JOINED));

            LatLng latLng = new LatLng(club_latitude, club_longitude);
            if(club_joined != 0){
                Marker joined_club_marker = mMap.addMarker(new MarkerOptions()
                                .title(club_name)
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                );
                joined_club_marker.showInfoWindow();
            }
            else{
                int distance = getDistance(myLocationLatlng, latLng);
                if(distance < club_range){
                    Marker avail_club_marker = mMap.addMarker(new MarkerOptions()
                                    .title(club_name)
                                    .snippet(club_address)
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    );
                    avail_club_marker.showInfoWindow();
                    availableClubsList.add(avail_club_marker);
                }
                else{
                    Marker unavail_club_marker = mMap.addMarker(new MarkerOptions()
                                    .title(club_name)
                                    .position(latLng)
                                    .snippet(club_address)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    );
                    unavail_club_marker.showInfoWindow();
                }
            }
        }
    }

    private void addMyLocationMarker(Location mylocation){
        double latitude = 0, longitude = 0;
        if(mylocation != null) {
            latitude = mylocation.getLatitude();
            longitude = mylocation.getLongitude();
        }
        LatLng latLng = new LatLng(latitude, longitude);
        Marker positionMaker = mMap.addMarker(new MarkerOptions()
                        .title("Your position")
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        positionMaker.showInfoWindow();
    }

    private Location getLastKnownLocation(){
        LocationManager mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_club, menu);
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

    private int getDistance(LatLng p1, LatLng p2){
        return DistanceUtils.getDistanceInMeters(p1, p2);
    }

    private class setClubsMark extends AsyncTask<Cursor, Void, Void>{
        // Downloading data in non-ui thread
        @Override
        protected Void doInBackground(Cursor... cursors) {
            String club_address;
            String club_name;
            double club_latitude;
            double club_longitude;
            int club_range = Integer.MAX_VALUE;
            int club_joined;

            if (club_cursor != null && club_cursor.moveToFirst()) {
                do{
                    club_name = club_cursor.getString(club_cursor.getColumnIndex(ClubsDB.KEY_CLUB_NAME));
                    club_address = club_cursor.getString(club_cursor.getColumnIndex(ClubsDB.KEY_ADDRESS));
                    club_latitude = club_cursor.getDouble(club_cursor.getColumnIndex(ClubsDB.COLUMN_LATITUDE));
                    club_longitude = club_cursor.getDouble(club_cursor.getColumnIndex(ClubsDB.COLUMN_LONGITUDE));
                    club_joined = club_cursor.getInt(club_cursor.getColumnIndex(ClubsDB.COLUMN_JOINED));
                    club_range = club_cursor.getInt(club_cursor.getColumnIndex(ClubsDB.COLUMN_RANGE));

                    boolean joined;
                    if(club_joined > 0){
                        joined = true;
                    }
                    else {
                        joined = false;
                    }
                    clubsList.add(new Club(club_address, club_name, club_latitude, club_longitude,club_range,joined));
                }while (club_cursor.moveToNext());
            }
            club_cursor.close();
            return null;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(Void voi)
        {
            for (Club club:clubsList){
                AddClubMarkTask addClubMarkTask = new AddClubMarkTask();
                addClubMarkTask.execute(club);
            }
            bindMarkerListener();

        }
    }

    private class AddClubMarkTask extends AsyncTask<Club, Void, Integer>{
        double club_latitude;
        double club_longitude;
        String club_name;
        String club_address;
        int club_range;

        @Override
        protected Integer doInBackground(Club ...clubs) {
            Club club = clubs[0];
            club_latitude = club.getClubLatitude();
            club_longitude = club.getClubLongitude();
            club_name = club.getClubName();
            club_address = club.getClubAddress();
            club_range = club.getClubRange();

            LatLng latLng = new LatLng(club_latitude, club_longitude);
            LatLng myLocationLatlng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            if (club.isClubJoined()) {
                return -1;
            } else {
                int distance = getDistance(myLocationLatlng, latLng);
                return distance;
            }
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(Integer distance)
        {
            addClubsMarker(distance);
            bindMarkerListener();

        }

        private void addClubsMarker(Integer distance){
            LatLng latLng = new LatLng(club_latitude, club_longitude);
            if (distance < 0) {
                Marker joined_club_marker = mMap.addMarker(new MarkerOptions()
                                .title(club_name)
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                );
                joined_club_marker.showInfoWindow();
            } else {
                if (distance < club_range) {
                    Marker avail_club_marker = mMap.addMarker(new MarkerOptions()
                                    .title(club_name)
                                    .snippet(club_address)
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    );
                    avail_club_marker.showInfoWindow();
                    availableClubsList.add(avail_club_marker);
                } else {
                    Marker unavail_club_marker = mMap.addMarker(new MarkerOptions()
                                    .title(club_name)
                                    .position(latLng)
                                    .snippet(club_address)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    );
                    unavail_club_marker.showInfoWindow();
                }
            }
        }
    }
}
