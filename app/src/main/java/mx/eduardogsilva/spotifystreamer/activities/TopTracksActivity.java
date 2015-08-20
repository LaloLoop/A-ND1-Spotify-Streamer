package mx.eduardogsilva.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import mx.eduardogsilva.spotifystreamer.R;
import mx.eduardogsilva.spotifystreamer.activities.fragments.TopTracksFragment;
import mx.eduardogsilva.spotifystreamer.model.TrackWrapper;

public class TopTracksActivity extends AppCompatActivity implements TopTracksFragment.OnTopTracksListener{

    private final String LOG_TAG = TopTracksActivity.class.getSimpleName();

    public static final String EXTRA_TRACKS = "etracks";
    public static final String EXTRA_TRACK_POSITION = "cposition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackClicked(int position, List<TrackWrapper> tracks) {
        Intent topTracksIntent = getIntent();

        Intent playIntent = new Intent(this, PlayerActivity.class);
        playIntent.putExtras(topTracksIntent);
        playIntent.putParcelableArrayListExtra(EXTRA_TRACKS, (ArrayList<? extends Parcelable>) tracks);
        playIntent.putExtra(EXTRA_TRACK_POSITION, position);

        startActivity(playIntent);
    }
}
