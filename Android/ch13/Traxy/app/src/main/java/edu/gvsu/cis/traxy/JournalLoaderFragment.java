package edu.gvsu.cis.traxy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.gvsu.cis.traxy.model.Trip;

/**
 * Created by dulimarh on 8/18/17.
 */

public abstract class JournalLoaderFragment extends Fragment {
    protected List<Trip> allTrips;
    private DatabaseReference userRef;

    abstract void onJournalUpdated(List<Trip> trips);

    public JournalLoaderFragment() {
        allTrips = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(user.getUid());
    }

    @Override
    public void onResume() {
        super.onResume();
        userRef.addChildEventListener(chEvListener);
        userRef.addListenerForSingleValueEvent(valEvListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        userRef.removeEventListener(valEvListener);
        userRef.removeEventListener(chEvListener);
    }

    private ValueEventListener valEvListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            onJournalUpdated(allTrips);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener chEvListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Trip entry = (Trip) dataSnapshot.getValue(Trip.class);
            entry.setKey(dataSnapshot.getKey());
            /* Check for possible duplicate */
//          Java8 Stream requires API level 24 or later
//            boolean keyFound = allTrips.stream()
//                    .anyMatch(t -> t.getKey().equals(entry.getKey()));
            boolean keyFound = false;
            for (Trip t : allTrips)
                if (t.getKey().equals(entry.getKey())) {
                    keyFound = true;
                    break;
                }
            if (!keyFound)
                allTrips.add(entry);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String
                prevKey) {
            String key = dataSnapshot.getKey();
            for (int k = 0; k < allTrips.size(); k++) {
                if (allTrips.get(k).getKey().equals(key)) {
                    allTrips.set(k, dataSnapshot.getValue(Trip.class));
                    break;
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Trip entry = (Trip) dataSnapshot.getValue(Trip.class);
            List<Trip> newTrips = new ArrayList<Trip>();
            for (Trip t : allTrips) {
                if (!t.getKey().equals(dataSnapshot.getKey())) {
                    newTrips.add(t);
                }
            }
            allTrips = newTrips;
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

}
