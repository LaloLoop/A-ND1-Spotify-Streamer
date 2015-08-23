package mx.eduardogsilva.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import mx.eduardogsilva.spotifystreamer.activities.TopTracksActivity;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;

/**
 * Service to handle media player.
 * Created by Lalo on 22/08/15.
 */
public class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String LOG_TAG = SpotifyPlayerService.class.getSimpleName();

    // Actions
    public static final String ACTION_INIT_PLAY = "com.example.action.INIT_PLAY";

    // Attributes
    MediaPlayer mMediaPlayer = null;
    String mArtistId;
    int mCurrentPosition = 0;
    List<TrackWrapper> mTracks;
    boolean mPrepared = false;

    // Listeners
    OnAsyncServiceListener mListener;

    /**
     * Class for clients to access.
     * Service always runs in the same process as it's clients.
     */
    public class PlayerBinder extends Binder {
        public SpotifyPlayerService getService(OnAsyncServiceListener listener) {
            SpotifyPlayerService.this.mListener = listener;
            return SpotifyPlayerService.this;
        }
    }

    public interface OnAsyncServiceListener {
        void onPreparing(TrackWrapper track);
        void onPrepared(int duration, TrackWrapper trackPrepared);
        void onCompletion();
    }

    /* ==== LIFE CYCLE METHODS ==== */

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO Display system notification.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        switch (action) {

            case ACTION_INIT_PLAY:
                mArtistId = intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_ID);
                mTracks = intent.getParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACKS);
                mCurrentPosition = intent.getIntExtra(TopTracksActivity.EXTRA_TRACK_POSITION, 0);
                playCurrentTrack();
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO Cancel persistent notification.
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new PlayerBinder();


    /* ==== SERVICE MAIN FUNCTIONS ==== */
    public void playCurrentTrack() {
        playTrack(getCurrentTrack());
    }

    public void playTrack(TrackWrapper track) {
        if(track == null) {
            return;
        }

        // Reset media player
        if(mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
        } else {
            mMediaPlayer.reset();
        }

        // SetUp Media Player
        try {

            mPrepared = false;
            mMediaPlayer.setDataSource(track.preview_url);
            mMediaPlayer.prepareAsync();

        } catch(IOException e) {
            Log.e(LOG_TAG, "Could not open media " + track.preview_url, e);
            Toast.makeText(this, "Could not play media", Toast.LENGTH_SHORT).show();
        }

        if(mListener != null) {
            mListener.onPreparing(track);
        }

    }

    public void playPause() {
        if(mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    public void nextTrack() {
        changePosition(1);

        TrackWrapper track = mTracks.get(mCurrentPosition);
        playTrack(track);
    }

    public void prevTrack() {
        changePosition(-1);

        TrackWrapper track = mTracks.get(mCurrentPosition);
        playTrack(track);
    }

    public void seekTo(int position) {
        if(mMediaPlayer != null) {
            mMediaPlayer.seekTo(position * 1000);
        }
    }

    public int getCurrentPosition() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    private void changePosition(int delta) {
        mCurrentPosition += delta;

        if(mCurrentPosition >= mTracks.size()) {
            mCurrentPosition = 0;
        } else if(mCurrentPosition < 0) {
            mCurrentPosition = mTracks.size()-1;
        }
    }

    public boolean isPrepared() {
        return mPrepared;
    }

    public TrackWrapper getCurrentTrack() {
        return mTracks.get(mCurrentPosition);
    }

    /* ==== MEDIA PLAYER CALLBACKS ==== */

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(mListener != null) {
            mListener.onPrepared(mp.getDuration(), getCurrentTrack());
        }
        mPrepared = true;
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mListener != null) {
            mListener.onCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "An error occurred while downloading the track preview",
                Toast.LENGTH_SHORT).show();
        return false;
    }

    public void setOnAsyncServiceistener(OnAsyncServiceListener mListener) {
        this.mListener = mListener;
    }
}
