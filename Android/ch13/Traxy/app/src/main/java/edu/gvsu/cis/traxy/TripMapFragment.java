package edu.gvsu.cis.traxy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
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
public class TripMapFragment extends JournalLoaderFragment implements
        OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private OnMapInteractionListener mListener;
    private final static int LOCATION_PERM_REQUEST = 148;
    private final static String[] permArr = {Manifest.permission.ACCESS_FINE_LOCATION};

    @BindView(R.id.tripMap) MapView myMap;
    private GoogleMap gMap;
    private boolean mapReady, dataReady;
    private List<Marker> allMarkers;

    private static DateTimeFormatter longFormat, medFormat, shortFormat;
    static {
        longFormat = DateTimeFormat.forPattern("MMM d, yyyy");
        medFormat = DateTimeFormat.forPattern("MMM d");
        shortFormat = DateTimeFormat.forPattern("MMM");

    }
    public TripMapFragment() {
        // Required empty public constructor
        allMarkers = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
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


    private String formatDate (DateTime beg, DateTime end) {
        if (beg.getYear() != end.getYear())
            return longFormat.print(beg) + "-" + longFormat.print(end);
        if (beg.getMonthOfYear() != end.getMonthOfYear())
            return medFormat.print(beg) + "-" + medFormat.print(end) + ", " + beg
                    .getYear();
        return shortFormat.print(beg) + " " + beg.getDayOfMonth() + "-" +
                end.getDayOfMonth() + ", " + beg.getYear();

    }

    private void refreshMarkers() {
//        if (allMarkers.size() == allTrips.size()) return;
        LatLngBounds.Builder boundBuilder = new LatLngBounds.Builder();
        allMarkers.clear();
        gMap.clear();
        for (Trip t : allTrips) {
            DateTime begDate = DateTime.parse(t.getStartDate());
            DateTime endDate = DateTime.parse(t.getEndDate());
            LatLng thisLoc = new LatLng(t.getLat(), t.getLng());
            Marker m = gMap.addMarker(
                    new MarkerOptions().position(thisLoc).title(t.getName())
                            .snippet(formatDate(begDate, endDate)));
            m.setTag(t);
            allMarkers.add(m);
            boundBuilder.include(thisLoc);
        }
        if (allMarkers.size() > 0) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            LatLngBounds bound = boundBuilder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bound,
                    screenWidth, screenHeight, 56);
            gMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setOnInfoWindowClickListener(this);
        if (ContextCompat.checkSelfPermission(getActivity(), permArr[0]) ==
                PackageManager.PERMISSION_DENIED)
            requestPermissions(permArr, LOCATION_PERM_REQUEST);
        else
            gMap.setMyLocationEnabled(true);
        mapReady = true;
        if (dataReady) refreshMarkers();
    }

    @Override
    public void onJournalUpdated(List<Trip> trips) {
        dataReady = true;
        if (mapReady) refreshMarkers();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Trip t = (Trip) marker.getTag();
        mListener.onMarkerSelected(t);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERM_REQUEST) {
            try {
                gMap.setMyLocationEnabled(grantResults[0] == PackageManager.PERMISSION_GRANTED);
            } catch (SecurityException se) {

            }
        }
    }

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
