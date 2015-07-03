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

    public TrackWrapper(Track t) {
        this.album = t.album;
        this.external_ids = t.external_ids;
        this.popularity = t.popularity;
        this.id = t.id;
        this.name = t.name;
        // TODO add other properties.

        // Init aux fields
        getLargeImage();
        getThumbImage();
        getAlbumName();
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

    /* Parcelable implementation */

    protected TrackWrapper(Parcel in) {
        thumbImage = in.readString();
        largeImage = in.readString();
        albumName = in.readString();
        name = in.readString();
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
    }
}
