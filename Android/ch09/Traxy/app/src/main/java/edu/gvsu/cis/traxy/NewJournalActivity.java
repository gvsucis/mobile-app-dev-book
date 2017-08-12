package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.parceler.Parcels;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.gvsu.cis.traxy.model.Trip;

public class NewJournalActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "NewJournalActivity";

    @BindView(R.id.journal_name) EditText jname;
    @BindView(R.id.location) TextView location;
    @BindView(R.id.start_date) TextView startDateView;
    @BindView(R.id.end_date) TextView endDateView;

    private DateTime startDate, endDate;
    private DatePickerDialog dpDialog;
    private Trip currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_journal);
        ButterKnife.bind(this);

        currentTrip = new Trip();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DateTime today = DateTime.now();
        dpDialog = DatePickerDialog.newInstance(this,
                today.getYear(), today.getMonthOfYear() - 1, today.getDayOfMonth());


        startDateView.setText(formatted(today));
        endDateView.setText(formatted(today.plusDays(1)));
        startDate = today;
        endDate = today.plusDays(1);
    }

    @OnClick(R.id.location)
    public void locationPressed() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.start_date, R.id.end_date})
    public void datePressed() {
        dpDialog.show(getFragmentManager(), "daterangedialog");
    }

    @OnClick(R.id.fab)
    public void FABPressed() {
        Intent result = new Intent();
        currentTrip.setName(jname.getText().toString());
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        currentTrip.setStartDate(fmt.print(startDate));
        currentTrip.setEndDate(fmt.print(endDate));
        // add more code to initialize the rest of the fields
        Parcelable parcel = Parcels.wrap(currentTrip);
        result.putExtra("TRIP", parcel);
        setResult(RESULT_OK, result);
        finish();
    }

    private String formatted(DateTime d) {
        return d.monthOfYear().getAsShortText(Locale.getDefault()) + " " +
                d.getDayOfMonth() + ", " + d.getYear();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place pl = PlaceAutocomplete.getPlace(this, data);
                location.setText(pl.getName());
                currentTrip.setLocation(pl.getName().toString());
                currentTrip.setLat(pl.getLatLng().latitude);
                currentTrip.setLng(pl.getLatLng().longitude);
                currentTrip.setPlaceId(pl.getId());

                Log.i(TAG, "onActivityResult: " + pl.getName() + "/" + pl.getAddress());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status stat = PlaceAutocomplete.getStatus(this, data);
                Log.d(TAG, "onActivityResult: ");
            }
            else if (requestCode == RESULT_CANCELED){
                System.out.println("Cancelled by the user");
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        startDate = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
        endDate = new DateTime(yearEnd, monthOfYearEnd + 1, dayOfMonthEnd, 0, 0);
        startDateView.setText(formatted(startDate));
        endDateView.setText(formatted(endDate));
    }
}
