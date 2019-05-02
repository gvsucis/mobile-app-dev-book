package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.storage.FirebaseStorage;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.gvsu.cis.traxy.model.JournalEntry;

public class MediaViewActivity extends AppCompatActivity {

    @BindView(R.id.photoView)
    ImageView photoView;

    @BindView(R.id.videoView)
    SimpleExoPlayerView videoView;

    private JournalEntry entry;
    private FirebaseImageLoader imgLoader;
    private FirebaseStorage storage;
    private SimpleExoPlayer player;

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
            switch (entry.getType()) {
                case 1:
                    break;
                case 2:
                    videoView.setVisibility(View.GONE);
                    break;
                case 3:
                case 4:
                    photoView.setVisibility(View.GONE);
                    initExoPlayer();
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String url = null;
        if (entry.getType() == 2) // Photo
            url = entry.getUrl();
        else if (entry.getType() == 4) { // Video
            url = entry.getThumbnailUrl();
        }
        if (url != null) {
            GlideApp.with(this)
//                    .using(imgLoader)
                    .load(Uri.parse(url))
//                    .centerCrop()
                    .into(photoView);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void initExoPlayer() {
        DefaultBandwidthMeter bwMeter = new DefaultBandwidthMeter();
        AdaptiveTrackSelection.Factory trackFactory = new AdaptiveTrackSelection.Factory(bwMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        videoView.setPlayer(player);

        DataSource.Factory dsFactory = new DefaultDataSourceFactory(getBaseContext(),
                Util.getUserAgent(this, "Traxy"), bwMeter);
        ExtractorsFactory exFactory = new DefaultExtractorsFactory();
        Uri mediaUri = Uri.parse(entry.getUrl());
        MediaSource videoSource = new ExtractorMediaSource(mediaUri,
                dsFactory, exFactory, null, null);
        player.prepare(videoSource);
    }
}
