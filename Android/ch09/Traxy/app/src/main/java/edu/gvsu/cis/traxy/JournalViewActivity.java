package edu.gvsu.cis.traxy;

import android.content.Intent;
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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.gvsu.cis.traxy.model.JournalEntry;

public class JournalViewActivity extends AppCompatActivity {

    private final static int CAPTURE_PHOTO_REQUEST = 678;
    private final static int CAPTURE_VIDEO_REQUEST = 679;

    @BindView(R.id.journal_name) TextView title;
    @BindView(R.id.journal_entries) RecyclerView entries;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private String tripKey;
    private DatabaseReference entriesRef;
    private Uri mediaUri;
    private FirebaseRecyclerAdapter<JournalEntry, EntryHolder> adapter;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_view);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        entries.setLayoutManager(new LinearLayoutManager(this));
        FirebaseImageLoader imgLoader = new FirebaseImageLoader();
        Intent incoming = getIntent();
        if (incoming.hasExtra("TRIP")) {
            Parcelable par = incoming.getParcelableExtra("TRIP");
            Trip t = Parcels.unwrap(par);
            tripKey = t.getKey();
            title.setText(t.getName());
            FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            entriesRef = dbRef.getReference(user.getUid())
                    .child(tripKey + "/entries");
            storage = FirebaseStorage.getInstance();
            adapter = new FirebaseRecyclerAdapter<JournalEntry, EntryHolder>
                    (JournalEntry.class, R.layout.journal_entry_item,
                            EntryHolder.class, entriesRef) {

                @Override
                protected void populateViewHolder(EntryHolder viewHolder, JournalEntry model, int position) {
                    viewHolder.setCaption(model.getCaption());
                    viewHolder.setDate(model.getDate());

                    switch (model.getType()) {
                        case 2: // photo
                            viewHolder.topImage.setVisibility(View.VISIBLE);
                            viewHolder.playIcon.setVisibility(View.GONE);
                            Glide.with(viewHolder.topImage.getContext())
                                    .using(imgLoader)
                                    .load(storage.getReferenceFromUrl(model.getUrl()))
                                    .into(viewHolder.topImage);
                            break;
                        case 4: // video
                            viewHolder.topImage.setVisibility(View.VISIBLE);
                            viewHolder.playIcon.setVisibility(View.VISIBLE);
                            Glide.with(viewHolder.topImage.getContext())
                                    .using(imgLoader)
                                    .load(storage.getReferenceFromUrl
                                            (model.getThumbnailUrl()))
                                    .into(viewHolder.topImage);
                            break;
                        default:
                            viewHolder.topImage.setVisibility(View.GONE);
                            break;
                    }
                    viewHolder.editBtn.setOnClickListener( view -> {
                        String key = getRef(position).getKey();
                        toMediaEdit(model, key);
                    });
                    viewHolder.topImage.setOnClickListener( view -> {
                        toMediaView(model);
                    });
                }

            };
            entries.setAdapter(adapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

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
        toEdit.putExtra ("DB_REF", entriesRef.getParent().toString());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent showDetails = new Intent(this, MediaDetailsActivity.class);
        showDetails.putExtra("FIREBASE_REF", entriesRef.toString());
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case CAPTURE_PHOTO_REQUEST:
                    showDetails.putExtra("PHOTO_URI", mediaUri);
                    break;
                case CAPTURE_VIDEO_REQUEST:
                    showDetails.putExtra("VIDEO_URI", mediaUri);
                    break;
            }
            startActivity(showDetails);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
