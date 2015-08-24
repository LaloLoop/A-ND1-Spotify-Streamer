package mx.eduardogsilva.spotifystreamer.utilities;

import android.content.Intent;

/**
 * Common methods to create intents
 * Created by Lalo on 23/08/15.
 */
public class IntentUtils {

    /**
     * Creates the share intent for plain text
     * @param text
     * @return
     */
    public static Intent createShareTextIntent(String text) {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");

        return shareIntent;
    }
}
