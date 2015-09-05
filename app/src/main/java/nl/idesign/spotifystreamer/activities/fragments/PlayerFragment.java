package nl.idesign.spotifystreamer.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.SpotifyStreamer;
import nl.idesign.spotifystreamer.data.model.Track;
import nl.idesign.spotifystreamer.service.MediaPlayerService;

/**
 * Created by huib on 4-7-2015.
 */
public class PlayerFragment extends DialogFragment{

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    public static final String PARAM_EXTRA_TRACK_ID = "track_id";
    public static final String PARAM_EXTRA_ARTIST_ID = "artist_id";

    private long mTrackId = -1;
    private String mCurrentArtistId;

    private TextView mTrackNameTextView;
    private TextView mAlbumNameTextView;
    private TextView mArtistNameTextView;
    private ImageView mAlbumImageView;
    private ImageButton mPlayerPreviousImageView;
    private ImageButton mPlayerPlayImageView;
    private ImageButton mPlayerNextImageView;
    private SeekBar mPlayerProgressSeekbar;


    private MediaPlayerService mService;

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
        setHasOptionsMenu(true);
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

        SpotifyStreamer streamer = (SpotifyStreamer)getActivity().getApplicationContext();
        mService = streamer.getMediaService();


        if(!mIsDialog) {
            Intent intent = getActivity().getIntent();
            mTrackId = intent.getLongExtra(PARAM_EXTRA_TRACK_ID, -1);
            mCurrentArtistId = intent.getStringExtra(PARAM_EXTRA_ARTIST_ID);
        }

        //bind the controls
        mTrackNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_track_name);
        mArtistNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_artist_name);

        mAlbumImageView = (ImageView) rootView.findViewById(R.id.spotify_player_album_image);
        mAlbumNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_album_name);
        mArtistNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_artist_name);


        mPlayerPreviousImageView = (ImageButton)rootView.findViewById(R.id.spotify_player_previous_image_button);
        mPlayerPlayImageView = (ImageButton)rootView.findViewById(R.id.spotify_player_play_image_button);
        mPlayerNextImageView = (ImageButton)rootView.findViewById(R.id.spotify_player_next_image_button);

        mPlayerPlayImageView.setOnClickListener(mOnPlayClickedListener);
        mPlayerNextImageView.setOnClickListener(mOnNextClickedListener);
        mPlayerPreviousImageView.setOnClickListener(mOnPreviousClickedListener);

        mPlayerProgressSeekbar = (SeekBar)rootView.findViewById(R.id.spotify_player_track_seekbar);
        mAlbumNameTextView = (TextView)rootView.findViewById(R.id.spotify_player_album_name);
        mPlayerProgressSeekbar.setOnSeekBarChangeListener(mSeekbarChangedListener);


        Track track = mService.loadMediaPlayer(mTrackId, mCurrentArtistId);

        if(!mIsDialog) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(track.getArtistName());
            ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(track.getTrackname());

        }else {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        //setData(track);

        return rootView;
    }


    private void setData(Track track){
        mAlbumNameTextView.setText(track.getAlbumName());
        mArtistNameTextView.setText(track.getArtistName());
        Picasso.with(getActivity()).load(track.getThumbnail()).into(mAlbumImageView);
        mAlbumImageView.setContentDescription(String.format(getString(R.string.content_description_album_image),track.getAlbumName(),track.getArtistName()));

        mTrackNameTextView.setText(track.getTrackname());
    }


    private void startPlaying(){
        if(mMediaplayer != null) {
            mMediaplayer.start();
        }
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

    private final View.OnClickListener mOnPlayClickedListener = new View.OnClickListener() {
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

    private final View.OnClickListener mOnNextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Load the next song
            Track track = mService.nextTrack();
            setData(track);
        }
    };

    private final View.OnClickListener mOnPreviousClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Load the previous song
            Track track = mService.previousTrack();
            setData(track);

        }
    };

    private final SeekBar.OnSeekBarChangeListener mSeekbarChangedListener = new SeekBar.OnSeekBarChangeListener() {
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


    private final MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {

        }
    };

    private final MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mMediaplayer.setOnCompletionListener(null);
            mPlayerProgressSeekbar.setProgress(0);
            mPlayerPlayImageView.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.ic_media_play));
        }
    };

    private final MediaPlayerService.OnPreparedListener mOnPreparedListener = new MediaPlayerService.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
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

    private final  MediaPlayerService.OnPlaybackUpdateListener mPlaybackListener = new MediaPlayerService.OnPlaybackUpdateListener() {
        @Override
        public void onPlaybackUpdate(int duration) {
            if(!mIsSeeking)
                mPlayerProgressSeekbar.setProgress(mMediaplayer.getCurrentPosition());
        }
    };


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


//    @Override
//    public void onStop() {
//        super.onStop();
//        mService.setOnPlaybackUpdateListener(null);
//        mService.removeOnPreparedListener(mOnPreparedListener);
//        mMediaplayer.setOnCompletionListener(null);
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_player, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createSpotifyShareIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

    }

    /** Returns a share intent */
    private Intent createSpotifyShareIntent(){
        Track currentTrack = mService.getCurrentTrack();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.action_share_title));
        intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.action_share_content), currentTrack.getPreviewUrl()));
        return intent;
    }

    @Override
    public void onResume() {
        super.onResume();
        mService.addOnPreparedListener(mOnPreparedListener);
        setData(mService.getCurrentTrack());
        if(mMediaplayer != null){
            mMediaplayer.setOnCompletionListener(mOnCompletionListener);
            mService.setOnPlaybackUpdateListener(mPlaybackListener);
            mPlayerProgressSeekbar.setMax(mMediaplayer.getDuration());
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mService.removeOnPreparedListener(mOnPreparedListener);

        if(mMediaplayer != null) {
            mService.setOnPlaybackUpdateListener(null);
            mMediaplayer.setOnCompletionListener(null);
        }
    }

}
