package mx.eduardogsilva.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.MainActivity;
import mx.eduardogsilva.spotifystreamer.activities.PlayerActivity;
import mx.eduardogsilva.spotifystreamer.activities.TopTracksActivity;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;
import mx.eduardogsilva.spotifystreamer.utilities.PrefsUtility;

/**
 * Service to handle media player.
 * Created by Lalo on 22/08/15.
 */
public class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String LOG_TAG = SpotifyPlayerService.class.getSimpleName();

    // Actions
    public static final String ACTION_INSTANTIATE = "mx.eduardogsilva.action.INSTANTIATE";
    public static final String ACTION_INIT_PLAY = "mx.eduardogsilva.action.INIT_PLAY";
    public static final String ACTION_PLAY_PAUSE = "mx.eduardogsilva.action.PLAY_PAUSE";
    public static final String ACTION_PREVIOUS = "mx.eduardogsilva.action.PREVIOUS";
    public static final String ACTION_NEXT = "mx.eduardogsilva.action.NEXT";
    public static final String ACTION_UPDATE_NOTIFICATION = "mx.eduardogsilva.action.UPDATE_NOTIF";
    public static final String ACTION_STOP = "mx.eduardogsilva.action.STOP";

    // Media session token
    private static final String MEDIA_SESSION_TOKEN = "mx.eduardosilva.token.JLAKJS0";

    // Notification ID to handle it.
    private static final int STREAMER_NOTIFICATION_ID = 1;

    // Attributes
    // Media control
    private MediaPlayer mMediaPlayer = null;
    private MediaSession mMediaSession;

    // Model
    String mArtistId = "";
    int mCurrentPosition = 0;
    List<TrackWrapper> mTracks;
    boolean mPrepared = false;
    boolean mPlaying = false;
    boolean mTwoPane = false;

    // Preferences status
    private String mLastLocation = "";

    // Get image from picasso as bitmap
    private Bitmap mAlbumArt;
    private Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mAlbumArt = bitmap;
            // Update whenever picasso is ready to show album art.
            updateNotification();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

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

    /**
     * Interface to be implemented if notifications are required from the service.
     */
    public interface OnAsyncServiceListener {
        void onPreparing(TrackWrapper track);
        void onPrepared(int duration, TrackWrapper trackPrepared);
        void onCompletion();
    }

    /* ==== LIFE CYCLE METHODS ==== */

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mMediaPlayer == null) {
            initMediaSession();
        }

        handleIntent(intent);

        return START_STICKY;
    }

    private void initMediaSession() {
        initMediaPlayer();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaSession = new MediaSession(getApplicationContext(), MEDIA_SESSION_TOKEN);
        }
    }

    private void handleIntent(Intent intent) {
        if(intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        switch (action) {

            case ACTION_INSTANTIATE:
                if(intent.hasExtra(MainActivity.EXTRA_TWO_PANE)) {
                    mTwoPane = intent.getBooleanExtra(MainActivity.EXTRA_TWO_PANE, false);
                }
                break;
            case ACTION_INIT_PLAY:
                initAndPlay(intent);
                break;
            case ACTION_PLAY_PAUSE:
                playPause();
                updateNotification();
                break;
            case ACTION_NEXT:
                nextTrack();
                updateNotification();
                break;
            case ACTION_PREVIOUS:
                prevTrack();
                updateNotification();
                break;
            case ACTION_UPDATE_NOTIFICATION:
                updateNotification();
                break;
            case ACTION_STOP:
                stopSelf();
                break;
            default:
                break;
        }
    }

    private void initAndPlay(Intent intent) {
        // Get current location
        String location = PrefsUtility.getLocation(getApplicationContext());

        String artistId = intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_ID);
        int position = intent.getIntExtra(TopTracksActivity.EXTRA_TRACK_POSITION, 0);
        if(!mLastLocation.equals(location) || !mArtistId.equals(artistId)) {

            mLastLocation = location;

            mArtistId = artistId;
            mTracks = intent.getParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACKS);
            mCurrentPosition = position;
            playCurrentTrack();

        } else if(mCurrentPosition != position) {

            mCurrentPosition = position;
            playCurrentTrack();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        Picasso.with(this).cancelRequest(mTarget);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaSession.release();
        }

        stopForeground(true);
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
            initMediaPlayer();
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

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
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
        // Load album art
        TrackWrapper tw = getCurrentTrack();
        String largeAlbum = tw.getLargeImage();
        String mediumAlbum = tw.getThumbImage();

        if(largeAlbum != null && !largeAlbum.isEmpty()){
            Picasso.with(this).load(largeAlbum).into(mTarget);
        } else if(mediumAlbum != null && !mediumAlbum.isEmpty()) {
            // Fallback to medium.
            Picasso.with(this).load(largeAlbum).into(mTarget);
        }

        if(mListener != null) {
            mListener.onPrepared(mp.getDuration(), tw);
        }

        mPrepared = true;
        mp.start();
        mPlaying = true;

        updateNotification();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlaying = false;
        if(mListener != null) {
            mListener.onCompletion();
        }
        updateNotification();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "An error occurred while downloading the track preview",
                Toast.LENGTH_SHORT).show();
        return false;
    }

    public void setOnAsyncServiceListener(OnAsyncServiceListener mListener) {
        this.mListener = mListener;
    }

    /* ===== NOTIFICATION ===== */
    private void updateNotification() {
        boolean displayNotifications = PrefsUtility.getNotificationsEnabled(getApplicationContext());

        if(!displayNotifications) {
            stopForeground(true);
            return;
        }

        TrackWrapper cTrack = getCurrentTrack();

        Intent resultIntent;

        if(mTwoPane) {
            resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.setAction(MainActivity.ACTION_SHOW_PLAYING);
        } else {
            resultIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        }

        PendingIntent contentIntent =  PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
            mediaStyle.setShowActionsInCompactView(0, 1, 2);
            mediaStyle.setMediaSession(mMediaSession.getSessionToken());


            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            // Show controls on lock screen even when user hides sensitive content.
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.addAction(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    buildPendingIntent(ACTION_PREVIOUS)); // #0 Prev button
            builder.addAction(
                    (mPlaying)?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,
                    (mPlaying)?"Pause":"Play",
                    buildPendingIntent(ACTION_PLAY_PAUSE));  // #1 Pause button
            builder.addAction(
                    android.R.drawable.ic_media_next,
                    "Next",
                    buildPendingIntent(ACTION_NEXT));     // #2 Next button
            // Apply the media style template
            builder.setStyle(mediaStyle);
            builder.setContentTitle(cTrack.name);
            builder.setContentText(cTrack.getAlbumName());
            builder.setContentIntent(contentIntent);

            if(mAlbumArt != null) {
                builder.setLargeIcon(mAlbumArt);
            }

            notification = builder.build();
        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setOngoing(true);
            builder.setTicker(getString(R.string.app_name));
            builder.setContentTitle(cTrack.name);
            builder.setContentText(cTrack.getAlbumName());
            builder.setContentIntent(contentIntent).build();

            notification = builder.build();
        }

        startForeground(STREAMER_NOTIFICATION_ID, notification);

    }

    private PendingIntent buildPendingIntent(String action) {
        Intent intent = new Intent(getApplicationContext(), SpotifyPlayerService.class);
        intent.setAction(action);

        return PendingIntent.getService(getApplicationContext(), 1, intent, 0);
    }

}
