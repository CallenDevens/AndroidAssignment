package com.example.aya.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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

/**
 * Created by aya on 15-10-8.
 */
public class DistanceUtils {

    public static List<List<HashMap<String, String>>> getRoutes(LatLng position_origin, LatLng position_target){
        String url = getDirectionsUrl(position_origin, position_target);
        String data = "";
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        Log.e("TEST", "url:" + url);
        try
        {
            // Fetching the data from web service
            data = downloadUrl(url);
        } catch (Exception e)
        {
            Log.d("Background Task", e.toString());
        }

        try
        {
            jObject = new JSONObject(data);
            DirectionJsonParser parser = new DirectionJsonParser();
            routes = parser.parse(jObject);
            if(routes == null){
                Log.e("TEST", "routes is null in getRoutes");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return routes;
    }

    public static int getTimeInSeconds(LatLng position_origin, LatLng position_target){
        List<List<HashMap<String, String>>> routes = getRoutes(position_origin, position_target);

        ArrayList<LatLng> points = null;
        int time_seconds = Integer.MAX_VALUE;

        // Traversing through all the routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<LatLng>();

            List<HashMap<String, String>> path = routes.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                if (j == 0) { // Get distance from the list
                    // distance = point.get("distance");
                    continue;
                } else if (j == 1) { // Get duration from the list
                    time_seconds = Integer.parseInt(point.get("duration_seconds"));
                    continue;
                }
            }
        }

        return time_seconds;
    }

    public static String getTimeInString(LatLng position_origin, LatLng position_target){
        List<List<HashMap<String, String>>> routes = getRoutes(position_origin, position_target);

        ArrayList<LatLng> points = null;
        String duration = "";
        // Traversing through all the routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<LatLng>();
            List<HashMap<String, String>> path = routes.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                if (j == 0) { // Get distance from the list
                    // distance = point.get("distance");
                    continue;
                } else if (j == 1) { // Get duration from the list
                    duration = point.get("duration");
                    continue;
                }

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
        }
        return duration;
    }
    public static String getDistanceInString(LatLng position_origin, LatLng position_target){
        List<List<HashMap<String, String>>> routes = getRoutes(position_origin, position_target);

        ArrayList<LatLng> points = null;
        String distance = "";
        // Traversing through all the routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<LatLng>();
            List<HashMap<String, String>> path = routes.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                if (j == 0) { // Get distance from the list
                    distance = point.get("distance");
                    continue;
                } else if (j == 1) { // Get duration from the list
                    //duration = point.get("duration");
                    continue;
                }

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
        }
        return distance;
    }

    public static int getDistanceInMeters(LatLng position_origin, LatLng position_target){
        List<List<HashMap<String, String>>> routes = getRoutes(position_origin, position_target);

        ArrayList<LatLng> points = null;

        if(routes == null){
            Log.e("TEST", "routes null");
        }
        int distance_meters = 0;
        // Traversing through all the routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<LatLng>();
            List<HashMap<String, String>> path = routes.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                if (j == 0) { // Get distance from the list
                    distance_meters = Integer.parseInt(point.get("distance_meters"));
                    continue;
                } else if (j == 1) { // Get duration from the list
                    //duration = point.get("duration");
                    continue;
                }

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
        }
        return distance_meters;
    }
    private static String getDirectionsUrl(LatLng origin, LatLng dest)
    {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private static String downloadUrl(String strUrl) throws IOException
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
}
