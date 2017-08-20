package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.gvsu.cis.traxy.model.Trip;

public class MainActivity extends AppCompatActivity implements
        JournalFragment.OnJournalInteractionListener,
        TripMapFragment.OnMapInteractionListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private final static int NEW_TRIP_REQUEST = 146;
    private final static int EDIT_TRIP_REQUEST = 147;
    private FirebaseAuth mAuth;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewpager) ViewPager viewPager;
//    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.bottom_tabs) BottomNavigationView bottomTabs;

    DatabaseReference topRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        GoogleApiClient apiClient;

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        JournalPageAdapter pageAdapter = new JournalPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pageAdapter);
//        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(pageListener);
        bottomTabs.setOnNavigationItemSelectedListener(bottomTabsSelected);
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
        Intent newJournal = new Intent(MainActivity.this, TripEditorActivity.class);
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
    public void onTripSelected(Trip item) {
        System.out.println("interact!");
        Intent toDetails = new Intent(this, JournalViewActivity.class);
        //toDetails.putExtra("TRIP_NAME", item.name);
        toDetails.putExtra ("TRIP", Parcels.wrap(item));
        startActivity (toDetails);
    }

    @Override
    public void onTripEdit(Trip item) {
        Intent toEdit = new Intent(this, TripEditorActivity.class);
        toEdit.putExtra("TRIP", Parcels.wrap(item));
        startActivityForResult(toEdit, EDIT_TRIP_REQUEST);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("oops");
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
        else if (requestCode == EDIT_TRIP_REQUEST) {
            if (data != null && data.hasExtra("TRIP")) {
                Parcelable par = data.getParcelableExtra("TRIP");
                Trip t = Parcels.unwrap(par);
                topRef.child(t.getKey()).setValue(t);
                topRef.child(t.getKey()).child("key").removeValue();
                Snackbar.make(toolbar, "Trip Edited", Snackbar
                        .LENGTH_SHORT).show();
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMarkerSelected(Trip t) {

    }

    private class JournalPageAdapter extends FragmentPagerAdapter {

        public JournalPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Trips";
                case 1: return "Calendar";
                case 2: return "Maps";
                default: return null;
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return JournalFragment.newInstance(R.layout.journal_card_tall);
                case 1:
                    return MonthlyFragment.newInstance();
                case 2:
                    return TripMapFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {
        private MenuItem prevSelection;
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (prevSelection != null)
                prevSelection.setChecked(false);
            MenuItem thisMenu = bottomTabs.getMenu().getItem(position);
            thisMenu.setChecked(true);
            prevSelection = thisMenu;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener
            bottomTabsSelected = (@NonNull MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.all_trips:
                    viewPager.setCurrentItem(0, true);
                    break;
                case R.id.monthly_trips:
                    viewPager.setCurrentItem(1, true);
                    break;
                case R.id.trips_on_map:
                    viewPager.setCurrentItem(2, true);
            }
            return true;
        };


}
