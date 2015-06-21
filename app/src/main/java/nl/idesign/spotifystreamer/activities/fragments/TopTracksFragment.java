package nl.idesign.spotifystreamer.activities.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.adapters.SpotifyTopTracksAdapter;
import nl.idesign.spotifystreamer.data.SpotifyStreamerDataContract;
import nl.idesign.spotifystreamer.service.SpotifyIntentService;

/**
 * Created by huib on 8-6-2015.
 */
public class TopTracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    public static final String PARAM_EXTRA_ARTIST_ID = "artist_id";

    private SpotifyService mSpotifyService;

    private static TopTracksFragment mInstance;
    private String mArtistId;

    private static final int TOP_TRACKS_LOADER = 0;


    private ListView mTopTracksListView;
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
        if(intent == null){

        }

        String artistId = intent.getStringExtra(PARAM_EXTRA_ARTIST_ID);
        SpotifyIntentService.getTopTracks(getActivity(), artistId);

        mTopTracksListView = (ListView)rootView.findViewById(R.id.top_tracks_listview);
        mAdapter = new SpotifyTopTracksAdapter(getActivity(), null, true);
        mTopTracksListView.setAdapter(mAdapter);

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

}
