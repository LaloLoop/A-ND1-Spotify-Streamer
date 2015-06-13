package mx.eduardogsilva.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import mx.eduardogsilva.spotifystreamer.R;

/**
 * Adapter used to handle track data.
 * Created by Lalo on 12/06/15.
 */
public class TracksAdapter extends BaseAdapter{

    // Model
    private List<Track> tracks;

    // Inflater for creating list item views
    private LayoutInflater inflater;

    // Context to create inflater and picasso purposes
    private Context context;

    public TracksAdapter(Context context) {
        super();
        tracks = new ArrayList<>();
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Object getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tracks.get(position).id.hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Generate our view in this object if non existent
        if(convertView == null){
            convertView = inflater.inflate(R.layout.list_item_top_tracks, parent, false);
        }

        // Get track we will show
        Track track = tracks.get(position);

        // Get Main image view
        ImageView trackImage = (ImageView) convertView.findViewById(R.id.list_item_track_imageview);
        if(!track.album.images.isEmpty()){
            Picasso.with(context).load(track.album.images.get(0).url).into(trackImage);
        }else {
            trackImage.setImageResource(R.mipmap.ic_launcher);
        }

        // Get Track name textview
        TextView nameTextView = (TextView) convertView.findViewById(R.id.list_item_track_name);
        nameTextView.setText(track.name);

        // Get album name textview
        TextView albumTextView = (TextView) convertView.findViewById(R.id.list_item_track_album);
        albumTextView.setText(track.album.name);

        return convertView;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        super.notifyDataSetChanged();
    }
}
