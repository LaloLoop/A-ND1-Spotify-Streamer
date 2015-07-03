package mx.eduardogsilva.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;
import mx.eduardogsilva.spotifystreamer.utilities.FilterUtils;

/**
 * Artist class for fast access to images
 * Created by Lalo on 17/06/15.
 */
public class ArtistWrapper extends Artist implements Parcelable {
    private String thumbImage = "";
    private String largeImage = "";

    public ArtistWrapper(Parcel in) {
        id = in.readString();
        name = in.readString();
        thumbImage = in.readString();
        largeImage = in.readString();
    }

    public ArtistWrapper(Artist a) {
        this.images = a.images;
        this.id = a.id;
        this.external_urls = a.external_urls;
        this.uri = a.uri;
        this.followers = a.followers;
        this.genres = a.genres;
        this.href = a.href;
        this.name = a.name;
        this.popularity = a.popularity;
        this.type = a.type;

        // Call images filter
        this.getThumbImage();
        this.getLargeImage();
    }


    public String getThumbImage() {
        if(thumbImage.isEmpty()){
            thumbImage = FilterUtils.getThumbImage(images);
        }
        return thumbImage;
    }

    public String getLargeImage() {
        if(largeImage.isEmpty()){
            largeImage = FilterUtils.getLargeImage(images);
        }
        return largeImage;
    }

    /* Methods implemented from parcelable to save artist instances */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(thumbImage);
        dest.writeString(largeImage);
    }

    public static final Parcelable.Creator<ArtistWrapper> CREATOR = new Parcelable.Creator<ArtistWrapper>() {

        @Override
        public ArtistWrapper createFromParcel(Parcel source) {
            return new ArtistWrapper(source);
        }

        @Override
        public ArtistWrapper[] newArray(int size) {
            return new ArtistWrapper[size];
        }
    };
}
