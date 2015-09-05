package nl.idesign.spotifystreamer.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.SpotifyStreamer;
import nl.idesign.spotifystreamer.media.MediaPlayerController;
import nl.idesign.spotifystreamer.service.MediaPlayerService;

public class TopTracksActivity extends AppCompatActivity implements SpotifyStreamer.OnServiceBindListener,
        MediaPlayerService.OnPreparedListener
{

    public static final String PARAM_ARTIST_NAME = "artist_name";

    private MediaPlayerController mMediaController;
    private MediaPlayerService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        String artistName = getIntent().getStringExtra(PARAM_ARTIST_NAME);

        if(artistName != null && !artistName.isEmpty()) {
            if(getSupportActionBar() == null){
                return;
            }
            getSupportActionBar().setSubtitle(artistName);
        }

        if(mService == null){
            SpotifyStreamer streamer = (SpotifyStreamer)getApplicationContext();
            mService = streamer.getMediaService();
            //Not bound yet
            if(mService == null){
                streamer.setOnServiceBindListener(this);
            }
        }

        if(mMediaController == null){
            //spotify_bottom_player_fragment
            mMediaController = new MediaPlayerController(this);
            mMediaController.setAnchorView(findViewById(R.id.spotify_top_tracks_container));
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

}
