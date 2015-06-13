package mx.eduardogsilva.spotifystreamer.activities.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.adapters.TracksAdapter;
import retrofit.RetrofitError;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();

    // Adapter to handle track data
    private TracksAdapter mTrackAdapter;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // Create adapter
        mTrackAdapter = new TracksAdapter(getActivity());

        // Empty tracks view
        View emptyTracksView = rootView.findViewById(R.id.tracks_no_items_found);

        // Get ListView reference
        ListView topTracksListView = (ListView) rootView.findViewById(R.id.top_tracks_listview);
        topTracksListView.setAdapter(mTrackAdapter);
        topTracksListView.setEmptyView(emptyTracksView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get intent to obtain artist id
        Intent intent = getActivity().getIntent();
        String artistId = intent.getStringExtra(Intent.EXTRA_REFERRER);

        // Create the asyncTask to download tracks data
        new TracksDownloadTask().execute(artistId);
    }

    private class TracksDownloadTask extends AsyncTask<String, Void, Tracks>{
        private SpotifyApi spotifyApi;
        private SpotifyService spotifyService;
        private final String LOG_TAG = TracksDownloadTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spotifyApi = new SpotifyApi();
            spotifyService = spotifyApi.getService();
        }

        @Override
        protected Tracks doInBackground(String... params) {

            if(params[0] == null){
                return null;
            }

            Tracks tracks = null;

            try {

                Map<String, Object> options = new HashMap<>();
                options.put("country", "MX");

                tracks = spotifyService.getArtistTopTrack(params[0], options);

            } catch (RetrofitError e) {

                SpotifyError spotifyError = SpotifyError.fromRetrofitError(e);

                Log.e(LOG_TAG, "Error downloading tracks: " + spotifyError.toString());
                e.printStackTrace();
            }

            return tracks;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);
            if(tracks != null){
                mTrackAdapter.setTracks(tracks.tracks);
            }
            // TODO show error handler.
        }
    }
}
