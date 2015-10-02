package com.example.android.spotifystreamer3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.spotifystreamer3.parcelable.ArtistName;
import com.example.android.spotifystreamer3.parcelable.SongParcelable;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.ErrorHandler;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment {
///////////////////////
public final String Log_tag1 = TopTracks.class.getSimpleName();
    public List<Track> resultTracks = new ArrayList<Track>();

    CallBackTT mHandleCallBack;
    ListView mTopTracksListview;
    MyTopTracksAdapter mTopTracksAdapter;
    TextView mTopTracksTextView;
    boolean mTwoPane=false;
    public List<SongParcelable> songList;
    public String artistName;
    public String songIndexSharedPref="SONGINDEX_SP";
    SharedPreferences sharedPref;

    static final String TOPTRACKS_PARCELABLEOBJECT = "ParcelableObject";
    ArtistName parcelReceivingObj;
    ////////////////////////////


    public TopTracksFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //landscape mode



        if (savedInstanceState != null) {
            List<SongParcelable> values = savedInstanceState.getParcelableArrayList
                    ("TopTracksParcelableArrayList");
            if (values != null) {
                songList = values;
            }


        }

        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);


        // retriving the mTwoPane value from shared preference
        SharedPreferences prefstwopane = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mTwoPane = prefstwopane.getBoolean("TwoPane",false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            parcelReceivingObj = arguments.getParcelable(TopTracksFragment.TOPTRACKS_PARCELABLEOBJECT);
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Get the extra text from the Intent
        Intent intent = getActivity().getIntent();
        if(intent.getParcelableExtra(Intent.EXTRA_TEXT)!=null) {
            parcelReceivingObj = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            artistName = parcelReceivingObj.getArtistName();
        }
        if(parcelReceivingObj!=null) {
            // Calling AsynTask
            TopTenTracks mAsynTask = new TopTenTracks();
            mAsynTask.execute(parcelReceivingObj.getArtistId());
        }

        // Initiating the text view
        mTopTracksTextView = (TextView) view.findViewById(R.id.noTopTrackResultTextView);
        mTopTracksTextView.setText("No top tracks are found for the selected artist");
        //Initialind dat into the listview
        mTopTracksListview = (ListView) view.findViewById(R.id.listViewTopTracks);
        mTopTracksAdapter = new MyTopTracksAdapter(getActivity());
        mTopTracksListview.setAdapter(mTopTracksAdapter);

        mTopTracksListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // show dialog fragment
                kaaes.spotify.webapi.android.models.Track track = resultTracks.get(position);
                //saving the selected song index in shared preference
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(songIndexSharedPref, position);
                editor.apply();


                List<Image> imagesList = track.album.images;

                String imageURL300 = "";
                String imageURL64 = "";
                String imageURL640 = "";
                String imageurl = "";


                for (int i = 0; i < imagesList.size(); i++) {

                    switch (imagesList.get(i).width) {
                        case 300:
                            imageURL300 = imagesList.get(i).url;
                            break;
                        case 64:
                            imageURL64 = imagesList.get(i).url;
                            break;
                        case 640:
                            imageURL640 = imagesList.get(i).url;
                            break;
                    }

                }

                if (!imageURL640.isEmpty()) {

                    imageurl = imageURL640;
                } else if (!imageURL300.isEmpty()) {
                    imageurl = imageURL300;
                } else {
                    imageurl = imageURL64;
                }
                // musicPlayerExtraString = new String[]{artistName, track.album.name, imageurl, track.name, track.preview_url};
                //generating the custom song list
                songList= new ArrayList<SongParcelable>();

                if (resultTracks.size() != 0) {
                    for(int i=0; i<resultTracks.size(); i++){

                        // to get the track imageUrl
                        List<Image> imagesSongList = resultTracks.get(i).album.images;

                        String imageURL300Song = "";
                        String imageURL64Song = "";
                        String imageURL640Song = "";
                        String imageurlSong = "";


                        for (int j = 0; j < imagesSongList.size(); j++) {

                            switch (imagesSongList.get(j).width) {
                                case 300:
                                    imageURL300Song = imagesList.get(j).url;
                                    break;
                                case 64:
                                    imageURL64Song = imagesList.get(j).url;
                                    break;
                                case 640:
                                    imageURL640Song = imagesList.get(j).url;
                                    break;
                            }

                        }

                        if (!imageURL640.isEmpty()) {

                            imageurlSong = imageURL640Song;
                        } else if (!imageURL300.isEmpty()) {
                            imageurlSong = imageURL300Song;
                        } else {
                            imageurlSong = imageURL64Song;
                        }
                        SongParcelable songinfo = new SongParcelable(artistName,resultTracks.get(i).album.name,
                                imageurlSong,resultTracks.get(i).name,resultTracks.get(i).preview_url);

                        songList.add(songinfo);
                    }
                }

                /*((CallbackTT)getActivity())
                        .onItemSelected_topTracks(songList);*/
                mHandleCallBack.topTrackCallBack(songList);
                Log.v(Log_tag1, "Song List :" + songList);


               // showDialog();


            }

        });


        return view;
    }



    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */

    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        List<SongParcelable> values = songList;
        savedState.putParcelableArrayList("TopTracksParcelableArrayList", (ArrayList<? extends Parcelable>) values);

    }


    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof CallBackTT){
            mHandleCallBack = (CallBackTT)activity;
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        /*Fragment prev = getActivity().getSupportFragmentManager().findFragmentById(R.id
                .fragment_player_dialog);


        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }*/

        mTopTracksAdapter.notifyDataSetChanged();
        mTopTracksListview.setAdapter(mTopTracksAdapter);

        mTopTracksListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // show dialog fragment
                kaaes.spotify.webapi.android.models.Track track = resultTracks.get(position);
                //saving the selected song index in shared preference
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(songIndexSharedPref, position);
                editor.apply();


                List<Image> imagesList = track.album.images;

                String imageURL300 = "";
                String imageURL64 = "";
                String imageURL640 = "";
                String imageurl = "";


                for (int i = 0; i < imagesList.size(); i++) {

                    switch (imagesList.get(i).width) {
                        case 300:
                            imageURL300 = imagesList.get(i).url;
                            break;
                        case 64:
                            imageURL64 = imagesList.get(i).url;
                            break;
                        case 640:
                            imageURL640 = imagesList.get(i).url;
                            break;
                    }

                }

                if (!imageURL640.isEmpty()) {

                    imageurl = imageURL640;
                } else if (!imageURL300.isEmpty()) {
                    imageurl = imageURL300;
                } else {
                    imageurl = imageURL64;
                }

                songList= new ArrayList<SongParcelable>();

                if (resultTracks.size() != 0) {
                    for(int i=0; i<resultTracks.size(); i++){

                        // to get the track imageUrl
                        List<Image> imagesSongList = resultTracks.get(i).album.images;

                        String imageURL300Song = "";
                        String imageURL64Song = "";
                        String imageURL640Song = "";
                        String imageurlSong = "";


                        for (int j = 0; j < imagesSongList.size(); j++) {

                            switch (imagesSongList.get(j).width) {
                                case 300:
                                    imageURL300Song = imagesList.get(j).url;
                                    break;
                                case 64:
                                    imageURL64Song = imagesList.get(j).url;
                                    break;
                                case 640:
                                    imageURL640Song = imagesList.get(j).url;
                                    break;
                            }

                        }

                        if (!imageURL640.isEmpty()) {

                            imageurlSong = imageURL640Song;
                        } else if (!imageURL300.isEmpty()) {
                            imageurlSong = imageURL300Song;
                        } else {
                            imageurlSong = imageURL64Song;
                        }
                        SongParcelable songinfo = new SongParcelable(artistName,resultTracks.get(i).album.name,
                                imageurlSong,resultTracks.get(i).name,resultTracks.get(i).preview_url);

                        songList.add(songinfo);
                    }
                }

                mHandleCallBack.topTrackCallBack(songList);
                Log.v(Log_tag1, "Song List :" + songList);





            }

        });

    }






    private class MyTopTracksAdapter extends BaseAdapter {
        Context context;

        public MyTopTracksAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return resultTracks.size();
        }

        @Override
        public Object getItem(int position) {
            return resultTracks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            kaaes.spotify.webapi.android.models.Track track = resultTracks.get(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.row_items, null);
            }
            // Album name from search results
            TextView textView_album = (TextView) convertView
                    .findViewById(R.id.list_item_artistname_textview1);
            textView_album.setText(track.album.name);
            // Track name from search results
            TextView textView_track = (TextView) convertView
                    .findViewById(R.id.list_item_artistname_textview2);
            textView_track.setText(track.name);


            //the image view
            ImageView imagenow = (ImageView) convertView
                    .findViewById(R.id.list_item_thumbnail_Image);
            List<Image> imagesList = track.album.images;

            String imageURL300 = "";
            String imageURL64 = "";
            String imageURL640 = "";
            String imageurl = "";

            for (int i = 0; i < imagesList.size(); i++) {

                switch (imagesList.get(i).width) {
                    case 300:
                        imageURL300 = imagesList.get(i).url;
                        break;
                    case 64:
                        imageURL64 = imagesList.get(i).url;
                        break;
                    case 640:
                        imageURL640 = imagesList.get(i).url;
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
            if (mTwoPane == true) {
                if (!imageurl.isEmpty())
                    Picasso.with(context).load(imageurl).error(R.drawable.images1).resize(100,
                            100).into(imagenow);
                else {
                    Picasso.with(context).load(R.drawable.music_icon).error(R.drawable.images1)
                            .resize(100, 100).into(imagenow);
                }


            } else {

                if (!imageurl.isEmpty())
                    Picasso.with(context).load(imageurl).error(R.drawable.images1).resize(200, 200).into(imagenow);
                else {
                    Picasso.with(context).load(R.drawable.music_icon).error(R.drawable.images1).resize(200, 200).into(imagenow);
                }

            }
            return convertView;
        }

    }
    public class TopTenTracks extends AsyncTask<String, Void, Tracks> {

        Tracks artistTopTracks = null;


        protected void preExecute() {

        }

        @Override
        protected Tracks doInBackground(String... params) {


            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService mspotifyService = api.getService();

            final String COUNTRY = "country";
            HashMap options = new HashMap();
            options.put(COUNTRY, "US");
            try {
                artistTopTracks = mspotifyService.getArtistTopTrack(params[0], options);
                Log.v(Log_tag1, "Tracks :" + artistTopTracks);
            } catch (Exception e) {

                ErrorHandler errorHandler = (ErrorHandler) mspotifyService.getArtistTopTrack(params[0]);
            }
            return artistTopTracks;
        }

        @Override
        protected void onPostExecute(Tracks listOfTracks) {
            if (listOfTracks.tracks.size() != 0) {
                mTopTracksTextView.setVisibility(View.INVISIBLE);
                mTopTracksListview.setVisibility(View.VISIBLE);
                resultTracks = listOfTracks.tracks;
                mTopTracksAdapter.notifyDataSetChanged();
                mTopTracksListview.setAdapter(mTopTracksAdapter);
                mTopTracksListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        // show dialog fragment
                        kaaes.spotify.webapi.android.models.Track track = resultTracks.get(position);
                        //saving the selected song index in shared preference
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(songIndexSharedPref, position);
                        editor.apply();


                        List<Image> imagesList = track.album.images;

                        String imageURL300 = "";
                        String imageURL64 = "";
                        String imageURL640 = "";
                        String imageurl = "";


                        for (int i = 0; i < imagesList.size(); i++) {

                            switch (imagesList.get(i).width) {
                                case 300:
                                    imageURL300 = imagesList.get(i).url;
                                    break;
                                case 64:
                                    imageURL64 = imagesList.get(i).url;
                                    break;
                                case 640:
                                    imageURL640 = imagesList.get(i).url;
                                    break;
                            }

                        }

                        if (!imageURL640.isEmpty()) {

                            imageurl = imageURL640;
                        } else if (!imageURL300.isEmpty()) {
                            imageurl = imageURL300;
                        } else {
                            imageurl = imageURL64;
                        }

                        songList = new ArrayList<SongParcelable>();

                        if (resultTracks.size() != 0) {
                            for (int i = 0; i < resultTracks.size(); i++) {

                                // to get the track imageUrl
                                List<Image> imagesSongList = resultTracks.get(i).album.images;

                                String imageURL300Song = "";
                                String imageURL64Song = "";
                                String imageURL640Song = "";
                                String imageurlSong = "";


                                for (int j = 0; j < imagesSongList.size(); j++) {

                                    switch (imagesSongList.get(j).width) {
                                        case 300:
                                            imageURL300Song = imagesList.get(j).url;
                                            break;
                                        case 64:
                                            imageURL64Song = imagesList.get(j).url;
                                            break;
                                        case 640:
                                            imageURL640Song = imagesList.get(j).url;
                                            break;
                                    }

                                }

                                if (!imageURL640.isEmpty()) {

                                    imageurlSong = imageURL640Song;
                                } else if (!imageURL300.isEmpty()) {
                                    imageurlSong = imageURL300Song;
                                } else {
                                    imageurlSong = imageURL64Song;
                                }
                                SongParcelable songinfo = new SongParcelable(artistName, resultTracks.get(i).album.name,
                                        imageurlSong, resultTracks.get(i).name, resultTracks.get(i).preview_url);

                                songList.add(songinfo);
                            }
                        }

                        Log.v(Log_tag1, "Song List :" + songList);
                        mHandleCallBack.topTrackCallBack(songList);
                    }
                });

            } else if (listOfTracks.tracks.size() == 0) {
                mTopTracksTextView.setVisibility(View.VISIBLE);
                mTopTracksListview.setVisibility(View.INVISIBLE);
            }
        }
    }

}
