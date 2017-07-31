package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class JournalViewActivity extends AppCompatActivity {

    private final static int CAPTURE_PHOTO_REQUEST = 678;

    @BindView(R.id.journal_name) TextView title;
    @BindView(R.id.my_photo) ImageView photoView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private String tripKey;
    private DatabaseReference entriesRef;

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

    @OnClick(R.id.fab_add_photo)
    public void do_add_photo()
    {
        Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (capture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(capture, CAPTURE_PHOTO_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_PHOTO_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                Bitmap thumbnail = (Bitmap) data.getParcelableExtra
                        ("data");
                photoView.setImageBitmap(thumbnail);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
