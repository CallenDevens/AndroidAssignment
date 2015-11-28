package com.example.aya.myapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.Task;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PartyNotificationService extends Service {
    double latitude;
    double longitude;
    ArrayList<LatLng> markerPoints;
    LocationManager mLocationManager;
    long partyDateInMillis;
    private int request_code;
    private String partyAddress;

    public PartyNotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        Log.e("TEST", "onCread");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TEST", "SErvice start");
        this.markerPoints = new ArrayList<LatLng>();
        Location myLocation = this.getLastKnownLocation();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        partyDateInMillis = intent.getLongExtra("party_time", 0);
        request_code = intent.getIntExtra("request_code", 0);
        partyAddress = intent.getStringExtra("party_address");

        Log.e("TEST", "party date in millis" + partyDateInMillis);

        if (markerPoints.size() > 1) {
            markerPoints.clear();
        }

        markerPoints.add(new LatLng(latitude, longitude));
        markerPoints.add(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        // Checks, whether start and end locations are captured
        if (markerPoints.size() >= 2) {
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);


            GetTimeTask getTimeTask = new GetTimeTask();
            getTimeTask.execute(origin, dest);
        }
        return 0;
    }

    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e)
        {
            Log.d("TEST", "Exception while downloading url" + e.toString());
        } finally
        {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class GetTimeTask extends AsyncTask<LatLng, Void, Integer>
    {
        // Downloading data in non-ui thread
        @Override
        protected Integer doInBackground(LatLng... pos)
        {

            int time_seconds = DistanceUtils.getTimeInSeconds(pos[0], pos[1]);

            return time_seconds;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(Integer time_seconds)
        {
            super.onPostExecute(time_seconds);
            if((partyDateInMillis - System.currentTimeMillis() < (time_seconds+900)*1000)
                    && partyDateInMillis - System.currentTimeMillis() >0){
                Log.e("TEST", "party date millis:" + partyDateInMillis + " System.millis:" + System.currentTimeMillis()
                        + ", timeseconds to millis:" + (time_seconds + 900) * 1000);
                GetDurationTask durationTask = new GetDurationTask();
                durationTask.execute(markerPoints.get(0), markerPoints.get(1));
            }
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class GetDurationTask extends AsyncTask<LatLng, Integer, String>
    {

        // Parsing the data in non-ui thread
        @Override
        protected String doInBackground(LatLng... pos)
        {
            String duration = DistanceUtils.getTimeInString(pos[0], pos[1]);
            return duration;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(String duration)
        {
            timeNotifier(duration);
        }
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

    public void timeNotifier(String duration)
    {

        final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(this, NotificationMapActivity.class);

        notifyIntent.putExtra("duration", duration);
        notifyIntent.putExtra("party_latitude", latitude);
        notifyIntent.putExtra("party_longitude", longitude);
        notifyIntent.putExtra("party_address", partyAddress);
        notifyIntent.putExtra("request_code", request_code);

        PendingIntent intent = PendingIntent.getActivity(this, 0, notifyIntent , 0);

        Notification.Builder builder = new Notification.Builder(PartyNotificationService.this);
        builder.setSmallIcon(R.drawable.notification_template_icon_bg)
                .setTicker("Movie Party notification")
                .setContentTitle("Movie Party notification")
                .setContentText("you are " + duration + " away from the destination of an upcoming party")
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .setVibrate(new long[]{0, 500, 250})
                .setContentIntent(intent);

        Notification notification = builder.getNotification();
        notification.tickerText = "Movie Party notification";

        manager.notify(R.drawable.notification_template_icon_bg, notification);

        this.stopSelf();
    }

    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
