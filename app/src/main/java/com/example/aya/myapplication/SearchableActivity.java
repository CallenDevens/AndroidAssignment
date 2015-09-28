package com.example.aya.myapplication;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.search.SearchAdRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchableActivity extends AppCompatActivity {

    private String IMDB_ID_PATTERN = "tt\\d{7}";
    private final String OMDP_API_URL = "http://www.omdbapi.com/";
    private ImageLoader imageLoader = new ImageLoader(this);
    private ContentResolver reslover;

    private ListAdapter adapter;
    private ListView lstView;
    private List<Movie> movies;
    private String query;

    private IntentFilter intentFilter;
    private ConnectionChangeReceiver connectionChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        reslover = getContentResolver();

        movies  = new ArrayList<Movie>();
        lstView = (ListView)findViewById(R.id.lstResults);
        handleIntent(getIntent());

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        connectionChangeReceiver = new ConnectionChangeReceiver();
        registerReceiver(connectionChangeReceiver,intentFilter);
    }

    @Override
    protected  void onNewIntent(Intent intent){
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent){
        movies.clear();
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            query = intent.getStringExtra((SearchManager.QUERY));
            //TODO change query! for test
            //query = "tt0468569";

            if(searchMovieFromDatabase(query)){

            }
            else if (NetworkUtils.isNetworkConnected(this)) {
                new SearchTask().execute(query);
            }
            else{
                Toast.makeText(SearchableActivity.this, "no result...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    boolean searchMovieFromDatabase(String query){
        String [] projection = new String[] {MoviesDB.KEY_ROWID, MoviesDB.COLUMN_TITLE,MoviesDB.COLUMN_GENRE,
        MoviesDB.COLUMN_YEAR, MoviesDB.COLUMN_POSTER, MoviesDB.COLUMN_RATING};

        Cursor cursor = reslover.query(AppContentProvider.MOVIE_URI, projection,
                MoviesDB.KEY_ROWID + " = ? OR  "  + MoviesDB.COLUMN_TITLE + " LIKE ? ", new String[]{query, "%" + query + "%"}, null, null);

        if(cursor == null){
            return false;
        }
        else {
            if(cursor.moveToFirst()) {
                do{
                    String _id = cursor.getString(cursor.getColumnIndex(MoviesDB.KEY_ROWID));
                    String title = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_TITLE));
                    String genre = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_GENRE));
                    String poster_url = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_POSTER));
                    int year = cursor.getInt(cursor.getColumnIndex(MoviesDB.COLUMN_YEAR));
                    float rating = cursor.getFloat(cursor.getColumnIndex(MoviesDB.COLUMN_RATING));

                    Bitmap bitmap = imageLoader.getCacheBitmap(title + "_" + _id);
                    if (bitmap == null) {
                        bitmap = imageLoader.getUrlBitmap(poster_url);
                        if(bitmap == null){
                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nopicture);
                        }
                    }
                    movies.add(new Movie(_id, title, genre, bitmap, year, rating));
                }while (cursor.moveToNext());
                cursor.close();

                adapter = new MovieAdapter(movies, this);
                lstView.setAdapter(adapter);
                ((BaseAdapter) adapter).notifyDataSetChanged();
                return true;
            }
           else {
                //Toast.makeText(SearchableActivity.this, "cursor.movetofirst not pass ",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_searchable, menu);
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


    class SearchTask extends AsyncTask<String, Integer, Movie> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Movie doInBackground(String ...keywords) {
            Pattern p = Pattern.compile(IMDB_ID_PATTERN);
            Matcher m = p.matcher(keywords[0]);
            boolean match = m.matches();
            Movie movie = null;

            if(match){
                movie = getMovieFromOMDP(OMDP_API_URL + "?i=" + keywords[0] +"&plot=short&r=json");
            }
            else{
                String key = keywords[0].replaceAll("\\s+", "+");
                movie = getMovieFromOMDP(OMDP_API_URL + "?t=" + key +"&plot=short&r=json");
            }

            return movie;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //invoked when doInBackground calls publishProgress
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Movie result) {
            //invoked after doInBackground finishes
            super.onPostExecute(result);
            if(result != null) {
                movies.add(result);
                adapter = new MovieAdapter(movies, SearchableActivity.this);
                lstView.setAdapter(adapter);
                ((BaseAdapter) adapter).notifyDataSetChanged();
            }
            else
            {
                Toast.makeText(SearchableActivity.this, "No results...", Toast.LENGTH_SHORT).show();

            }
        }

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(connectionChangeReceiver);
    }

    private String getJSONFromUrl(String url) {
        final String TAG = "JsonParser.java";
        String line = "";
        try {
            StringBuilder sb = new StringBuilder();
            HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
            connect.setDoInput(true);
            connect.connect();
            InputStream is = connect.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            //Log.e("TEST", sb.toString());
            return sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private Movie getMovieFromOMDP(String url){
        String jsonString = getJSONFromUrl(url);
        Movie movie = null;

        try {
            /*
            JSONArray array = new JSONArray(jsonString);
            */

            JSONObject object = new JSONObject(jsonString);
            String response = object.getString("Response");
            if(response.equals("False")){
                return null;
            }

            String _id    =object.getString("imdbID");
            String title  = object.getString("Title");
            int    year   = Integer.parseInt(object.getString("Year"));
            String poster_url = object.getString("Poster");
            String shot_plot = object.getString("Plot");
            String genre = object.getString("Genre");
            String writers = object.getString("Writer");
            String director = object.getString("Director");
            String actors = object.getString("Actors");

            Bitmap bitmap = null;
            Cursor cursor = reslover.query(AppContentProvider.MOVIE_URI, new String[]{MoviesDB.KEY_ROWID},
                    MoviesDB.KEY_ROWID + " =? " , new String[]{_id}, null, null);

            if (cursor == null || !cursor.moveToFirst()) {
                insertMovieIntoDatabase(_id, title, year, poster_url, shot_plot, genre,
                        writers, director, actors);
                bitmap = imageLoader.getCacheBitmap(title + "_" + _id);
                if (bitmap == null) {
                    bitmap = imageLoader.getUrlBitmap(poster_url);
                    imageLoader.addBitmapToMemoryCache(title+"_"+_id, bitmap);
                    imageLoader.saveBitmapToSD(title + "_" + _id, bitmap);
                }
            }
            else
            {
                //TODO delete for test
                //Log.e("TEST", "_id"+ _id +", title:"+title);
            }
            movie = new Movie(_id, title, genre , bitmap, year, 0);

            cursor.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movie;
    }

    private void insertMovieIntoDatabase(String _id, String title, int year,
                                    String poster, String shot_plot, String genre,
                                    String writers, String director, String actors){

        ContentValues values = new ContentValues();

        values.put(MoviesDB.KEY_ROWID        , _id);
        values.put(MoviesDB.COLUMN_TITLE     , title);
        values.put(MoviesDB.COLUMN_YEAR      , year);
        values.put(MoviesDB.COLUMN_SHORT_PLOT, shot_plot);
        values.put(MoviesDB.COLUMN_GENRE     , genre);
        values.put(MoviesDB.COLUMN_POSTER    , poster);
        values.put(MoviesDB.COLUMN_RATING    , 0);
        values.put(MoviesDB.COLUMN_ACTORS    , actors);
        values.put(MoviesDB.COLUMN_WRITERS   , writers);
        values.put(MoviesDB.COLUMN_DIRECTOR  , director);

        reslover.insert(AppContentProvider.MOVIE_URI, values);
        values.clear();
    }

    private class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null) {
                Toast.makeText(SearchableActivity.this, "Network connected.", Toast.LENGTH_SHORT).show();
                new SearchTask().execute(query);
            }
        }
    }

}
