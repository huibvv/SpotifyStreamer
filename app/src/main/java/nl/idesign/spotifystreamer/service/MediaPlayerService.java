package nl.idesign.spotifystreamer.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.activities.MainActivity;
import nl.idesign.spotifystreamer.activities.PlayerActivity;
import nl.idesign.spotifystreamer.activities.fragments.PlayerFragment;
import nl.idesign.spotifystreamer.data.SpotifyStreamerDataContract;
import nl.idesign.spotifystreamer.data.model.Track;

public class MediaPlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        android.widget.MediaController.MediaPlayerControl{

    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    private static final String ACTION_PLAY_NEXT = "play_next";
    private static final String ACTION_PLAY_PREVIOUS = "play_previous";
    private static final String ACTION_TOGGLE = "toggle_player";
    public static final String ACTION_PLAYER = "show_player";

    private boolean mIsTwoPane = false;
    public void setTwoPane(boolean twoPane){
        mIsTwoPane = twoPane;
    }


    private static final String[] mProjection = new String[]{
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_NAME,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_NAME,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_PREVIEW_URL,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_THUMBNAIL,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_NAME,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_ID,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_POPULARITY,
            SpotifyStreamerDataContract.TopTracksEntry._ID,
    };

    private static final int COL_ALBUM_NAME = 0;
    private static final int COL_TRACK_NAME = 1;
    private static final int COL_TRACK_PREVIEW_URL = 2;
    private static final int COL_THUMBNAIL = 3;
    private static final int COL_ARTIST_NAME = 4;
    private static final int COL_ARTIST_ID = 5;
    private static final int COL_POPULARITY = 6;
    private static final int COL_TRACK_ID = 7;

    private static final int RUNNABLE_TIME = 100;

    private final ServiceBinder mBinder = new ServiceBinder();

    private String mMediaUrl;
    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mMediaSession;
    private AudioManager mAudioManager;

    private List<Track> mPlaylist;
    private List<Long> mPlaylistIndex;

    private static long mTrackId = -1;
    private static Track mCurrentTrack;

    private int mBufferingPercentage;

    private boolean mIsPaused = false;

    private Handler mProgressHandler = new Handler();

    public MediaPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mMediaSession == null) {
            createMediaPlayerSession();
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        }

        if(intent == null){
            return super.onStartCommand(null, flags, startId);
        }

        String action = intent.getAction();
        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        switch(action){
            case ACTION_PLAY_NEXT:{
                nextTrack();
                break;
            }case ACTION_PLAY_PREVIOUS:{
                previousTrack();
                break;
            }case ACTION_TOGGLE:{
                if (!mIsPaused) {
                    pause();
                } else {
                    playTrack();
                }
                break;
            }
        }

        showMediaNotificationAsync();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Helper method to use the mediaplayer in other classes
     * @return the media player
     */
    public MediaPlayer getMediaPlayer(){
        return mMediaPlayer;
    }

    /**
     * Loads the mediaplayer with the give trackId and artistId.
     * @param trackId The spotify track ID
     * @param artistId The local storage artist ID
     * @return The current track
     */
    public Track loadMediaPlayer(long trackId, String artistId) {
        mTrackId = trackId;
        loadTracklist(artistId);
        return playTrack();
    }

    /**
     * Method to load the Spotify URL into the media player.
     * @param url The spotify preview Url
     */
    private void loadMediaPlayer(String url) {
        if (!url.equals(mMediaUrl)) {
            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepareAsync();
            } catch (IOException ex) {
                if (mOnErrorListener != null) {
                    mOnErrorListener.onError(mMediaPlayer, 99, -1);
                }
            }
        } else {
            if (mOnPreparedListener != null) {
                for(OnPreparedListener listener : mOnPreparedListener) {
                    listener.onPrepared(mMediaPlayer);
                }

            }
        }

        mMediaUrl = url;
    }


    /**
     * Creates a media session to show in the notifications and the lockscreen.
     */
    private void createMediaPlayerSession() {

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                                            @Override
                                            public void onAudioFocusChange(int focusChange) {

                                            }
                                        }, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);


        ComponentName mRemoteControlResponder = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
        PendingIntent pi = PendingIntent.getBroadcast(this, 99, new Intent(Intent.ACTION_MEDIA_BUTTON), PendingIntent.FLAG_UPDATE_CURRENT);

        mMediaSession = new MediaSessionCompat(this, LOG_TAG, mRemoteControlResponder, pi);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f, SystemClock.elapsedRealtime())
                .build();

        mMediaSession.setPlaybackState(state);
        mMediaSession.setActive(true);
    }

    private void updateMediaSessionMetaData() {
        new Thread() {
            @Override
            public void run() {
                Track track = getCurrentTrack();

                int playState = mIsPaused
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED;

                MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtistName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getAlbumName())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTrackname())
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (long) mMediaPlayer.getDuration())
                        .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, mPlaylistIndex.indexOf(mTrackId))
                        .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, mPlaylist.size());


                try {
                    InputStream is = (InputStream) new URL(track.getThumbnail()).getContent();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                } catch (MalformedURLException ex) {
                Log.e(LOG_TAG, "MalformedURLException occurred", ex);
                } catch (IOException iEx) {
                    Log.e(LOG_TAG, "IOException occurred", iEx);
                }

                mMediaSession.setMetadata(builder.build());
                mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(playState, mMediaPlayer.getCurrentPosition(), 1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build());
                mMediaSession.setActive(true);
            }
        }.start();
    }


    private void loadTracklist(String artistId) {

        if (mPlaylist == null) {
            mPlaylist = new ArrayList<>();
        }

        if (mPlaylistIndex == null) {
            mPlaylistIndex = new ArrayList<>();
        }

        Cursor c = null;
        try {
            c = getBaseContext().getContentResolver().query(SpotifyStreamerDataContract.TopTracksEntry.CONTENT_URI,
                    mProjection,
                    SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_ID + " = ?",
                    new String[]{artistId},
                    SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_POPULARITY + "," + SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_NAME + " ASC ");

            Track track;
            while (c.moveToNext()) {
                track = new Track();
                track.setAlbumName(c.getString(COL_ALBUM_NAME));
                track.setArtistId(c.getString(COL_ARTIST_ID));
                track.setArtistName(c.getString(COL_ARTIST_NAME));
                track.setId(c.getLong(COL_TRACK_ID));
                track.setPopularity(c.getInt(COL_POPULARITY));
                track.setThumbnail(c.getString(COL_THUMBNAIL));
                track.setTrackname(c.getString(COL_TRACK_NAME));
                track.setPreviewUrl(c.getString(COL_TRACK_PREVIEW_URL));

                mPlaylist.add(track);
                mPlaylistIndex.add(track.getId());
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }


    }

    public Track nextTrack() {
        int index = mPlaylistIndex.indexOf(mTrackId);

        if (index < (mPlaylist.size() - 1)) {
            mTrackId = mPlaylistIndex.get(index + 1);
            mCurrentTrack = mPlaylist.get(index + 1);
            loadMediaPlayer(mCurrentTrack.getPreviewUrl());
        }

        return mCurrentTrack;

    }

    public Track previousTrack() {
        //Load the previous song
        int index = mPlaylistIndex.indexOf(mTrackId);
        if (index > 0) {
            mTrackId = mPlaylistIndex.get(index - 1);
            mCurrentTrack = mPlaylist.get(index - 1);
            loadMediaPlayer(mCurrentTrack.getPreviewUrl());
        }
        return mCurrentTrack;
    }

    private Track playTrack() {
        int index = mPlaylistIndex.indexOf(mTrackId);
        if (index >= 0) {
            mCurrentTrack = mPlaylist.get(index);
            mIsPaused = false;
            loadMediaPlayer(mCurrentTrack.getPreviewUrl());

        }
        return mCurrentTrack;
    }

    public Track getCurrentTrack() {
        return mCurrentTrack;
    }

    public String getMediaUrl() {
        return mMediaUrl;
    }

    public void resume() {
        mIsPaused = false;
        mMediaPlayer.start();
    }

    public void pause() {
        mIsPaused = true;
        mMediaPlayer.pause();
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mBufferingPercentage = percent;
        if (mOnBufferingUpdateListener != null) {
            mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mp);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mOnErrorListener != null) {
            mOnErrorListener.onError(mp, what, extra);
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
        updateMediaSessionMetaData();
        mProgressHandler.postDelayed(mProgressRunnable, RUNNABLE_TIME);
        if (mOnPreparedListener != null) {
            for(OnPreparedListener listener : mOnPreparedListener) {
                listener.onPrepared(mp);
            }
        }
    }

    public void hideMediaNotification() {
        NotificationManager nm = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(0);

    }

    public void showMediaNotificationAsync(){
        new Thread() {
            @Override
            public void run() {
                showMediaNotification();
            }
        }.start();
    }

    private void showMediaNotification() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("pref_notification", true)){
            return;
        }

        Track track = getCurrentTrack();

        NotificationCompat.Builder mediaNotification = new NotificationCompat.Builder(getBaseContext());

        mediaNotification.setSmallIcon(R.drawable.no_image);
        mediaNotification.setContentTitle(track.getArtistName());
        mediaNotification.setContentText(track.getTrackname());
        mediaNotification.setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intent;
        if(mIsTwoPane){
            intent = new Intent( getApplicationContext(), MainActivity.class );
        }else {
            intent = new Intent( getApplicationContext(), PlayerActivity.class );
        }

        intent.setAction(ACTION_PLAYER);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(PlayerFragment.PARAM_EXTRA_TRACK_ID, mTrackId);
        intent.putExtra(MainActivity.PARAM_ARTIST_NAME, track.getArtistName());
        intent.putExtra(PlayerFragment.PARAM_EXTRA_ARTIST_ID, track.getArtistId());
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mediaNotification.setContentIntent(pendingIntent);
        mediaNotification.addAction(createMediaAction(android.R.drawable.ic_media_previous, getString(R.string.previous), ACTION_PLAY_PREVIOUS, 0)); // #0
        if(mIsPaused) {
            mediaNotification.addAction(createMediaAction(android.R.drawable.ic_media_play, getString(R.string.play), ACTION_TOGGLE, 1));
        }else {
            mediaNotification.addAction(createMediaAction(android.R.drawable.ic_media_pause, getString(R.string.pause), ACTION_TOGGLE, 1));
        }

        mediaNotification.addAction(createMediaAction(android.R.drawable.ic_media_next, getString(R.string.next), ACTION_PLAY_NEXT, 2)) ;    // #2

        mediaNotification.setAutoCancel(false);
        mediaNotification.setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mMediaSession.getSessionToken()));
        try {
            InputStream is = (InputStream) new URL(track.getThumbnail()).getContent();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            mediaNotification.setLargeIcon(bitmap);
        } catch (MalformedURLException ex) {

        } catch (IOException iEx) {

        }

        NotificationManager nm = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, mediaNotification.build());

    }

    private NotificationCompat.Action createMediaAction( int icon, String title, String intentAction, int tag ) {
        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), tag, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action.Builder( icon, title, pendingIntent ).build();
    }

    @Override
    public void start() {
        if(mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    @Override
    public int getDuration() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(mMediaPlayer != null) {
            Log.d(LOG_TAG, "Current Position:" + mMediaPlayer.getCurrentPosition());
            return mMediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if(mMediaPlayer != null)
             mMediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return mBufferingPercentage;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }

    public class RemoteControlReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            /* handle media button intent here by reading contents */
            /* of EXTRA_KEY_EVENT to know which key was pressed    */
            }

        }
    }

    private List<OnPreparedListener> mOnPreparedListener = new ArrayList<>();

    public interface OnPreparedListener {
        void onPrepared(MediaPlayer mp);
    }

    public void addOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener.add(listener);
    }

    public void removeOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener.remove(listener);
    }

    private OnErrorListener mOnErrorListener;

    public interface OnErrorListener {
        void onError(MediaPlayer mp, int what, int extra);
    }

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    public interface OnCompletionListener {
        void onCompletion(MediaPlayer mp);
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnBufferingUpdateListener mOnBufferingUpdateListener;

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(MediaPlayer mp, int percent);
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    private OnPlaybackUpdateListener mOnPlaybackUpdateListener;

    public interface OnPlaybackUpdateListener {
        void onPlaybackUpdate(int duration);
    }

    public void setOnPlaybackUpdateListener(OnPlaybackUpdateListener listener) {
        mOnPlaybackUpdateListener = listener;
    }


    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mOnPlaybackUpdateListener != null && mMediaPlayer != null) {
                mOnPlaybackUpdateListener.onPlaybackUpdate(mMediaPlayer.getCurrentPosition());
            }
            //if(mMediaPlayer != null && mMediaPlayer.i)
            mProgressHandler.postDelayed(mProgressRunnable, RUNNABLE_TIME);
        }
    };

    @Override
    public void onDestroy() {
        if(mMediaSession != null)
            mMediaSession.release();
    }

    public class ServiceBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


}
