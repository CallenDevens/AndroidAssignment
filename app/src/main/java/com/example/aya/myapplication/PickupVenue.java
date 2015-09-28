package com.example.aya.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.location.Location;
import android.location.LocationManager;
import android.app.FragmentTransaction;
import android.app.ActionBar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class PickupVenue extends AppCompatActivity {

    GoogleMap mMap = null;
    LocationManager mLocationManager;
    ContentResolver contentResolver = null;
    Location partyLocation = null;
    Location myLocation = null;
    Marker partyMaker = null;
    public static final int RETURN_PARTY_VENUE = 2015;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_venue);
        setUpMapIfNeed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pickup_venue, menu);

        final EditText edtAddress = (EditText) findViewById(R.id.edtSelectVenue);
        edtAddress.setMaxWidth(edtAddress.getWidth());
        Button btnOK = (Button)findViewById(R.id.btnSearchForVenue);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        reputMyLocationMaker();
                    }
                });
                String addressEntered = edtAddress.getText().toString();
                if (addressEntered.equals("")) {
                    Toast.makeText(getBaseContext(), "Please enter party address", Toast.LENGTH_SHORT).show();
                    return;
                }
                String url = "https://maps.googleapis.com/maps/api/geocode/json?";

                try {
                    addressEntered = URLEncoder.encode(addressEntered, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String address = "address=" + addressEntered;
                String sensor = "sensor=false";

                url = url + address + "&" + sensor;
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        });
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

    private void setUpMapIfNeed(){
        SupportMapFragment sMapFragment;
        if(mMap == null) {
            sMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragMap);
            if(sMapFragment == null){
                Log.e("TEST", "getChildrenManager().findFragmentByIf fail..");
            }
            else{
                mMap = sMapFragment.getMap();
                //Log.e("TEST", "getChildrenManager().findFragmentByIf success..");
                //mMap.setMyLocationEnabled(true);
                setUpMap(mMap);
            }
        }
    }

    private void setUpMap(GoogleMap mMap){
        myLocation = getLastKnownLocation();
        double latitude = 0, longitude = 0;
        if(myLocation != null) {
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
        }
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        mMap.addMarker(new MarkerOptions()
                        .title("Your position")
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );
    }

    private void reputMyLocationMaker(){
        mMap.clear();
        myLocation = getLastKnownLocation();
        double latitude = 0, longitude = 0;
        if(myLocation != null) {
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
        }
        LatLng latLng = new LatLng(latitude, longitude);
        Marker positionMaker = mMap.addMarker(new MarkerOptions()
                        .title("Your position")
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );
        positionMaker.showInfoWindow();

    }

    private Location getLastKnownLocation(){
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
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

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        String data = null;
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while((line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        }catch(Exception e){
            Log.d("Exception downloading", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            GeocodeJSONParser parser = new GeocodeJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a an ArrayList */
                places = parser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){

            HashMap<String, String> place = list.get(0);
            MarkerOptions markerOptions = new MarkerOptions();
            String name = place.get("formatted_address");
            double lat = Double.parseDouble(place.get("lat"));
            double lng = Double.parseDouble(place.get("lng"));
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(name);
            partyMaker = mMap.addMarker(markerOptions);
            partyMaker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));



            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    new AlertDialog.Builder(PickupVenue.this)
                            .setTitle("Party Venue")
                            .setMessage("set "+ partyMaker.getTitle() +"as party venue?")
                            .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    LatLng position = marker.getPosition();
                                    Intent i = new Intent();
                                    i.putExtra("address", partyMaker.getTitle());
                                    i.putExtra("latitude", position.latitude);
                                    i.putExtra("longitude", position.longitude);
                                    setResult(RETURN_PARTY_VENUE, i);
                                    finish();
                                }
                            })
                            .create()
                            .show();

                    return false;
                }
            });
        }
    }

    //not working for getFromLocationName always returns null
    /*
    private Location getLocationByAddress(String addressString){
        Log.e("TEST", addressString);
        //TODO delete foe test
        addressString= "22-24 Jane Bell lane";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = null;
        List<Address> addresses = null;
        try{
            addresses = geocoder.getFromLocationName(addressString, 2);
            while (addresses.size() == 0){
                addresses = geocoder.getFromLocationName(addressString, 2);
            }
            if (addresses.size()>0) {
                Address address = addresses.get(0);
                double longitude = address.getLongitude();
                double latitude = address.getLatitude();
                location = new Location(addressString);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return location;
    }

*/
}