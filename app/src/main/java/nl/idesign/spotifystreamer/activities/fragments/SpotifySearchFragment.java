package nl.idesign.spotifystreamer.activities.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.activities.MainActivity;
import nl.idesign.spotifystreamer.activities.TopTracksActivity;
import nl.idesign.spotifystreamer.adapters.SpotifySearchAdapter;
import nl.idesign.spotifystreamer.utils.Connectivity;

/**
 * Created by huib on 7-6-2015.
 */
public class SpotifySearchFragment extends Fragment {

    private SpotifyService mSpotifyService;

    private ListView mSpotifyResultListView;
    private SearchView mSearchEditText;
    private SpotifySearchAdapter mAdapter;

    private LinearLayout mNothingFound;

    private static final String SAVED_INSTANCE_RESULTS = "query_results";

    private boolean mLoadingResults;
    private String mCurrentSearchQuery;
    private boolean mShouldClear = true;

    private List<Artist> mQueryArtists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spotify_search, null);

        //To use this application we need a connection, so first we check if there is a connection
        if(!Connectivity.isNetworkAvailable(getActivity())){
            showNoConnectionDialog();
            return rootView;
        }


        mSearchEditText = (SearchView)rootView.findViewById(R.id.spotify_search_textview);
        mSpotifyResultListView = (ListView)rootView.findViewById(R.id.spotify_search_result_listview);
        mNothingFound = (LinearLayout)rootView.findViewById(R.id.nothing_found_layout);

        mAdapter = new SpotifySearchAdapter(getActivity());
        if(mQueryArtists != null){
            mAdapter.addAll(mQueryArtists);
        }

        mSpotifyResultListView.setAdapter(mAdapter);

        mSpotifyResultListView.setOnItemClickListener(mOnSportifyResultItemClickListener);

        //scroll listener so we know when to load more results
        mSpotifyResultListView.setOnScrollListener(mOnResultScrollListener);
        mSearchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchSportify(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchSportify(newText);
                return false;
            }
        });
        return rootView;
    }

    private void showNoConnectionDialog(){
        new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.no_connection_title))
                .setMessage(getString(R.string.no_connection_description))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                }).show();
    }

    private void searchSportify(String artist){
        if(mCurrentSearchQuery != null && mCurrentSearchQuery.equals(artist)){
            return;
        }
        mCurrentSearchQuery = artist;

        if(mSpotifyService == null) {
            SpotifyApi api = new SpotifyApi();
            mSpotifyService = api.getService();
        }

        if(artist.isEmpty()){
            mAdapter.clear();
            return;
        }
        mLoadingResults = true;
        mShouldClear = true;
        new SearchArtistsTask().execute(artist);

    }

    private final AbsListView.OnScrollListener mOnResultScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(!mLoadingResults && view.getLastVisiblePosition() == mAdapter.getCount()-1 && mAdapter.getCount() > 0){
                if(visibleItemCount == totalItemCount){
                    //no scroll, but a reload
                    return;
                }
                //We need to load more...
                mLoadingResults = true;
                mShouldClear = false;
                new SearchArtistsTask().execute(mSearchEditText.getQuery().toString());
            }
        }
    };

    private final AdapterView.OnItemClickListener mOnSportifyResultItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Artist artist =  mAdapter.getItem(position);
            ((MainActivity)getActivity()).onItemSelected(artist.id, artist.name);
        }
    };

    private class SearchArtistsTask extends AsyncTask<String, Integer, List<Artist>> {
        @Override
        protected List<Artist> doInBackground(String... params) {

            HashMap<String, Object> optionMap = new HashMap<>();
            optionMap.put(SpotifyService.MARKET, "NL");
            optionMap.put(SpotifyService.LIMIT, "20");
            if (mShouldClear) {
                optionMap.put(SpotifyService.OFFSET, 0);
            } else{
                optionMap.put(SpotifyService.OFFSET, mAdapter.getCount());
            }

            ArtistsPager artistsPager =  mSpotifyService.searchArtists(params[0] + "*", optionMap );
            return artistsPager.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            if(artists.size() == 0){
                mNothingFound.setVisibility(View.VISIBLE);
                mSpotifyResultListView.setVisibility(View.GONE);
            }else {
                mNothingFound.setVisibility(View.GONE);
                mSpotifyResultListView.setVisibility(View.VISIBLE);
            }

            mQueryArtists = artists;
            if(mShouldClear)
                mAdapter.clear();
            mAdapter.addAll(artists);
            mLoadingResults = false;
        }
    }

    public interface OnItemSelectedCallback {
        void onItemSelected(String artistId, String artistName);
    }
}
