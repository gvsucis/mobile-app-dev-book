package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.libraries.places.api.Places;

import org.joda.time.DateTime;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.gvsu.cis.traxy.dummy.DummyContent;

public class MainActivity extends AppCompatActivity implements JournalFragment.OnListFragmentInteractionListener {

    private final static int NEW_TRIP_REQUEST = 146;

    @BindView(R.id.toolbar) Toolbar toolbar;

    private JournalFragment journalFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        journalFragment = (JournalFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        setSupportActionBar(toolbar);

        Places.initialize(getApplicationContext(), BuildConfig.PLACES_API_KEY);
    }

    @OnClick(R.id.fab)
    public void addNewJournal() {
        Intent newJournal = new Intent(MainActivity.this, NewJournalActivity.class);
        startActivityForResult(newJournal, NEW_TRIP_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_logout) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyJournal item) {
        System.out.println("interact!");
        Intent toDetails = new Intent(this, JournalViewActivity.class);
        toDetails.putExtra("TRIP_NAME", item.name);
        startActivity (toDetails);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_TRIP_REQUEST) {
            if (data != null && data.hasExtra("TRIP")) {
                Parcelable parcel = data.getParcelableExtra("TRIP");
                Trip t = Parcels.unwrap(parcel);
                DateTime startDate = DateTime.parse(t.startDate);
                DummyContent.DummyJournal newEntry = new DummyContent.DummyJournal(t.name, t.location, startDate);
                DummyContent.ITEMS.add(newEntry);
                journalFragment.reloadView();
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
