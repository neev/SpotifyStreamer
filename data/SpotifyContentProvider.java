/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.spotifystreamer3.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

public class SpotifyContentProvider extends ContentProvider {


    public static final String PROVIDER_NAME = "com.example.provider.SpotifyDB";
    public static final String URL = "content://" + PROVIDER_NAME + "/spotifyArtistTable";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String _ID = "_id";
    public static final String  ARTIST_NAME= "artistName";
    public static final String  ARTIST_SPOTIFY_ID= "artistSpotifyId";
    public static final String  ARTIST_IMAGE_URL= "artistImageUrl";

    static final String  TOPTRACKS_ARTIST_NAME= "toptracksArtistNAME";
    static final String  TOPTRACKS_ALBUM_NAME= "toptracksAlbumNAME";
    static final String  TOPTRACKS_TRACK_IMAGE_URL= "toptracksImageUrl";
    static final String  TOPTRACKS_TRACK_NAME= "toptracksTrackName";
    static final String  TOPTRACKS_TRACK_PREVIEW_URL= "toptracksPreviewUrl";

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int ARTIST = 1;
    static final int TRACKS = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "spotifyArtistTable", ARTIST);
        uriMatcher.addURI(PROVIDER_NAME, "spotifYTracksTable", TRACKS);

    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "SpotifyDB";
    static final String SPOTIFY_ARTIST_TABLE_NAME = "spotifyArtistTable";
    static final String SPOTIFY_TRACKS_TABLE_NAME = "spotifYTracksTable";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_ARTIST_DB_TABLE =
            " CREATE TABLE " + SPOTIFY_ARTIST_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " artistName TEXT NOT NULL, " +
                    " artistImageUrl TEXT NOT NULL, " +
                    " artistSpotifyId TEXT NOT NULL);";
    static final String CREATE_TRACKS_DB_TABLE =
            " CREATE TABLE " + SPOTIFY_TRACKS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " toptracksArtistNAME TEXT NOT NULL, " +
                    " toptracksAlbumNAME TEXT NOT NULL, " +
                    " toptracksImageUrl TEXT NOT NULL, " +
                    " toptracksTrackName TEXT NOT NULL, " +
                    " toptracksPreviewUrl TEXT NOT NULL);";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_ARTIST_DB_TABLE);
            db.execSQL(CREATE_TRACKS_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  SPOTIFY_ARTIST_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " +  SPOTIFY_TRACKS_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long ARTISTrowID = db.insert(	SPOTIFY_ARTIST_TABLE_NAME, "", values);
        //long TRACKSrowID = db.insert(	SPOTIFY_TRACKS_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */

        if (ARTISTrowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, ARTISTrowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case ARTIST:

                qb.setTables(SPOTIFY_ARTIST_TABLE_NAME);
                qb.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
                break;
            case TRACKS:

                qb.setTables(SPOTIFY_TRACKS_TABLE_NAME);
                qb.appendWhere(_ID + "=" + uri.getPathSegments().get(2));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == ""){
            /**
             * By default sort on student names
             */
            //sortOrder = ARTIST_NAME;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case ARTIST:
                count = db.delete(SPOTIFY_ARTIST_TABLE_NAME, selection, selectionArgs);
                break;



            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case ARTIST:
                count = db.update(SPOTIFY_ARTIST_TABLE_NAME, values, selection, selectionArgs);
                break;

            case TRACKS:
                count = db.update(SPOTIFY_TRACKS_TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case ARTIST:
                return "vnd.android.cursor.dir/vnd.example.spotifyArtistTable";

            /**
             * Get a particular student
             */
            case TRACKS:
                return "vnd.android.cursor.dir/vnd.example.spotifYTracksTable";


            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
