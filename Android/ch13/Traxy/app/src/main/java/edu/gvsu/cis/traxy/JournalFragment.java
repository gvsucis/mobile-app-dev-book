package edu.gvsu.cis.traxy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class JournalFragment extends JournalLoaderFragment {
    private static final String TAG = "JournalFragment";
    // TODO: Customize parameter argument names
    private static final String ARG_LAYOUT = "cell-layout";
    // TODO: Customize parameters
    private OnJournalInteractionListener mListener;
    private List<Trip> allTrips, selectedTrips;
    private JournalAdapter adapter;
    private Interval dateFilter;
//    private int cellLayout;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public JournalFragment() {
        allTrips = new ArrayList<Trip>();
        selectedTrips = new ArrayList<>();
    }

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            cellLayout = getArguments().getInt(ARG_LAYOUT);
//            adapter = new JournalAdapter(selectedTrips, cellLayout, mListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal_list, container, false);
        Bundle data = getArguments();
        int cellLayout = data.getInt(ARG_LAYOUT);
        // Set the adapter
        if (view instanceof RecyclerView) {
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
        if (selectedTrips == null) return;
        selectedTrips.clear();
        if (allTrips == null) return;;
        if (dateRange != null) {
            for (Trip t : allTrips) {
                DateTime begDate = DateTime.parse(t.getStartDate());
                DateTime endDate = DateTime.parse(t.getEndDate());
                if (dateRange.isAfter(endDate)) continue;
                if (dateRange.isBefore(begDate)) continue;
                selectedTrips.add(t);
            }
        } else
            selectedTrips.addAll(allTrips);
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

    @Override
    void onJournalUpdated(List<Trip> trips) {
        allTrips = trips;
        if (dateFilter != null) {
            filterTripByDate(dateFilter);
            adapter.reloadFrom(selectedTrips);   // for the adapter to refresh
        } else {
            adapter.reloadFrom(allTrips);
        }

    }


}
