package mx.eduardogsilva.spotifystreamer.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.TopTracksActivity;
import mx.eduardogsilva.spotifystreamer.adapters.ArtistsAdapter;
import mx.eduardogsilva.spotifystreamer.filters.ArtistsLiveFilter;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener {

    private final String LOG_TAG = SearchFragment.class.getSimpleName();

    // Edit text for searching
    private EditText artistSearchEditText;
    private View emptyView;

    // Artists adapter
    private ArtistsAdapter mArtistsAdapter;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Main view for the fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Query edit text
        artistSearchEditText = (EditText) rootView.findViewById(R.id.artist_search_edittext);

        // Setup adapter
        mArtistsAdapter = new ArtistsAdapter(getActivity());

        // Set adapter for list
        ListView artistsListView = (ListView) rootView.findViewById(R.id.artist_search_listview);
        artistsListView.setAdapter(mArtistsAdapter);
        artistsListView.setOnItemClickListener(this);

        // View to show when no results are shown.
        emptyView = rootView.findViewById(R.id.artist_no_items_found);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get filter we will be using.
        final ArtistsLiveFilter artistsLiveFilter = (ArtistsLiveFilter) mArtistsAdapter.getFilter();
        // Set the empty view to show when no results.
        artistsLiveFilter.setEmptyView(emptyView);

        // Check for text changes
        artistSearchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public synchronized void afterTextChanged(Editable s) {

                // Query the service once the text changed
                artistsLiveFilter.filter(s.toString());

            }
        });

        // Fire filter manually in case we are returning from a lifecycle event
        artistsLiveFilter.filter(artistSearchEditText.getText().toString());
    }

    // Listener to catch artist items clicks
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String artistId = mArtistsAdapter.getArtistId(position);

        // Create intent and launch tracks activity.
        Intent tracksIntent = new Intent(getActivity(), TopTracksActivity.class);
        tracksIntent.putExtra(Intent.EXTRA_REFERRER, artistId);

        startActivity(tracksIntent);
    }
}
