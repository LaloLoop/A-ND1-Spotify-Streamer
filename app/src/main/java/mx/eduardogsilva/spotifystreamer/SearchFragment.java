package mx.eduardogsilva.spotifystreamer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.ErrorDetails;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {

    private final String LOG_TAG = SearchFragment.class.getSimpleName();

    // Edit text for searching
    private EditText artistSearchEditText;
    private View emptyView;

    // Instance of service we will be using
    private SpotifyService service;

    // Artists adapter
    private ArtistsAdapter mArtistsAdapter;

    // UI messages
    private final int MSG_TYPE_SEARCH_SUCCESS = 1;
    private final int MSG_TYPE_SEARCH_ERROR = 2;
    private final int MSG_TYPE_NO_RESULTS = 3;
    private final int MSG_TYPE_BAD_REQUEST = 4;

    // UI thread handler
    private static Handler mHandler;

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

        emptyView = rootView.findViewById(R.id.artist_no_items_found);

        // Create handler
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case MSG_TYPE_SEARCH_SUCCESS:
                        emptyView.setVisibility(View.GONE);
                        List<Artist> artists = (List<Artist>) msg.obj;
                        mArtistsAdapter.replaceAll(artists);
                        break;
                    case MSG_TYPE_SEARCH_ERROR:
                        Toast.makeText(getActivity(), (String)msg.obj, Toast.LENGTH_LONG).show();
                        emptyView.setVisibility(View.VISIBLE);
                        break;
                    case MSG_TYPE_NO_RESULTS:
                        mArtistsAdapter.removeAll();
                        emptyView.setVisibility(View.VISIBLE);
                        break;
                    case MSG_TYPE_BAD_REQUEST:
                        mArtistsAdapter.removeAll();
                        break;
                }
            }
        };

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Spotify API
        SpotifyApi wrapper = new SpotifyApi();
        service = wrapper.getService();

        // Check for text changes
        artistSearchEditText.addTextChangedListener(new TextWatcher() {

            boolean updateUi;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public synchronized void afterTextChanged(Editable s) {

                if(s.toString().isEmpty()){
                    mArtistsAdapter.removeAll();
                    updateUi = false;
                    emptyView.setVisibility(View.GONE);
                    return;
                }else {
                    updateUi = true;
                }

                // Query the service once the text changed
                service.searchArtists(s.toString(), new SpotifyCallback<ArtistsPager>() {
                    @Override
                    public void failure(SpotifyError spotifyError) {

                        if(spotifyError.hasErrorDetails()){
                            ErrorDetails details = spotifyError.getErrorDetails();

                            sendMessageToUI(MSG_TYPE_SEARCH_ERROR, details.message);
                            Log.e(LOG_TAG, details.message);

                        }else {
                            String error = spotifyError.toString();
                            sendMessageToUI(MSG_TYPE_SEARCH_ERROR, error);
                            Log.e(LOG_TAG, error);
                        }
                    }

                    @Override
                    public synchronized void success(ArtistsPager artistsPager, Response response) {

                        if(updateUi){
                            if(artistsPager.artists.total == 0){
                                sendMessageToUI(MSG_TYPE_NO_RESULTS, null);
                            }else{
                                sendMessageToUI(MSG_TYPE_SEARCH_SUCCESS, artistsPager.artists.items);
                            }
                        }
                    }

                    private void sendMessageToUI(int msgType, Object data) {
                        Message msg = Message.obtain();
                        msg.what = msgType;
                        msg.obj = data;
                        mHandler.sendMessage(msg);
                    }
                });

            }
        });

        if(!artistSearchEditText.getText().toString().isEmpty()){
            artistSearchEditText.setText(artistSearchEditText.getText());
        }
    }
}
