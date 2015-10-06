package com.example.android.spotifystreamer3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.android.spotifystreamer3.parcelable.ArtistName;
import com.example.android.spotifystreamer3.parcelable.MyTrack;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    SearchView searchView;
    public final String Log_tag = FetchTracks.class.getSimpleName();
    public final String LOG_TAG1 = MainActivity.class.getSimpleName();
    public List<MyTrack> resultList = new ArrayList<MyTrack>();
    MyAdapter mlistadapter;
    ListView mlistview;
    TextView mTextView;
    View msearchViewOpenPageLayout;
    CallBack mCallBack;
    View emptyView;
    ImageView msearchViewOpenPageImage;
    String artistName;
    public static final String QUERY_KEY = "query";
    public boolean mTwoPane;
    private boolean isOnline;
    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            List<MyTrack> values = savedInstanceState.getParcelableArrayList("MyTracksParcelableArrayList");
            if (values != null) {
                resultList = values;
            }
        }





// retriving the selected song index from shared preference
        SharedPreferences prefstwopane = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mTwoPane = prefstwopane.getBoolean("TwoPane",false);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mlistview = (ListView) rootView.findViewById(R.id.listview_artists);
        emptyView = (View)rootView.findViewById(R.id.listview_forecast_empty);
        mlistadapter = new MyAdapter(resultList, getActivity());
        msearchViewOpenPageLayout = (View) rootView.findViewById(R.id.searchViewOpenPageLayout);
        mTextView = (TextView) rootView.findViewById(R.id.noResultTextView);
        msearchViewOpenPageImage = (ImageView) rootView.findViewById(R.id.searchViewOpenPageImage);



        if (resultList.size() == 0) {
            msearchViewOpenPageLayout.setVisibility(View.VISIBLE);
            msearchViewOpenPageImage.setVisibility(View.VISIBLE);
            mTextView.setVisibility(TextView.VISIBLE);
            mlistview.setVisibility(View.INVISIBLE);

            mTextView.setTextSize(18);
            // mTextView.setTextColor(getResources().getColor(R.color.customPink));
            mTextView.setText("Find your favourite music!\n");

            mTextView.append("Search for songs by typing the artist name in the above search box.");

        } else if (resultList.size() != 0) {
            msearchViewOpenPageLayout.setVisibility(View.INVISIBLE);
            msearchViewOpenPageImage.setVisibility(View.INVISIBLE);
            mTextView.setVisibility(TextView.INVISIBLE);
            mlistview.setVisibility(View.VISIBLE);


            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );


            mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    MyTrack mtrack = resultList.get(position);
                    String spotifyId = mtrack.getSpotifyArtistID();
                    String name = mtrack.getArtist_names();
                    ArtistName parcelArtistObj = new ArtistName(name, spotifyId);
                    mCallBack.onItemSelected(parcelArtistObj);
                }
            });
        }
        return rootView;
    }



    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        List<MyTrack> values = resultList;
        savedState.putParcelableArrayList("MyTracksParcelableArrayList", (ArrayList<? extends Parcelable>) values);

    }

    class MyAdapter extends BaseAdapter {
        Context context;
        List<MyTrack> mArrayList;

        public MyAdapter(List<MyTrack> trackList, Context context) {
            this.mArrayList = trackList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyTrack mfetchTracks = (MyTrack) mArrayList.get(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.row_items, null);
            }
            // atrist names from search results
            TextView textView = (TextView) convertView
                    .findViewById(R.id.list_item_artistname_textview1);
            textView.setText(mfetchTracks.getArtist_names());

            //the image view
            ImageView imagenow = (ImageView) convertView
                    .findViewById(R.id.list_item_thumbnail_Image);
            String imageurl = mfetchTracks.getImageurl();

            if(mTwoPane==true) {
                if (!imageurl.isEmpty())
                    Picasso.with(context).load(imageurl).error(R.drawable.images1).resize(100,
                            100).into(imagenow);
                else {
                    Picasso.with(context).load(R.drawable.images1).error(R.drawable.images1)
                            .resize(100, 100).into(imagenow);
                }
            }else {
                if (!imageurl.isEmpty())
                    Picasso.with(context).load(imageurl).error(R.drawable.images1).resize(200, 200).into(imagenow);
                else {
                    Picasso.with(context).load(R.drawable.images1).error(R.drawable.images1).resize(200, 200).into(imagenow);
                }
            }
            return convertView;

        }
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //return super.onPrepareOptionsMenu(menu);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
   /* public interface Callback {

        public void onItemSelected(ArtistName selected_artistName);

    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mainfragment, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context
                .SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();



        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setFocusable(true);
        searchView.setIconified(false);  // Do not iconify the widget; expand it by default
        searchView.requestFocusFromTouch();
        searchView.requestFocus();

        checkConnectivity();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isOnline) {
                    FetchTracks mFetchTracks = new FetchTracks();
                    String artist_name = query;
                    mFetchTracks.execute(artist_name);

                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!isOnline) {
                    mlistview.setEmptyView(emptyView);
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Network Not Connected...");
                    alertDialog.setMessage("Please connect to a network and try again");
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // here you can add functions
                        }
                    });
                    alertDialog.setIcon(R.drawable.images1);

                    alertDialog.show();
                }
                return false;
            }
        });



        // return true;

    }
    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }*/

    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        checkConnectivity();
        // Special processing of the incoming intent only occurs if the if the action specified
        // by the intent is ACTION_SEARCH.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // SearchManager.QUERY is the key that a SearchManager will use to send a query string
            // to an Activity.
            String query = intent.getStringExtra(SearchManager.QUERY);
            // We need to create a bundle containing the query string to send along to the
            // LoaderManager, which will be handling querying the database and returning results.
            Bundle bundle = new Bundle();
            bundle.putString(QUERY_KEY, query);

            if (isOnline) {
                FetchTracks mFetchTracks = new FetchTracks();
                String artist_name = bundle.getString(QUERY_KEY);
                mFetchTracks.execute(artist_name);
            }else {

                mlistview.setEmptyView(emptyView);
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Network Not Connected...");
                alertDialog.setMessage("Please connect to a network and try again");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        ((MainActivity)getActivity()).finish();
                    }
                });
                alertDialog.setIcon(R.drawable.images1);

                alertDialog.show();
            }


        }
    }
    private void checkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
           /* if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    .isConnectedOrConnecting()
                    || cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .isConnectedOrConnecting())*/
        if(activeNetwork !=null && activeNetwork.isConnectedOrConnecting())
            isOnline = true;
        else
            isOnline = false;

    }
    @Override
    public void onStart() {
        super.onStart();
        handleIntent(getActivity().getIntent());

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof CallBack){
            mCallBack = (CallBack)activity;
           // Toast.makeText(getActivity(), "on Attach landscape mode", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onResume(){
        super.onResume();
        handleIntent(getActivity().getIntent());
        // mlistadapter.getActivity().getFilter().filter(getFilterSettings());

        mlistadapter.notifyDataSetChanged();
        if(!isOnline)
            mlistview.setEmptyView(emptyView);
        else
            mlistview.setAdapter(mlistadapter);

        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MyTrack mtrack = resultList.get(position);
                String spotifyId = mtrack.getSpotifyArtistID();
                String name = mtrack.getArtist_names();
                ArtistName parcelArtistObj = new ArtistName(name, spotifyId);

                mCallBack.onItemSelected(parcelArtistObj);

            }
        });

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
    public class FetchTracks extends AsyncTask<String, Void, List<MyTrack>> {

        private List<MyTrack> getArtistTracksFromJson(String artistTracksJsonStr)
                throws JSONException

        {

            // These are the names of the JSON objects that need to be extracted.
            final String SPOTIFY_ARTISTS = "artists";
            final String SPOTIFY_ARTISTID = "id";
            final String SPOTIFY_NODEITEMS = "items";
            final String SPOTIFY_ARTISTNAME = "name";
            final String SPOTIFY_ALBUMIMAGES = "images";

            JSONObject spotifyJson = new JSONObject(artistTracksJsonStr);
            JSONObject tracksObject = spotifyJson.getJSONObject(SPOTIFY_ARTISTS);
            JSONArray tracksArray = tracksObject.getJSONArray(SPOTIFY_NODEITEMS);

            List<MyTrack> resultTracks = new ArrayList<MyTrack>();
            MyTrack track = null;

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

                track = new MyTrack(tracknames, imageurl, artistid);
                resultTracks.add(track);


            }

            return resultTracks;

        }

        @Override
        protected List<MyTrack> doInBackground(String... params) {
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

            try {
                return getArtistTracksFromJson(spotifyJsonStr);
            } catch (JSONException e) {
                Log.e(Log_tag, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
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

                  mCallBack.onItemSelected(parcelArtistObj);

                }
            });


        }
    }

}