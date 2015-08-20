package mx.eduardogsilva.spotifystreamer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;

/**
 * Tracks adapter for recycler view.
 * Created by Lalo on 14/06/15.
 */
public class TracksRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Tracks data
    private List<TrackWrapper> tracks;
    // For use in picasso
    private Context context;

    // View Types
    private final static int LOADING_VIEW = 2;
    private final static int EMPTY_VIEW = 1;
    private final static int TRACK_VIEW = 0;

    private boolean isLoading;

    // Listener for item clicks
    TrackViewHolder.ITracksViewHolderClicks mTracksClicksListener;

    /**
     * Main constructor.
     * @param context   Context to inflate views.
     */
    public TracksRecyclerAdapter(Context context, TrackViewHolder.ITracksViewHolderClicks listener) {
        this.tracks = new ArrayList<>();
        this.context = context;
        isLoading = true;
        mTracksClicksListener = listener;
    }

    /**
     * Get view type depending of the position. In our case we will get it according
     * @param position Position of the current item to get type
     * @return  View type
     */
    @Override
    public int getItemViewType(int position) {
        if(isLoading) {
            return LOADING_VIEW;
        }if(tracks.isEmpty()){
            return EMPTY_VIEW;
        } else {
            return TRACK_VIEW;
        }
    }

    /**
     * Creates a view depending on the parent and view type
     * @param parent    Parent the view will belong to.
     * @param viewType  Type of the view to be created.
     * @return          The view created.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case LOADING_VIEW:
                // Returns empty view.
                View loadingView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_loading_top_tracks, parent, false);
                loadingView.getLayoutParams().height = parent.getHeight();
                loadingView.getLayoutParams().width = parent.getWidth();

                return new LoadingViewHolder(loadingView);

            case TRACK_VIEW:
                // Returns view for tracks.
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_top_tracks, parent, false);
                return new TrackViewHolder(itemView, mTracksClicksListener);

            default:
                // Returns empty view.
                View emptyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_top_tracks, parent, false);
                emptyView.getLayoutParams().height = parent.getHeight();
                emptyView.getLayoutParams().width = parent.getWidth();

                return new EmptyViewHolder(emptyView);
        }
    }

    /**
     * Binds the holder to the view in the current position.
     * @param holder    Holder to show in the view.
     * @param position  Position to get data.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(!tracks.isEmpty()){
            TrackWrapper track = tracks.get(position);
            TrackViewHolder tvh = (TrackViewHolder) holder;
            tvh.bind(track, context, position);
        }
    }

    /**
     * Get item counts.
     * @return  Number of items ins data set.
     */
    @Override
    public int getItemCount() {

        return tracks.isEmpty()? 1 : tracks.size();
    }

    /**
     * Updates track data
     * @param tracks    tracks to handle
     */
    public void setTracks(List<TrackWrapper> tracks) {
        this.tracks = tracks;
        super.notifyDataSetChanged();
    }

    /**
     * Sets the value for showing the loading view.
     * @param isLoading true if view is still loading.
     */
    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public List<TrackWrapper> getTracks() {
        return tracks;
    }

    /**
     * Holder class to keep references to all subviews
     */
    public static class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        // Component views for tracks view.
        private ImageView albumImage;
        private TextView albumNameTextView;
        private TextView trackNameTextView;
        private ITracksViewHolderClicks mListener;
        private int mCurrentPosition;

        public TrackViewHolder(View itemView, ITracksViewHolderClicks listener) {
            super(itemView);

            albumImage = (ImageView) itemView.findViewById(R.id.list_item_track_imageview);
            albumNameTextView = (TextView) itemView.findViewById(R.id.list_item_track_album);
            trackNameTextView = (TextView) itemView.findViewById(R.id.list_item_track_name);
            mListener = listener;

            itemView.setOnClickListener(this);
        }

        public void bind(TrackWrapper track, Context context, int position){
            // load image if exists.
            if(!track.getThumbImage().isEmpty()){
                Picasso.with(context)
                        .load(track.getThumbImage())
                        .placeholder(R.mipmap.track_placeholder)
                        .error(R.mipmap.track_error)
                        .into(albumImage);
            }else {
                albumImage.setImageResource(R.mipmap.track_default);
            }

            albumNameTextView.setText(track.getAlbumName());
            trackNameTextView.setText(track.name);
            mCurrentPosition = position;
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onItemClicked(mCurrentPosition);
            }
        }

        public interface ITracksViewHolderClicks {
            void onItemClicked(int position);
        }
    }

    /**
     * View holder to be shown when no tracks are available
     */
    public class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * ViewHolder for loading animation
     */
    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

}
