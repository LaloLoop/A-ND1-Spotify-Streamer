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

import kaaes.spotify.webapi.android.models.Track;
import mx.eduardogsilva.spotifystreamer.R;

/**
 * Tracks adapter for recycler view.
 * Created by Lalo on 14/06/15.
 */
public class TracksRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Tracks data
    private List<Track> tracks;
    // For use in picasso
    private Context context;

    // View Types
    private final static int LOADING_VIEW = 2;
    private final static int EMPTY_VIEW = 1;
    private final static int TRACK_VIEW = 0;

    private boolean isLoading;

    /**
     * Main constructor.
     * @param context   Context to inflate views.
     */
    public TracksRecyclerAdapter(Context context) {
        this.tracks = new ArrayList<>();
        this.context = context;
        isLoading = true;
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
                return new TrackViewHolder(itemView);

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
            Track track = tracks.get(position);
            TrackViewHolder tvh = (TrackViewHolder) holder;
            tvh.bind(track, context);
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
    public void setTracks(List<Track> tracks) {
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

    /**
     * Holder class to keep references to all subviews
     */
    public class TrackViewHolder extends RecyclerView.ViewHolder {

        // Component views for tracks view.
        private ImageView albumImage;
        private TextView albumNameTextView;
        private TextView trackNameTextView;

        public TrackViewHolder(View itemView) {
            super(itemView);

            albumImage = (ImageView) itemView.findViewById(R.id.list_item_track_imageview);
            albumNameTextView = (TextView) itemView.findViewById(R.id.list_item_track_name);
            trackNameTextView = (TextView) itemView.findViewById(R.id.list_item_track_album);
        }

        public void bind(Track track, Context context){
            // load image if exists.
            if(!track.album.images.isEmpty()){
                Picasso.with(context)
                        .load(track.album.images.get(0).url)
                        .placeholder(R.mipmap.track_placeholder)
                        .error(R.mipmap.track_error)
                        .into(albumImage);
            }else {
                albumImage.setImageResource(R.mipmap.track_default);
            }

            albumNameTextView.setText(track.album.name);
            trackNameTextView.setText(track.name);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

}
