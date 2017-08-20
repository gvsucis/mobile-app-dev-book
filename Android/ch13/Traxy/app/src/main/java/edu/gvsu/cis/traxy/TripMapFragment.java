package edu.gvsu.cis.traxy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.gvsu.cis.traxy.model.Trip;

// API Key:
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMapInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripMapFragment extends /*JournalLoader*/Fragment implements
        OnMapReadyCallback {

    private OnMapInteractionListener mListener;

    @BindView(R.id.tripMap) MapView myMap;

    public TripMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TripMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripMapFragment newInstance() {
        TripMapFragment fragment = new TripMapFragment();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        myMap.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        myMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        myMap.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        myMap.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myMap.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        myMap.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        myMap.onLowMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_map, container,
                false);
        ButterKnife.bind(this, view);
        myMap.onCreate(savedInstanceState);
        myMap.getMapAsync(this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onMarkerSelected(null);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMapInteractionListener) {
            mListener = (OnMapInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMapInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

//    @Override
//    public void onJournalUpdated(List<Trip> trips) {
//
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMapInteractionListener {
        // TODO: Update argument type and name
        void onMarkerSelected(Trip t);
    }
}
