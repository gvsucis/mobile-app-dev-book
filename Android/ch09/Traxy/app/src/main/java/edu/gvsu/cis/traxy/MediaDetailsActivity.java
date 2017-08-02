package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.gvsu.cis.traxy.model.JournalEntry;

public class MediaDetailsActivity extends AppCompatActivity {

    static final double ALLENDALE_LAT = 42.9722;
    static final double ALLENDATE_LNG = -85.9540;

    @BindView(R.id.journal_entry_photo) ImageView photoView;
    @BindView(R.id.journal_entry_caption) TextView entry_caption;
    @BindView(R.id.journal_entry_date) TextView entry_date;
    @BindView(R.id.journal_entry_time) TextView entry_location;
    private DatabaseReference entriesRef;
    private Uri dataUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_details);
        ButterKnife.bind(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        storageRef = FirebaseStorage.getInstance().getReference().child
                (uid);
        Intent incoming = getIntent();
        if (incoming.hasExtra("FIREBASE_REF")) {
            String fbUrl = incoming.getStringExtra("FIREBASE_REF");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            entriesRef = db.getReferenceFromUrl(fbUrl);
        }
        if (incoming.hasExtra("PHOTO_URI")) {
            try {
                dataUri = incoming.getParcelableExtra("PHOTO_URI");
                InputStream istr = getContentResolver().openInputStream
                        (dataUri);
                Bitmap bmp = BitmapFactory.decodeStream(istr);
                photoView.setImageBitmap(bmp);
                istr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.media_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_media) {
            DateTime now = DateTime.now();
            DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
            JournalEntry currentEntry = new JournalEntry();
            currentEntry.setCaption(entry_caption.getText().toString());
            currentEntry.setType(2); /* photo */
            currentEntry.setLat(ALLENDALE_LAT);
            currentEntry.setLng(ALLENDATE_LNG);
            currentEntry.setDate(fmt.print(now));
            
            DatabaseReference savedEntry = entriesRef.push();
            savedEntry.setValue(currentEntry);

            StorageMetadata.Builder metaBuilder = new StorageMetadata
                    .Builder();
            StorageMetadata meta;
            String mediaName = dataUri.getLastPathSegment();
            meta = metaBuilder.setContentType("image/jpeg").build();
            UploadTask task = storageRef.child("photos/" + mediaName)
                    .putFile(dataUri, meta);
            task.addOnSuccessListener(snapshot -> {
                @SuppressWarnings("VisibleForTests")
                Uri uri = snapshot.getDownloadUrl();
                savedEntry.child("url").setValue(uri.toString());
                Snackbar.make(photoView,
                        "Your photo is saved to " + uri.toString(),
                        Toast.LENGTH_LONG).show();
            });
            return true;
        }
        return false;
    }
}
