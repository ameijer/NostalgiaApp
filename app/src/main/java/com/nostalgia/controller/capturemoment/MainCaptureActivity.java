package com.nostalgia.controller.capturemoment;

import android.animation.Animator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nostalgia.controller.drawer.MaterialDrawerActivity;
import com.nostalgia.runnable.ProgressHelper;
import com.nostalgia.Nostalgia;
import com.nostalgia.controller.capturemoment.review.MediaReviewerPagerActivity;
import com.nostalgia.controller.capturemoment.supportcamera.SupportCaptureFragment;

import com.nostalgia.controller.exoplayer.VideoPlayerFragment;

import com.nostalgia.view.CaptureButton;
import com.vuescape.nostalgia.R;

import java.io.File;
import java.io.IOException;

import com.nostalgia.controller.peek.picker.LocationsActivity;

public class MainCaptureActivity extends MaterialDrawerActivity implements View.OnClickListener, View.OnTouchListener, ProgressHelper.TimedButtonEventListener {

    public static final int VIDEO_UPLOAD = 111;

    protected FragmentManager mainFragmentManager;
    private CaptureFragment captureFrag;
    private SupportCaptureFragment supportCaptureFrag;

    private Nostalgia mApp;

    private TextView mCountdown;
    private CameraApiVersion mCameraType;

    private CaptureButton mCaptureButton;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public enum CameraApiVersion {
        OLD, NEW
    }

    private Toolbar mToolbar;

    public static final String TAG = "MainCaptureActivity";

    @Override
    public void onClick(View v) {
        helper.startDeterminate();
        switch (v.getId()) {
            case R.id.flip_camera:
                break;
            case R.id.toggle_camera_flash:
                break;
            default:
                break;

        }
    }

    private ProgressHelper helper;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        try{
            helper.cancelCountdown();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void detectAvaliableCameraApi() {
        if (Build.VERSION.SDK_INT >= 20) {
            mCameraType = CameraApiVersion.NEW;
        } else {
            mCameraType = CameraApiVersion.OLD;
        }
    }

    private void createCaptureFragment() {
        switch (mCameraType) {
            case NEW:
                if (captureFrag == null) {
                    captureFrag = new CaptureFragment();
                }
                break;
            case OLD:
                if (supportCaptureFrag == null) {
                    supportCaptureFrag = new SupportCaptureFragment();
                }
                break;
            default:
                break;
        }
    }

    private boolean activeToolbar=true;
    private void flipCamera() {
        switch (mCameraType) {
            case NEW:
                captureFrag.flipCamera();
                break;
            case OLD:
                supportCaptureFrag.flipCamera();
                break;
            default:
                break;
        }
    }

    private void addCaptureFragment() {
        FragmentTransaction fragTransaction = mainFragmentManager.beginTransaction();
        switch (mCameraType) {
            case NEW:
                fragTransaction.replace(R.id.camera_holder, captureFrag);
                break;
            case OLD:
                fragTransaction.replace(R.id.camera_holder, supportCaptureFrag);
                break;
            default:
                break;
        }
        fragTransaction.commit();
    }

    private void startRecordingVideo() {
        switch (mCameraType) {
            case NEW:
                captureFrag.startRecordingVideo();
                break;
            case OLD:
                supportCaptureFrag.startRecordingVideo();
                break;
            default:
                break;
        }
    }


    private File stopRecordingVideo() throws IOException {
        File recorded;
        switch (mCameraType) {
            case NEW:
                recorded = captureFrag.stopRecordingVideo();
                break;
            case OLD:
                recorded = supportCaptureFrag.stopRecordingVideo();
                break;
            default:
                recorded = null;
                break;
        }
        return recorded;
    }


    ///////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detectAvaliableCameraApi();

        mApp = (Nostalgia) getApplication();
        setContentView(R.layout.activity_capture);

        createCaptureFragment();

        mCountdown = (TextView) findViewById(R.id.countdown);

        mCaptureButton = (CaptureButton) findViewById(R.id.capture_button);

        helper = new ProgressHelper(mCountdown, mCaptureButton, this);
        helper.setTimerListener(this);

        mToolbar = (Toolbar) findViewById(R.id.nostalgia_actionbar_container);
        setSupportActionBar(mToolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setShowHideAnimationEnabled(true);

        attachDrawer(savedInstanceState, mToolbar);


        final MainCaptureActivity me = this;

        ImageView flipper = (ImageView) findViewById(R.id.flip_camera);
        flipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCamera();
            }
        });

        mainFragmentManager = getFragmentManager();

        //initially, mStart with the choice fragment
        addCaptureFragment();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        setupCaptureButton();
    }

    private void goToPeekPicker() {
        Intent peekPickerIntent = new Intent(this, LocationsActivity.class);
        startActivity(peekPickerIntent);
    }

    boolean recordPressed = false;
    long startTime;

    private boolean isRecording = false;
    public void setupCaptureButton(){
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    isRecording = true;
                    helper.startDeterminate();
                    startTime = System.currentTimeMillis();
                    hideToolbar();
                } else {
                    long duration = System.currentTimeMillis() - startTime;

                    /*
                     * Keep recording if the video is shorter than 3 seconds
                     */
                    if (duration > 3000) {
                        showToolbar();
                        isRecording = false;
                        helper.completeTask();
                        isRecording = false;
                        recordPressed = false;
                    } else {

                        Toast.makeText(mApp, "At least 3 seconds long.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    @Override
    public void onTimerStart() {
        startRecordingVideo();
    }

    @Override
    public void onTimerStop(long duration) {

        try {

            File recorded = stopRecordingVideo();
            if(recorded != null) {
                Intent mpdIntent = new Intent(this, MediaReviewerPagerActivity.class)
                        .setData(Uri.parse(recorded.getAbsolutePath()))
                        .putExtra(VideoPlayerFragment.CONTENT_TYPE_EXTRA, VideoPlayerFragment.TYPE_OTHER);


                startActivityForResult(mpdIntent, VIDEO_UPLOAD);
            } else {
                Toast.makeText(this, "Error recording video.", Toast.LENGTH_LONG);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        helper.clear();

        switch (requestCode) {
            case VIDEO_UPLOAD:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainCaptureActivity.this, "video uploaded successfully", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                Log.d(TAG, "Request code: " + Integer.toString(requestCode) + " isn't handled.");
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        helper.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_capture, menu);

        menu.findItem(R.id.action_button_videos).getActionView().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Do nothing, already here!
                        goToPeekPicker();
                    }
                }
        );
        return super.onCreateOptionsMenu(menu);
    }

    private boolean mToolbarShowing = false;
    private void toggleToolbar(){
        if(mToolbarShowing){
            hideToolbar();
        } else {
            showToolbar();
        }
    }

    private void hideToolbar() {
        mToolbar.animate()
                .translationY(-mToolbar.getHeight())
                .setInterpolator(new AccelerateInterpolator(2))
                .setListener(new Animator.AnimatorListener() {
                                 @Override
                                 public void onAnimationStart(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                 }

                                 @Override
                                 public void onAnimationCancel(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationRepeat(Animator animation) {

                                 }
                             }
                );

        mToolbarShowing = false;
    }

    private void showToolbar() {
        mToolbar.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(2))
                .setListener(new Animator.AnimatorListener() {
                                 @Override
                                 public void onAnimationStart(Animator animation) {
                                 }

                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                 }

                                 @Override
                                 public void onAnimationCancel(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationRepeat(Animator animation) {

                                 }
                             }
                );
        mToolbarShowing = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
    }

}
