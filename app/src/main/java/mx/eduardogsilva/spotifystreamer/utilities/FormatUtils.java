package mx.eduardogsilva.spotifystreamer.utilities;

import android.content.Context;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;

/**
 * Format common text
 * Created by Lalo on 23/08/15.
 */
public class FormatUtils {

    public static String getTrackShareInfo(TrackWrapper track, Context context) {
        return context.getString(R.string.format_share, track.name, track.getArtistName(),
                track.getExternalUrl());
    }
}
