package com.example.aya.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by aya on 15-9-9.
 */
public class ClubsDB {
    public static final String KEY_CLUB_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String COLUMN_RANGE = "range";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    private static final String LOG_TAG = "ClubsDB";
    public static final String COLUMN_JOINED = "joined";
    public static final String SQLITE_TABLE_CLUBS = "clubs";

    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + SQLITE_TABLE_CLUBS + "("+
                    KEY_CLUB_NAME    + " TEXT," +
                    KEY_ADDRESS      + " TEXT," +
                    COLUMN_LATITUDE  + " REAL, "+
                    COLUMN_LONGITUDE + " REAL, "+
                    COLUMN_RANGE     + " REAL," +
                    COLUMN_JOINED    + " INTEGER DEFAULT 0, "+
                    "PRIMARY KEY("+ KEY_CLUB_NAME + "," + KEY_ADDRESS + ")"+
                    ");";

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
