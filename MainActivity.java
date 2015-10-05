package com.example.android.spotifystreamer3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.spotifystreamer3.parcelable.ArtistName;
import com.example.android.spotifystreamer3.parcelable.SongParcelable;

import java.util.List;

public class MainActivity extends AppCompatActivity implements CallBack,CallBackTT{




    private static final String TOPTRACKS_FRAGMENT_TAG = "TTFAG";
    public boolean mTwoPane;
    public ArtistName mSelected_artistName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_top_tracks) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_top_tracks, new TopTracksFragment(),
                                TOPTRACKS_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }


        SharedPreferences sharedPref_twopane;
        sharedPref_twopane = PreferenceManager.getDefaultSharedPreferences(this);
        //saving the selected song index in shared preference
        SharedPreferences.Editor editor = sharedPref_twopane.edit();
        editor.putBoolean("TwoPane", mTwoPane);
        editor.apply();



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    @Override
    protected void onResume() {
        super.onResume();

        MainActivityFragment ff = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id
                .fragment_main);
        Toast.makeText(this, "landscape mode", Toast.LENGTH_SHORT).show();

    }
    @Override
    protected void onStart() {
        super.onStart();

    }

    public void onItemSelected(ArtistName selected_artistName) {

        if(getSupportFragmentManager().findFragmentById(R.id.fragment_top_tracks) != null &&
                getSupportFragmentManager().findFragmentById(R.id.fragment_top_tracks).isInLayout() ){

            Toast.makeText(this, "test mode", Toast.LENGTH_SHORT).show();
            mSelected_artistName = selected_artistName;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(TopTracksFragment.TOPTRACKS_PARCELABLEOBJECT,selected_artistName);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_top_tracks, fragment, TOPTRACKS_FRAGMENT_TAG)
                    .commit();

        }

        if(mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(TopTracksFragment.TOPTRACKS_PARCELABLEOBJECT,selected_artistName);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_top_tracks, fragment, TOPTRACKS_FRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(this, TopTracks.class).putExtra(Intent.EXTRA_TEXT,
                    selected_artistName);
            startActivity(intent);
        }
    }



    public void topTrackCallBack(List<SongParcelable> songList) {

        //show dialog

        FragmentManager fragmentManager = getSupportFragmentManager();
        PlayerDialog newFragment = PlayerDialog.newInstance(songList);
        // The device is using a large layout, so show the fragment as a dialog

        if (mTwoPane) {
            // The device is using a large layout, so show the fragment as a dialog
            newFragment.show(fragmentManager, "dialog");
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(R.id.fragment_toptracks, newFragment)
                    .addToBackStack(null).commit();
        }

    }




}

