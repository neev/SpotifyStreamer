package com.example.android.spotifystreamer3;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.spotifystreamer3.parcelable.SongParcelable;

import java.util.List;

public class TopTracks extends AppCompatActivity implements CallBackTT {


    boolean mTwoPane=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        // retriving the mTwoPane value from shared preference
        SharedPreferences prefstwopane = PreferenceManager.getDefaultSharedPreferences(this);
        mTwoPane = prefstwopane.getBoolean("TwoPane",false);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mTwoPane == false) {

               //finish();
                return;
            }
        }

            if (savedInstanceState == null) {
                // Create the detail fragment and add it to the activity
                // using a fragment transaction.

                Bundle arguments = new Bundle();
                arguments.putParcelable(TopTracksFragment.TOPTRACKS_PARCELABLEOBJECT, getIntent().getData());

                TopTracksFragment fragment = new TopTracksFragment();
                fragment.setArguments(arguments);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_toptracks, fragment)
                        .commit();
            }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
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

    public void topTrackCallBack(List<SongParcelable> songList) {

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
