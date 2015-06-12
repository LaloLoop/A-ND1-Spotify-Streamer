package mx.eduardogsilva.spotifystreamer;

import android.util.Log;
import android.view.View;
import android.widget.Filter;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * Filter for Live searching.
 * Created by Lalo on 12/06/15.
 */
public class ArtistsLiveFilter extends Filter {

    private static final String LOG_TAG = ArtistsLiveFilter.class.getSimpleName();

    // Instance of service we will be using
    private final SpotifyService service;

    // Reference to adapter to update the view
    private final ArtistsAdapter mArtistsAdapter;

    // Reference to empty view to show.
    private View emptyView;

    // Constructor
    public ArtistsLiveFilter(ArtistsAdapter mArtistsAdapter) {
        super();
        // Instantiate service we will be using.
        SpotifyApi wrapper = new SpotifyApi();
        service = wrapper.getService();
        this.mArtistsAdapter = mArtistsAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        ArtistsPager artistsPager = null;
        ArtistsFilterResults filterResults = new ArtistsFilterResults();

        if(constraint.toString().isEmpty()){
            filterResults.resultType = ArtistsFilterResults.TYPE_EMPTY_STRING;
            return filterResults;
        }

        // Search artists asynchronously with the filter
        try{

            artistsPager = service.searchArtists(constraint.toString());

            // Set data in result
            filterResults.count = artistsPager.artists.total;
            filterResults.values = artistsPager.artists.items;
            filterResults.resultType = ArtistsFilterResults.TYPE_SUCCESS;

        }catch(RetrofitError re){
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(re);

            filterResults.values = null;
            filterResults.count=0;
            filterResults.resultType = ArtistsFilterResults.TYPE_ERROR;

            if(spotifyError.hasErrorDetails()){
                filterResults.errorMsg = spotifyError.getErrorDetails().message;
            }else {
                filterResults.errorMsg = spotifyError.toString();
            }
        }

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        ArtistsFilterResults artistsResults = (ArtistsFilterResults) results;

        switch (artistsResults.resultType){

            case ArtistsFilterResults.TYPE_SUCCESS:

                if(artistsResults.count > 0){

                    List<Artist> artists = (List<Artist>) artistsResults.values;
                    mArtistsAdapter.replaceAll(artists);
                    showEmptyView(false);

                } else {

                    mArtistsAdapter.removeAll();
                    showEmptyView(true);

                }

                break;

            case ArtistsFilterResults.TYPE_ERROR:

                Log.e(LOG_TAG, "Call error: " + artistsResults.errorMsg);

                showEmptyView(false);

                break;

            case ArtistsFilterResults.TYPE_EMPTY_STRING:

                showEmptyView(false);
                mArtistsAdapter.removeAll();

                break;
        }

    }

    /* ===== SETTERS & GETTERS ==== */
    public View getEmptyView() {
        return emptyView;
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    private void showEmptyView(boolean show){
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Custom FilterResults class to handle more info.
     */
    protected static class ArtistsFilterResults extends FilterResults{
        public int resultType;
        public String errorMsg;
        public static final int TYPE_SUCCESS = 1;
        public static final int TYPE_ERROR = 2;
        public static final int TYPE_EMPTY_STRING = 3;
    }

}
