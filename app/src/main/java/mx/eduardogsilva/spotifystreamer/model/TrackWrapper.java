package mx.eduardogsilva.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;
import mx.eduardogsilva.spotifystreamer.utilities.FilterUtils;

/**
 * Tracks wrapper for parcelable support
 * Created by Lalo on 03/07/15.
 */
public class TrackWrapper extends Track implements Parcelable {

    private String thumbImage = "";
    private String largeImage = "";
    private String albumName = "";
    private String artistName;
    private String externalUrl = "";

    private static final String EXTERNAL_URL = "spotify";

    public TrackWrapper(Track t) {
        this.album = t.album;
        this.external_ids = t.external_ids;
        this.popularity = t.popularity;
        this.id = t.id;
        this.name = t.name;
        this.preview_url = t.preview_url;
        this.external_urls = t.external_urls;
        // TODO add other properties.

        // Init aux fields
        getLargeImage();
        getThumbImage();
        getAlbumName();
        getExternalUrl();
    }

    public String getThumbImage() {
        if(thumbImage.isEmpty() && album != null){
            thumbImage = FilterUtils.getThumbImage(album.images);
        }
        return thumbImage;
    }

    public String getLargeImage() {
        if(largeImage.isEmpty() && album != null){
            largeImage = FilterUtils.getLargeImage(album.images);
        }
        return largeImage;
    }

    public String getAlbumName() {
        if(albumName.isEmpty() && album != null){
            albumName = album.name;
        }
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getExternalUrl() {
        if(externalUrl.isEmpty()) {
            if(external_urls != null) {
                externalUrl = external_urls.get(EXTERNAL_URL);
            }
        }
        return externalUrl;
    }

    /* Parcelable implementation */

    protected TrackWrapper(Parcel in) {
        thumbImage = in.readString();
        largeImage = in.readString();
        albumName = in.readString();
        name = in.readString();
        preview_url = in.readString();
        artistName = in.readString();
        externalUrl = in.readString();
    }

    public static final Creator<TrackWrapper> CREATOR = new Creator<TrackWrapper>() {
        @Override
        public TrackWrapper createFromParcel(Parcel in) {
            return new TrackWrapper(in);
        }

        @Override
        public TrackWrapper[] newArray(int size) {
            return new TrackWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbImage);
        dest.writeString(largeImage);
        dest.writeString(albumName);
        dest.writeString(name);
        dest.writeString(preview_url);
        dest.writeString(artistName);
        dest.writeString(externalUrl);
    }
}
