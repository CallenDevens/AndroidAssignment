package com.example.aya.myapplication;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.net.URI;

/**
 * Created by aya on 15-9-8.
 */
public class AppContentProvider extends ContentProvider {
    private AppDataBaseHelper dbHelper;
  //  private static final int
    private static  final int ALL_MOVIES     = 1;
    private static final int SINGLE_MOVIE    = 2;
    private static final int MULTPLE_MOVIES  = 3;
    private static final int MULTPLE_PARTIES = 4;
    private static final int MULTIPLE_CLUBS = 5;
    private static final int PARTY = 6;
    private static final int MULTIPLY_CONTACTS = 7;
    private static final int SINGLE_PENDING_CODE = 8;

    private static final String AUTHORITY = "com.example.aya.myapplication.dbprovider";
    public static Uri MOVIE_URI = Uri.parse("content://" + AUTHORITY + "/movies");
    public static Uri PARTY_URI = Uri.parse("content://" + AUTHORITY + "/parties");
    public static Uri CLUB_URI = Uri.parse("content://"+AUTHORITY + "/clubs");
    public static Uri PARTY_CONTACTS_URI = Uri.parse("content://"+AUTHORITY+"/contacts");
    public static Uri PENDING_CODE_URI = Uri.parse("content://"+AUTHORITY+"/pending_code");


    private static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "movies/", ALL_MOVIES);
        uriMatcher.addURI(AUTHORITY, "movies/*", MULTPLE_MOVIES);
        uriMatcher.addURI(AUTHORITY,"movies/#", SINGLE_MOVIE);
        uriMatcher.addURI(AUTHORITY,"parties/*", MULTPLE_PARTIES);
        uriMatcher.addURI(AUTHORITY, "clubs/", MULTIPLE_CLUBS);
        uriMatcher.addURI(AUTHORITY, "parties/", PARTY);
        uriMatcher.addURI(AUTHORITY, "contacts/", MULTIPLY_CONTACTS);
        uriMatcher.addURI(AUTHORITY, "pending_code", SINGLE_PENDING_CODE);
    }

    private SQLiteDatabase myMoviesDataBase;

    public boolean onCreate(){
        //initiliaze databasehelper
        Context context = getContext();
        dbHelper = new AppDataBaseHelper(context);
        myMoviesDataBase = dbHelper.getWritableDatabase();
        return false;
    }

    @Override
    public String getType(Uri uri){
        switch(uriMatcher.match(uri)){
            default:
                throw new IllegalArgumentException("Unsupported URI: "+ uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = 0;
        switch (uriMatcher.match(uri)){

            case MULTPLE_MOVIES:
            case ALL_MOVIES:
                 id = db.insert(MoviesDB.SQLITE_TABLE_MOVIES, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(MOVIE_URI+"/"+id);
            case MULTIPLE_CLUBS:
                id = db.insert(ClubsDB.SQLITE_TABLE_CLUBS, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CLUB_URI+"/"+id);
            case PARTY:
                id = db.insert(PartiesDB.SQLITE_TABLE_PARTIES, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(PARTY_URI+"/"+id);
            case MULTIPLY_CONTACTS:
                id = db.insert(ContactsDB.SQLITE_TABLE_CONTACTS,null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(PARTY_CONTACTS_URI+"/"+id);
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String [] projection, String selection,
                        String [] selectionArgs, String sortOrder){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)){
            case SINGLE_MOVIE:
            case ALL_MOVIES:
            case MULTPLE_MOVIES:
                queryBuilder.setTables(MoviesDB.SQLITE_TABLE_MOVIES);
                break;
            case MULTIPLE_CLUBS:
                queryBuilder.setTables(ClubsDB.SQLITE_TABLE_CLUBS);
                break;
            case PARTY:
                queryBuilder.setTables(PartiesDB.SQLITE_TABLE_PARTIES);
                break;
            case MULTIPLY_CONTACTS:
                queryBuilder.setTables(ContactsDB.SQLITE_TABLE_CONTACTS);
                break;
            case SINGLE_PENDING_CODE:
                queryBuilder.setTables(PendingCodeDB.SQL_TABLE_PENDING_CODE);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String []  selectionArgs){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
            case MULTPLE_MOVIES:
            case ALL_MOVIES:
                break;

            case SINGLE_MOVIE:

                //fetch given id from uri
                String id = uri.getPathSegments().get(1);

                //delete by id and given selection
                selection = MoviesDB.KEY_ROWID + " = " + id +
                        (!TextUtils.isEmpty(selection)? "AND ("+selection+")" : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        int deleteCount = db.delete(MoviesDB.SQLITE_TABLE_MOVIES, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri,null);
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String [] selectionArgs){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updateCount = 0;
        switch (uriMatcher.match(uri)){
            case ALL_MOVIES:
            case MULTPLE_MOVIES:
            case SINGLE_MOVIE:
                updateCount = db.update(MoviesDB.SQLITE_TABLE_MOVIES, values, selection, selectionArgs);
                break;
            case MULTIPLE_CLUBS:
                updateCount = db.update(ClubsDB.SQLITE_TABLE_CLUBS, values, selection, selectionArgs);
                break;
            case SINGLE_PENDING_CODE:
                updateCount = db.update(PendingCodeDB.SQL_TABLE_PENDING_CODE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    private static class AppDataBaseHelper extends SQLiteOpenHelper{
        private static final String DATABASE_NAME = "MyMovies";
        private static final int DATABASE_VERSION = 1;

        AppDataBaseHelper(Context c){
            super(c, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db){
            MoviesDB.onCreate(db);
            PartiesDB.onCreate(db);
            ContactsDB.onCreate(db);
            ClubsDB.onCreate(db);
            PendingCodeDB.onCreate(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("Drop tables");
            onCreate(db);
        }
    }
}
