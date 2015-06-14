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
public class TracksRecyclerAdapter extends RecyclerView.Adapter<TracksRecyclerAdapter.TrackViewHolder> {

    // Tracks data
    private List<Track> tracks;
    // For use in picasso
    private Context context;

    public TracksRecyclerAdapter(Context context) {
        this.tracks = new ArrayList<>();
        this.context = context;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_top_tracks, parent, false);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.bind(track, context);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
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
     * Holder class to keep references to all subviews
     */
    public class TrackViewHolder extends RecyclerView.ViewHolder {

        // Views every item has.
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
            if(!track.album.images.isEmpty()){
                Picasso.with(context).load(track.album.images.get(0).url).into(albumImage);
            }else {
                albumImage.setImageResource(R.mipmap.ic_launcher);
            }

            albumNameTextView.setText(track.album.name);
            trackNameTextView.setText(track.name);
        }
    }

}
