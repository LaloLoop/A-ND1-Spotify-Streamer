package mx.eduardogsilva.spotifystreamer.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.TopTracksActivity;
import mx.eduardogsilva.spotifystreamer.adapters.ArtistsAdapter;
import mx.eduardogsilva.spotifystreamer.filters.ArtistsFilter;
import mx.eduardogsilva.spotifystreamer.model.ArtistWrapper;

import static android.widget.AdapterView.OnItemClickListener;
import static mx.eduardogsilva.spotifystreamer.filters.ArtistsFilter.OnDataFilteredListener;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment implements OnItemClickListener, OnQueryTextListener, OnDataFilteredListener{

    private final String LOG_TAG = SearchFragment.class.getSimpleName();

    // The artists ListView.
    private ListView artistsListView;
    // View to show when no artists were found
    private View emptyView;
    // View to show progress while loading
    private View loadingView;

    // Search view
    private SearchView searchView;
    // Current search query.
    private String currentSearchQuery;
    // State of search view
    boolean iconified;

    // Artists adapter
    private ArtistsAdapter mArtistsAdapter;

    // Live filter
    private ArtistsFilter artistsFilter;

    // Intent keys
    public static final String EXTRA_ARTIST_ID = "ARTIST_ID";
    public static final String EXTRA_ARTIST_NAME = "ARTIST_NAME";
    public static final String EXTRA_ARTIST_IMAGE_URL = "ARTIST_IMAGE_URL";
    public static final String EXTRA_ARTIST_LV_STATE = "ARTIST_LV_POSITION";

    // ListView state
    private Parcelable mListState = null;

    // Bundle keys
    private static final String BUNDLE_ARTISTS = "artistsList";
    private static final String BUNDLE_QUERY = "queryString";
    private static final String BUNDLE_SV_ICONIFIED = "svIconified";

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Main view for the fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Setup adapter
        mArtistsAdapter = new ArtistsAdapter(getActivity());

        // Set adapter for list
        artistsListView = (ListView) rootView.findViewById(R.id.artist_search_listview);
        artistsListView.setAdapter(mArtistsAdapter);
        artistsListView.setOnItemClickListener(this);

        // View to show when no results are shown.
        emptyView = rootView.findViewById(R.id.artist_no_items_found);

        loadingView = rootView.findViewById(R.id.loading_view);

        // Request menu options event
        setHasOptionsMenu(true);

        // Restore artist list.
        if(savedInstanceState != null){
            ArrayList<ArtistWrapper> artist = savedInstanceState.getParcelableArrayList(BUNDLE_ARTISTS);
            if(artist != null){
                mArtistsAdapter.replaceAll(artist);
            }
            mListState = savedInstanceState.getParcelable(EXTRA_ARTIST_LV_STATE);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get filter we will be using.
        artistsFilter = (ArtistsFilter) mArtistsAdapter.getFilter();
        // Set listener to check when data is updated
        artistsFilter.setDataFilteredListener(this);

        // If we have a list state, restore it.
        if(mListState != null){
            artistsListView.onRestoreInstanceState(mListState);
        }

    }

    /**
     * Saves search state
     * @param outState State of the app previous to be destroyed
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Add current query
        outState.putString(BUNDLE_QUERY, currentSearchQuery);
        outState.putBoolean(BUNDLE_SV_ICONIFIED, searchView.isIconified());

        // Save ListView position
        mListState = artistsListView.onSaveInstanceState();
        outState.putParcelable(EXTRA_ARTIST_LV_STATE, mListState);

        // Save artist data.
        outState.putParcelableArrayList(BUNDLE_ARTISTS, (ArrayList<? extends Parcelable>) mArtistsAdapter.getArtists());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null){
            currentSearchQuery = savedInstanceState.getString(BUNDLE_QUERY);
            iconified = savedInstanceState.getBoolean(BUNDLE_SV_ICONIFIED);

        }else {
            currentSearchQuery = "";
            iconified = true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Get Search view item
        MenuItem searchItem = menu.findItem(R.id.action_search);

        if(searchItem != null){
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint(getString(R.string.hint_artist_name));

            // Restore search state
            searchView.setQuery(currentSearchQuery, false);
            searchView.setIconified(iconified);
            searchView.clearFocus();
        }
    }

    // Listener to catch artist items clicks
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArtistWrapper artist = (ArtistWrapper) mArtistsAdapter.getItem(position);

        // Create intent and launch tracks activity.
        Intent tracksIntent = new Intent(getActivity(), TopTracksActivity.class);
        tracksIntent.putExtra(EXTRA_ARTIST_ID, artist.id);
        tracksIntent.putExtra(EXTRA_ARTIST_NAME, artist.name);
        tracksIntent.putExtra(EXTRA_ARTIST_IMAGE_URL, artist.getLargeImage());

        startActivity(tracksIntent);
    }

    // Listeners for SearchView
    @Override
    public boolean onQueryTextSubmit(String query) {

        currentSearchQuery = query;

        artistsListView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);

        searchView.clearFocus();

        artistsFilter.filter(query);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //Log.v(LOG_TAG, "onQueryTextChange: " + newText);
        return false;
    }

    @Override
    public void onDataFiltered() {
        // Go to top on ListView
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        artistsListView.setVisibility(View.VISIBLE);

        // Set top position.
        artistsListView.smoothScrollToPosition(0);


    }

    @Override
    public void onNoResultsFound() {
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFilterError(String errorMsg) {
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        Toast.makeText(getActivity(), R.string.error_search_artists, Toast.LENGTH_LONG).show();
    }
}
