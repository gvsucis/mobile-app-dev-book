package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.gvsu.cis.traxy.model.JournalEntry;

public class MediaViewActivity extends AppCompatActivity {

    @BindView(R.id.photoView)
    ImageView photoView;

    private JournalEntry entry;
    private FirebaseImageLoader imgLoader;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);
        ButterKnife.bind(this);

        imgLoader = new FirebaseImageLoader();
        storage = FirebaseStorage.getInstance();
        Intent incoming = getIntent();
        if (incoming.hasExtra("JRNL_ENTRY")) {
            Parcelable parcel = incoming.getParcelableExtra("JRNL_ENTRY");
            entry = Parcels.unwrap(parcel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String url = null;
        if (entry.getType() == 2) // Photo
            url = entry.getUrl();
        else if (entry.getType() == 4) // Video
            url = entry.getThumbnailUrl();
        if (url != null) {
            Glide.with(this)
                    .using(imgLoader)
                    .load(storage.getReferenceFromUrl(url))
                    .centerCrop()
                    .into(photoView);
        }
    }
}
