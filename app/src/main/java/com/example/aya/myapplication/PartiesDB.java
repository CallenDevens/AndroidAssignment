package com.example.aya.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by aya on 15-9-9.
 */
public class PartiesDB {
    public static String KEY_MOVIE_ID = "movie_id";
    public static String KEY_DATETIME = "date_time";
    public static String KEY_VENUE = "venue";
    public static String COLUMN_LATITUDE = "latitude";
    public static String COLUMN_LONGITUDE = "longitude";

    private static final String LOG_TAG = "PartiesDB";
    public static final String SQLITE_TABLE_PARTIES = "parties";

    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + SQLITE_TABLE_PARTIES + "( " +
                    KEY_MOVIE_ID + " TEXT NOT NULL, " +
                    KEY_DATETIME + " DATETIME, " +
                    KEY_VENUE    + " TEXT,"  +
                    COLUMN_LATITUDE + " REAL, "+
                    COLUMN_LONGITUDE+ " REAL," +
                    " PRIMARY KEY(" + KEY_MOVIE_ID+ "," + KEY_DATETIME +"," +KEY_VENUE+"),"+
                    " FOREIGN KEY(" + KEY_MOVIE_ID + ") REFERENCES " + MoviesDB.SQLITE_TABLE_MOVIES+"("+MoviesDB.KEY_ROWID+")"+
                    " );";

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
