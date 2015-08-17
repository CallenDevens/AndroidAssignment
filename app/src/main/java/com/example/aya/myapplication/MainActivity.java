package com.example.aya.myapplication;

import android.app.ListActivity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends ListActivity {

    public List<Movie> movies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movies= getMovies();

        ListAdapter adapter = new MovieAdapter(movies, this);
        ListView lstView = getListView();
        lstView.setAdapter(adapter);
        lstView.setTextFilterEnabled(true);

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Movie movie = movies.get(position);

                Intent i = new Intent(MainActivity.this, MovieDetails.class);
                i.putExtra("ID", movie.getId());
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private List<Movie> getMovies() {

        List<Movie> movieList = new ArrayList<Movie>();
        XmlResourceParser xmlReader = getResources().getXml(R.xml.movies);

        try {
            while (xmlReader.getEventType()!= XmlResourceParser.END_DOCUMENT) {
                if (xmlReader.getEventType()== XmlResourceParser.START_TAG)
                    if (xmlReader.getName().equals("movie")) {
                        Map<String, Movie> map = new HashMap<String, Movie>();
                        // get movie identifier
                        Integer id = Integer.parseInt(xmlReader.getAttributeValue(null, "id"));
                        //get title
                        String title = xmlReader.getAttributeValue(null, "title");

                        //set movie poster image
                        //String url = xmlReader.getAttributeValue(null,"poster");
                        //Drawable poster = new BitmapDrawable(getUrlBitmap(url));

                        int year = Integer.parseInt(xmlReader.getAttributeValue(null, "year"));
                        double rating = Double.parseDouble(xmlReader.getAttributeValue(null, "rating"));

                        movieList.add(
                                new Movie(id, title,
                                        getResources().getDrawable(getResources().getIdentifier("poster_"+id, "drawable",getPackageName())),
                                        year, rating));
                    }
                xmlReader.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        xmlReader.close();
        return movieList;
    }

    //get image from Internet
    private Bitmap getUrlBitmap(String url) {
        Bitmap PosterBitmap = null;
        try {
            HttpURLConnection connect =  (HttpURLConnection)new URL(url).openConnection();
            connect.setDoInput(true);
            connect.connect();
            InputStream is = connect.getInputStream();
            PosterBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return PosterBitmap;
    }
}
