package mx.eduardogsilva.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.fragments.PlayerFragment;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;

public class PlayerActivity extends AppCompatActivity implements PlayerFragment.OnPlayerInteractionListener{

    private static final String LOG_TAG = PlayerActivity.class.getSimpleName();

    private List<TrackWrapper> mTracks;
    private int mCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent playerIntent = getIntent();

        mTracks = playerIntent.getParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACKS);
        mCurrentPosition = playerIntent.getIntExtra(TopTracksActivity.EXTRA_TRACK_POSITION, 0);

    }

    @Override
    protected void onResume() {
        super.onResume();
        TrackWrapper currentTrack = mTracks.get(mCurrentPosition);

        playTrack(currentTrack);
    }

    private void playTrack(TrackWrapper currentTrack) {
        PlayerFragment pf = (PlayerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_player);

        if(pf != null) {

            pf.playTrack(currentTrack);
        }
    }

    @Override
    public void onNextTrackClicked() {
        changePosition(1);

        TrackWrapper track = mTracks.get(mCurrentPosition);
        playTrack(track);
    }

    @Override
    public void onPrevTrackClicked() {
        changePosition(-1);

        TrackWrapper track = mTracks.get(mCurrentPosition);
        playTrack(track);
    }

    private void changePosition(int delta) {
        mCurrentPosition += delta;

        if(mCurrentPosition >= mTracks.size()) {
            mCurrentPosition = 0;
        } else if(mCurrentPosition < 0) {
            mCurrentPosition = mTracks.size()-1;
        }
    }
}
