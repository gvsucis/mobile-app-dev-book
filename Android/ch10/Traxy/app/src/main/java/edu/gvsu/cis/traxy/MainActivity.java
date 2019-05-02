package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.gvsu.cis.traxy.model.Trip;

public class MainActivity extends AppCompatActivity implements JournalFragment.OnListFragmentInteractionListener {

    private final static int NEW_TRIP_REQUEST = 146;
    private FirebaseAuth mAuth;
    @BindView(R.id.toolbar) Toolbar toolbar;

    DatabaseReference topRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        Places.initialize(getApplicationContext(), BuildConfig.PLACES_API_KEY);
    }

    @Override
    public void onResume(){
        super.onResume();
        FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        topRef = dbRef.getReference(uid);
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
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onListFragmentInteraction(Trip item) {
        System.out.println("interact!");
        Intent toDetails = new Intent(this, JournalViewActivity.class);
        //toDetails.putExtra("TRIP_NAME", item.name);
        toDetails.putExtra ("TRIP", Parcels.wrap(item));
        startActivity (toDetails);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_TRIP_REQUEST) {
            if (data != null && data.hasExtra("TRIP")) {
                Parcelable par = data.getParcelableExtra("TRIP");
                Trip t = Parcels.unwrap(par);
                topRef.push().setValue(t);
                Snackbar.make(toolbar, "New Trip Added", Snackbar.LENGTH_SHORT).show();
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
