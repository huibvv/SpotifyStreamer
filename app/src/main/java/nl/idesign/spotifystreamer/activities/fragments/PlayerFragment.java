package nl.idesign.spotifystreamer.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.data.SpotifyStreamerDataContract;
import nl.idesign.spotifystreamer.data.model.Track;
import nl.idesign.spotifystreamer.service.MediaPlayerService;

/**
 * Created by huib on 4-7-2015.
 */
public class PlayerFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String PARAM_EXTRA_TRACK_ID = "track_id";
    public static final String PARAM_EXTRA_ARTIST_ID = "artist_id";

    private static final int RUNNABLE_TIME = 100;
    private static final int CURSOR_LOADER = 0;


    private long mTrackId = -1;
    private String mCurrentArtistId;

    private List<Track> mPlaylist;
    private List<Long> mPlaylistIndex;

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

    private static int COL_ALBUM_NAME = 0;
    private static int COL_TRACK_NAME = 1;
    private static int COL_TRACK_PREVIEW_URL = 2;
    private static int COL_THUMBNAIL = 3;
    private static int COL_ARTIST_NAME = 4;
    private static int COL_ARTIST_ID = 5;
    private static int COL_POPULARITY = 6;
    private static int COL_TRACK_ID = 7;

    private TextView mTrackNameTextView;
    private TextView mAlbumNameTextView;
    private TextView mArtistNameTextView;
    private ImageView mAlbumImageView;
    private ImageButton mPlayerPreviousImageView;
    private ImageButton mPlayerPlayImageView;
    private ImageButton mPlayerNextImageView;
    private SeekBar mPlayerProgressSeekbar;


    private MediaPlayerService mService;
    boolean mBound = false;
    private String mPreviewUrl;

    private boolean mIsSeeking = false;
    private boolean mMediaPlayerLoaded = false;

    private boolean mIsDialog = false;

    private MediaPlayer mMediaplayer;

    public PlayerFragment() {
        super();
    }

    public void setTrackId(long trackId) {
        mTrackId = trackId;
    }

    public void setArtistId(String artistId) {
        mCurrentArtistId = artistId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mIsDialog = true;
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container);

        if(!mIsDialog) {
            Intent intent = getActivity().getIntent();
            mTrackId = intent.getLongExtra(PARAM_EXTRA_TRACK_ID, -1);
            mCurrentArtistId = intent.getStringExtra(PARAM_EXTRA_ARTIST_ID);
        }else {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        //bind the controls
        mTrackNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_track_name);
        mAlbumNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_album_name);
        mArtistNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_artist_name);


        mAlbumImageView = (ImageView) rootView.findViewById(R.id.spotify_player_album_image);
        mAlbumNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_album_name);
        mArtistNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_artist_name);


        mPlayerPreviousImageView = (ImageButton)rootView.findViewById(R.id.spotify_player_previous_image_button);
        mPlayerPlayImageView = (ImageButton)rootView.findViewById(R.id.spotify_player_play_image_button);
        mPlayerNextImageView = (ImageButton)rootView.findViewById(R.id.spotify_player_next_image_button);
        mPlayerProgressSeekbar = (SeekBar)rootView.findViewById(R.id.spotify_player_track_seekbar);

        mPlayerPlayImageView.setOnClickListener(mOnPlayClickedListener);
        mPlayerNextImageView.setOnClickListener(mOnNextClickedListener);
        mPlayerPreviousImageView.setOnClickListener(mOnPreviousClickedListener);

        mPlayerProgressSeekbar.setOnSeekBarChangeListener(mSeekbarChangedListener);

        return rootView;
    }

    private void setData(){

        int index = mPlaylistIndex.indexOf(mTrackId);
        Track track = mPlaylist.get(index) ;

        mAlbumNameTextView.setText(track.getAlbumName());
        mArtistNameTextView.setText(track.getArtistName());
        Picasso.with(getActivity()).load(track.getThumbnail()).into(mAlbumImageView);

        mTrackNameTextView.setText(track.getTrackname());

        mPreviewUrl = track.getPreviewUrl();

        if(mBound){
            loadMediaPlayer();
        }

    }


    private void startPlaying(){
        if(mMediaplayer != null)
            mMediaplayer.start();
    }

    private void stopPlaying(){
        if(mMediaplayer != null)
            mMediaplayer.stop();
    }

    private void pausePlaying(){
        if(mMediaplayer != null)
            mMediaplayer.pause();
    }

    private void enableControls(){
        mPlayerPreviousImageView.setEnabled(true);
        mPlayerNextImageView.setEnabled(true);
        mPlayerPlayImageView.setEnabled(true);
        mPlayerProgressSeekbar.setEnabled(true);
    }

    private View.OnClickListener mOnPlayClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!mMediaPlayerLoaded){
                return;
            }

            if(!mMediaplayer.isPlaying()){
                mPlayerPlayImageView.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.ic_media_pause));
                startPlaying();
            }else {
                mPlayerPlayImageView.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.ic_media_play));
                pausePlaying();
            }
        }
    };

    private View.OnClickListener mOnNextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Load the next song
            int index = mPlaylistIndex.indexOf(mTrackId);
            if(index < (mPlaylist.size()-1)) {
                mTrackId = mPlaylistIndex.get(index + 1);
                setData();
            }
        }
    };

    private View.OnClickListener mOnPreviousClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Load the previous song
            int index = mPlaylistIndex.indexOf(mTrackId);
            if(index > 0) {
                mTrackId = mPlaylistIndex.get(index - 1);
                setData();
            }

        }
    };

    private SeekBar.OnSeekBarChangeListener mSeekbarChangedListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(mIsSeeking)
                mMediaplayer.seekTo(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mIsSeeking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mIsSeeking = false;
        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mTrackId == -1){
            return null;
        }

        return new CursorLoader(getActivity(),
                SpotifyStreamerDataContract.TopTracksEntry.CONTENT_URI,
                mProjection,
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_ID + " = ?",
                new String[]{mCurrentArtistId},
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_POPULARITY + "," + SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_NAME + " ASC "
                );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Set the values

        if(mPlaylist == null){
            mPlaylist = new ArrayList<>();
        }

        if(mPlaylistIndex == null){
            mPlaylistIndex = new ArrayList<>();
        }

        Track track = null;
        while(data.moveToNext()){
            track = new Track();
            track.setAlbumName(data.getString(COL_ALBUM_NAME));
            track.setArtistId(data.getString(COL_ARTIST_ID));
            track.setArtistName(data.getString(COL_ARTIST_NAME));
            track.setId(data.getLong(COL_TRACK_ID));
            track.setPopularity(data.getInt(COL_POPULARITY));
            track.setThumbnail(data.getString(COL_THUMBNAIL));
            track.setTrackname(data.getString(COL_TRACK_NAME));
            track.setPreviewUrl(data.getString(COL_TRACK_PREVIEW_URL));
            mPlaylist.add(track);
            mPlaylistIndex.add(track.getId());
        }

        setData();


    }

    private void loadMediaPlayer(){
        mService.loadMediaPlayer(mPreviewUrl);

    }

    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {

        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mPlayerProgressSeekbar.setProgress(0);
            mPlayerPlayImageView.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.ic_media_play));
        }
    };

    private MediaPlayerService.OnPreparedListener mOnPreparedListener = new MediaPlayerService.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            //mMediaplayer.setVolume();
            mMediaplayer = mp;
            mMediaplayer.setOnCompletionListener(mOnCompletionListener);
            mService.setOnPlaybackUpdateListener(mPlaybackListener);
            mPlayerPlayImageView.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.ic_media_pause));
            mPlayerProgressSeekbar.setMax( mMediaplayer.getDuration());
            mMediaPlayerLoaded = true;
            startPlaying();
            enableControls();
        }
    };

    private MediaPlayerService.OnPlaybackUpdateListener mPlaybackListener = new MediaPlayerService.OnPlaybackUpdateListener() {
        @Override
        public void onPlaybackUpdate(int duration) {
            if(!mIsSeeking)
                mPlayerProgressSeekbar.setProgress(mMediaplayer.getCurrentPosition());
        }
    };

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURSOR_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void showPlaybackErrorDialog(){
       new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.player_playback_error))
               .setMessage(getString(R.string.player_playback_error_desc))
               .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       startPlaying();
                       dialog.dismiss();
                   }
               })
               .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                   }
               }).show();
    }

    /**
     * Developer docs Android Bound service
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.ServiceBinder binder = (MediaPlayerService.ServiceBinder) service;
            mService = binder.getService();
            mService.setOnPreparedListener(mOnPreparedListener);

            if(!mMediaPlayerLoaded && mPreviewUrl != null )
                loadMediaPlayer();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(intent);

    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            mService.setOnPlaybackUpdateListener(null);
            mService.setOnPreparedListener(null);
            mMediaplayer.setOnCompletionListener(null);
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }


}
