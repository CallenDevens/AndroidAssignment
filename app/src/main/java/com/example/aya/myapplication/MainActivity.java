package com.example.aya.myapplication;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

public class MainActivity extends AppCompatActivity{

    private ListAdapter adapter;
    private ListView lstView;
    public  List<Movie> movies = new ArrayList<Movie>();

    private Uri MOIVE_URL = AppContentProvider.MOVIE_URI;
    private Handler handler;
    private ContentResolver reslover;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageLoader = new ImageLoader(MainActivity.this);
        reslover = getContentResolver();
        lstView = (ListView)findViewById(R.id.lstMovies);
        handler  = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // create adapter and set to listview.
                if(movies.size() != 0) {
                    adapter = new MovieAdapter(movies, MainActivity.this);
                    lstView.setAdapter(adapter);
                }
            }
        };

        insertTestData();
        insertClubData();
        if(NetworkUtils.isNetworkConnected(MainActivity.this)){
            //get movie info thourgh network
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Cursor cursor = reslover.query(MOIVE_URL, null, null, null, null);
                    if(cursor !=null){
                        if(cursor.moveToFirst()){
                            do {
                                String _id = cursor.getString(cursor.getColumnIndex(MoviesDB.KEY_ROWID));
                                String title = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_TITLE));
                                String genre = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_GENRE));
                                int year = cursor.getInt(cursor.getColumnIndex(MoviesDB.COLUMN_YEAR));
                                float rating = cursor.getFloat(cursor.getColumnIndex(MoviesDB.COLUMN_RATING));
                                String poster_url = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_POSTER));
                                Bitmap bitmap = imageLoader.getCacheBitmap(title + "_" + _id);
                                if (bitmap == null) {
                                    bitmap = imageLoader.getUrlBitmap(poster_url);
                                    if(bitmap == null){
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nopicture);
                                        Log.e("TESTIAMGE", "noimage");
                                    }else {
                                        imageLoader.addBitmapToMemoryCache(title + "_" + _id, bitmap);
                                        imageLoader.saveBitmapToSD(title + "_" + _id, bitmap);
                                    }
                                }

                                movies.add(new Movie(_id, title,genre, bitmap, year, rating));

                                //add img to SD card as cache
                                /*
                                addDrawableToMemoryCache(_id, b);
                                */
                                //Log.e("Database movie", "_id:" + _id +", title:" + title);
                            } while (cursor.moveToNext());
                        }
                        Message msg = new Message();
                        msg.obj = movies;
                        handler.sendMessage(msg);
                    }
                    cursor.close();

                }
            }).start();
        }else{
            displayCacheMovies();
        }

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Movie movie = movies.get(position);
                Intent i = new Intent(MainActivity.this, MovieDetails.class);
                i.putExtra("ID", movie.getId());
                i.putExtra("position", position);
                startActivityForResult(i, 1000);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000 && resultCode == 1001)
        {
            int index = data.getIntExtra("position", 999);
            float rating = data.getFloatExtra("rating",0);
            movies.get(index).setRating(rating);
            ((BaseAdapter)adapter).notifyDataSetChanged();
        }
        if( requestCode == 1000 && resultCode == MovieDetails.RESULT_HOLD_PARTY){
            int index = data.getIntExtra("index", 999);

            //TODO result set party?
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items t
        // o the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // get SearchView (in menu_main.xml)
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if(searchView == null){
            Log.e("SearchView", "Fail to get Search View.");
            return true;
        }
        searchView.setIconifiedByDefault(true);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName cn = new ComponentName(this,SearchableActivity.class);
        SearchableInfo info = searchManager.getSearchableInfo(cn);
        if(info == null){
            Log.e("SearchableInfo","Fail to get search info.");
        }
        // link searchView with SearchableActivity.info
        searchView.setSearchableInfo(info);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_search){
            //add input area to actionbar
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clubs) {

            Intent intent = new Intent(MainActivity.this, ClubActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //get image from Internet


    //TODO rewirte to fetch info from memoryCache
    private void displayCacheMovies() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = reslover.query(MOIVE_URL, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            String _id = cursor.getString(cursor.getColumnIndex(MoviesDB.KEY_ROWID));
                            String title = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_TITLE));
                            int year = cursor.getInt(cursor.getColumnIndex(MoviesDB.COLUMN_YEAR));
                            float rating = cursor.getFloat(cursor.getColumnIndex(MoviesDB.COLUMN_RATING));
                            String genre = cursor.getString(cursor.getColumnIndex(MoviesDB.COLUMN_GENRE));

                            Bitmap bitmap = imageLoader.getCacheBitmap(title+"_"+_id);
                            if(bitmap == null){
                                //using default poster
                                //Log.e("POSTER", "using default poster");
                                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nopicture);
                            }
                            else {
                                //Log.e("POSTER", "d is not null");
                            }
                            movies.add(new Movie(_id, title, genre, bitmap, year, rating));
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                if(movies.size()!=0) {
                    Message msg = new Message();
                    msg.obj = movies;
                    handler.sendMessage(msg);
                }
            }
        }).start();

    }


    private void insertTestData(){
        reslover.delete(MOIVE_URL, null, null);

        ContentValues values = new ContentValues();

        values.put(MoviesDB.KEY_ROWID, "tt1392190");
        values.put(MoviesDB.COLUMN_TITLE, "Mad Max: Fury Road");
        values.put(MoviesDB.COLUMN_YEAR, 2015);
        values.put(MoviesDB.COLUMN_SHORT_PLOT, "In a stark desert landscape where humanity is broken, two rebels just might be able to restore order: Max, a man of action and of few words, and Furiosa, a woman of action who is looking to make it back to her childhood homeland.");
        values.put(MoviesDB.COLUMN_FULL_PLOT,  "An apocalyptic story set in the furthest reaches of our planet, in a stark desert landscape where humanity is broken, and almost everyone is crazed fighting for the necessities of life. Within this world exist two rebels on the run who just might be able to restore order. There's Max, a man of action and a man of few words, who seeks peace of mind following the loss of his wife and child in the aftermath of the chaos. And Furiosa, a woman of action and a woman who believes her path to survival may be achieved if she can make it across the desert back to her childhood homeland.");
        values.put(MoviesDB.COLUMN_GENRE,"Action, Adventure, Sci-Fi");
        values.put(MoviesDB.COLUMN_POSTER, "http://ia.media-imdb.com/images/M/MV5BMTUyMTE0ODcxNF5BMl5BanBnXkFtZTgwODE4NDQzNTE@._V1_SX300.jpg");
        values.put(MoviesDB.COLUMN_RATING, 5);
        values.put(MoviesDB.COLUMN_ACTORS, "Tom Hardy, Charlize Theron, Nicholas Hoult, Hugh Keays-Byrne");
        values.put(MoviesDB.COLUMN_WRITERS, "George Miller, Brendan McCarthy, Nick Lathouris");
        values.put(MoviesDB.COLUMN_DIRECTOR, "George Miller");

        reslover.insert(MOIVE_URL, values);
        values.clear();

        values.put(MoviesDB.KEY_ROWID, "tt1638355");
        values.put(MoviesDB.COLUMN_TITLE, "The Man from U.N.C.L.E.");
        values.put(MoviesDB.COLUMN_YEAR, 2015);
        values.put(MoviesDB.COLUMN_SHORT_PLOT, "In the early 1960s, CIA agent Napoleon Solo and KGB operative Illya Kuryakin participate in a joint mission against a mysterious criminal organization, which is working to proliferate nuclear weapons.");
        values.put(MoviesDB.COLUMN_FULL_PLOT,  "2015 has been a great year for espionage films. From the beginning of the year, there was Kingsmen: Secret Service. Just this past summer, there was Spy followed by Mission:Impossible - Rogue Nation. All of these were very successful, both commercially and critically. And now, another spy film is gunning to join that illustrious list. The Man from U.N.C.L.E. was originally a TV series from the mid-1960s developed by Sam Rolfe. It starred Robert Vaughn (as American agent Napoleon Solo) and David McCallum (as Russian agent Illya Kuryakin). U.N.C.L.E. was an acronym for the United Network Command for Law and Enforcement, a secret international counter-espionage organization, aiming to maintain worldwide political and legal order. This series lasted for four years from 1964 to 68, becoming a cultural icon of sorts at that time with its audacious theme of US-Russian cooperation at the height of the Cold War.");
        values.put(MoviesDB.COLUMN_GENRE,  "Action, Adventure, Comedy");
        values.put(MoviesDB.COLUMN_POSTER, "http://ia.media-imdb.com/images/M/MV5BMTc2NjQ4ODYyNF5BMl5BanBnXkFtZTgwODA3OTU5NTE@._V1_SX300.jpg");
        values.put(MoviesDB.COLUMN_RATING, 0);
        values.put(MoviesDB.COLUMN_ACTORS, "Alicia Vikander, Henry Cavill, Armie Hammer, Hugh Grant");
        values.put(MoviesDB.COLUMN_WRITERS, "Guy Ritchie (screenplay), Lionel Wigram (screenplay), Jeff Kleeman (story), David C. Wilson (story), Guy Ritchie (story), Lionel Wigram (story), Sam Rolfe (based on the television series by)");
        values.put(MoviesDB.COLUMN_DIRECTOR, "Guy Ritchie");

        reslover.insert(MOIVE_URL, values);
        values.clear();

        values.put(MoviesDB.KEY_ROWID, "tt0111161");
        values.put(MoviesDB.COLUMN_TITLE, "The Shawshank Redemption");
        values.put(MoviesDB.COLUMN_YEAR, 1994);
        values.put(MoviesDB.COLUMN_SHORT_PLOT, "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.");
        values.put(MoviesDB.COLUMN_FULL_PLOT, "Andy Dufresne is a young and successful banker whose life changes drastically when he is convicted and sentenced to life imprisonment for the murder of his wife and her lover. Set in the 1940s, the film shows how Andy, with the help of his friend Red, the prison entrepreneur, turns out to be a most unconventional prisoner.");
        values.put(MoviesDB.COLUMN_GENRE,  "Crime, Drama");
        values.put(MoviesDB.COLUMN_POSTER, "http://ia.media-imdb.com/images/M/MV5BODU4MjU4NjIwNl5BMl5BanBnXkFtZTgwMDU2MjEyMDE@._V1_SX300.jpg");
        values.put(MoviesDB.COLUMN_RATING, 4);
        values.put(MoviesDB.COLUMN_ACTORS,"Tim Robbins, Morgan Freeman, Bob Gunton, William Sadler");
        values.put(MoviesDB.COLUMN_WRITERS, "Stephen King (short story &quot;Rita Hayworth and Shawshank Redemption&quot;),Frank Darabont (screenplay)");
        values.put(MoviesDB.COLUMN_DIRECTOR, "Frank Darabont");

        reslover.insert(MOIVE_URL, values);
        values.clear();

        values.put(MoviesDB.KEY_ROWID, "tt2294629");
        values.put(MoviesDB.COLUMN_TITLE, "Frozen");
        values.put(MoviesDB.COLUMN_YEAR,       2014);
        values.put(MoviesDB.COLUMN_SHORT_PLOT, "When Seth Posner suffers a brain injury at work he is left in a vegetative state, existing in the world of his memories, dreams and imagination. His wife, Sarah, and best friend Jordan's ...");
        values.put(MoviesDB.COLUMN_FULL_PLOT,  "Anna, a fearless optimist, sets off on an epic journey - teaming up with rugged mountain man Kristoff and his loyal reindeer Sven - to find her sister Elsa, whose icy powers have trapped the kingdom of Arendelle in eternal winter. Encountering Everest-like conditions, mystical trolls and a hilarious snowman named Olaf, Anna and Kristoff battle the elements in a race to save the kingdom. From the outside Anna's sister, Elsa looks poised, regal and reserved, but in reality, she lives in fear as she wrestles with a mighty secret-she was born with the power to create ice and snow. It's a beautiful ability, but also extremely dangerous. Haunted by the moment her magic nearly killed her younger sister Anna, Elsa has isolated herself, spending every waking minute trying to suppress her growing powers. Her mounting emotions trigger the magic, accidentally setting off an eternal winter that she can't stop. She fears she's becoming a monster and that no one, not even her sister, can help her. ");
        values.put(MoviesDB.COLUMN_GENRE,      "Short, Drama" );
        values.put(MoviesDB.COLUMN_POSTER,     "http://ia.media-imdb.com/images/M/MV5BMTQ1MjQwMTE5OF5BMl5BanBnXkFtZTgwNjk3MTcyMDE@._V1_SX300.jpg");
        values.put(MoviesDB.COLUMN_RATING,     4.0);
        values.put(MoviesDB.COLUMN_ACTORS,     "Kelly Jackson, Samuel Dent Chapman, Christopher Bird, Ian Richardson");
        values.put(MoviesDB.COLUMN_WRITERS,    "James Johnson");
        values.put(MoviesDB.COLUMN_DIRECTOR,   "Chris Austen");

        reslover.insert(MOIVE_URL, values);
        values.clear();

        values.put(MoviesDB.KEY_ROWID        , "tt1690953");
        values.put(MoviesDB.COLUMN_TITLE     , "Despicable Me 2");
        values.put(MoviesDB.COLUMN_YEAR      , 2013 );
        values.put(MoviesDB.COLUMN_SHORT_PLOT, "When Gru, the world's most super-bad turned super-dad has been recruited by a team of officials to stop lethal muscle and a host of Gru's own, He has to fight back with new gadgetry, cars, and more minion madness.");
        values.put(MoviesDB.COLUMN_FULL_PLOT , "While Gru, the ex-supervillain is adjusting to family life and an attempted honest living in the jam business, a secret Arctic laboratory is stolen. The Anti-Villain League decides it needs an insider's help and recruits Gru in the investigation. Together with the eccentric AVL agent, Lucy Wilde, Gru concludes that his prime suspect is the presumed dead supervillain, El Macho, whose his teenage son is also making the moves on his eldest daughter, Margo. Seemingly blinded by his overprotectiveness of his children and his growing mutual attraction to Lucy, Gru seems on the wrong track even as his minions are being quietly kidnapped en masse for some malevolent purpose.");
        values.put(MoviesDB.COLUMN_GENRE     , "Animation, Comedy, Family");
        values.put(MoviesDB.COLUMN_POSTER    , "http://ia.media-imdb.com/images/M/MV5BMjExNjAyNTcyMF5BMl5BanBnXkFtZTgwODQzMjQ3MDE@._V1_SX300.jpg");
        values.put(MoviesDB.COLUMN_RATING    , 3.5);
        values.put(MoviesDB.COLUMN_ACTORS    , "Steve Carell, Kristen Wiig, Benjamin Bratt, Miranda Cosgrove");
        values.put(MoviesDB.COLUMN_WRITERS   , "Cinco Paul, Ken Daurio");
        values.put(MoviesDB.COLUMN_DIRECTOR  , "Pierre Coffin, Chris Renaud");
        reslover.insert(MOIVE_URL, values);
        values.clear();

        values.put(MoviesDB.KEY_ROWID, "tt0816692");
        values.put(MoviesDB.COLUMN_TITLE     , "Interstellar");
        values.put(MoviesDB.COLUMN_YEAR      , 2014);
        values.put(MoviesDB.COLUMN_SHORT_PLOT, "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.");
        values.put(MoviesDB.COLUMN_FULL_PLOT , "In the near future, Earth has been devastated by drought and famine, causing a scarcity in food and extreme changes in climate. When humanity is facing extinction, a mysterious rip in the space-time continuum is discovered, giving mankind the opportunity to widen its lifespan. A group of explorers must travel beyond our solar system in search of a planet that can sustain life. The crew of the Endurance are required to think bigger and go further than any human in history as they embark on an interstellar voyage into the unknown. Coop, the pilot of the Endurance, must decide between seeing his children again and the future of the human race. ");
        values.put(MoviesDB.COLUMN_GENRE     , "Adventure, Drama, Sci-Fi");
        values.put(MoviesDB.COLUMN_POSTER    , "http://ia.media-imdb.com/images/M/MV5BMjIxNTU4MzY4MF5BMl5BanBnXkFtZTgwMzM4ODI3MjE@._V1_SX300.jpg" );
        values.put(MoviesDB.COLUMN_RATING    , 0);
        values.put(MoviesDB.COLUMN_ACTORS    , "Ellen Burstyn, Matthew McConaughey, Mackenzie Foy, John Lithgow");
        values.put(MoviesDB.COLUMN_WRITERS   , "Jonathan Nolan, Christopher Nolan");
        values.put(MoviesDB.COLUMN_DIRECTOR  , "Christopher Nolan");
        reslover.insert(MOIVE_URL, values);
        values.clear();

        values.put(MoviesDB.KEY_ROWID, "tt2084970");
        values.put(MoviesDB.COLUMN_TITLE     , "The Imitation Game");
        values.put(MoviesDB.COLUMN_YEAR      , 2014);
        values.put(MoviesDB.COLUMN_SHORT_PLOT, "During World War II, mathematician Alan Turing tries to crack the enigma code with help from fellow mathematicians.");
        values.put(MoviesDB.COLUMN_FULL_PLOT , "Based on the real life story of legendary cryptanalyst Alan Turing, the film portrays the nail-biting race against time by Turing and his brilliant team of code-breakers at Britain's top-secret Government Code and Cypher School at Bletchley Park, during the darkest days of World War II.");
        values.put(MoviesDB.COLUMN_GENRE     , "Biography, Drama, Thriller");
        values.put(MoviesDB.COLUMN_POSTER    , "http://ia.media-imdb.com/images/M/MV5BNDkwNTEyMzkzNl5BMl5BanBnXkFtZTgwNTAwNzk3MjE@._V1_SX300.jpg");
        values.put(MoviesDB.COLUMN_RATING    , 3);
        values.put(MoviesDB.COLUMN_ACTORS    , "Benedict Cumberbatch, Keira Knightley, Matthew Goode, Rory Kinnear");
        values.put(MoviesDB.COLUMN_WRITERS   , "Graham Moore, Andrew Hodges (book)");
        values.put(MoviesDB.COLUMN_DIRECTOR  , "Morten Tyldum");
        reslover.insert(MOIVE_URL, values);
    }

    private void insertClubData(){
        ContentValues values = new ContentValues();

        values.put(ClubsDB.KEY_ADDRESS      ,"22-24 JaneBell Lane, Melbourne VIC 3000");
        values.put(ClubsDB.KEY_CLUB_NAME    , "QV Square");
        values.put(ClubsDB.COLUMN_LATITUDE  ,-37.81020);
        values.put(ClubsDB.COLUMN_LONGITUDE, 144.96620);
        reslover.insert(AppContentProvider.CLUB_URI, values);

        values.put(ClubsDB.KEY_ADDRESS, "211 La Trobe St, Melbourne VIC 3000");
        values.put(ClubsDB.KEY_CLUB_NAME    , "Melbourne Central");
        values.put(ClubsDB.COLUMN_LATITUDE  ,-37.81092);
        values.put(ClubsDB.COLUMN_LONGITUDE, 144.96280);
        reslover.insert(AppContentProvider.CLUB_URI, values);
        values.clear();

        values.put(ClubsDB.KEY_ADDRESS, "Swanston St & La Trobe St, Melbourne VIC 3000");
        values.put(ClubsDB.KEY_CLUB_NAME, "Hoyts Melbourne Central");
        values.put(ClubsDB.COLUMN_LATITUDE, -37.80964);
        values.put(ClubsDB.COLUMN_LONGITUDE, 144.96390);
        reslover.insert(AppContentProvider.CLUB_URI, values);
        values.clear();

        values.put(ClubsDB.KEY_ADDRESS, "252 Swanston St, Melbourne VIC 3000");
        values.put(ClubsDB.KEY_CLUB_NAME, "Rooftop Cinema");
        values.put(ClubsDB.COLUMN_LATITUDE  ,-37.81206);
        values.put(ClubsDB.COLUMN_LONGITUDE, 144.96512);
        reslover.insert(AppContentProvider.CLUB_URI, values);
        values.clear();


        values.put(ClubsDB.KEY_ADDRESS, "45 Collins St, Melbourne VIC 3000");
        values.put(ClubsDB.KEY_CLUB_NAME    , "Kino Cinemas");
        values.put(ClubsDB.COLUMN_LATITUDE  ,-37.81405);
        values.put(ClubsDB.COLUMN_LONGITUDE, 144.97221);
        reslover.insert(AppContentProvider.CLUB_URI, values);
        values.clear();

        values.put(ClubsDB.KEY_ADDRESS      , "1/194-200 Bourke St, Melbourne VIC 3000");
        values.put(ClubsDB.KEY_CLUB_NAME    , "Chinatown Cinema");
        values.put(ClubsDB.COLUMN_LATITUDE  ,-37.81244);
        values.put(ClubsDB.COLUMN_LONGITUDE ,144.96728);
        reslover.insert(AppContentProvider.CLUB_URI, values);
        values.clear();

        values.put(ClubsDB.KEY_ADDRESS      , "Rathdowne St, Carlton VIC 3053");
        values.put(ClubsDB.KEY_CLUB_NAME    , "IMAX Melbourne Museum");
        values.put(ClubsDB.COLUMN_LATITUDE  , -37.79987);
        values.put(ClubsDB.COLUMN_LONGITUDE , 144.97002);
        reslover.insert(AppContentProvider.CLUB_URI, values);
    }
}