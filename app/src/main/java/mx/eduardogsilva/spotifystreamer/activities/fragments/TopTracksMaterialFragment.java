package mx.eduardogsilva.spotifystreamer.activities.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.adapters.TracksRecyclerAdapter;
import retrofit.RetrofitError;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopTracksMaterialFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopTracksMaterialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopTracksMaterialFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TracksRecyclerAdapter mTracksAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String artistId;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TopTracksMaterialFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TopTracksMaterialFragment newInstance(String param1, String param2) {
        TopTracksMaterialFragment fragment = new TopTracksMaterialFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TopTracksMaterialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_top_tracks_material, container, false);

        // Get intent to obtain artist id
        Intent intent = getActivity().getIntent();
        artistId = intent.getStringExtra(SearchFragment.EXTRA_ARTIST_ID);
        String artistName = intent.getStringExtra(SearchFragment.EXTRA_ARTIST_NAME);
        String artistImageUrl = intent.getStringExtra(SearchFragment.EXTRA_ARTIST_IMAGE_URL);

        // Get Toolbar to setup
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.top_tracks_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Get collapsing toolbar to set title
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id.tracks_collapsing_toolbar_layout);
        collapsingToolbar.setTitle(artistName);

        // Get reference to RecyclerView
        RecyclerView tracksRecyclerView = (RecyclerView) rootView.findViewById(R.id.tracks_recyclerview);

        // Get layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        tracksRecyclerView.setLayoutManager(mLayoutManager);

        // Create adapter
        mTracksAdapter = new TracksRecyclerAdapter(getActivity());
        tracksRecyclerView.setAdapter(mTracksAdapter);

        // Get ImageView to show artist picture
        ImageView imageView = (ImageView) rootView.findViewById(R.id.top_tracks_header_imageview);
        if(artistImageUrl != null){
            Picasso.with(getActivity()).load(artistImageUrl).into(imageView);
        }else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        updateTracks(artistId);
    }

    /**
     * Retrieves the location preferences and updates the track data based on the artist and location
     * @param artistId Artist to get top tracks
     */
    private void updateTracks(String artistId) {
        // Get the location saved in preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String locationPref = sharedPreferences.getString(getString(R.string.pref_key_location), getString(R.string.pref_default_location));

        // Create the asyncTask to download tracks data
        new TracksDownloadTask().execute(artistId, locationPref);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            /*throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");*/
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private class TracksDownloadTask extends AsyncTask<String, Void, Tracks> {
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

            if(params[0] == null || params[1] == null){
                return null;
            }

            Tracks tracks = null;

            try {

                Map<String, Object> options = new HashMap<>();
                options.put("country", params[1]);

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
                mTracksAdapter.setTracks(tracks.tracks);
            }
            // TODO show error handler.
        }
    }

}
