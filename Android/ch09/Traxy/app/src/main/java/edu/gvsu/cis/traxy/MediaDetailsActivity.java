package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaDetailsActivity extends AppCompatActivity {

    @BindView(R.id.my_photo) ImageView photoView;

    private DatabaseReference entriesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_details);
        ButterKnife.bind(this);

        Intent incoming = getIntent();
        if (incoming.hasExtra("FIREBASE_REF")) {
            String fbUrl = incoming.getStringExtra("FIREBASE_REF");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            entriesRef = db.getReferenceFromUrl(fbUrl);
        }
        if (incoming.hasExtra("PHOTO_URI")) {
            try {
                Uri dataUri = incoming.getParcelableExtra("PHOTO_URI");
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
}
