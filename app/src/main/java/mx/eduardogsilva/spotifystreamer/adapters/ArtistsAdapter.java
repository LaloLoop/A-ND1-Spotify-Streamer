package mx.eduardogsilva.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import mx.eduardogsilva.spotifystreamer.filters.ArtistsLiveFilter;
import mx.eduardogsilva.spotifystreamer.R;

/**
 * Adapter to display artists info on ListView.
 * Created by Lalo on 10/06/15.
 */
public class ArtistsAdapter extends BaseAdapter implements Filterable{

    // Model - artists to handle
    private List<Artist> artists;
    // layout inflater to create views on getView method
    private LayoutInflater inflater;
    // Context to create the inflater and picasso reference
    private Context context;

    public ArtistsAdapter(Context context){
        this.artists = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Object getItem(int position) {
        return artists.get(position);
    }

    // Return a hashcode based on the artist id to prevent redrawing if possible.
    @Override
    public long getItemId(int position) {
        return artists.get(position).id.hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.list_item_artist, parent, false);
        }

        Artist artist = artists.get(position);

        // Get TextView
        TextView nameTextView = (TextView) convertView.findViewById(R.id.list_item_artist_textview);
        nameTextView.setText(artist.name);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_artist_imageview);

        // Load image
        if(!artist.images.isEmpty()){
            Picasso.with(context).load(artist.images.get(0).url).into(imageView);
        }else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }

        return convertView;
    }

    /**
     * Replaces al items in the adapter
     * @param artists   Artists to substitute current data set.
     */
    public void replaceAll(List<Artist> artists){
        this.artists = artists;
        super.notifyDataSetChanged();
    }

    /**
     * Removes all items from the adapter.
     */
    public void removeAll() {
        artists.clear();
        super.notifyDataSetChanged();
    }

    /**
     * Get a live filter for use in live search.
     */
    @Override
    public Filter getFilter() {
        return new ArtistsLiveFilter(this);
    }

    /**
     * Return an artist Id.
     * @param position  Position of the artist in the data set
     * @return  Spotify Id of the artist
     */
    public String getArtistId(int position) {
        return artists.get(position).id;
    }
}
