package edu.gvsu.cis.traxy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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
    private Handler myHandler;
    private MediaPlayer audioPlay;
    private AudioManager audioMgr;

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

    @BindView(R.id.timeMarker)
    TextView timeMarker;

    private MediaRecorder audioRec;

    private String audioFilePath;
    private Status currentState;
    private int duration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        ButterKnife.bind(this);
        currentState = Status.START;
        myHandler = new Handler();
        audioMgr = (AudioManager) getSystemService(AUDIO_SERVICE);
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


    @Override
    protected void onPause() {
        super.onPause();
        if (audioRec != null)
            stopRecording();
        if (audioPlay != null)
            pausePlayback();
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
        switch (currentState) {
            case START:
                startRecording();
                currentState = Status.RECORDING;
                recordBtn.setImageResource(R.drawable.ic_stop_black_24dp);
                status.setText("Recording");
                myHandler.post(myRunner);
                break;
            case RECORDING:
                myHandler.removeCallbacks(myRunner);
                stopRecording();
                currentState = Status.RECORD_STOP;
                status.setText("Audio Recorded");
                duration = 0;
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

    private void initAudioPlayer() throws IOException {
        audioPlay = new MediaPlayer();
//        audioPlay.setOnPreparedListener(prepareListener);
        audioPlay.setOnCompletionListener(endAudioListener);
        audioPlay.setAudioStreamType(AudioManager.STREAM_MUSIC);
        audioPlay.setDataSource(audioFilePath);
        audioPlay.prepare();
    }

    private void startPlayback() {
        try {
            if (audioPlay == null)
                initAudioPlayer();
            int result = audioMgr.requestAudioFocus(audioFocusListener,
                    AudioManager.STREAM_MUSIC, AudioManager
                            .AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                audioPlay.start();
        } catch (IOException ioe) {
            Snackbar.make(recordBtn, "Unable to initialize Audio", Snackbar.LENGTH_LONG).show();
        }
    }

    private void pausePlayback() {
        audioPlay.pause();
    }

    @OnClick(R.id.rightBtn)
    public void doRightButton() {
        if (currentState == Status.PLAYING) {
            myHandler.removeCallbacks(myRunner);
            pausePlayback();
            currentState = Status.PLAY_PAUSE;
            playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            status.setText("Paused");
        } else {
            startPlayback();
            currentState = Status.PLAYING;
            playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
            status.setText("Playing");
            myHandler.post(myRunner);
        }
    }


    @OnClick(R.id.leftBtn)
    public void resetAll() {
        currentState = Status.START;
        myHandler.removeCallbacks(myRunner);
        status.setText("");
        resetBtn.setVisibility(View.GONE);
        recordBtn.setImageResource(R.drawable.ic_record_black_24dp);
        playBtn.setVisibility(View.GONE);
        playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        duration = 0;
        timeMarker.setText("0");
        if (audioPlay != null) {
            audioPlay.stop();
            audioPlay.release();
            audioPlay = null;
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

    private Runnable myRunner = new Runnable() {
        @Override
        public void run() {
            timeMarker.setText(duration + " secs");
            duration++;
            myHandler.postDelayed(myRunner, 1000);
        }
    };

    private MediaPlayer.OnCompletionListener endAudioListener =
            mediaPlayer -> {
                myHandler.removeCallbacks(myRunner);
                currentState = Status.PLAY_PAUSE;
                status.setText("End of audio");
                playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                duration = 0;
            };

    private boolean lostFocus, lowerVolume;

    private AudioManager.OnAudioFocusChangeListener x = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {

        }
    };

    private AudioManager.OnAudioFocusChangeListener audioFocusListener =
            focusChange -> {
              switch (focusChange) {
                  case AudioManager.AUDIOFOCUS_LOSS:
                      if (audioPlay.isPlaying()) {
                          audioPlay.pause();
                          audioPlay.stop();
                          myHandler.removeCallbacks(myRunner);
//                          lostFocus = true;
                      }
                      break;
                  case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                      if (audioPlay.isPlaying()) {
                          audioPlay.pause();
                          myHandler.removeCallbacks(myRunner);
                          lostFocus = true;
                      }
                      break;
                  case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                      if (audioPlay.isPlaying()) {
                          lowerVolume = true;
                          audioMgr.adjustStreamVolume(
                                  AudioManager.STREAM_MUSIC,
                                  AudioManager.ADJUST_LOWER, 0);
                      }
                      break;
                  case AudioManager.AUDIOFOCUS_GAIN:
                      if (audioPlay != null) {
                          if (lostFocus) {
                              lostFocus = false;
                              audioPlay.start();
                              myHandler.post(myRunner);
                          }
                          if (lowerVolume) {
                              lowerVolume = false;
                              audioMgr.adjustStreamVolume(
                                      AudioManager.STREAM_MUSIC,
                                      AudioManager.ADJUST_RAISE, 0);
                          }
                      }
                      break;
                  default:
                      System.out.println("Type of focus change " +
                              focusChange);
              }
            };
}
