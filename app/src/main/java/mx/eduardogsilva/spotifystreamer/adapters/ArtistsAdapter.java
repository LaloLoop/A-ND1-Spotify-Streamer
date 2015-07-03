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

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.filters.ArtistsFilter;
import mx.eduardogsilva.spotifystreamer.model.ArtistWrapper;
import mx.eduardogsilva.spotifystreamer.transforms.CircleTransform;

/**
 * Adapter to display artists info on ListView.
 * Created by Lalo on 10/06/15.
 */
public class ArtistsAdapter extends BaseAdapter implements Filterable{

    private static final String LOG_TAG = ArtistsAdapter.class.getSimpleName();
    // Model - artists to handle
    private List<ArtistWrapper> artists;
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
        ArtistViewHolder viewHolder;

        if(convertView == null){

            convertView = inflater.inflate(R.layout.list_item_artist, parent, false);

            viewHolder = new ArtistViewHolder(convertView);
            convertView.setTag(viewHolder);

        }else {
            viewHolder = (ArtistViewHolder) convertView.getTag();
        }

        ArtistWrapper artist = artists.get(position);

        viewHolder.bind(artist, context);

        return convertView;
    }

    /**
     * Replaces al items in the adapter
     * @param artists   Artists to substitute current data set.
     */
    public void replaceAll(List<ArtistWrapper> artists){
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
        return new ArtistsFilter(this);
    }

    /**
     * Return an artist Id.
     * @param position  Position of the artist in the data set
     * @return  Spotify Id of the artist
     */
    public String getArtistId(int position) {
        return artists.get(position).id;
    }

    public List<ArtistWrapper> getArtists() {
        return artists;
    }

    /* View Holder for artists */
    private static class ArtistViewHolder {
        public ImageView artistImage;
        public TextView artistName;

        public ArtistViewHolder(View convertView) {
            artistImage = (ImageView) convertView.findViewById(R.id.list_item_artist_imageview);
            artistName = (TextView) convertView.findViewById(R.id.list_item_artist_textview);
        }

        public void bind(ArtistWrapper artist, Context context) {
            artistName.setText(artist.name);

            String thumbImage = artist.getThumbImage();

            // Load image
            if(!thumbImage.isEmpty()){
                Picasso.with(context)
                        .load(thumbImage)
                        .placeholder(R.mipmap.artist_placeholder)
                        .error(R.mipmap.artist_error)
                        .transform(new CircleTransform())
                        .into(artistImage);
            }else {
                Picasso.with(context)
                        .load(R.mipmap.artist_default)
                        .transform(new CircleTransform())
                        .into(artistImage);
            }
        }

    }
}
