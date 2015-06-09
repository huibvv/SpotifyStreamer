package nl.idesign.spotifystreamer.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import nl.idesign.spotifystreamer.data.SpotifyStreamerDataContract;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by huib on 9-6-2015.
 */
public class SpotifyTopTracksIntentService extends IntentService {

    private static final String LOG_TAG = SpotifyTopTracksIntentService.class.getSimpleName();
    public static final String PARAM_ARTIST_ID = "artist_id";
    private SpotifyService mSpotifyService;


    public SpotifyTopTracksIntentService() {
        super("SpotifyTopTracksIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the top tracks

        String spotifyArtistId = intent.getStringExtra(PARAM_ARTIST_ID);

        if(mSpotifyService == null) {
            SpotifyApi api = new SpotifyApi();
            mSpotifyService = api.getService();
        }

        mSpotifyService.getArtistTopTrack(spotifyArtistId, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                if(tracks.tracks.size() == 0) {
                    return;
                }
                ContentValues values;
                List<ContentValues> contentList = new ArrayList<ContentValues>();
                for(Track track : tracks.tracks){
                    values = new ContentValues();
                    values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID, track.id);
                    values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_NAME, track.album.name);
                    if(track.album.images != null && track.album.images.size() > 0) {
                        values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_THUMBNAIL, track.album.images.get(0).url);
                    }

                    //Hmm, artists is a list. Lets assume that we always have one artist
                    values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_ID, track.artists.get(0).id);
                    values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_NAME, track.name);
                    values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_POPULARITY, track.popularity);
                    values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_PREVIEW_URL, track.preview_url);
                    contentList.add(values);
                }

                ContentValues[] cvArray = new ContentValues[contentList.size()];
                getBaseContext().getContentResolver().bulkInsert(SpotifyStreamerDataContract.TopTracksEntry.CONTENT_URI, contentList.toArray(cvArray));


            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(LOG_TAG, "Failed to get the top tracks", error);
            }
        });


    }
}
