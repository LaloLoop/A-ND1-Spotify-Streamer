package mx.eduardogsilva.spotifystreamer.activities.fragments;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends Fragment implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, View.OnClickListener, MediaPlayer.OnErrorListener {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private ShareActionProvider mShareActionProvider;
    private String mTrackInfo;

    // Views
    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private ImageView mAlbumImageView;
    private TextView mSongNameTextView;
    private ImageButton mPlayPauseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private SeekBar mSeekBar;
    private TextView mCurrentTimeTextView;
    private TextView mTotalTimeTextView;

    private MediaPlayer mMediaPlayer;

    private Handler mHandler = new Handler();

    private OnPlayerInteractionListener mListener;

    public PlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        mArtistTextView = (TextView) rootView.findViewById(R.id.artist_textview);
        mAlbumTextView = (TextView) rootView.findViewById(R.id.album_name_textview);
        mAlbumImageView = (ImageView) rootView.findViewById(R.id.album_cover_imageview);
        mSongNameTextView = (TextView) rootView.findViewById(R.id.song_name_textview);
        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.playpause_button);
        mNextButton = (ImageButton) rootView.findViewById(R.id.next_button);
        mPrevButton = (ImageButton) rootView.findViewById(R.id.prev_button);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.track_seekbar);
        mCurrentTimeTextView = (TextView) rootView.findViewById(R.id.current_time_textview);
        mTotalTimeTextView = (TextView) rootView.findViewById(R.id.total_time_textview);

        // Setup listeners
        mPlayPauseButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_player, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        // Store share action provider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(mShareActionProvider != null && mTrackInfo != null) {
            mShareActionProvider.setShareIntent(createSongIntent());
        } else {
            Log.e(LOG_TAG, "ShareActionProvider is null!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPlayerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPlayerInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playpause_button:
                onPlayPauseClicked();
            break;
            case R.id.next_button:
                mListener.onNextTrackClicked();
            break;
            case R.id.prev_button:
                mListener.onPrevTrackClicked();
            break;
        }
    }

    public void onPlayPauseClicked() {
        if(mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mMediaPlayer.start();
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        }
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

        // Bind view
        mArtistTextView.setText(track.getArtistName());
        mAlbumTextView.setText(track.getAlbumName());
        mSongNameTextView.setText(track.name);
        Picasso.with(getActivity()).load(track.getLargeImage()).into(mAlbumImageView);

        // SetUp Media Player
        try {

            mMediaPlayer.setDataSource(track.preview_url);
            mMediaPlayer.prepareAsync();

        } catch(IOException e) {
            Log.e(LOG_TAG, "Could not open media " + track.preview_url, e);
            Toast.makeText(getActivity(), "Could not play media", Toast.LENGTH_SHORT).show();
        }

        // Lock controls
        mPlayPauseButton.setClickable(false);
        mSeekBar.setEnabled(false);

        mTrackInfo = getString(R.string.format_share, track.name, track.getArtistName(), track.getExternalUrl());

        // Set Share data
        if(mShareActionProvider != null && mTrackInfo != null) {
            mShareActionProvider.setShareIntent(createSongIntent());
        } else {
            Log.e(LOG_TAG, "ShareActionProvider is null!");
        }
    }

    private Intent createSongIntent() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTrackInfo);
        shareIntent.setType("text/plain");

        return shareIntent;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        int duration = mp.getDuration();
        duration = (duration == -1)? 30 : (duration / 1000);
        mSeekBar.setMax(duration);
        mTotalTimeTextView.setText(getString(R.string.format_time, duration));

        mp.start();
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);

        // Unlock UI.
        mPlayPauseButton.setClickable(true);
        mSeekBar.setEnabled(true);

        Runnable updateProgress = new Runnable() {
            @Override
            public void run() {
                if(mMediaPlayer != null) {
                    try{

                        int currentPosition = mMediaPlayer.getCurrentPosition() / 1000;

                        mSeekBar.setProgress(currentPosition);

                        mCurrentTimeTextView.setText(getString(R.string.format_time, currentPosition));

                    } catch(IllegalStateException e) {
                        return;
                    }
                }
                mHandler.postDelayed(this, 1000);
            }
        };

        getActivity().runOnUiThread(updateProgress);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getActivity(), "An error occurred while downloading the track preview",
                Toast.LENGTH_SHORT).show();
        return false;
    }

    public interface OnPlayerInteractionListener {
        void onNextTrackClicked();
        void onPrevTrackClicked();
    }
}
