package com.example.aya.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by aya on 15-9-9.
 */
public class ContactsDB {
    public static String KEY_PARTY_MOVIE_ID = "party_movie_id";
    public static String KEY_PARTY_DATETIME = "party_date_time";
    public static String KEY_PARTY_VENUE = "party_venue";
    public static String KEY_PHONE_NUMBER = "phone_number";
    public static String COLUMN_NAME = "name";

    private static final String LOG_TAG = "ContactsDB";
    public static String SQLITE_TABLE_CONTACTS = "contacts";

    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + SQLITE_TABLE_CONTACTS + "(" +
                    KEY_PARTY_MOVIE_ID + " TEXT NOT NULL, " +
                    KEY_PARTY_DATETIME + " DATETIME NOT NULL, "+
                    KEY_PARTY_VENUE    + " TEXT NOT NULL," +
                    KEY_PHONE_NUMBER   + " TEXT NOT NULL," +
                    COLUMN_NAME        + " TEXT, "+
                    " PRIMARY KEY(" + KEY_PARTY_MOVIE_ID+ "," + KEY_PARTY_DATETIME +"," +KEY_PARTY_VENUE+"," + KEY_PHONE_NUMBER +"),"+
                    " FOREIGN KEY(" + KEY_PARTY_MOVIE_ID + ") REFERENCES " + MoviesDB.SQLITE_TABLE_MOVIES+"("+MoviesDB.KEY_ROWID+"),"+
                    " FOREIGN KEY(" + KEY_PARTY_DATETIME +" ) REFERENCES " + PartiesDB.SQLITE_TABLE_PARTIES + "("+PartiesDB.KEY_DATETIME+"),"+
                    " FOREIGN KEY(" + KEY_PARTY_VENUE +") REFERENCES " + PartiesDB.SQLITE_TABLE_PARTIES +"("+PartiesDB.KEY_VENUE + ")"+
                    " );";

    public static void onCreate(SQLiteDatabase db){
        Log.w(LOG_TAG, DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version +" + oldVersion + " to " + newVersion
                + ", old data deleted.");
        onCreate(db);
    }

}
