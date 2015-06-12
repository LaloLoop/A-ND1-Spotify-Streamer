package mx.eduardogsilva.spotifystreamer;

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

/**
 * Adapter to display artists info on ListView.
 * Created by Lalo on 10/06/15.
 */
public class ArtistsAdapter extends BaseAdapter implements Filterable{

    private List<Artist> artists;
    private LayoutInflater inflater;
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

    public void replaceAll(List<Artist> artists){
        this.artists = artists;
        super.notifyDataSetChanged();
    }

    public void removeAll() {
        artists.clear();
        super.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new ArtistsLiveFilter(this);
    }
}
