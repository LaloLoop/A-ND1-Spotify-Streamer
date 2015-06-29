package mx.eduardogsilva.spotifystreamer.filters;

import android.util.Log;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import mx.eduardogsilva.spotifystreamer.adapters.ArtistsAdapter;
import mx.eduardogsilva.spotifystreamer.model.ArtistImageSort;
import retrofit.RetrofitError;

/**
 * Filter for Live searching.
 * Created by Lalo on 12/06/15.
 */
public class ArtistsFilter extends Filter {

    private static final String LOG_TAG = ArtistsFilter.class.getSimpleName();

    // Instance of service we will be using
    private final SpotifyService service;

    // Reference to adapter to update the view
    private final ArtistsAdapter mArtistsAdapter;

    // Listen data changes (filtering).
    private OnDataFilteredListener dataFilteredListener;

    // Constructor
    public ArtistsFilter(ArtistsAdapter mArtistsAdapter) {
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
            filterResults.values = toArtistImageSort(artistsPager.artists.items);
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

    private Object toArtistImageSort(List<Artist> items) {
        List<ArtistImageSort> artistImageSorts = new ArrayList<>(items.size());

        for(Artist a : items){
            ArtistImageSort ais = new ArtistImageSort(a);
            artistImageSorts.add(ais);
        }

        return artistImageSorts;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        ArtistsFilterResults artistsResults = (ArtistsFilterResults) results;

        switch (artistsResults.resultType){

            case ArtistsFilterResults.TYPE_SUCCESS:

                if(artistsResults.count > 0){

                    List<ArtistImageSort> artists = (List<ArtistImageSort>) artistsResults.values;
                    mArtistsAdapter.replaceAll(artists);

                    if(dataFilteredListener != null){
                        dataFilteredListener.onDataFiltered();
                    }

                } else {

                    mArtistsAdapter.removeAll();

                    if(dataFilteredListener != null){
                        dataFilteredListener.onNoResultsFound();
                    }
                }

                break;

            case ArtistsFilterResults.TYPE_ERROR:

                Log.e(LOG_TAG, "Call error: " + artistsResults.errorMsg);

                if(dataFilteredListener != null){
                    dataFilteredListener.onFilterError(artistsResults.errorMsg);
                }

                break;

            case ArtistsFilterResults.TYPE_EMPTY_STRING:

                mArtistsAdapter.removeAll();

                if(dataFilteredListener != null){
                    dataFilteredListener.onNoResultsFound();
                }

                break;
        }

    }

    /* ===== SETTERS & GETTERS ==== */

    public void setDataFilteredListener(OnDataFilteredListener dataFilteredListener) {
        this.dataFilteredListener = dataFilteredListener;
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

    /* Interface for listener */
    public interface OnDataFilteredListener {
        /**
         * Notify that data was filtered
         */
        public void onDataFiltered();

        public void onNoResultsFound();

        public void onFilterError(String errorMsg);

    }

}
