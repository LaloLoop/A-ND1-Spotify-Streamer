package mx.eduardogsilva.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {

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

        artistsLiveFilter.filter(artistSearchEditText.getText().toString());
    }
}
