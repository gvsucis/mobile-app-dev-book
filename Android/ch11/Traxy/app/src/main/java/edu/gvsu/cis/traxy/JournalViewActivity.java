package edu.gvsu.cis.traxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.core.view.Event;
import com.google.firebase.storage.FirebaseStorage;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.gvsu.cis.traxy.model.JournalEntry;
import edu.gvsu.cis.traxy.model.Trip;
import edu.gvsu.cis.traxy.webservice.DarkSkyServices;
import edu.gvsu.cis.traxy.webservice.DarkSkyWeather;
import edu.gvsu.cis.traxy.webservice.WeatherData;
import edu.gvsu.cis.traxy.webservice.WeatherService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.firebase.ui.common.ChangeEventType.ADDED;

public class JournalViewActivity extends AppCompatActivity {
    private static final String TAG = "JournalViewActivity";

    private static final int CAPTURE_PHOTO_REQUEST = 678;
    private static final int CAPTURE_VIDEO_REQUEST = 679;
    private static final int RECORD_AUDIO_REQUEST = 680;
    private static final int SELECT_ALBUM = 681;
    private static final int ITEM_VERTICAL_GAP = 32 ;

    @BindView(R.id.journal_name) TextView title;
    @BindView(R.id.journal_entries) RecyclerView entries;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private String tripKey;
    private Query entriesRef;
    private Uri mediaUri;
    private SectionedFirebaseRecyclerAdapter<JournalEntry, EntryHolder,SectionHolder>
            adapter;
    private FirebaseStorage storage;
    private DateTimeFormatter dateFormat = DateTimeFormat.forPattern
            ("yyyyMMdd");
    private Map<String,Double> dayToTemp;
    private Map<String,String> dayToIcon;
    private List<String> seenDates;
    private DarkSkyServices darkSkyClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_view);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        entries.setLayoutManager(new LinearLayoutManager(this));
        Retrofit retro = new Retrofit.Builder()
                .baseUrl("https://api.darksky.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        darkSkyClient = retro.create(DarkSkyServices.class);
        Intent incoming = getIntent();
        if (incoming.hasExtra("TRIP")) {
            Parcelable par = incoming.getParcelableExtra("TRIP");
            Trip t = Parcels.unwrap(par);
            tripKey = t.getKey();
            title.setText(t.getName());
            FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            // TODO: the orderByChild method call is causing probs downstream!
            entriesRef = dbRef.getReference(user.getUid())
                    .child(tripKey + "/entries").orderByChild("date");
            storage = FirebaseStorage.getInstance();
            FirebaseRecyclerOptions<JournalEntry> options;
            options = new FirebaseRecyclerOptions.Builder<JournalEntry>()
                    .setQuery(entriesRef, JournalEntry.class).build();
            adapter = new MyAdapter(options);
            entries.setAdapter(adapter);
            entries.addItemDecoration(verticalGap);
        }
    }

// These two methods are not needed when we use Retrofit
//    @Override
//    protected void onResume() {
//        super.onResume();
//        IntentFilter weatherFilter;
//        weatherFilter = new IntentFilter(WeatherService.BROADCAST_WEATHER);
//        LocalBroadcastManager.getInstance(this).registerReceiver
//                (weatherReceiver, weatherFilter);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(weatherReceiver);
//    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private RecyclerView.ItemDecoration verticalGap = new RecyclerView.ItemDecoration() {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = ITEM_VERTICAL_GAP;
        }
    };

    private File createFileName(String prefix, String ext) throws
            IOException {
        DateTime now = DateTime.now();
        DateTimeFormatter fmt = DateTimeFormat.forPattern
                ("yyyyMMdd-HHmmss");
        File cacheDir = getExternalCacheDir();
        File media = File.createTempFile(prefix + "-" + fmt.print(now),
                ext, cacheDir);
        return media;
    }

    private void toMediaView (JournalEntry model) {
        Intent toView = new Intent(this, MediaViewActivity.class);
        Parcelable parcel = Parcels.wrap(model);
        toView.putExtra ("JRNL_ENTRY", parcel);
        startActivity (toView);
    }

    private void toMediaEdit (JournalEntry model, String key) {
        Intent toEdit = new Intent(this, JournalEditActivity.class);
        Parcelable parcel = Parcels.wrap(model);
        toEdit.putExtra ("JRNL_ENTRY", parcel);
        toEdit.putExtra ("JRNL_KEY", key);
        toEdit.putExtra ("DB_REF", entriesRef.getRef().getParent()
                .toString());
        startActivity (toEdit);
    }

    @OnClick(R.id.fab_add_photo)
    public void do_add_photo()
    {
        Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (capture.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createFileName("traxypic", ".jpg");
                mediaUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider", photoFile);
                capture.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
                startActivityForResult(capture, CAPTURE_PHOTO_REQUEST);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.fab_add_video)
    public void do_add_video()
    {
        Intent capture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (capture.resolveActivity(getPackageManager()) != null) {
            try {
                File videoFile = createFileName("traxyvid", ".mp4");
                mediaUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider", videoFile);
                capture.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
                startActivityForResult(capture, CAPTURE_VIDEO_REQUEST);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.fab_add_audio)
    public void do_add_audio() {
        Intent toAudio = new Intent(this, AudioActivity.class);
        try {
            File audioFile = createFileName("traxyau",
                    ".m4a");
            mediaUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", audioFile);
            toAudio.putExtra("AUDIO_PATH", audioFile.getAbsolutePath());
            startActivityForResult(toAudio, RECORD_AUDIO_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.fab_select_album)
    public void do_add_photo_from_album() {
        Intent toAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        toAlbum.setType("image/*");
        startActivityForResult(toAlbum, SELECT_ALBUM);
    }

    @OnClick(R.id.fab_add_text)
    public void do_add_textentry() {
        Intent toDetails = new Intent(this, MediaDetailsActivity.class);
        toDetails.putExtra("FIREBASE_REF", entriesRef.getRef().toString());
        startActivity(toDetails);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent showDetails = new Intent(this, MediaDetailsActivity.class);
        showDetails.putExtra("FIREBASE_REF", entriesRef.getRef().toString());
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_PHOTO_REQUEST:
                    showDetails.putExtra("PHOTO_URI", mediaUri);
                    break;
                case CAPTURE_VIDEO_REQUEST:
                    showDetails.putExtra("VIDEO_URI", mediaUri);
                    break;
                case RECORD_AUDIO_REQUEST:
                    showDetails.putExtra("AUDIO_URI",mediaUri);
                    break;
                case SELECT_ALBUM:
                    showDetails.putExtra("PHOTO_URI", data.getData());
            }
            startActivity(showDetails);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private class MyAdapter extends
            SectionedFirebaseRecyclerAdapter<JournalEntry,EntryHolder,
                    SectionHolder> {
        private Map<String,List<JournalEntry>> entryMap;

        public MyAdapter(FirebaseRecyclerOptions<JournalEntry> options) {
            super(R.layout.journal_entry_item,
                    EntryHolder.class,
                    R.layout.journal_entry_header,
                    SectionHolder.class,
                    options);
            entryMap = new TreeMap<>();
            dayToTemp = new TreeMap<>();
            dayToIcon = new TreeMap<>();
            seenDates = new ArrayList<>();
        }

        @Override
        public void onChildChanged(ChangeEventType type, DataSnapshot snapshot, int index, int oldIndex) {
            super.onChildChanged(type, snapshot, index, oldIndex);
            if (type != ADDED) return;
            JournalEntry model = snapshot.getValue(JournalEntry.class);
            DateTime entryDate = DateTime.parse(model.getDate());
            String timeStr = dateFormat.print(entryDate);
            // Group by date, is it any date?
            List<JournalEntry> entryList = entryMap.get(timeStr);
            if (entryList == null) { /* new date */
                entryList = new ArrayList<>();
                entryMap.put(timeStr, entryList);
            }
            entryList.add(model);
        }

        @Override
        public void startListening() {
            super.startListening();
            if (entryMap != null)
                entryMap.clear();
        }

        @Override
        protected void populateItemViewHolder(EntryHolder viewHolder,
                                              JournalEntry __unused__,
                                              int section, int secPos,
                                              int listPos) {
            String thisKey = (String) entryMap.keySet().toArray()[section];
            JournalEntry model = entryMap.get(thisKey).get(secPos);
            if (!seenDates.contains(thisKey)) {
                seenDates.add(thisKey);
                DateTime entryDate = DateTime.parse(model.getDate());
                final DateTime midDay = entryDate.withTimeAtStartOfDay().plusHours(12);
                /* fetch weather data if midDay is a new insert */
                fetchWeatherForDate(model.getLat(), model.getLng(),
                        midDay);
            }
            viewHolder.setCaption(model.getCaption() + "(" + model.getType() + ")");
            viewHolder.setDate(model.getDate());

            switch (model.getType()) {
                case 1:
                    viewHolder.mediaContainer.setVisibility(View
                            .GONE);
                    break;
                case 2: // photo
                    viewHolder.topImage.setVisibility(View.VISIBLE);
                    viewHolder.playIcon.setVisibility(View.GONE);
                    GlideApp.with(viewHolder.topImage.getContext())
//                            .using(imgLoader)
                            .load(storage.getReferenceFromUrl(model.getUrl()))
                            .into(viewHolder.topImage);
                    break;
                case 3: // audio
                    viewHolder.topImage.setVisibility(View.VISIBLE);
                    viewHolder.playIcon.setVisibility(View.VISIBLE);
                    viewHolder.topImage.setImageResource(0);
                    break;
                case 4: // video
                    viewHolder.topImage.setVisibility(View.VISIBLE);
                    viewHolder.playIcon.setVisibility(View.VISIBLE);
                    GlideApp.with(viewHolder.topImage.getContext())
//                            .using(imgLoader)
                            .load(storage.getReferenceFromUrl
                                    (model.getThumbnailUrl()))
                            .into(viewHolder.topImage);
                    break;
                default:
                    viewHolder.topImage.setVisibility(View.GONE);
                    break;
            }
            viewHolder.editBtn.setOnClickListener( view -> {
                String key = getRef(listPos).getKey();
                toMediaEdit(model, key);
            });
            viewHolder.topImage.setOnClickListener( view -> {
                toMediaView(model);
            });
            viewHolder.playIcon.setOnClickListener( view -> {
                toMediaView(model);
            });
        }

        @Override
        void populateHeaderViewHolder(SectionHolder viewHolder, int
                section) {
            String key = (String) entryMap.keySet().toArray()[section];
            String date = key.substring(0,4) + "-"
                    + key.substring(4,6) + "-"
                    + key.substring(6);

            Double temp = dayToTemp.get(key);
            String icon = dayToIcon.get(key);
            if (temp == null)
                if (date != null)
                    viewHolder.headerText.setText("Fetching temperature for " +
                        date);
                else
                    viewHolder.headerText.setText("No date");
            else {
                viewHolder.headerText.setText(date);
                if (temp != null && icon != null) {
                    viewHolder.temperature.setText(String.format("%.0f\u2109", temp));
                    switch (icon) {
                        case "clear-day":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_clear_day));
                            break;
                        case "clear-night":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_clear_night));
                            break;
                        case "rain":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_rain));
                            break;
                        case "snow":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_snow));
                            break;
                        case "sleet":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_sleet));
                            break;
                        case "wind":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_wind));
                            break;
                        case "fog":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_fog));
                            break;
                        case "cloudy":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_cloudy));
                            break;
                        case "partly-cloudy-day":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_partly_cloudy_day));
                            break;
                        case "partly-cloudy-night":
                            viewHolder.icon.setImageDrawable(getResources()
                                    .getDrawable(R.drawable.ic_darksky_partly_cloudy_night));
                            break;
                    }
                }
            }
        }

        @Override
        int getSectionCount() {
            return entryMap.size();
        }

        @Override
        int getItemCountForSection(int section) {
            String dateStr = (String) entryMap.keySet().toArray()[section];
            return entryMap.get(dateStr).size();
        }
    }

    private void fetchWeatherForDate(double lat, double lng, DateTime when) {
//        Intent wsRequest = new Intent(this, WeatherService.class);
//        wsRequest.putExtra(WeatherService.EXTRA_KEY, dateFormat.print(when));
//        wsRequest.putExtra(WeatherService.EXTRA_LAT, lat);
//        wsRequest.putExtra(WeatherService.EXTRA_LNG, lng);
//        wsRequest.putExtra(WeatherService.EXTRA_TIME, when.getMillis() / 1000);
//        startService(wsRequest);
        darkSkyClient.fetchWeatherForDate(lat, lng, when.getMillis()/1000)
                .enqueue(new Callback<DarkSkyWeather>() {
                    @Override
                    public void onResponse(Call<DarkSkyWeather> call, Response<DarkSkyWeather> response) {
                        if (response.isSuccessful()) {
                            String key;
                            synchronized (dayToTemp) {
                                key = dateFormat.print(when);
                                DarkSkyWeather weather = response.body();
                                WeatherData data = weather.currently;
                                dayToTemp.put(key, data.temperature);
                                dayToIcon.put(key, data.icon);
                            }
                            int section = seenDates.indexOf(key);
                            int pos = adapter.positionOfSection(section);
                            adapter.notifyItemChanged(pos);
                        }
                    }

                    @Override
                    public void onFailure(Call<DarkSkyWeather> call, Throwable t) {

                    }
                });
    }

    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key;
            synchronized (dayToTemp) {
                key = intent.getStringExtra(WeatherService.EXTRA_KEY);
                double temperature = intent.getDoubleExtra(WeatherService
                        .EXTRA_TEMP, 0.0);
                String icon = intent.getStringExtra(WeatherService.EXTRA_ICON);
                dayToTemp.put(key, temperature);
                dayToIcon.put(key, icon);
            }
            int section = seenDates.indexOf(key);
            int pos = adapter.positionOfSection(section);
            adapter.notifyItemChanged(pos);
        }
    };
}
