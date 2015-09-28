package com.example.aya.myapplication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import android.widget.ListView;
import android.widget.ListAdapter;

public class MovieDetails extends AppCompatActivity {

    String id;
    private final int REQUEST_HOLD_PARTY = 3;
    public static final int RESULT_HOLD_PARTY =4;
    private ContentResolver resolver;
    private ImageLoader imageLoader;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        imageLoader = new ImageLoader(this);
        resolver = getContentResolver();
        Intent in = getIntent();
        id = in.getStringExtra("ID");
        position = in.getIntExtra("position", -1);

        Movie movie = getMovieDetails(id);
        setTitle(movie.getTitle());
        //Party party = getPartyInfo(id,movie.getId());
        if(movie != null){
            setMovieInfo(movie);
        }

        Button partiesButton = (Button)findViewById(R.id.btnViewParties);
        partiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MovieDetails.this, PartyListActivity.class);
                i.putExtra("movieId", id);
                startActivity(i);
            }
        });
    }

    private void setMovieInfo(Movie movie){
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
        //TODO change hold party
        Button btnParty = (Button) findViewById(R.id.btnParty);
        btnParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MovieDetails.this, HoldParty.class);
                i.putExtra("movieId",id);
                startActivityForResult(i, REQUEST_HOLD_PARTY);
            }
        });

        bindPosterEvent(movie.getPicture());

        RatingBar ratingBar = (RatingBar)findViewById(R.id.Rating);
        ratingBar.setRating(movie.getRating());
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                updateRating(id, rating);
                Intent intent = new Intent();
                intent.putExtra("rating", rating);
                intent.putExtra("position", position);
                setResult(1001, intent);
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

    private void updateRating(String id, float rating){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesDB.COLUMN_RATING, rating);
        long influenced = resolver.update(AppContentProvider.MOVIE_URI, contentValues, MoviesDB.KEY_ROWID + "= ?",new String[]{id});
        if( influenced == 0){
            Toast.makeText(MovieDetails.this, "Update failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindPosterEvent(Bitmap bitmap){
        ImageView img = (ImageView)findViewById(R.id.imgPoster);
        if(bitmap != null) {
            img.setImageBitmap(bitmap);
            final Bitmap poster = bitmap;
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = LayoutInflater.from(MovieDetails.this);
                    View imgEntryView = inflater.inflate(R.layout.dialog_poster, null);
                    final AlertDialog dialog = new AlertDialog.Builder(MovieDetails.this).create();
                    ImageView img = (ImageView) imgEntryView.findViewById(R.id.imgLarge);
                    img.setImageBitmap(poster);
                    dialog.setView(imgEntryView);
                    dialog.show();
                    imgEntryView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                }
            });
        }else {
            img.setImageDrawable(getResources().getDrawable(R.drawable.nopicture));
        }
    }
    /**
     * fetch movie details from local Database
     * @param id
     * @return
     */
    private Movie getMovieDetails(String id){

        Cursor cursor = resolver.query(AppContentProvider.MOVIE_URI, null, MoviesDB.KEY_ROWID + "= ?", new String[]{id},null,null);
        if(cursor!=null && cursor.moveToFirst()){
            String _id = cursor.getString(cursor.getColumnIndex(MoviesDB.KEY_ROWID));
            String title = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_TITLE));
            int year = cursor.getInt(cursor.getColumnIndex(MoviesDB.COLUMN_YEAR));
            float rating = cursor.getFloat(cursor.getColumnIndex(MoviesDB.COLUMN_RATING));
            String poster_url = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_POSTER));
            String shot_plot = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_SHORT_PLOT));
            String genre = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_GENRE));
            String actors = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_ACTORS));
            String writers = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_WRITERS));
            String director = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_DIRECTOR));

            Bitmap bitmap = imageLoader.getCacheBitmap(title + "_" + _id);
            if (bitmap == null) {
                bitmap = imageLoader.getUrlBitmap(poster_url);
                imageLoader.addBitmapToMemoryCache(title+"_"+_id, bitmap);
                imageLoader.saveBitmapToSD(title+"_"+_id, bitmap);
            }
            Movie movie = new Movie(id, title, genre, bitmap, year, rating, shot_plot, "", actors, writers, director);
            return movie;
        }
        else {
            return null;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_HOLD_PARTY && resultCode == HoldParty.INTENT_HOLD_PARTY){
            /*
            String inviteeList = data.getStringExtra("invitee");
            String venue = data.getStringExtra("venue");
            String datetime = data.getStringExtra("datetime");

            TextView txtVenue = (TextView)findViewById(R.id.txtVenue);
            txtVenue.setText(venue);

            TextView txtInvitee = (TextView)findViewById(R.id.txtInvitee);
            txtInvitee.setText(inviteeList);

            TextView txtDate = (TextView)findViewById(R.id.txtDate);
            txtDate.setText(datetime);

            //data.putExtra("index", id - 1);
            setResult(RESULT_HOLD_PARTY, data);
            */
        }
        /*
        if(resultCode == 1002)//add Invitee
        {
            String Invitee = data.getStringExtra("name");
            TextView txtInvitee = (TextView)findViewById(R.id.txtInvitee);
            txtInvitee.append(" "+Invitee);
        }*/
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

}
