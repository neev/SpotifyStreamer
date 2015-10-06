package com.example.android.spotifystreamer3.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.android.spotifystreamer3.PlayerDialog;
import com.example.android.spotifystreamer3.R;

import java.io.IOException;


/**
 * Created by neeraja on 7/23/2015.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,MediaPlayer.OnSeekCompleteListener {

    private static final String LOG_TAG = MusicService.class.getSimpleName();
    String mUrl = "";
    // Set up the notification ID
    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_TOGGLE_PLAYBACK = "com.example.android.myspotifystreamer" +
            ".playAudio";
    private static final String ACTION_PREV = "com.example.android.myspotifystreamer.PREV";
    public static final String ACTION_NEXT = "com.example.android.myspotifystreamer.nextAudio";

    NotificationManager mNotificationManager;
    Notification mNotification = null;

    MediaSession mSession;


    private MediaPlayer mMediaPlayer = null;    // The Media Player
    private int mBufferPosition;
    //variables for seekBar
    String playingSongTitle;
    String playingSongArtistName;
    String playingSongImageURL;
    int integerSeekPosition;
    int trackPosition;
    int trackMaxDuration;
    int songEnded=0;
    private Handler handler = new Handler();
    int seekPos=0;
    int pausedSeekPos=0;


    public static final String BROADCAST_ACTION = "com.example.android.myspotifystreamer" +
            ".seekprogress";
    //SET up broadcast identifier and Intent
    public static final String BROADCAST_BUFFER="com.example.android.myspotifystreamer" +
            ".broadcastbuffer";
    Intent bufferIntent;
    Intent seekIntent;
    Intent playIntent;
    Intent nextIntent;
    AudioManager am;


    public MusicService() {

    }


    @Override
    public void onCreate() {
        Log.v(LOG_TAG,"Creating Music Service");
        // instantiate bufferIntent to communicate with the activity for progress dialog
        bufferIntent=new Intent(BROADCAST_BUFFER);
        //set up seekbar broadcast
        seekIntent=new Intent(BROADCAST_ACTION);

        mMediaPlayer = new MediaPlayer(); // initialize it here
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.reset();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(songEnded !=1) {

            mp.reset();
            songEnded = 1;
            seekIntent.putExtra("counter", String.valueOf(mp.getCurrentPosition()));
            seekIntent.putExtra("mediamax", String.valueOf(mp.getDuration()));
            seekIntent.putExtra("song_ended", String.valueOf(songEnded));
            sendBroadcast(seekIntent);

            Toast.makeText(this,
                    "Song Complete", Toast.LENGTH_SHORT).show();
            Toast.makeText(this,
                    "click the play button to repeat the song"+" max : ", Toast
                            .LENGTH_SHORT).show();
            resetButtonPlayStopBroadcast();
        }else{
            resetButtonPlayStopBroadcast();
        }

        // Abandon audio focus when playback complete
        am.abandonAudioFocus(afChangeListener);


    }



    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
                // Pause playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                am.abandonAudioFocus(afChangeListener);
                // Stop playback
            }
        }
    };
    @Override
    public void onPrepared(MediaPlayer arg0) {

        // Send a message to activity to end progress dialogue

        sendBufferCompleteBroadcast();
        playMedia();

    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        mMediaPlayer.reset();

        songEnded=0;
        // ---Set up receiver for seekbar change ---
        registerReceiver(broadcastReceiver, new IntentFilter(
                PlayerDialog.BROADCAST_SEEKBAR));
        if (intent.getStringExtra("pausedSeekPos") != null) {
            mUrl = intent.getStringExtra("SONG_URL");
            String strpausedSeekPos = intent.getStringExtra("pausedSeekPos");
            playingSongTitle = intent.getStringExtra("SongTitle");
            playingSongArtistName = intent.getStringExtra("ArtistName");
            playingSongImageURL = intent.getStringExtra("imageURL");
            pausedSeekPos = Integer.parseInt(strpausedSeekPos);
        } else {
            Toast.makeText(this,
                    "*cannot play the song*", Toast.LENGTH_SHORT).show();
        }

        // Request audio focus for playback
        int result = am.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //am.registerMediaButtonEventReceiver(RemoteControlReceiver);
            // Start playback.

            if (!mMediaPlayer.isPlaying()) {
                try {
                    mMediaPlayer.setDataSource(mUrl);
                    mMediaPlayer.prepareAsync();

                    // Send message to Activity to display progress dialogue
                    sendBufferingBroadcast();

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
// --- Set up seekbar handler ---
            setupHandler();

            // Insert notification start
            // initNotification();

        }
        try {
            initNotification();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }



    // ---Send seekbar info to activity----
    private void setupHandler(){
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI,1000); // one second

    }

    private Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {
            LogMediaPosition();
            handler.postDelayed(this, 1000);

        }
    };

    private void LogMediaPosition() {
        if(mMediaPlayer.isPlaying()) {
            trackPosition=mMediaPlayer.getCurrentPosition();
            trackMaxDuration=mMediaPlayer.getDuration();

            seekIntent.putExtra("counter", String.valueOf(trackPosition));
            seekIntent.putExtra("mediamax", String.valueOf(trackMaxDuration));
            seekIntent.putExtra("song_ended", String.valueOf(songEnded));
            sendBroadcast(seekIntent);
        }

    }
    // --Receive seekbar position if it has been changed by the user in the
    // activity
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekPos(intent);
        }
    };

    // Update seek position from Activity
    public void updateSeekPos(Intent intent) {
        seekPos = intent.getIntExtra("seekpos", 0);
        if (mMediaPlayer.isPlaying()) {
            handler.removeCallbacks(sendUpdatesToUI);
            mMediaPlayer.seekTo(seekPos);
            setupHandler();
        }

    }
    @Override
    public void onSeekComplete(MediaPlayer mp) {

        if (!mMediaPlayer.isPlaying()){
            playMedia();

        }

    }

    // ---End of seekbar code


    public void restartMusic() {
        // Restart music
    }

    protected void setBufferPosition(int progress) {
        mBufferPosition = progress;
    }





    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:" + extra, Toast.LENGTH_SHORT).show();
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this, "MEDIA_ERROR_SERVER_DIED:" + extra, Toast.LENGTH_SHORT).show();
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this, "MEDIA_ERROR_UNKNOWN:" + extra, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();

            mMediaPlayer.release();
        }
        // mState = State.Retrieving;
        stopForeground(true);
        // Cancel the notification
        //cancelNotification();
        //stop seek handler fron sending updates to UI
        handler.removeCallbacks(sendUpdatesToUI);
        // Unregister seekbar receiver
        unregisterReceiver(broadcastReceiver);
        // Service ends, need to tell activity to display "Play" button
        resetButtonPlayStopBroadcast();
    }
    // Send a message to Activity that audio is being prepared and buffering
    // started.
    private void sendBufferingBroadcast() {
        // Log.v(TAG, "BufferStartedSent");
        bufferIntent.putExtra("buffering", "1");
        sendBroadcast(bufferIntent);
    }


    // Send a message to Activity that audio is prepared and ready to start
    // playing.
    private void sendBufferCompleteBroadcast() {
        // Log.v(TAG, "BufferCompleteSent");
        bufferIntent.putExtra("buffering", "0");
        sendBroadcast(bufferIntent);
    }

    // Send a message to Activity to reset the play button.
    private void resetButtonPlayStopBroadcast() {
        // Log.v(TAG, "BufferCompleteSent");
        bufferIntent.putExtra("buffering", "2");
        sendBroadcast(bufferIntent);
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void stopMusic() {
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();

    }
    public void playMedia() {
        if(pausedSeekPos == 0) {

            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }else{

            mMediaPlayer.seekTo(pausedSeekPos);
            mMediaPlayer.start();
        }

    }

    public void pauseMusic() {

        mMediaPlayer.pause();

        //updateNotification(mSongTitle + "(paused)");

    }



    public boolean isPlaying() {

        return true;

    }

    public void seekTo(int position) {
        mMediaPlayer.seekTo(position);

    }

    public int getMusicDuration() {
        // Return current music duration
        return mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        // Return current position
        return mMediaPlayer.getCurrentPosition();
    }

    public int getBufferPercentage() {
        return mBufferPosition;
    }

    public void seekMusicTo(int pos) {
        // Seek music to pos
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        setBufferPosition(percent * getMusicDuration() / 1000);
    }


    void updateNotification(String text) {
        // Notify NotificationManager of new intent
    }


    // Cancel Notification
    private void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void initNotification() throws IOException {

        //Bitmap artwork = getBitmap(playingSongImageURL);

        // Create a new MediaSession
        final MediaSession mediaSession = new MediaSession(this, "debug tag");
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadata.Builder()
                //.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, artwork )
                .putString(MediaMetadata.METADATA_KEY_ARTIST, playingSongArtistName)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, playingSongTitle)
                        //.putString(MediaMetadata.METADATA_KEY_TITLE, selectedSong.getTrackName())
                .build());
        // Indicate you're ready to receive media commands
        mediaSession.setActive(true);
        // Attach a new Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSession.Callback() {

            // Implement your callbacks

        });
        // Indicate you want to receive transport controls via your Callback
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Create a new Notification
        final Notification noti = new Notification.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                        // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                        // Set the Notification style
                .setStyle(new Notification.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                                // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                        // Set the Notification color
                .setColor(0xFFDB4437)
                        // Set the large and small icons
                        //.setLargeIcon(R.drawable.images1)
                .setSmallIcon(R.drawable.images1)
                        // Set Notification content information
                .setContentText(playingSongArtistName)
                        //.setContentInfo(selectedSong.getTrackName())
                .setContentTitle(playingSongTitle)
                        // Add some playback controls
                .addAction(android.R.drawable.ic_media_previous, "prev", retreivePlaybackAction(3))
                .addAction(android.R.drawable.ic_media_pause, "pause", retreivePlaybackAction(1))
                .addAction(android.R.drawable.ic_media_next, "next", retreivePlaybackAction(2))
                .build();

        // Do something with your TransportControls
        final MediaController.TransportControls controls = mediaSession.getController().getTransportControls();

        ((NotificationManager) getSystemService(
                NOTIFICATION_SERVICE))
                .notify(1,
                        noti);
    }

    private PendingIntent retreivePlaybackAction(int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(getApplicationContext(), PlayerDialog.class);


        switch (which) {
            case 1:
                // Play and pause
                action = new Intent(ACTION_TOGGLE_PLAYBACK);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, new Intent
                                (getApplicationContext(), PlayerDialog.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                return pendingIntent;
            case 2:
                // Skip tracks
                action = new Intent(ACTION_NEXT);
                action.setComponent(serviceName);
                action.putExtra("nextNoti","next");
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 2, action, 0);

                return pendingIntent;
            case 3:
                // Previous tracks
                action = new Intent(ACTION_PREV);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 3, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }





}
