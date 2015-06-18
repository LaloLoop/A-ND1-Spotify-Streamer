package mx.eduardogsilva.spotifystreamer.model;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Artist class for fast access to images
 * Created by Lalo on 17/06/15.
 */
public class ArtistImageSort extends Artist {
    private String thumbImage = "";
    private String largeImage = "";

    public ArtistImageSort(Artist a) {
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
            thumbImage = imageInRange(200, 300);
        }
        return thumbImage;
    }

    public String getLargeImage() {
        if(largeImage.isEmpty()){
            largeImage = imageInRange(500, 700);
        }
        return largeImage;
    }

    private String imageInRange(int minWidth, int maxWidth) {

        if(images.isEmpty()){
            return "";
        }

        int indexResult = -1;
        int index = 0;

        for(Image image : images){
            if(image.width >= minWidth && image.width <= maxWidth){
                indexResult = index;
                break;
            }
            index++;
        }

        // Fallback to whatever image we have.
        if(indexResult == -1){
            return images.get(0).url;
        }else {
            return images.get(indexResult).url;
        }

    }

}
