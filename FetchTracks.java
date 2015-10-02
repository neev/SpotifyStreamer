package com.example.android.spotifystreamer3;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.spotifystreamer3.data.SpotifyContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;


public class FetchTracks extends AsyncTask<String, Void, Void> {

    public final String Log_tag = FetchTracks.class.getSimpleName();


    private final Context mContext;

    public FetchTracks(Context context) {
        mContext = context;

    }

    private boolean DEBUG = true;



    private void getArtistTracksFromJson(String artistTracksJsonStr)
            throws JSONException

    {

        // These are the names of the JSON objects that need to be extracted.
        final String SPOTIFY_ARTISTS = "artists";
        final String SPOTIFY_ARTISTID = "id";
        final String SPOTIFY_NODEITEMS = "items";
        final String SPOTIFY_ARTISTNAME = "name";
        final String SPOTIFY_ALBUMIMAGES = "images";

        try {

        JSONObject spotifyJson = new JSONObject(artistTracksJsonStr);
        JSONObject tracksObject = spotifyJson.getJSONObject(SPOTIFY_ARTISTS);
        JSONArray tracksArray = tracksObject.getJSONArray(SPOTIFY_NODEITEMS);


        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(tracksArray.length());



        //List<MyTrack> resultTracks = new ArrayList<MyTrack>();
        //MyTrack track = null;

        for (int i = 0; i < tracksArray.length(); i++) {

            String artistid;
            String tracknames;
            String imageurl = "";


            JSONObject artistTracks = tracksArray.getJSONObject(i);
            artistid = artistTracks.getString(SPOTIFY_ARTISTID);


            tracknames = artistTracks.getString(SPOTIFY_ARTISTNAME);

            JSONArray images = artistTracks.getJSONArray(SPOTIFY_ALBUMIMAGES);

            int length = images.length();
            String imageURL300 = "";
            String imageURL64 = "";
            String imageURL640 = "";


            for (int index = 0; index < length; index++) {
                JSONObject imageObject = (JSONObject) images.getJSONObject(index);
                switch (imageObject.getInt("width")) {
                    case 300:
                        imageURL300 = imageObject.getString("url");
                        break;
                    case 64:
                        imageURL64 = imageObject.getString("url");
                        break;
                    case 640:
                        imageURL640 = imageObject.getString("url");
                        break;
                }
            }
            if (!imageURL300.isEmpty()) {

                imageurl = imageURL300;
            } else if (!imageURL64.isEmpty()) {
                imageurl = imageURL64;
            } else {
                imageurl = imageURL640;
            }

            //track = new MyTrack(tracknames, imageurl, artistid);
            ContentValues resultTracksCV = new ContentValues();
            //resultTracks.add(track);
            resultTracksCV.put(SpotifyContentProvider.ARTIST_NAME,tracknames);
            resultTracksCV.put(SpotifyContentProvider.ARTIST_IMAGE_URL,imageurl);
            resultTracksCV.put(SpotifyContentProvider.ARTIST_SPOTIFY_ID,artistid);

            cVVector.add(resultTracksCV);
            /*Toast.makeText(mContext.getBaseContext(),
                    uri.toString(), Toast.LENGTH_LONG).show();*/
        }

        int inserted = 0;

        // add to database
        if ( cVVector.size() > 0 ) {
            // Student: call bulkInsert to add the weatherEntries to the database here

            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(
                    SpotifyContentProvider.CONTENT_URI, cvArray);
        }

        Log.d(Log_tag, "FetchTracks Complete. " + inserted + " Inserted");

    } catch (JSONException e) {
        Log.e(Log_tag, e.getMessage(), e);
        e.printStackTrace();
    }

    }

    @Override
    protected Void doInBackground(String... params) {
        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String spotifyJsonStr = null;
        String type = "artist";


        try {

            final String SPOTIFY_BASE_URL =
                    "https://api.spotify.com/v1/search?";
            final String QUERY_PARAM = "q";
            final String TYPE_PARAM = "type";


            Uri builtUri = Uri.parse(SPOTIFY_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(TYPE_PARAM, type)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(Log_tag, "URI :" + url);

            // Create the request to Spotify, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            spotifyJsonStr = buffer.toString();


        } catch (IOException e) {
            Log.e(Log_tag, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(Log_tag, "Error closing stream", e);
                }
            }
        }

        /*try {
            return getArtistTracksFromJson(spotifyJsonStr);
        } catch (JSONException e) {
            Log.e(Log_tag, e.getMessage(), e);
            e.printStackTrace();
        }*/

        return null;
    }

   /* @Override
    protected void onPostExecute(List<MyTrack> listOfTracks) {


        if (listOfTracks.size() != 0) {
            msearchViewOpenPageLayout.setVisibility(View.INVISIBLE);
            msearchViewOpenPageImage.setVisibility(View.INVISIBLE);
            mTextView.setVisibility(TextView.INVISIBLE);

            mlistview.setVisibility(View.VISIBLE);
            resultList.clear();
            for (MyTrack track : listOfTracks) {
                resultList.add(track);
            }
            // New data is back from the server.  Hooray!

        } else if (listOfTracks.size() == 0) {
            msearchViewOpenPageLayout.setVisibility(View.VISIBLE);
            msearchViewOpenPageImage.setVisibility(View.VISIBLE);
            mTextView.setVisibility(TextView.VISIBLE);
            mlistview.setVisibility(View.INVISIBLE);
            mTextView.setText("No results found for this artist ");
        }
        mlistadapter.notifyDataSetChanged();
        mlistview.setEmptyView(emptyView);
        mlistview.setAdapter(mlistadapter);
        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MyTrack mtrack = resultList.get(position);
                String spotifyId = mtrack.getSpotifyArtistID();
                String name = mtrack.getArtist_names();
                ArtistName parcelArtistObj = new ArtistName(name, spotifyId);

                ((Callback) getActivity()).onItemSelected(parcelArtistObj);
                    *//*Intent intent = new Intent(getActivity(), ToptracksActivity.class).putExtra(Intent.EXTRA_TEXT,
                            parcelArtistObj);
                    startActivity(intent);*//*
            }
        });


    }*/
}