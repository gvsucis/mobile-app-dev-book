package edu.gvsu.cis.traxy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonthlyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MonthlyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthlyFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    @BindView(R.id.monthly_calendar)
    MaterialCalendarView monthly;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private JournalFragment journalFragment;

    public MonthlyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MonthlyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonthlyFragment newInstance() {
        MonthlyFragment fragment = new MonthlyFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monthly, container,
                false);
        ButterKnife.bind(this, view);
        monthly.setOnMonthChangedListener(monthListener);
        FragmentManager fm = getChildFragmentManager();
        journalFragment = JournalFragment.newInstance(R.layout
                .journal_card_mini);
        fm.beginTransaction()
                .add(R.id.fragment_container, journalFragment)
                .commit();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        DateTime begMonth = DateTime.now().withDayOfMonth(1);
        DateTime endMonth = begMonth.plusMonths(1);
        journalFragment.filterTripByDate(new Interval(begMonth, endMonth));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private OnMonthChangedListener monthListener = (widget, date) -> {
        DateTime monthBegin = new DateTime(date.getDate())
                .withDayOfMonth(1).withTimeAtStartOfDay();
        DateTime monthEnd = monthBegin.plusMonths(1);
        Interval selection = new Interval(monthBegin, monthEnd);
        journalFragment.filterTripByDate(selection);
    };

}
