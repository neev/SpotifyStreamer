package com.example.android.spotifystreamer3;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.spotifystreamer3.parcelable.SongParcelable;
import com.example.android.spotifystreamer3.service.MusicService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //////////////////////////////////

    //Log variable
    public final String Log_PD = PlayerDialog.class.getSimpleName();
    // --Set up constant ID for broadcast of seekbar position--
    public static final String BROADCAST_SEEKBAR = "com.example.android.myspotifystreamer" +
            ".sendseekbar";

    private int songEnded = 0;
    Intent serviceIntent;
    boolean mBroadcastIsRegistered;
    //progress dialog and broadcast receiver variables
    boolean mBufferBroadcastIsRegistered;
    Intent intent;

    private ImageButton shareButton;
    private TextView artistNameTextBox;
    private TextView albumNameTextBox;
    private TextView trackNameTextBox;
    private ImageView trackImageView;
    private ToggleButton playPauseButton;
    private Button previousTrackButton;
    private Button nextTrackButton;
    private SeekBar musicSeekBar ;
    private TextView maxDurationText;
    private TextView currentLocText;
    //---seekbar variables
    //variables for seekBar

    private String seekPosition;
    public int seekMax;

    public int seekProgress=0;
    public int pausedSeekProgress=0;
    private ProgressDialog pdBuff = null;
    private boolean isOnline;
    private boolean boolMusicPlaying = false;
    Handler seekHandler = new Handler();
    Handler mHandler=new Handler();
    int songIndex=0;
    List<SongParcelable> playerSongList= new ArrayList<SongParcelable>();
    List<SongParcelable> parcelableSongList = new ArrayList<SongParcelable>();
    SongParcelable selectedSong;
    //to dismiss the dialog fragment
    private boolean m_status;

    private ShareActionProvider mShareActionProvider;
    SharedPreferences sharedPref;
    SharedPreferences sharedPausedSeekPos;
    private static final String SPOTIFY_SHARE_HASHTAG = " #Spotify streamer";


    public PlayerDialog() {
        setHasOptionsMenu(true);
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment PlayerDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayerDialog newInstance(List<SongParcelable> parcelSongList) {
        PlayerDialog fragment = new PlayerDialog();
        Bundle args = new Bundle();

        args.putParcelableArrayList("KEY_SONG_LIST", (ArrayList<? extends Parcelable>) parcelSongList);
                 fragment.setArguments(args);
        return fragment;
    }




    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        // Dialog dialog = super.onCreateDialog(savedInstanceState);

        // creating the fullscreen dialog
        Dialog dialog = super.onCreateDialog(savedInstanceState);
       dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR);

        return dialog;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout to use as dialog or embedded fragment
        View view = inflater.inflate(R.layout.fragment_player_dialog, container, false);
        artistNameTextBox = (TextView) view.findViewById(R.id.artistName);
        albumNameTextBox = (TextView) view.findViewById(R.id.albumName);
        trackNameTextBox = (TextView) view.findViewById(R.id.songName);
        trackImageView = (ImageView) view.findViewById(R.id.mucisPlayerImage);
        playPauseButton = (ToggleButton) view.findViewById(R.id.playPauseButton);
        previousTrackButton = (Button) view.findViewById(R.id.previousButton);
        nextTrackButton = (Button) view.findViewById(R.id.nextButton);
        musicSeekBar = (SeekBar) view.findViewById(R.id.musicSeekBar);
        maxDurationText=(TextView)view.findViewById(R.id.musicMaxDurationText);
        currentLocText=(TextView)view.findViewById(R.id.musicCurrentLocText);
        // shareButton=(ImageButton)view.findViewById(R.id.shareButton);


        parcelableSongList =getArguments().getParcelableArrayList("KEY_SONG_LIST");

        // for reading the parcelabe song list
        for(int i=0;i<parcelableSongList.size();i++){

            SongParcelable songinfo=new SongParcelable(parcelableSongList.get(i).getArtistName(),
                    parcelableSongList
                    .get(i).getAlbumName(),parcelableSongList.get(i).getImageUrl(),parcelableSongList.get(i).getTrackName(),parcelableSongList.get(i).getTrackPreviewUrl());

            playerSongList.add(songinfo);
        }

        // retriving the selected song index from shared preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        songIndex = prefs.getInt("SONGINDEX_SP",0);

        //retriving the paused seek bar position after screen rotation
        SharedPreferences pausedSeekPosPrefs = PreferenceManager.getDefaultSharedPreferences
                (getActivity());
        pausedSeekProgress = pausedSeekPosPrefs.getInt("PAUSED_SEEK_POS",0);

        // Toast.makeText(getActivity(), "Shared preference : " + songIndex, Toast.LENGTH_SHORT).show();
        selectedSong = new SongParcelable(
                playerSongList.get(songIndex).getArtistName(),
                playerSongList.get(songIndex).getAlbumName(),
                playerSongList.get(songIndex).getImageUrl(),
                playerSongList.get(songIndex).getTrackName(),
                playerSongList.get(songIndex).getTrackPreviewUrl()
        );
        artistNameTextBox.setText(selectedSong.getArtistName());
        albumNameTextBox.setText(selectedSong.getAlbumName());
        if (!selectedSong.getImageUrl().isEmpty())
            Picasso.with(getActivity()).load(selectedSong.getImageUrl()).error(R.drawable
                    .images1).resize(540, 540).into(trackImageView);
        else {
            Picasso.with(getActivity()).load(R.drawable.music_icon).error(R.drawable.images1)
                    .resize(540, 540).into(trackImageView);
        }
        trackNameTextBox.setText(selectedSong.getTrackName());



        try {
            serviceIntent = new Intent(getActivity(), MusicService.class);
// --- set up seekbar intent for broadcasting new position to service ---
            intent = new Intent(BROADCAST_SEEKBAR);
            playAudio(selectedSong.getTrackPreviewUrl());
            playPauseButton.setChecked(true);

            playPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Perform action on clicks
                    if (playPauseButton.isChecked()) { // Checked - Pause icon visible
                        playAudio(selectedSong.getTrackPreviewUrl());

                        playPauseButton.setChecked(true);


                    } else { // Unchecked - Play icon visible


                        pauseAudio();
                        playPauseButton.setChecked(false);

                    }
                }
            });

            musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }


                public void onStartTrackingTouch(SeekBar seekBar) {

                }


                public void onStopTrackingTouch(SeekBar seekBar) {
                    int seekPos = seekBar.getProgress();
                    intent.putExtra("seekpos", seekPos);
                    getActivity().sendBroadcast(intent);
                    pausedSeekProgress=seekBar.getProgress();

                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Unable to play the song", Toast.LENGTH_SHORT).show();
        }





        previousTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear seek bar
               // musicSeekBar.clearFocus();
                // Checked - Pause icon visible
                playPauseButton.setChecked(true);
                //play the previous track
                songIndex--;
                sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

                //saving the selected song index in shared preference
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("SONGINDEX_SP", songIndex);
                editor.apply();
                pausedSeekProgress=0;
                musicSeekBar.setProgress(0);
                // check if previous song is there or not

                if(songIndex >0) {
                    //select the next song from playerSongList
                    selectedSong = new SongParcelable(
                            playerSongList.get(songIndex).getArtistName(),
                            playerSongList.get(songIndex).getAlbumName(),
                            playerSongList.get(songIndex).getImageUrl(),
                            playerSongList.get(songIndex).getTrackName(),
                            playerSongList.get(songIndex).getTrackPreviewUrl()
                    );
                    //update the player dialogue
                    albumNameTextBox.setText(selectedSong.getAlbumName());
                    trackNameTextBox.setText(selectedSong.getTrackName());
                    if (!selectedSong.getImageUrl().isEmpty())
                        Picasso.with(getActivity()).load(selectedSong.getImageUrl()).error(R
                                .drawable.images1).resize(540, 540).into(trackImageView);
                    else {
                        Picasso.with(getActivity()).load(R.drawable.music_icon).error(R.drawable
                                .images1).resize(540, 540).into(trackImageView);
                    }
                    playAudio(selectedSong.getTrackPreviewUrl());
                    // Checked - Pause icon visible
                    playPauseButton.setChecked(true);


                }else{
                    // play first song

                    songIndex =0;
                    Toast.makeText(getActivity(),
                            "This is the First Song in the playlist", Toast.LENGTH_SHORT).show();
                    //select the next song from playerSongList
                    selectedSong = new SongParcelable(
                            playerSongList.get(songIndex).getArtistName(),
                            playerSongList.get(songIndex).getAlbumName(),
                            playerSongList.get(songIndex).getImageUrl(),
                            playerSongList.get(songIndex).getTrackName(),
                            playerSongList.get(songIndex).getTrackPreviewUrl()
                    );
                    //update the player dialogue
                    albumNameTextBox.setText(selectedSong.getAlbumName());
                    trackNameTextBox.setText(selectedSong.getTrackName());
                    if (!selectedSong.getImageUrl().isEmpty())
                        Picasso.with(getActivity()).load(selectedSong.getImageUrl()).error(R
                                .drawable.images1).resize(540, 540).into(trackImageView);
                    else {
                        Picasso.with(getActivity()).load(R.drawable.music_icon).error(R.drawable
                                .images1).resize(540, 540).into(trackImageView);
                    }
                    playAudio(selectedSong.getTrackPreviewUrl());

                    // Checked - Pause icon visible
                    playPauseButton.setChecked(true);
                }
            }
        });

        nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nextAudio();
            }
        });


        return view;


    }






    // ---Send seekbar info to activity----
    private void setupSeekHandler(){
        seekHandler.removeCallbacks(sendUpdatesToUI);
        seekHandler.postDelayed(sendUpdatesToUI, 1000); // one second

    }

    private Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {

            seekbarCurrentPosition();

            seekHandler.postDelayed(this, 1000);

        }
    };

    private void seekbarCurrentPosition() {
        if(playPauseButton.isChecked()) {
            currentLocText.setText(String.valueOf("0:" + (seekProgress / 1000)));
            maxDurationText.setText(String.valueOf("0:" + (seekMax / 1000)));
            pausedSeekProgress=seekProgress;
        }
    }


    // -- Broadcast Receiver to update position of seekbar from service --
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent serviceIntent) {
            updateUI(serviceIntent);

        }
    };

    private void updateUI(Intent serviceIntent) {
        String counter = serviceIntent.getStringExtra("counter");
        String mediamax = serviceIntent.getStringExtra("mediamax");
        String strSongEnded = serviceIntent.getStringExtra("song_ended");
        seekProgress = Integer.parseInt(counter);
        seekMax = Integer.parseInt(mediamax);
        songEnded = Integer.parseInt(strSongEnded);

        musicSeekBar.setMax(seekMax);

        musicSeekBar.setProgress(seekProgress);

        setupSeekHandler();

        if (songEnded == 1) {
            playPauseButton.setChecked(false);
            songEnded=0;
            pausedSeekProgress=0;
            currentLocText.setVisibility(TextView.INVISIBLE);
            maxDurationText.setVisibility(TextView.INVISIBLE);
            mBroadcastIsRegistered=false;
        }


    }
    //------next audio---//
    public void nextAudio(){
        // Checked - Pause icon visible
        playPauseButton.setChecked(true);
        //clear seek bar
       musicSeekBar.clearFocus();
        //play the next track
        songIndex++;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //saving the selected song index in shared preference
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("SONGINDEX_SP", songIndex);
        editor.apply();
        pausedSeekProgress=0;
        musicSeekBar.setProgress(0);
        // check if next song is there or not
        if(songIndex < (playerSongList.size() - 1)){
            //select the next song from playerSongList
            selectedSong = new SongParcelable(
                    playerSongList.get(songIndex).getArtistName(),
                    playerSongList.get(songIndex).getAlbumName(),
                    playerSongList.get(songIndex).getImageUrl(),
                    playerSongList.get(songIndex).getTrackName(),
                    playerSongList.get(songIndex).getTrackPreviewUrl()
            );
            //update the player dialogue
            albumNameTextBox.setText(selectedSong.getAlbumName());
            trackNameTextBox.setText(selectedSong.getTrackName());
            if (!selectedSong.getImageUrl().isEmpty())
                Picasso.with(getActivity()).load(selectedSong.getImageUrl()).error(R.drawable
                        .images1).resize(540, 540).into(trackImageView);
            else {
                Picasso.with(getActivity()).load(R.drawable.music_icon).error(R.drawable.images1)
                        .resize(540, 540).into(trackImageView);
            }
            playAudio(selectedSong.getTrackPreviewUrl());
            // Checked - Pause icon visible
            playPauseButton.setChecked(true);

        }else{
            // play last song

            songIndex = playerSongList.size()-1;
            Toast.makeText(getActivity(),
                    "This is the Last Song in the playlist", Toast.LENGTH_SHORT).show();
            //select the last song from playerSongList
            selectedSong = new SongParcelable(
                    playerSongList.get(songIndex).getArtistName(),
                    playerSongList.get(songIndex).getAlbumName(),
                    playerSongList.get(songIndex).getImageUrl(),
                    playerSongList.get(songIndex).getTrackName(),
                    playerSongList.get(songIndex).getTrackPreviewUrl()
            );
            //update the player dialogue
            albumNameTextBox.setText(selectedSong.getAlbumName());
            trackNameTextBox.setText(selectedSong.getTrackName());
            if (!selectedSong.getImageUrl().isEmpty())
                Picasso.with(getActivity()).load(selectedSong.getImageUrl()).error(R
                        .drawable.images1).resize(540, 540).into(trackImageView);
            else {
                Picasso.with(getActivity()).load(R.drawable.music_icon).error(R.drawable
                        .images1).resize(540, 540).into(trackImageView);
            }
            playAudio(selectedSong.getTrackPreviewUrl());
            // Checked - Pause icon visible
            playPauseButton.setChecked(true);
        }


    }




    // --- Stop service (and music) ---
    public void pauseAudio() {
        // --Unregister broadcastReceiver for seekbar
        if (mBroadcastIsRegistered) {
            try {


                // serviceIntent.putExtra("Pause", pausedSeekProgress);
                getActivity().stopService(serviceIntent);

                getActivity().unregisterReceiver(broadcastReceiver);


                mBroadcastIsRegistered = false;
            } catch (Exception e) {
                // Log.e(TAG, "Error in Activity", e);
                // TODO Auto-generated catch block

                e.printStackTrace();
                Toast.makeText(

                        getActivity(),

                        e.getClass().getName() + " " + e.getMessage(),

                        Toast.LENGTH_LONG).show();
            }
        }


    }


    // --- Start service and play music ---
    public void playAudio(String selectedSongUrl) {


        checkConnectivity();
        if (isOnline) {

            if(pausedSeekProgress == 0) {
                serviceIntent.putExtra("ArtistName", selectedSong.getArtistName());
                serviceIntent.putExtra("SONG_URL", selectedSongUrl);
                serviceIntent.putExtra("pausedSeekPos", String.valueOf(pausedSeekProgress));
                serviceIntent.putExtra("SongTitle",selectedSong.getTrackName());
                serviceIntent.putExtra("imageURL", selectedSong.getImageUrl());


                try {
                    getActivity().startService(serviceIntent);
                    //Register receiver for seekbar
                    getActivity().registerReceiver(broadcastReceiver, new IntentFilter(MusicService.BROADCAST_ACTION));
                } catch (Exception e) {

                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                currentLocText.setVisibility(TextView.VISIBLE);
                maxDurationText.setVisibility(TextView.VISIBLE);

                setupSeekHandler();
                mBroadcastIsRegistered = true;

            }
            else {

                serviceIntent.putExtra("ArtistName", selectedSong.getArtistName());
                serviceIntent.putExtra("SONG_URL", selectedSongUrl);
                serviceIntent.putExtra("pausedSeekPos", String.valueOf(pausedSeekProgress));
                serviceIntent.putExtra("SongTitle",selectedSong.getTrackName());
                serviceIntent.putExtra("imageURL", selectedSong.getImageUrl());
                try {
                    getActivity().startService(serviceIntent);
                    //Register receiver for seekbar
                    getActivity().registerReceiver(broadcastReceiver, new IntentFilter(MusicService.BROADCAST_ACTION));
                }catch (Exception e) {

                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                setupSeekHandler();
                mBroadcastIsRegistered = true;
            }

        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Network Not Connected...");
            alertDialog.setMessage("Please connect to a network and try again");
            alertDialog.setIcon(R.drawable.images1);
            playPauseButton.setChecked(false);
            alertDialog.show();
        }
    }


    /**
     * The system calls this only when creating the layout in a dialog.
     */

    // Handle progress dialogue for buffering...
    private void showPD(Intent bufferIntent) {
        String bufferValue = bufferIntent.getStringExtra("buffering");
        int bufferIntValue = Integer.parseInt(bufferValue);

        // When the broadcasted "buffering" value is 1, show "Buffering"
        // progress dialogue.
        // When the broadcasted "buffering" value is 0, dismiss the progress
        // dialogue.

        switch (bufferIntValue) {
            case 0:
                // Log.v(TAG, "BufferIntValue=0 RemoveBufferDialogue");
                // txtBuffer.setText("");
                if (pdBuff != null) {
                    pdBuff.dismiss();
                }
                break;

            case 1:
                BufferDialogue();
                break;

            // Listen for "2" to reset the button to a play button
            case 2:

                playPauseButton.setChecked(false);
                break;

        }
    }

    // Progress dialogue...
    private void BufferDialogue() {

        pdBuff = ProgressDialog.show(getActivity(), "Buffering...",
                "Acquiring song...", true);
    }

    // Set up broadcast receiver
    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent bufferIntent) {
            showPD(bufferIntent);
        }
    };







    // -- onPause, unregister broadcast receiver. To improve, also save screen data ---
    @Override
    public void onPause() {
        super.onPause();
        sharedPausedSeekPos = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //saving the selected song index in shared preference
        SharedPreferences.Editor editor = sharedPausedSeekPos.edit();
        editor.putInt("PAUSED_SEEK_POS", pausedSeekProgress);
        editor.apply();

        // Unregister broadcast receiver
        if (mBufferBroadcastIsRegistered) {
            getActivity().unregisterReceiver(broadcastBufferReceiver);
            getActivity().unregisterReceiver(broadcastReceiver);
            mBufferBroadcastIsRegistered = false;
        }




    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
        dismiss();
        // stop the music service
        getActivity().stopService(serviceIntent);
        // Unregister broadcast receiver
        if (mBufferBroadcastIsRegistered) {
            getActivity().unregisterReceiver(broadcastBufferReceiver);
            getActivity().unregisterReceiver(broadcastReceiver);
            mBufferBroadcastIsRegistered = false;
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
        // Unregister broadcast receiver
        if (mBufferBroadcastIsRegistered) {
            getActivity().unregisterReceiver(broadcastBufferReceiver);
            getActivity().unregisterReceiver(broadcastReceiver);
            mBufferBroadcastIsRegistered = false;
        }

        getActivity().stopService(serviceIntent);

    }


    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);


        // Note: getValues() is a method in your ArrayAdaptor subclass
        List<SongParcelable> values = parcelableSongList;
        savedState.putParcelableArrayList("SongListParcelableArrayList", (ArrayList<? extends
                Parcelable>) values);

    }
    // -- onResume register broadcast receiver. To improve, retrieve saved screen data ---
    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            List<SongParcelable> values = savedInstanceState.getParcelableArrayList
                    ("SongListParcelableArrayList");
            if (values != null) {
                parcelableSongList = values;
            }

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            PlayerDialog newFragment = PlayerDialog.newInstance(parcelableSongList);
            // The device is using a large layout, so show the fragment as a dialog


            // The device is using a large layout, so show the fragment as a dialog
            newFragment.show(fragmentManager, "dialog");
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        // stop the music service
        getActivity().stopService(serviceIntent);

        // Unregister broadcast receiver
        if (mBufferBroadcastIsRegistered) {
            getActivity().unregisterReceiver(broadcastBufferReceiver);

            mBufferBroadcastIsRegistered = false;
        }
        getActivity().unregisterReceiver(broadcastReceiver);

    }

    public interface dialogDoneListener{
        void onDone(boolean state);

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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // pausedSeekProgress=seekBar.getProgress();
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        // mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int seekPos = seekBar.getProgress();
        intent.putExtra("seekpos", seekPos);
        getActivity().sendBroadcast(intent);
        pausedSeekProgress=seekBar.getProgress();

    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_playerdialog, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);


        mShareActionProvider.setShareIntent(createShareForecastIntent());



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, selectedSong.getTrackPreviewUrl() + SPOTIFY_SHARE_HASHTAG);
        return shareIntent;

    }



}
