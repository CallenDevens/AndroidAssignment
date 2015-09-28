package com.example.aya.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by aya on 15-9-9.
 */
public class MoviesDB {
    public static final String KEY_ROWID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_SHORT_PLOT = "short_plot";
    public static final String COLUMN_FULL_PLOT = "full_plot";
    public static final String COLUMN_GENRE = "genre";
    public static final String COLUMN_POSTER = "poster";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_ACTORS = "actors";
    public static final String COLUMN_WRITERS = "writers";
    public static final String COLUMN_DIRECTOR = "director";

    private static final String LOG_TAG = "MoviesDb";
    public static final String SQLITE_TABLE_MOVIES = "movies";

    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + SQLITE_TABLE_MOVIES + "(" +
                    KEY_ROWID         + " TEXT PRIMARY KEY NOT NULL," +
                    COLUMN_TITLE      + " TEXT NOT NULL, "+
                    COLUMN_YEAR       + " integer NOT NULL, "+
                    COLUMN_SHORT_PLOT + " TEXT, " +
                    COLUMN_FULL_PLOT  + " TEXT, " +
                    COLUMN_GENRE      + " TEXT, " +
                    COLUMN_POSTER     + " TEXT, " +
                    COLUMN_RATING     + " REAL, " +
                    COLUMN_ACTORS     + " TEXT, " +
                    COLUMN_WRITERS    + " TEXT, " +
                    COLUMN_DIRECTOR   + " TEXT " + ");";

    public static void onCreate(SQLiteDatabase db){
        Log.w(LOG_TAG, DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.w(LOG_TAG, "Upgrading database from version +" + oldVersion + " to " + newVersion
            +", old data deleted.");
        onCreate(db);
    }

}
