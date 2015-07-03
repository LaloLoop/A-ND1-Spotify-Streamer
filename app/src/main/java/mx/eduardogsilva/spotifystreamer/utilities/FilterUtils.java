package mx.eduardogsilva.spotifystreamer.utilities;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Filter data utilities.
 * Created by Lalo on 03/07/15.
 */
public class FilterUtils {

    private static final int THUMB_MIN_WIDTH = 200;
    private static final int THUMB_MAX_WIDTH = 300;

    private static final int LARGE_MIN_WIDTH = 500;
    private static final int LARGE_MAX_WIDTH = 700;

    /**
     * Search image in a given range
     * @param images    Source of images.
     * @param minWidth
     * @param maxWidth
     * @return          Image for the given range or empty string otherwise.
     */
    public static String imageInRange(List<Image> images, int minWidth, int maxWidth) {

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

    public static String getThumbImage (List<Image> images){
        return FilterUtils.imageInRange(images, THUMB_MIN_WIDTH, THUMB_MAX_WIDTH);
    }

    public static String getLargeImage(List<Image> images) {
        return FilterUtils.imageInRange(images, LARGE_MIN_WIDTH, LARGE_MAX_WIDTH);
    }

}
