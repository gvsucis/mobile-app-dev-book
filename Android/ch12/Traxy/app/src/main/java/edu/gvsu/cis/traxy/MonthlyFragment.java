package edu.gvsu.cis.traxy;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.gvsu.cis.traxy.model.Trip;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthlyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthlyFragment extends Fragment implements JournalFragment.JournalDataListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    @BindView(R.id.monthly_calendar)
    MaterialCalendarView monthly;

    private JournalFragment journalFragment;
    private Set<LocalDate> tripDays;

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
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monthly, container,
                false);
        ButterKnife.bind(this, view);
        monthly.setOnMonthChangedListener(monthListener);
        monthly.addDecorator(decor);
        FragmentManager fm = getChildFragmentManager();
        journalFragment = JournalFragment.newInstance(R.layout
                .journal_card_mini);
        fm.beginTransaction()
                .add(R.id.fragment_container, journalFragment)
                .commit();
        tripDays = new TreeSet<>();
        journalFragment.setJournalDataListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        DateTime begMonth = DateTime.now().withDayOfMonth(1);
        DateTime endMonth = begMonth.plusMonths(1);
        journalFragment.filterTripByDate(new Interval(begMonth, endMonth));
    }

    private OnMonthChangedListener monthListener = (widget, date) -> {
        DateTime monthBegin = new DateTime(date.getDate())
                .withDayOfMonth(1).withTimeAtStartOfDay();
        DateTime monthEnd = monthBegin.plusMonths(1);
        Interval selection = new Interval(monthBegin, monthEnd);
        journalFragment.filterTripByDate(selection);
        monthly.invalidateDecorators();
    };

    private DayViewDecorator decor = new DayViewDecorator() {

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            LocalDate testDate = LocalDate.fromDateFields(day.getDate());
            if (tripDays.contains(testDate))
                return true;
            else
                return false;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(10, Color.RED));
        }
    };

    @Override
    public void onJournalUpdated(List<Trip> trips) {
        tripDays.clear();
        for (Trip t : trips) {
            LocalDate d = DateTime.parse(t.getStartDate()).toLocalDate();
            LocalDate endDate = DateTime.parse(t.getEndDate()).toLocalDate();
            while (!d.isAfter(endDate)) {
                tripDays.add(d);
                d = d.plusDays(1);
            }
        }
        monthly.invalidateDecorators();
    }
}
