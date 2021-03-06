package mx.eduardogsilva.spotifystreamer.activities.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.MainActivity;
import mx.eduardogsilva.spotifystreamer.adapters.TracksRecyclerAdapter;
import mx.eduardogsilva.spotifystreamer.adapters.TracksRecyclerAdapter.TrackViewHolder.ITracksViewHolderClicks;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;
import mx.eduardogsilva.spotifystreamer.utilities.PrefsUtility;
import retrofit.RetrofitError;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnTopTracksListener} interface
 * to handle interaction events.
 */
public class TopTracksFragment extends Fragment implements ITracksViewHolderClicks{

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();

    // Activity listener for events
    private OnTopTracksListener mListener;

    // Custom adapter for tracks.
    private TracksRecyclerAdapter mTracksAdapter;
    // Layout manager for elements placing in recycler view
    private RecyclerView.LayoutManager mLayoutManager;

    private String mArtistId;
    private String mArtistName;
    private String mArtistImageUrl;

    // State for recycler view
    private Parcelable mTracksState = null;
    private final static String BUNDLE_TRACKS_LV_STATE = "tracksState";
    private final static String BUNDLE_TRACKS_DATA = "tracksList";

    // Indicates whether we are on two pane mode or not
    private boolean mTwoPane = false;

    public TopTracksFragment() {
        // Required empty public constructor
    }

    /**
     * Auxiliary method to instantiate this fragment dynamically.
     * @param intent    Intent with extras to load top tracks
     * @return          Instance of top tracks fragment.
     */
    public static TopTracksFragment newInstance(Intent intent) {
        TopTracksFragment ttf = new TopTracksFragment();

        ttf.setArguments(intent.getExtras());

        return ttf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        initData();

        // Init toolbar an parallax effect only on single pane
        if(!mTwoPane) {
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
            collapsingToolbar.setTitle(mArtistName);

            // On Older devices the title does not appear with correct text color.
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1){
                collapsingToolbar.setCollapsedTitleTextAppearance(android.support.v7.appcompat.R.style.Base_TextAppearance_AppCompat_Widget_ActionBar_Title_Inverse);
                collapsingToolbar.setExpandedTitleTextAppearance(android.support.v7.appcompat.R.style.Base_TextAppearance_AppCompat_Widget_ActionBar_Title_Inverse);
            }

            // Get ImageView to show artist picture
            ImageView imageView = (ImageView) rootView.findViewById(R.id.top_tracks_header_imageview);
            if(mArtistImageUrl != null && !mArtistImageUrl.isEmpty()){
                Picasso.with(getActivity()).load(mArtistImageUrl).into(imageView);
            }else {
                imageView.setImageResource(R.mipmap.default_banner);
            }
        }

        // Get reference to RecyclerView
        RecyclerView tracksRecyclerView = (RecyclerView) rootView.findViewById(R.id.tracks_recyclerview);

        // Get layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        tracksRecyclerView.setLayoutManager(mLayoutManager);

        // Create adapter
        mTracksAdapter = new TracksRecyclerAdapter(getActivity(), this);
        tracksRecyclerView.setAdapter(mTracksAdapter);

        // Set item separator
        tracksRecyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .color(Color.LTGRAY)
                        .sizeResId(R.dimen.divider)
                        .build());

        // Restore tracks list data.
        if(savedInstanceState != null) {
            ArrayList<TrackWrapper> tracks = savedInstanceState.getParcelableArrayList(BUNDLE_TRACKS_DATA);
            if(tracks != null) {
                mTracksAdapter.setIsLoading(false);
                mTracksAdapter.setTracks(tracks);
            }
            mTracksState = savedInstanceState.getParcelable(BUNDLE_TRACKS_LV_STATE);
            if(mTracksState != null) {
                mLayoutManager.onRestoreInstanceState(mTracksState);
            }
        } else {
            // Query top tracks data.
            updateTracks(mArtistId);
        }

        return rootView;
    }

    private void initData() {
        Bundle args;
        if(getArguments() != null) {
            args = getArguments();
        } else {
            // Get intent to obtain artist id
            Intent intent = getActivity().getIntent();
            args = intent.getExtras();
        }
        if(args != null){
            mArtistName = args.getString(SearchFragment.EXTRA_ARTIST_NAME);
            mArtistImageUrl = args.getString(SearchFragment.EXTRA_ARTIST_IMAGE_URL);
            mArtistId = args.getString(SearchFragment.EXTRA_ARTIST_ID);
            mTwoPane = args.getBoolean(MainActivity.EXTRA_TWO_PANE);
        } else {
            mArtistName = null;
            mArtistImageUrl = null;
            mArtistId = null;
            mTwoPane = true;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Save list view position.
        mTracksState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(BUNDLE_TRACKS_LV_STATE, mTracksState);

        // Save tracks data.
        outState.putParcelableArrayList(BUNDLE_TRACKS_DATA,
                (ArrayList<? extends Parcelable>) mTracksAdapter.getTracks());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTopTracksListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTopTracksListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClicked(int position) {
        mListener.onTrackSelected(mArtistId, position, mTracksAdapter.getTracks());
    }

    /**
     * Retrieves the location preferences and updates the track data based on the artist and location
     * @param artistId Artist to get top tracks
     */
    private void updateTracks(String artistId) {
        // Get the location saved in preferences
        String locationPref = PrefsUtility.getLocation(getActivity());

        // Create the asyncTask to download tracks data
        new TracksDownloadTask().execute(artistId, locationPref);
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
    public interface OnTopTracksListener {
        void onTrackSelected(String artistId, int position, List<TrackWrapper> tracks);
    }

    private class TracksDownloadTask extends AsyncTask<String, Void, List<TrackWrapper>> {
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
        protected List<TrackWrapper> doInBackground(String... params) {

            // Get artist Id an country
            if(params[0] == null || params[1] == null){
                mTracksAdapter.setIsLoading(false);
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

            ArrayList<TrackWrapper> tracksWrapped = new ArrayList<>();

            if(tracks != null){
                for(Track t : tracks.tracks){
                    TrackWrapper tw = new TrackWrapper(t);
                    tw.setArtistName(mArtistName);
                    tracksWrapped.add(tw);
                }
            }

            return tracksWrapped;
        }

        @Override
        protected void onPostExecute(List<TrackWrapper> tracks) {
            super.onPostExecute(tracks);

            if(tracks != null){
                mTracksAdapter.setIsLoading(false);
                mTracksAdapter.setTracks(tracks);

                // Restore position.
                if(mTracksState != null){
                    mLayoutManager.onRestoreInstanceState(mTracksState);
                }
            }
        }
    }

}
