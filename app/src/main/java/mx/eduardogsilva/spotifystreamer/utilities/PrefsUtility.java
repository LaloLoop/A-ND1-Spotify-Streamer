package mx.eduardogsilva.spotifystreamer.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import mx.eduardogsilva.spotifystreamer.R;

/**
 * Quick access to shared preferences
 * Created by Lalo on 23/08/15.
 */
public class PrefsUtility {

    public static String getLocation(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getString(
                context.getString(R.string.pref_key_location),
                context.getString(R.string.pref_default_location)
        );
    }

}
