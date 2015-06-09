package nl.idesign.spotifystreamer.activities.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.activities.TopTracksActivity;
import nl.idesign.spotifystreamer.adapters.SpotifySearchAdapter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by huib on 7-6-2015.
 */
public class SpotifySearchFragment extends Fragment {

    private SpotifyService mSpotifyService;

    private ListView mSpotifyResultListView;
    private EditText mSearchEditText;
    private SpotifySearchAdapter mAdapter;

    private boolean mLoadingResults;
    private boolean mShouldClear = true;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spotify_search, container);

        mSearchEditText = (EditText)rootView.findViewById(R.id.spotify_search_textview);
        mSpotifyResultListView = (ListView)rootView.findViewById(R.id.spotify_search_result_listview);
        mAdapter = new SpotifySearchAdapter(getActivity(), R.layout.spotify_search_result_list_item);
        mSpotifyResultListView.setAdapter(mAdapter);

        mSpotifyResultListView.setOnItemClickListener(mOnSportifyResultItemClickListener);

        //scroll listener so we know when to load more results
        mSpotifyResultListView.setOnScrollListener(mOnResultScrollListener);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchSportify(s.toString());
            }
        });

        return rootView;
    }

    private void searchSportify(String artist){
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
        new SearchArtistsTask().execute(new String[]{artist});

    }

    private AbsListView.OnScrollListener mOnResultScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(!mLoadingResults && view.getLastVisiblePosition() == mAdapter.getCount()-1 && mAdapter.getCount() > 0){
                //We need to load more...
                mLoadingResults = true;
                mShouldClear = false;
                new SearchArtistsTask().execute(new String[]{mSearchEditText.getText().toString()});
            }
        }
    };

    private AdapterView.OnItemClickListener mOnSportifyResultItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent topTrackIntent = new Intent(getActivity(), TopTracksActivity.class);

            Artist artist =  mAdapter.getItem(position);
            topTrackIntent.putExtra(TopTracksFragment.PARAM_EXTRA_ARTIST_ID, artist.id);
            startActivity(topTrackIntent);
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

            ArtistsPager artistsPager =  mSpotifyService.searchArtists(params[0], optionMap );
            return artistsPager.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            if(mShouldClear)
                mAdapter.clear();
            mAdapter.addAll(artists);
            mLoadingResults = false;
            //super.onPostExecute(aLong);
        }
    }


}
