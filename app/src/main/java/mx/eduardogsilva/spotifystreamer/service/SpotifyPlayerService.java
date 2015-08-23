package mx.eduardogsilva.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.PlayerActivity;
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
    public static final String ACTION_INSTANTIATE = "com.example.action.INSTANTIATE";
    public static final String ACTION_INIT_PLAY = "com.example.action.INIT_PLAY";

    private NotificationManager nMn;

    // Notification ID to handle it.
    private static final int STREAMER_NOTIFICATION_ID = 1;

    // Attributes
    MediaPlayer mMediaPlayer = null;
    String mArtistId = "";
    int mCurrentPosition = 0;
    List<TrackWrapper> mTracks;
    boolean mPrepared = false;
    boolean mPlaying = false;

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

        nMn = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        String action = intent.getAction();

        switch (action) {

            case ACTION_INIT_PLAY:
                String artistId = intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_ID);
                int position = intent.getIntExtra(TopTracksActivity.EXTRA_TRACK_POSITION, 0);
                if(!mArtistId.equals(artistId)) {
                    mArtistId = artistId;
                    mTracks = intent.getParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACKS);
                    mCurrentPosition = position;

                    playCurrentTrack();
                } else if(mCurrentPosition != position) {
                    mCurrentPosition = position;
                    playCurrentTrack();
                }
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        nMn.cancel(STREAMER_NOTIFICATION_ID);
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

            mMediaPlayer.setDataSource(track.preview_url);
            mMediaPlayer.prepareAsync();
            mPrepared = false;
            mPlaying = false;

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
            mPlaying = false;
        } else {
            mMediaPlayer.start();
            mPlaying = true;
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

    public boolean isPlaying() {
        return mPlaying;
    }

    public boolean isInitialized() {
        return (mTracks != null && mTracks.size() > 0);
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
        mPlaying = true;

        showNotification();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlaying = false;
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

    /* ===== NOTIFICATION ===== */
    private void showNotification() {
        TrackWrapper cTrack = getCurrentTrack();

        Intent resultIntent = new Intent(getApplicationContext(), PlayerActivity.class);

        PendingIntent contentIntent =  PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setOngoing(true);
        builder.setTicker("Lalo");
        builder.setContentTitle(cTrack.name);
        builder.setContentText(cTrack.getAlbumName());
        builder.setContentIntent(contentIntent).build();

        Notification notification = builder.build();

        startForeground(STREAMER_NOTIFICATION_ID, notification);

    }

}
