package mx.eduardogsilva.spotifystreamer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.fragments.TopTracksFragment;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;
import mx.eduardogsilva.spotifystreamer.service.SpotifyPlayerService;

public class TopTracksActivity extends AppCompatActivity implements TopTracksFragment.OnTopTracksListener,
    SpotifyPlayerService.OnAsyncServiceListener {

    private final String LOG_TAG = TopTracksActivity.class.getSimpleName();

    public static final String EXTRA_ARTIST_ID = "artistId";
    public static final String EXTRA_TRACKS = "etracks";
    public static final String EXTRA_TRACK_POSITION = "cposition";

    // Service
    private SpotifyPlayerService mBoundService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((SpotifyPlayerService.PlayerBinder)service).getService(TopTracksActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);

        MenuItem mPlayingItem = menu.findItem(R.id.action_now_playing);

        if(mBoundService != null) {
            if(!mBoundService.isInitialized()){
                mPlayingItem.setVisible(false);
            } else {
                mPlayingItem.setVisible(true);
                if(mBoundService.isPlaying()) {
                    mPlayingItem.setIcon(android.R.drawable.ic_media_play);
                } else {
                    mPlayingItem.setIcon(android.R.drawable.ic_media_pause);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if(id == R.id.action_now_playing) {
            Intent playIntent = new Intent(this, PlayerActivity.class);

            startActivity(playIntent);
        }

        return super.onOptionsItemSelected(item);
    }

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

    @Override
    public void onTrackClicked(String artistId, int position, List<TrackWrapper> tracks) {

        Intent playIntent = new Intent(this, PlayerActivity.class);

        startActivity(playIntent);

        Intent serviceIntent = new Intent(this, SpotifyPlayerService.class);
        serviceIntent.setAction(SpotifyPlayerService.ACTION_INIT_PLAY);
        serviceIntent.putExtra(EXTRA_ARTIST_ID, artistId);
        serviceIntent.putParcelableArrayListExtra(EXTRA_TRACKS, (ArrayList<? extends Parcelable>) tracks);
        serviceIntent.putExtra(EXTRA_TRACK_POSITION, position);

        startService(serviceIntent);

    }

    @Override
    public void onPreparing(TrackWrapper track) {

    }

    @Override
    public void onPrepared(int duration, TrackWrapper trackPrepared) {
        invalidateOptionsMenu();
    }

    @Override
    public void onCompletion() {
        invalidateOptionsMenu();
    }
}
