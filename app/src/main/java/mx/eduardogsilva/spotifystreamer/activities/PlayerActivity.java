package mx.eduardogsilva.spotifystreamer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.fragments.PlayerFragment;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;
import mx.eduardogsilva.spotifystreamer.service.SpotifyPlayerService;

public class PlayerActivity extends AppCompatActivity implements PlayerFragment.OnPlayerInteractionListener,
        SpotifyPlayerService.OnAsyncServiceListener{

    private static final String LOG_TAG = PlayerActivity.class.getSimpleName();

    // Service
    private SpotifyPlayerService mBoundService;

    // View / Fragment
    private PlayerFragment mPf;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((SpotifyPlayerService.PlayerBinder)service).getService(PlayerActivity.this);

            mPf.bindTrackData(mBoundService.getCurrentTrack());

            // Init service with new data only if user selected a new track.
            if(mBoundService.isPrepared()) {
                mPf.lockControls(false);
                mPf.setSeekbarMaxDuration(mBoundService.getDuration());
                mPf.playPause(mBoundService.isPlaying());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };
    private  boolean mIsBound = false;

    /* ==== LIFE CYCLE METHODS ==== */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mPf = (PlayerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_player);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mBoundService != null) {
            mBoundService.setOnAsyncServiceistener(this);
            invalidateOptionsMenu();
        } else {
            doBindService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* ==== AUXILIARY METHODS ==== */

    private void doBindService() {
        Intent serviceIntent = new Intent(this, SpotifyPlayerService.class);

        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if(mIsBound) {
            mBoundService.setOnAsyncServiceistener(null);
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /* ==== UI CALLBACK METHODS ==== */

    @Override
    public void onSeekTo(int position) {
        if(mBoundService != null) {
            mBoundService.seekTo(position);
        }
    }

    @Override
    public void onPlayPauseClicked() {
        if(mBoundService != null) {
            mBoundService.playPause();
        }
    }

    @Override
    public void onNextTrackClicked() {
        if(mBoundService != null) {
            mBoundService.nextTrack();
        }
    }

    @Override
    public void onPrevTrackClicked() {
        if(mBoundService != null) {
            mBoundService.prevTrack();
        }
    }

    @Override
    public int getCurrentPosition() {
        return (mBoundService != null)?mBoundService.getCurrentPosition():0;
    }


    /* ==== SERVICE ASYNC EVENTS ==== */
    @Override
    public void onPreparing(TrackWrapper track) {
        if(mPf != null) {
            mPf.bindTrackData(track);
            mPf.lockControls(true);
        } else {
            Log.e(LOG_TAG, "mPf is NULL");
        }
    }

    @Override
    public void onPrepared(int duration, TrackWrapper trackPrepared) {

        if(mPf != null) {
            mPf.lockControls(false);
            mPf.setSeekbarMaxDuration(mBoundService.getDuration());
            mPf.playPause(true);
        }
    }

    @Override
    public void onCompletion() {
        if(mPf != null) {
            mPf.playPause(false);
        }
    }
}
