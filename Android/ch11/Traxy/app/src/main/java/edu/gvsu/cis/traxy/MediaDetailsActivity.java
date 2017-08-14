package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.gvsu.cis.traxy.model.JournalEntry;

public class MediaDetailsActivity extends AppCompatActivity {

    static final double ALLENDALE_LAT = 42.9722;
    static final double ALLENDATE_LNG = -85.9540;

    @BindView(R.id.journal_entry_photo) ImageView photoView;
    @BindView(R.id.journal_entry_video) VideoView videoView;
    @BindView(R.id.journal_entry_caption) TextView entry_caption;
    @BindView(R.id.journal_entry_date) TextView entry_date;
    @BindView(R.id.journal_entry_time) TextView entry_location;
    @BindView(R.id.audioLabel) TextView audioLabel;
    @BindView(R.id.frameLayout) FrameLayout topContainer;

    ImageButton playIt;

    private DatabaseReference entriesRef;
    private Uri dataUri;
    private StorageReference storageRef;
    private int mediaType = 0;

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
            mediaType = 2;
            try {
                dataUri = incoming.getParcelableExtra("PHOTO_URI");
                InputStream istr = getContentResolver().openInputStream
                        (dataUri);
                Bitmap bmp = BitmapFactory.decodeStream(istr);
                photoView.setImageBitmap(bmp);
                audioLabel.setVisibility(View.GONE);
                photoView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                istr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (incoming.hasExtra("AUDIO_URI")) {
            mediaType = 3;
            dataUri = incoming.getParcelableExtra("AUDIO_URI");
            videoView.setVideoURI(dataUri);
            MediaController mc = new MediaController(this);
            videoView.setMediaController(mc);
            photoView.setVisibility(View.GONE);
            audioLabel.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.VISIBLE);
        }
        else if (incoming.hasExtra("VIDEO_URI")) {
            mediaType = 4;
            dataUri = incoming.getParcelableExtra("VIDEO_URI");
            videoView.setVideoURI(dataUri);
            MediaController mc = new MediaController(this);
            videoView.setMediaController(mc);
            audioLabel.setVisibility(View.GONE);
            photoView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
        } else {
            /* a text only entry */
            mediaType = 1;
            topContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.media_details, menu);
        return true;
    }

    private void uploadMedia (int type, String contentType, String
            topDir) {
        DateTime now = DateTime.now();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        JournalEntry currentEntry = new JournalEntry();
        currentEntry.setCaption(entry_caption.getText().toString());
        currentEntry.setType(type);
        currentEntry.setLat(ALLENDALE_LAT);
        currentEntry.setLng(ALLENDATE_LNG);
        currentEntry.setDate(fmt.print(now));

        DatabaseReference savedEntry = entriesRef.push();
        savedEntry.setValue(currentEntry);

        StorageMetadata.Builder metaBuilder = new StorageMetadata
                .Builder();
        StorageMetadata meta;
        String mediaName = dataUri.getLastPathSegment();
        meta = metaBuilder.setContentType(contentType).build();
        UploadTask task = storageRef.child(topDir + "/" + mediaName)
                .putFile(dataUri, meta);
        task.addOnSuccessListener(snapshot -> {
            @SuppressWarnings("VisibleForTests")
            Uri uri = snapshot.getDownloadUrl();
            savedEntry.child("url").setValue(uri.toString());
            Snackbar.make(entry_caption,
                    "Your media is uploaded to " + uri.toString(),
                    Snackbar.LENGTH_LONG).show();
        });
        if (mediaType == 4) { // is it a video?
            // Create a thumbnail image
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, dataUri);
            Bitmap thumb = mmr.getFrameAtTime();
            mmr.release();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            String thumbName = mediaName.replace(".mp4", "-thumb.jpg");
            StorageMetadata.Builder thumbMetaBuilder = new
                    StorageMetadata.Builder();
            StorageMetadata thumbMeta = thumbMetaBuilder
                    .setContentDisposition("image/jpeg").build();
            UploadTask thumbTask = storageRef.child("photos/" + thumbName)
                    .putBytes(baos.toByteArray(), thumbMeta);
            thumbTask.addOnSuccessListener(snapshot -> {
                @SuppressWarnings("VisibleForTests")
                Uri uri = snapshot.getDownloadUrl();
                savedEntry.child("thumbnailUrl").setValue(uri.toString());
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_media) {
            switch (mediaType) {
                case 1: // text
                    DateTime now = DateTime.now();
                    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
                    JournalEntry currentEntry = new JournalEntry();
                    currentEntry.setCaption(entry_caption.getText().toString());
                    currentEntry.setType(mediaType);
                    currentEntry.setLat(ALLENDALE_LAT);
                    currentEntry.setLng(ALLENDATE_LNG);
                    currentEntry.setDate(fmt.print(now));

                    DatabaseReference savedEntry = entriesRef.push();
                    savedEntry.setValue(currentEntry);
                    Snackbar.make(entry_caption,
                            "Your entry is saved",
                            Snackbar.LENGTH_LONG).show();
                    break;
                case 2: // photo
                    uploadMedia(mediaType, "image/jpeg", "photos");
                    break;
                case 3: // audio
                    uploadMedia(mediaType, "audio/m4a", "audio");

                    break;
                case 4: // video
                    uploadMedia(mediaType, "video/mp4", "videos");
                    break;
            }
            return true;
        }
        return false;
    }
}
