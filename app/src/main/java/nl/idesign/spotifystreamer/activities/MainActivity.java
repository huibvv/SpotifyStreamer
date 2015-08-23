package nl.idesign.spotifystreamer.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.activities.fragments.PlayerFragment;
import nl.idesign.spotifystreamer.activities.fragments.SpotifySearchFragment;
import nl.idesign.spotifystreamer.activities.fragments.TopTracksFragment;


public class MainActivity extends AppCompatActivity implements SpotifySearchFragment.OnItemSelectedCallback {

    private boolean mTwoPane;
    private String mArtistId;
    private String mArtistName;

    private static final String PARAM_ARTIST_NAME = "artist_name";
    private static final String PARAM_ARTIST_ID = "artist_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(getFragmentManager().findFragmentById(R.id.spotify_top_tracks_container) != null){
            mTwoPane = true;
            //if(getFragmentManager().findFragmentById(R.id.spotify_top_tracks_container) == null)
            //    getFragmentManager().beginTransaction().add(R.id.spotify_top_tracks_container, TopTracksFragment.getInstance()).commit();

            if(savedInstanceState != null){
                mArtistName = savedInstanceState.getString(PARAM_ARTIST_NAME);
                mArtistId =savedInstanceState.getString(PARAM_ARTIST_ID);
                onItemSelected(mArtistId , mArtistName);
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
}
