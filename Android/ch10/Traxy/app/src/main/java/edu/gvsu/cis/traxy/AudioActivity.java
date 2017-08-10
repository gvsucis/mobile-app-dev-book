package edu.gvsu.cis.traxy;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AudioActivity extends AppCompatActivity {
    private static final int AUDIO_PERMISSION_REQUEST = 681;
    private static final String[] permArr = {Manifest.permission
            .RECORD_AUDIO};
    private enum Status {START, RECORDING, RECORD_STOP, PLAYING,
        PLAY_PAUSE};

    @BindView(R.id.leftBtn)
    FloatingActionButton resetBtn;

    @BindView(R.id.centerBtn)
    FloatingActionButton recordBtn;

    @BindView(R.id.rightBtn)
    FloatingActionButton playBtn;

    @BindView(R.id.status)
    TextView status;

    private MediaRecorder audioRec;

    private String audioFilePath;
    private Status currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        ButterKnife.bind(this);
        currentStatus = Status.START;
        Intent incoming = getIntent();
        if (incoming.hasExtra("AUDIO_PATH")) {
            audioFilePath = incoming.getStringExtra("AUDIO_PATH");
//            audioFilePath = dataUri.getPath();
            recordBtn.setEnabled(true);
        } else {
            recordBtn.setEnabled(false);
        }
        int currentPerm = ActivityCompat.checkSelfPermission(this,
                permArr[0]);
        if (currentPerm == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, permArr, AUDIO_PERMISSION_REQUEST);
        }
    }

    private void startRecording() {
        audioRec = new MediaRecorder();
        audioRec.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRec.setOutputFile(audioFilePath);
        audioRec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        audioRec.setAudioSamplingRate(256);
        try {
            audioRec.prepare();
            audioRec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        audioRec.stop();
        audioRec.release();
        audioRec = null;
    }
    
    @OnClick(R.id.centerBtn)
    public void doCenterButton() {
        switch (currentStatus) {
            case START:
                startRecording();
                currentStatus = Status.RECORDING;
                recordBtn.setImageResource(R.drawable.ic_stop_black_24dp);
                status.setText("Recording");
                break;
            case RECORDING:
                stopRecording();
                currentStatus = Status.RECORD_STOP;
                status.setText("Audio Recorded");
                recordBtn.setImageResource(R.drawable.ic_done_black_24dp);
                resetBtn.setVisibility(View.VISIBLE);
                playBtn.setVisibility(View.VISIBLE);
                break;
            case RECORD_STOP:
            case PLAY_PAUSE:
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == AUDIO_PERMISSION_REQUEST && permissions[0]
                .equals(android.Manifest.permission.RECORD_AUDIO)) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    boolean showAgain =
                            shouldShowRequestPermissionRationale
                                    (android.Manifest.permission.RECORD_AUDIO);
                    if (showAgain) {

                    }
                }
                finish(); // can't use audio
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
