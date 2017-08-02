package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class JournalViewActivity extends AppCompatActivity {

    private final static int CAPTURE_PHOTO_REQUEST = 678;
    private final static int CAPTURE_VIDEO_REQUEST = 679;

    @BindView(R.id.journal_name) TextView title;
    @BindView(R.id.journal_entry_photo) ImageView photoView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private String tripKey;
    private DatabaseReference entriesRef;
    private Uri mediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_view);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

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
        }

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
