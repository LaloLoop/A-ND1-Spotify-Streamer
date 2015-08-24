package mx.eduardogsilva.spotifystreamer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.fragments.PlayerFragment;
import mx.eduardogsilva.spotifystreamer.activities.fragments.SearchFragment;
import mx.eduardogsilva.spotifystreamer.activities.fragments.TopTracksFragment;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;
import mx.eduardogsilva.spotifystreamer.service.SpotifyPlayerService;
import mx.eduardogsilva.spotifystreamer.utilities.FormatUtils;
import mx.eduardogsilva.spotifystreamer.utilities.IntentUtils;

public class MainActivity extends AppCompatActivity implements SpotifyPlayerService.OnAsyncServiceListener,
        SearchFragment.OnSearchListener, TopTracksFragment.OnTopTracksListener, PlayerFragment.OnPlayerInteractionListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Service
    private SpotifyPlayerService mBoundService;
    private boolean mIsBound = false;

    // Actions
    public static final String ACTION_SHOW_PLAYING = "mx.eduardogsilva.action.SHOW_NOW_PLAYING";


    // Fragments and pane mode
    private boolean mTwoPane;
    // Fragments tags
    private static final String TOPTRACKSFRAGMENT_TAG = "TTF";
    private static final String PLAYERFRAGMENT_TAG = "PF";

    private PlayerFragment mPlayerFragment;
    private SearchFragment mSearchFragment;

    // Share if two panes
    private ShareActionProvider mShareActionProvider;
    private String mTrackInfo = "";

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(mTwoPane &&
                intent != null && intent.getAction() != null) {
            if(intent.getAction().equals(ACTION_SHOW_PLAYING)) {
                Fragment pf = getSupportFragmentManager().findFragmentByTag(PLAYERFRAGMENT_TAG);

                if(pf != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.remove(pf);
                    ft.addToBackStack(null);
                    ft.commit();
                }

                if(mBoundService != null) {
                    launchPlayer(
                            mBoundService.getCurrentTrack(),
                            false,
                            mBoundService.getDuration(),
                            false,
                            mBoundService.isPlaying());
                }
            }
        }
    }

    /* ===== LIFECYCLE METHODS ===== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mSearchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_search);

        // Get Fragments
        if(findViewById(R.id.top_tracks_container) != null) {
            mTwoPane = true;
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, new TopTracksFragment(), TOPTRACKSFRAGMENT_TAG)
                    .commit();
            }
            // Reconnect fragment if existent
            mPlayerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentByTag(PLAYERFRAGMENT_TAG);
        }

        // Start service for all activities to bind
        Intent serviceIntent = new Intent(this, SpotifyPlayerService.class);
        serviceIntent.setAction(SpotifyPlayerService.ACTION_INSTANTIATE);
        serviceIntent.putExtra(EXTRA_TWO_PANE, mTwoPane);
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
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* ===== MENU METHODS ===== */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem playingItem = menu.findItem(R.id.action_now_playing);
        MenuItem shareItem = null;

        if(mTwoPane) {
            // If in two pane mode, create the share action
            getMenuInflater().inflate(R.menu.playerfragment, menu);

            shareItem = menu.findItem(R.id.action_share);
            // Store share action provider
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

            if(mShareActionProvider != null && mTrackInfo != null) {
                mShareActionProvider.setShareIntent(IntentUtils.createShareTextIntent(mTrackInfo));
            }
        }

        if(mBoundService != null) {
            if(!mBoundService.isInitialized()){
                playingItem.setVisible(false);
                if(shareItem != null) {
                    shareItem.setVisible(false);
                }
            } else {
                playingItem.setVisible(true);
                if(mBoundService.isPlaying()) {
                    playingItem.setIcon(android.R.drawable.ic_media_play);
                } else {
                    playingItem.setIcon(android.R.drawable.ic_media_pause);
                }

                if(shareItem != null) {
                    shareItem.setVisible(true);
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
            if(!mTwoPane) {
                Intent playIntent = new Intent(this, PlayerActivity.class);
                startActivity(playIntent);
            } else {
                launchPlayer(
                        mBoundService.getCurrentTrack(),
                        false,
                        mBoundService.getDuration(),
                        false,
                        mBoundService.isPlaying());
            }
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
        if(!mTwoPane) {
            return;
        }

        if(mPlayerFragment != null && mPlayerFragment.isAdded()) {
            mPlayerFragment.bindTrackData(track);
            mPlayerFragment.lockControls(true);
        }
    }

    @Override
    public void onPrepared(int duration, TrackWrapper trackPrepared) {

        invalidateOptionsMenu();

        if(!mTwoPane) {
            return;
        }

        if(mPlayerFragment != null && mPlayerFragment.isAdded()) {
            mPlayerFragment.lockControls(false);
            mPlayerFragment.setSeekBarMaxDuration(mBoundService.getDuration());
            mPlayerFragment.playPause(true);
        }
    }

    @Override
    public void onCompletion() {

        invalidateOptionsMenu();

        if(!mTwoPane) {
            return;
        }

        if(mPlayerFragment != null && mPlayerFragment.isVisible()) {
            mPlayerFragment.playPause(false);
        }
    }

    /* ===== SEARCH FRAGMENT CALLBACKS ===== */

    @Override
    public void onArtistSelected(Intent intent) {
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
    public void onQueryTextSubmit(String queryText) {
        // Reset top tracks
        if(mTwoPane) {
            // Replace top tracks fragment
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.top_tracks_container,
                    new TopTracksFragment(),
                    TOPTRACKSFRAGMENT_TAG
            ).commit();
        }
    }

    @Override
    public void onDataFiltered() {

        if(mTwoPane) {
            // Click first result to show something
            mSearchFragment.clickPosition(0);
        }
    }

    /* ===== TOP TRACKS CALLBACKS =====*/

    @Override
    public void onTrackSelected(String artistId, int position, List<TrackWrapper> tracks) {

        TrackWrapper tw = tracks.get(position);

        launchPlayer(tw, true, -1, false, false);

        // Create share intent
        mTrackInfo = FormatUtils.getTrackShareInfo(tw, this);

        if(mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(IntentUtils.createShareTextIntent(mTrackInfo));
        }

        // Send start command to service
        Intent serviceIntent = new Intent(this, SpotifyPlayerService.class);
        serviceIntent.setAction(SpotifyPlayerService.ACTION_INIT_PLAY);

        serviceIntent.putExtra(TopTracksActivity.EXTRA_ARTIST_ID, artistId);
        serviceIntent.putParcelableArrayListExtra(
                TopTracksActivity.EXTRA_TRACKS,
                (ArrayList<? extends Parcelable>) tracks
        );
        serviceIntent.putExtra(TopTracksActivity.EXTRA_TRACK_POSITION, position);

        startService(serviceIntent);

    }

    public void launchPlayer(
            TrackWrapper track,
            boolean lockedControls,
            int duration,
            boolean showShareAction,
            boolean playing) {

        Bundle args = new Bundle();
        args.putParcelable(PlayerFragment.EXTRA_TRACK, track);
        args.putBoolean(PlayerFragment.EXTRA_LOCKED_CONTROLS, lockedControls);
        args.putInt(PlayerFragment.EXTRA_SEEKBAR_MAX, duration);
        args.putBoolean(PlayerFragment.EXTRA_SHOW_SHARE_ACTION, showShareAction);
        args.putBoolean(PlayerFragment.EXTRA_PLAYING, playing);

        // Create the new dialog
        mPlayerFragment = PlayerFragment.newInstance(args);
        mPlayerFragment.show(getSupportFragmentManager(), PLAYERFRAGMENT_TAG);
    }

    /* ===== PLAYER CALLBACKS =====*/

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
}
