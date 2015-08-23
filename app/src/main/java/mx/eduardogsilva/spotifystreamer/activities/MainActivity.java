package mx.eduardogsilva.spotifystreamer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.fragments.SearchFragment;
import mx.eduardogsilva.spotifystreamer.activities.fragments.TopTracksFragment;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;
import mx.eduardogsilva.spotifystreamer.service.SpotifyPlayerService;

public class MainActivity extends AppCompatActivity implements SpotifyPlayerService.OnAsyncServiceListener,
        SearchFragment.OnSearchListener, TopTracksFragment.OnTopTracksListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Service
    private SpotifyPlayerService mBoundService;
    private boolean mIsBound = false;

    // Fragments and pane mode
    private boolean mTwoPane;
    // Fragments tags
    private static final String TOPTRACKSFRAGMENT_TAG = "TTF";

    // Bundle extras
    public final static String EXTRA_TWO_PANE = "etpm";

    // Service connection
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((SpotifyPlayerService.PlayerBinder)service).getService(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    /* ===== LIFECYCLE METHODS ===== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Get Fragments
        if(findViewById(R.id.top_tracks_container) != null) {
            mTwoPane = true;
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, new TopTracksFragment(), TOPTRACKSFRAGMENT_TAG)
                    .commit();
            }
        }

        // Start service for all activities to bind
        Intent serviceIntent = new Intent(this, SpotifyPlayerService.class);
        serviceIntent.setAction(SpotifyPlayerService.ACTION_INSTANTIATE);
        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBoundService != null) {
            mBoundService.setOnAsyncServiceListener(this);
            invalidateOptionsMenu();
        } else {
            doBindService();
        }

        // TODO Update when location changes.
    }

    @Override
    protected void onPause() {
        super.onPause();
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Intent serviceIntent = new Intent(this, SpotifyPlayerService.class);
//        serviceIntent.setAction(SpotifyPlayerService.ACTION_STOP);
//        stopService(serviceIntent);
    }

    /* ===== MENU METHODS ===== */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {

            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);

            return true;

        } else if(id == R.id.action_settings) {

            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);

            return true;
        } else if(id == R.id.action_now_playing) {
            Intent playIntent = new Intent(this, PlayerActivity.class);

            startActivity(playIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    /* ===== SERVICE BINDING ===== */

    private void doBindService() {
        Intent serviceIntent = new Intent(this, SpotifyPlayerService.class);

        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if(mIsBound) {
            mBoundService.setOnAsyncServiceListener(null);
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /* ===== SERVICE CALLBACKS ===== */

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

    /* ===== SEARCH FRAGMENT CALLBACKS ===== */

    @Override
    public void onItemSelected(Intent intent) {
        intent.putExtra(EXTRA_TWO_PANE, mTwoPane);

        if(mTwoPane) {
            // Replace top tracks fragment
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.top_tracks_container,
                    TopTracksFragment.newInstance(intent),
                    TOPTRACKSFRAGMENT_TAG
            ).commit();
        } else {
            // Launch activity
            startActivity(intent);
        }
    }

    @Override
    public void onTrackClicked(String artistId, int position, List<TrackWrapper> tracks) {
        Log.d(LOG_TAG, "Track selected!");
    }
}
