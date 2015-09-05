package nl.idesign.spotifystreamer.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.SpotifyStreamer;
import nl.idesign.spotifystreamer.activities.fragments.PlayerFragment;
import nl.idesign.spotifystreamer.activities.fragments.SpotifySearchFragment;
import nl.idesign.spotifystreamer.activities.fragments.TopTracksFragment;
import nl.idesign.spotifystreamer.media.MediaPlayerController;
import nl.idesign.spotifystreamer.service.MediaPlayerService;


public class MainActivity extends AppCompatActivity implements SpotifySearchFragment.OnItemSelectedCallback,
        SpotifyStreamer.OnServiceBindListener,
        MediaPlayerService.OnPreparedListener{

    private boolean mTwoPane;
    private String mArtistId;
    private String mArtistName;

    public static final String PARAM_ARTIST_NAME = "artist_name";
    private static final String PARAM_ARTIST_ID = "artist_id";

    private static final String PLAYER_FRAGMENT_TAG = "player_fragment";

    private MediaPlayerController mMediaController;
    private MediaPlayerService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if(getFragmentManager().findFragmentById(R.id.spotify_top_tracks_container) != null) {
            mTwoPane = true;
        }

        if(getIntent().getAction() != null && getIntent().getAction().equals(MediaPlayerService.ACTION_PLAYER)){
            mArtistId = getIntent().getStringExtra(PlayerFragment.PARAM_EXTRA_ARTIST_ID);
            mArtistName = getIntent().getStringExtra(PARAM_ARTIST_NAME);
            long trackId = getIntent().getLongExtra(PlayerFragment.PARAM_EXTRA_TRACK_ID, -1);

            if(mTwoPane){
                //Load the dialog
                PlayerFragment playerFragment = new PlayerFragment();
                playerFragment.setTrackId(trackId);
                playerFragment.setArtistId(mArtistId);
                playerFragment.show(getFragmentManager(), PLAYER_FRAGMENT_TAG);
                onItemSelected(mArtistId, mArtistName);

            }else {
                //Start the player dialog
                Intent playerIntent = new Intent(this, PlayerActivity.class);
                playerIntent.putExtra(PlayerFragment.PARAM_EXTRA_ARTIST_ID, mArtistId);
                playerIntent.putExtra(PlayerFragment.PARAM_EXTRA_TRACK_ID, trackId);
                startActivity(playerIntent);
            }
        }

        if(mTwoPane) {
            if (savedInstanceState != null) {
                mArtistName = savedInstanceState.getString(PARAM_ARTIST_NAME);
                mArtistId = savedInstanceState.getString(PARAM_ARTIST_ID);
                onItemSelected(mArtistId, mArtistName);
            }
        }


        if(mService == null){
            SpotifyStreamer streamer = (SpotifyStreamer)getApplicationContext();
            mService = streamer.getMediaService();

            //Not bound yet
            if(mService == null){
                streamer.setOnServiceBindListener(this);
            }else {
                mService.setTwoPane(mTwoPane);
            }
        }
        if(mMediaController == null){
            //spotify_bottom_player_fragment
            mMediaController = new MediaPlayerController(this);
            mMediaController.setAnchorView(findViewById(R.id.spotify_main_container));
            mMediaController.setPrevNextListeners(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mService.nextTrack();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mService.previousTrack();
                        }
                    });

        }


    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mService == null){
            SpotifyStreamer streamer = (SpotifyStreamer)getApplicationContext();
            mService = streamer.getMediaService();
            //Not bound yet
            if(mService == null){
                streamer.setOnServiceBindListener(this);
                return;
            }
        }

        setupMediaControl();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        SpotifyStreamer streamer = (SpotifyStreamer)getApplicationContext();
        streamer.setOnServiceBindListener(null);
        if(mService != null){
            mService.removeOnPreparedListener(this);
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaController.show(0);
        mMediaController.setEnabled(true);
    }

    @Override
    public void onServiceBound(MediaPlayerService mp) {
        mService = mp;
        mService.setTwoPane(mTwoPane);
        setupMediaControl();
    }

    @Override
    public void onServiceUnbound() {
        mService = null;
    }

    private void setupMediaControl(){
        mService.addOnPreparedListener(this);
        mMediaController.setMediaPlayer(mService);

        if(!mMediaController.isShowing()) {
            if (mService.getMediaPlayer().isPlaying()) {
                mMediaController.show(0);
                mMediaController.setEnabled(true);
            }
        }
    }

    @Override
    public void onItemSelected(String artistId, String artistName) {
        mArtistId = artistId;
        mArtistName = artistName;

        if(mTwoPane) {
            TopTracksFragment fragment = (TopTracksFragment)getFragmentManager().findFragmentById(R.id.spotify_top_tracks_container);
            fragment.setArtistId(artistId);
            fragment.setTwoPane(true);
        }else {
            Intent topTrackIntent = new Intent(this, TopTracksActivity.class);
            topTrackIntent.putExtra(TopTracksFragment.PARAM_EXTRA_ARTIST_ID, artistId);
            topTrackIntent.putExtra(TopTracksActivity.PARAM_ARTIST_NAME, artistName);
            startActivity(topTrackIntent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PARAM_ARTIST_NAME, mArtistName);
        outState.putString(PARAM_ARTIST_ID, mArtistId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_settings){
            //Show settings menu
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}
