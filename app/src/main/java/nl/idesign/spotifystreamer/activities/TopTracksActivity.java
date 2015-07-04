package nl.idesign.spotifystreamer.activities;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import nl.idesign.spotifystreamer.R;

public class TopTracksActivity extends AppCompatActivity {

    public static final String PARAM_ARTIST_NAME = "artist_name";

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
    }

}
