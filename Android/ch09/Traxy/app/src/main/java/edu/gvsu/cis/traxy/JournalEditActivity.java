package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
//import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import org.parceler.Parcels;

import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.gvsu.cis.traxy.model.JournalEntry;

public class JournalEditActivity extends AppCompatActivity {

    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.caption) EditText caption;
    @BindView(R.id.date_time) EditText dateTime;
    @BindView(R.id.fab_cover_photo) FloatingActionButton fabCover;

//    private FirebaseImageLoader imgLoader;
    private JournalEntry entry;
    private DatabaseReference parentRef, myRef;
    private Map<String,Object> updateMap = new TreeMap<>();
    private String coverUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_edit);
        ButterKnife.bind(this);
        Intent incoming = getIntent();
//        imgLoader = new FirebaseImageLoader();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if (incoming.hasExtra("JRNL_ENTRY")) {
            Parcelable parcel = incoming.getParcelableExtra("JRNL_ENTRY");
            entry = Parcels.unwrap(parcel);
            String url = null;
            if (entry.getType() == 2) {
                url = entry.getUrl();
                fabCover.setVisibility(View.VISIBLE);
            }
            else if (entry.getType() == 4) {
                url = entry.getThumbnailUrl();
                fabCover.setVisibility(View.GONE);
            }
            if (url != null) {
                Glide.with(this)
//                        .using(imgLoader)
                        .load(Uri.parse(url))
                        .into(this.imageView);
            }
            caption.setText(entry.getCaption());
            dateTime.setText(entry.getDate());
        }
        if (incoming.hasExtra("DB_REF")) {
            String ref = incoming.getStringExtra("DB_REF");
            parentRef = FirebaseDatabase.getInstance().getReferenceFromUrl(ref);
            String entryKey = incoming.getStringExtra("JRNL_KEY");
            myRef = parentRef.child("entries").child(entryKey);
        }
    }

    @OnClick(R.id.fab_cover_photo)
    public void useCoverPhoto() {

        fabCover.setSelected( ! fabCover.isSelected());
        if (fabCover.isSelected()) {
            coverUrl = entry.getUrl();
            Snackbar.make(fabCover, "Cover photo will be updated when " +
                    "you save changes", Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(fabCover, "Cover photo unchanged", Snackbar
                    .LENGTH_LONG).show();
            coverUrl = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* We reuse the menu from other activity */
        getMenuInflater().inflate(R.menu.media_details, menu);
        return true;
    }

    private DatabaseReference.CompletionListener updateListener =
            (dbError, dbRef) -> {
                if (dbError == null) {
                    Snackbar.make(getCurrentFocus(), "Update saved",
                            Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                dbRef.setValue(entry);
                            })
                            .show();
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_media) {
            updateMap.clear();
            updateMap.put("caption", caption.getText().toString());
            updateMap.put("date", dateTime.getText().toString());
            myRef.updateChildren(updateMap, updateListener);
            if (coverUrl != null) {
                updateMap.clear();
                parentRef.child("coverPhotoUrl").setValue(coverUrl);
            }
            return true;
        } else
            return false;
    }
}
