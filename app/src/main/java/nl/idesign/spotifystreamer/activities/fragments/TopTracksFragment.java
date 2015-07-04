package nl.idesign.spotifystreamer.activities.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import nl.idesign.spotifystreamer.Constants;
import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.adapters.SpotifyTopTracksAdapter;
import nl.idesign.spotifystreamer.data.SpotifyStreamerDataContract;
import nl.idesign.spotifystreamer.service.SpotifyIntentService;

/**
 * This fragment shows the top tracks of a artist.
 */
public class TopTracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    public static final String PARAM_EXTRA_ARTIST_ID = "artist_id";

    private static TopTracksFragment mInstance;
    private static final int TOP_TRACKS_LOADER = 0;

    private SpotifyTopTracksAdapter mAdapter;

    public static TopTracksFragment getInstance(){
        if(mInstance == null){
            mInstance = new TopTracksFragment();
        }

        return mInstance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container);

        Intent intent = getActivity().getIntent();

        String artistId = intent.getStringExtra(PARAM_EXTRA_ARTIST_ID);
        if(artistId == null || artistId.isEmpty()){
            Log.e(LOG_TAG, "No artist ID found, we can't get the top tracks");
            getActivity().finish();
        }

        IntentFilter filter = new IntentFilter(SpotifyIntentService.BROADCAST_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new TopTracksResponseReceiver(), filter);

        SpotifyIntentService.getTopTracks(getActivity(), artistId);



        ListView topTracksListView = (ListView)rootView.findViewById(R.id.top_tracks_listview);
        mAdapter = new SpotifyTopTracksAdapter(getActivity(), true);
        topTracksListView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        String artistId = intent.getStringExtra(PARAM_EXTRA_ARTIST_ID);
        return new CursorLoader(getActivity(),
                SpotifyStreamerDataContract.TopTracksEntry.CONTENT_URI,
                SpotifyTopTracksAdapter.mProjection,
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_ID + " = ? ",
                new String[]{artistId},
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_POPULARITY + " ASC ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getLoaderManager().initLoader(TOP_TRACKS_LOADER, null, this);
    }

    private class TopTracksResponseReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int result = intent.getIntExtra(SpotifyIntentService.BROADCAST_RESULT_CODE, -1);
            if(result == Constants.BROADCAST_RESULT_FAILED){
                //Something went wrong, show a message
                Toast.makeText(getActivity(),getActivity().getString(R.string.spotify_toptracks_failed), Toast.LENGTH_LONG);
            }
        }
    }

}
