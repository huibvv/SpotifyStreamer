package nl.idesign.spotifystreamer.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class SpotifyIntentService extends IntentService {


    private static final String LOG_TAG = SpotifyIntentService.class.getSimpleName();

    private static final String PARAM_ARTIST_ID = "artist_id";
    private static final String ACTION_GET_TOP_TRACKS = "nl.idesign.spotifystreamer.service.GET_TOP_TRACKS";

    private SpotifyService mSpotifyService;

    public SpotifyIntentService() {
        super("SpotifyTopTracksIntentService");
    }

    public static void getTopTracks(Context context, String artistId) {
        Intent intent = new Intent(context, SpotifyIntentService.class);
        intent.setAction(ACTION_GET_TOP_TRACKS);
        intent.putExtra(PARAM_ARTIST_ID, artistId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the top tracks
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_TOP_TRACKS.equals(action)) {
                String spotifyArtistId = intent.getStringExtra(PARAM_ARTIST_ID);
                getTopTracks(spotifyArtistId);
            }
        }
    }

    private void getTopTracks(final String artistId){

        if (mSpotifyService == null) {
            SpotifyApi api = new SpotifyApi();
            //api.setAccessToken()
            mSpotifyService = api.getService();

        }

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("country", "NL");

        mSpotifyService.getArtistTopTrack(artistId, queryParams,  new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                if (tracks.tracks.size() == 0) {
                    return;
                }
                ContentValues artistValues = new ContentValues();
                artistValues.put(SpotifyStreamerDataContract.ArtistEntry.COLUMN_NAME_ARTIST_ID, artistId);
                getBaseContext().getContentResolver().insert(SpotifyStreamerDataContract.ArtistEntry.CONTENT_URI, artistValues);

                ContentValues values;
                List<ContentValues> contentList = new ArrayList<ContentValues>();
                for (Track track : tracks.tracks) {
                    values = new ContentValues();
                    values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID, track.id);
                    values.put(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_NAME, track.album.name);
                    if (track.album.images != null && track.album.images.size() > 0) {
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
                int numInserted = getBaseContext().getContentResolver().bulkInsert(SpotifyStreamerDataContract.TopTracksEntry.CONTENT_URI, contentList.toArray(cvArray));

                if(numInserted == 0){
                    //hmm, something went wrong i guess
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(LOG_TAG, "Failed to get the top tracks", error);
            }
        });

    }
}
