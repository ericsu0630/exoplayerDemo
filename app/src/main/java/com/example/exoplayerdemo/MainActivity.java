package com.example.exoplayerdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

public class MainActivity extends AppCompatActivity {
    PlayerView playerView;
    SimpleExoPlayer player;
    ProgressBar progressBar;
    ImageView fullscreenButton;
    ImageView playbackSpeedButton;
    TextView speedText;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private ScaleGestureDetector gestureDetector;
    private float mScaleFactor = 1.0f;
    private boolean isFullScreen = false;
    private float speed = 1f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progress_bar);
        fullscreenButton = findViewById(R.id.button_fullscreen);
        playbackSpeedButton = findViewById(R.id.button_playback_speed);
        speedText = findViewById(R.id.text_playback_speed);
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        gestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                mScaleFactor *= scaleGestureDetector.getScaleFactor();
                mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
                playerView.setScaleX(mScaleFactor);
                playerView.setScaleY(mScaleFactor);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                Log.i("INFO","Pinch detected!");
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

            }
        });
    }

    private HlsMediaSource buildMediaSource(MediaItem mediaItem){
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayerdemo");
        return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
    }

    public void initializePlayer(){
        Uri uri = Uri.parse("https://ileaning.blueeyes.tv/hls/ed7405de-1b22-7a34-9579-c813d3c6d969_1080p_1.m3u8");
        MediaItem mediaItem = MediaItem.fromUri(uri);
        MediaSource mediaSource = buildMediaSource(mediaItem);
        player.setMediaSource(mediaSource);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare();
        player.setPlayWhenReady(playWhenReady);
        player.addListener(new Player.EventListener(){
            @Override
            public void onPlaybackStateChanged(int state) {
                if(state == Player.STATE_BUFFERING){
                    progressBar.setVisibility(View.VISIBLE);
                }else if(state == Player.STATE_READY){
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        fullscreenButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(isFullScreen){
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_fullscreen));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    isFullScreen = false;
                }else{
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_fullscreen_exit));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    isFullScreen = true;
                }
            }
        });

        playbackSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaybackParameters param;
                if(speed == 2f){
                    speedText.setText(R.string.quadSpeed);
                    speed = 4f; //set speed to 4x
                    param = new PlaybackParameters(speed);

                }else if (speed == 4f){
                    speedText.setText(R.string.normal);
                    speed = 1f; //return to normal speed
                    param = new PlaybackParameters(speed);
                }
                else{
                    speedText.setText(R.string.doubleSpeed);
                    speed = 2f; //set speed to 2x
                    param = new PlaybackParameters(speed);
                }
                player.setPlaybackParameters(param);
            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        hideSystemUi();
        initializePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if (player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        hideSystemUi();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void hideSystemUi() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                 View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }
}