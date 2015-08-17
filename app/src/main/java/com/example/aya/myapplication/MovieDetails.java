package com.example.aya.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.app.AlertDialog;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import android.widget.ListView;
import android.widget.ListAdapter;

public class MovieDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent in = getIntent();

        int id = in.getIntExtra("ID", 0);
        Movie movie = getMovie(id);

        ImageView img = (ImageView)findViewById(R.id.imgPoster);
        img.setImageDrawable(movie.getPicture());

        final Drawable poster = movie.getPicture();
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(MovieDetails.this);
                View imgEntryView = inflater.inflate(R.layout.dialog_poster, null);
                final AlertDialog dialog = new AlertDialog.Builder(MovieDetails.this).create();

                ImageView img = (ImageView) imgEntryView.findViewById(R.id.imgLarge);
                img.setImageDrawable(poster);
                dialog.setView(imgEntryView);
                dialog.show();
                imgEntryView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
            }
        });

        TextView txtViewTitle = (TextView) findViewById(R.id.txtTitle);
        txtViewTitle.setText(movie.getTitle() + "(" + movie.getYear() + ")");

        TextView txtViewGenre = (TextView)findViewById(R.id.txtGenre);
        txtViewGenre.setText(movie.getGenre());

        TextView txtViewActor = (TextView)findViewById(R.id.txtActor);
        txtViewActor.setText(movie.getActors());

        TextView txtViewDirector = (TextView)findViewById(R.id.txtDirector);
        txtViewDirector.setText(movie.getDirector());

        TextView txtViewWriter = (TextView)findViewById(R.id.txtWriter);
        txtViewWriter.setText(movie.getWriter());

        TextView txtViewSplot = (TextView)findViewById(R.id.txtSplot);
        txtViewSplot.setText(movie.getShortPlot());

        LinearLayout partyInfo = (LinearLayout) findViewById(R.id.partyInfo);

        Button btnParty = (Button) findViewById(R.id.btnParty);

        Party party = movie.getParty();
       // Party party = new Party(new Date(2014,8,9),"22-24 Janebell Lane", (float)38.88,(float)99.99 );

        if(party == null){
            //Toast.makeText(getBaseContext(),"No Party!", Toast.LENGTH_SHORT).show();
            partyInfo.setVisibility(View.GONE);
        }
        else{
            //Toast.makeText(getBaseContext(),"Party!", Toast.LENGTH_SHORT).show();
            btnParty.setVisibility(View.GONE);
            TextView txtPartyVenue = (TextView) findViewById(R.id.txtVenue);
            txtPartyVenue.setText(party.getVenue());

            TextView txtPartyDate = (TextView) findViewById(R.id.txtDate);
            txtPartyDate.setText(party.getDate().toString());
        }

        btnParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        RatingBar ratingb = (RatingBar)findViewById(R.id.Rating);
        ratingb.setRating((float) movie.getRating());

        ratingb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                //TODO add access to database
            }
        });

        TextView txtStory = (TextView)findViewById(R.id.txtStory);
        final String fplot = movie.getFullPlot();
        txtStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MovieDetails.this, FullPlot.class);
                i.putExtra("plot", fplot);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_details, menu);
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

    private Movie getMovie(int id) {

        Movie movie;
        XmlResourceParser xmlReader = getResources().getXml(R.xml.movies);

        try {
            while (xmlReader.getEventType()!= XmlResourceParser.END_DOCUMENT) {
                if (xmlReader.getEventType()== XmlResourceParser.START_TAG) {
                    if (xmlReader.getName().equals("movie")) {
                        int mId = Integer.parseInt(xmlReader.getAttributeValue(null, "id"));
                        if (id == mId) {
                            //get title
                            String title = xmlReader.getAttributeValue(null, "title");
                            int year = Integer.parseInt(xmlReader.getAttributeValue(null, "year"));
                            double rating = Double.parseDouble(xmlReader.getAttributeValue(null, "rating"));
                            String genre = xmlReader.getAttributeValue(null, "genre");
                            String splot = xmlReader.getAttributeValue(null, "shotplot");
                            String fplot = xmlReader.getAttributeValue(null, "fullplot");
                            String actors = xmlReader.getAttributeValue(null,"actors");
                            String director = xmlReader.getAttributeValue(null,"director");
                            String writer = xmlReader.getAttributeValue(null,"writer");

                            movie = new Movie(id, title,genre,
                                    getResources().getDrawable(getResources().getIdentifier("poster_" + id, "drawable", getPackageName())),
                                    year, rating, splot, fplot, actors, writer, director);

                            xmlReader.close();
                            return movie;
                        }
                    }
                }
                xmlReader.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        xmlReader.close();
        return null;
    }
}
