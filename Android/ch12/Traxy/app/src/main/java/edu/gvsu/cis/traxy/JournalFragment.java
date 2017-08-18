package edu.gvsu.cis.traxy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

import edu.gvsu.cis.traxy.model.Trip;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnJournalInteractionListener}
 * interface.
 */
public class JournalFragment extends Fragment {
    private static final String TAG = "JournalFragment";
    // TODO: Customize parameter argument names
    private static final String ARG_LAYOUT = "cell-layout";
    // TODO: Customize parameters
    private OnJournalInteractionListener mListener;
    private List<Trip> allTrips, selectedTrips;
    private JournalAdapter adapter;
    private Interval dateFilter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public JournalFragment() {
        allTrips = new ArrayList<Trip>();
        selectedTrips = new ArrayList<>();
    }

    private ValueEventListener valEvListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dateFilter != null) {
                filterTripByDate(dateFilter);
                adapter.reloadFrom(selectedTrips);   // for the adapter to refresh
            } else {
                adapter.reloadFrom(allTrips);
            }
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
            adapter.notifyDataSetChanged();
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

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static JournalFragment newInstance(@LayoutRes int cellLayout) {
        JournalFragment fragment = new JournalFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT, cellLayout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            cellLayout = getArguments().getInt(ARG_LAYOUT);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal_list, container, false);
        Bundle data = getArguments();
        int cellLayout = data.getInt(ARG_LAYOUT);
        // Set the adapter
        if (view instanceof RecyclerView) {
            FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            DatabaseReference userRef = dbRef.getReference(user.getUid());
            userRef.addChildEventListener (chEvListener);
            userRef.addValueEventListener(valEvListener);
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            adapter = new JournalAdapter(selectedTrips, cellLayout, mListener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnJournalInteractionListener) {
            mListener = (OnJournalInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnJournalInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void filterTripByDate(Interval dateRange) {
        dateFilter = dateRange;
        selectedTrips.clear();
        for (Trip t : allTrips) {
            DateTime begDate = DateTime.parse(t.getStartDate());
            DateTime endDate = DateTime.parse(t.getEndDate());
            if (dateRange.isAfter(endDate)) continue;
            if (dateRange.isBefore(begDate)) continue;
            selectedTrips.add(t);
        }
        adapter.reloadFrom(selectedTrips);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnJournalInteractionListener {
        // TODO: Update argument type and name
        void onTripSelected(Trip item);
        void onTripEdit(Trip item);
    }
}
