package mx.eduardogsilva.spotifystreamer.activities.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;
import mx.eduardogsilva.spotifystreamer.utilities.FormatUtils;
import mx.eduardogsilva.spotifystreamer.utilities.IntentUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment implements View.OnClickListener {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    public static String EXTRA_TRACK = "etrack";
    public static String EXTRA_LOCKED_CONTROLS = "lcontrols";
    public static String EXTRA_PLAYING = "eplaying";
    public static String EXTRA_SEEKBAR_MAX = "sbmax";
    public static String EXTRA_SHOW_SHARE_ACTION = "ssaction";

    private ShareActionProvider mShareActionProvider;
    // Share information
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

    // UI updating for progress bar
    private Handler mHandler = new Handler();

    // UI Events listener
    private OnPlayerInteractionListener mListener;

    // Indicates playing state
    private boolean mPlaying = false;
    private boolean mLockedControls = true;
    private boolean mShowShareAction = true;

    public PlayerFragment() {
    }

    public static PlayerFragment newInstance(Bundle args) {
        PlayerFragment pf = new PlayerFragment();
        pf.setArguments(args);

        return pf;
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
                if (mListener != null && fromUser) {
                    mListener.onSeekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Runnable updateProgress = new Runnable() {
            @Override
            public void run() {
                if(mListener != null) {
                    try{

                        int originalPosition = mListener.getCurrentPosition();
                        int currentPosition = originalPosition / 1000;

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

        // By default lock controls
        lockControls(true);

        if(getArguments() != null) {
            Bundle args = getArguments();
            TrackWrapper track = args.getParcelable(EXTRA_TRACK);
            mLockedControls = args.getBoolean(EXTRA_LOCKED_CONTROLS, true);
            int seekBarMax = args.getInt(EXTRA_SEEKBAR_MAX, 30);
            mShowShareAction = args.getBoolean(EXTRA_SHOW_SHARE_ACTION, true);
            mPlaying = args.getBoolean(EXTRA_PLAYING, false);

            bindTrackData(track);
            lockControls(mLockedControls);
            setSeekBarMaxDuration(seekBarMax);
            playPause(mPlaying);
        }

        if(savedInstanceState != null) {
            setSeekBarMaxDuration(savedInstanceState.getInt(EXTRA_SEEKBAR_MAX) * 1000);
            playPause(savedInstanceState.getBoolean(EXTRA_PLAYING));
            lockControls(savedInstanceState.getBoolean(EXTRA_LOCKED_CONTROLS));
        }

        if(mShowShareAction) {
            setHasOptionsMenu(true);
        }

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(EXTRA_PLAYING, mPlaying);
        if(mSeekBar != null){
            outState.putInt(EXTRA_SEEKBAR_MAX, mSeekBar.getMax());
        }
        outState.putBoolean(EXTRA_LOCKED_CONTROLS, mLockedControls);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if(!mShowShareAction) {
            return;
        }

        inflater.inflate(R.menu.playerfragment, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        // Store share action provider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(mShareActionProvider != null && mTrackInfo != null) {
            mShareActionProvider.setShareIntent(IntentUtils.createShareTextIntent(mTrackInfo));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
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
                playPause(!mPlaying);
                mListener.onPlayPauseClicked();
            break;
            case R.id.next_button:
                playPause(false);
                mListener.onNextTrackClicked();
            break;
            case R.id.prev_button:
                playPause(!false);
                mListener.onPrevTrackClicked();
            break;
        }
    }

    public void playPause(boolean playing) {
        mPlaying = playing;
        if(playing) {
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    public void bindTrackData(TrackWrapper track) {
        if(track == null) {
            return;
        }

        // Bind view
        mArtistTextView.setText(track.getArtistName());
        mAlbumTextView.setText(track.getAlbumName());
        mSongNameTextView.setText(track.name);
        Picasso.with(getActivity()).load(track.getLargeImage()).into(mAlbumImageView);

        mTrackInfo = FormatUtils.getTrackShareInfo(track, getActivity());

        // Set Share data
        if(mShowShareAction && mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(IntentUtils.createShareTextIntent(mTrackInfo));
        }
    }

    public void lockControls(boolean lock) {
        if(mLockedControls != lock) {
            mLockedControls = lock;
        }
        // Lock controls
        mPlayPauseButton.setClickable(!lock);
        mSeekBar.setEnabled(!lock);
    };

    public void setSeekBarMaxDuration(int duration) {
        duration = (duration == -1)? 30 : (duration / 1000);
        mSeekBar.setMax(duration);

        mTotalTimeTextView.setText(getString(R.string.format_time, duration));
    }

    /**
     * Interface to listen for UI interaction and ask for properties.
     */
    public interface OnPlayerInteractionListener {
        // Events
        void onSeekTo(int position);
        void onPlayPauseClicked();
        void onNextTrackClicked();
        void onPrevTrackClicked();

        // Get info from implementor
        int getCurrentPosition();
    }
}
