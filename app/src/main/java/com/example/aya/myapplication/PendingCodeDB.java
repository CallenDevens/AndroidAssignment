package com.example.aya.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by aya on 15-9-26.
 */
public class PendingCodeDB {
    public static final String KEY_PENDING_CODE = "request_code";
    public static final String SQL_TABLE_PENDING_CODE = "pending_code";
    public static final String LOG_TAG = "pending_code";

    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_PENDING_CODE + "("+
                    KEY_PENDING_CODE + " INTEGER );";

    private static final String DATABASE_INITIAL = "INSERT INTO "+SQL_TABLE_PENDING_CODE + " VALUES ( 0 )";

    public static void onCreate(SQLiteDatabase db){
        Log.w(LOG_TAG, DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE);
        db.execSQL(DATABASE_INITIAL);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.w(LOG_TAG, "Upgrading database from version +" + oldVersion + " to " + newVersion
                +", old data deleted.");
        onCreate(db);
    }
}
