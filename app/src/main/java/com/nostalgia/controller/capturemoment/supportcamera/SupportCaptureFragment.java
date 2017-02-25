/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nostalgia.controller.capturemoment.supportcamera;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.nostalgia.view.SupportCameraPreview;
import com.vuescape.nostalgia.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *  This activity uses the camera/camcorder as the A/V source for the {@link android.media.MediaRecorder} API.
 *  A {@link android.view.TextureView} is used as the camera preview which limits the code to API 14+. This
 *  can be easily replaced with a {@link android.view.SurfaceView} to run on older devices.
 */
public class SupportCaptureFragment extends Fragment implements SupportCameraPreview.PreviewReady, SurfaceHolder.Callback {
    private static final String TAG = "CamTestActivity";
    private SupportCameraPreview preview;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    private Button buttonClick;
    private Camera camera;
    private Activity act;
    private Context ctx;


    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;


    /*
     * CAMERA_FACING_FRONT is selfie
     * CAMERA_FACING_BACK is world
     */
    private int mCurrentCameraDirection;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity().getApplicationContext();
        act = getActivity();
        //act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.capture_video_support, container, false);
        //mPreview = (TextureView) view.findViewById(R.id.texture);
        mSurfaceView = (SurfaceView) mView.findViewById(R.id.surfaceView);
        preview = new SupportCameraPreview(act);
        preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        preview.setKeepScreenOn(true);

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        ((FrameLayout) mView.findViewById(R.id.layout)).addView(preview);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        startCam();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCam();
    }

    private void startCam(){
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            if(null == camera) {
                camera = SupportCameraHelper.getDefaultBackFacingCameraInstance();
                mCurrentCameraDirection = Camera.CameraInfo.CAMERA_FACING_BACK;
                //camera.unlock();
            }

            if(camera == null){
                Toast.makeText(getActivity(),"Could not open camera.", Toast.LENGTH_LONG).show();
                return;
            }

            camera.startPreview();
            preview.setCamera(camera);
        }

        initializeRecorder();

        try {
            preview.addPreviewTarget(this);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void stopCam(){

        closeMediaRecorder();

        if(camera != null) {
            camera.lock();
            camera.stopPreview();
            camera.release();
            camera = null;
            preview.setCamera(null);
        }
    }

    private void closeMediaRecorder(){
        if (null != mMediaRecorder) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }


    public void flipCamera() {
        stopCam();
        try {
            if (SupportCameraHelper.hasMultipleCameras()) {
                if (mCurrentCameraDirection == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    camera = SupportCameraHelper.getDefaultBackFacingCameraInstance();
                    mCurrentCameraDirection = Camera.CameraInfo.CAMERA_FACING_BACK;
                } else {
                    camera = SupportCameraHelper.getDefaultFrontFacingCameraInstance();
                    mCurrentCameraDirection = Camera.CameraInfo.CAMERA_FACING_FRONT;
                }
            }

            // Destroy previuos Holder
            surfaceDestroyed(mHolder);
            mHolder.removeCallback(this);

            // Remove and re-Add SurfaceView
            ViewGroup rootLayout = (ViewGroup) mSurfaceView.getParent();
            rootLayout.removeView(mSurfaceView);
            rootLayout.removeView(preview);
            mSurfaceView = new SurfaceView(getActivity());
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            rootLayout.addView(mSurfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


            //resetCam();
        } catch(Exception e){
            Toast.makeText(getActivity(), "Cannot access the camera.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        startCam();
    }


    private void closeCamera() {
        closeMediaRecorder();

        if (null != camera) {
            camera.release();
            camera = null;
        }
    }


    /*
     * File saving stuff, below.
     */

    private void initializeRecorder(){
        mMediaRecorder = new MediaRecorder();
    }

    private void setupMediaRecorder(Surface surface, int width, int height) throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if(output == null){
            this.getVideoFile(getActivity().getApplicationContext());
        }

        mMediaRecorder.setOutputFile(output.getAbsolutePath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(60);
        mMediaRecorder.setVideoSize(width, height);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = ORIENTATIONS.get(rotation);

        if(Camera.CameraInfo.CAMERA_FACING_BACK == mCurrentCameraDirection) {
            mMediaRecorder.setOrientationHint(orientation);
        } else {
            mMediaRecorder.setOrientationHint(orientation + 180);
        }

        mMediaRecorder.setPreviewDisplay(surface);

        mMediaRecorder.prepare();
    }

    private File output = null;
    private void getVideoFile(Context context) throws IOException {
        output = new File( context.getFilesDir(), "video.mp4");
        if(!output.exists()){
            output.createNewFile();
        }
    }

    public void startRecordingVideo() {
        try {
            // UI
            mIsRecordingVideo = true;

            // Start recording
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException re){
            re.printStackTrace();
            stopCam();
        }
    }

    private boolean mIsRecordingVideo = false;
    public File stopRecordingVideo() throws IOException {
        Thread resetter = new Thread() {
            @Override
            public void run(){
                // UI
                mIsRecordingVideo=false;
                //    mButtonVideo.setText("record");
                // Stop recording

                try {
                    mMediaRecorder.stop();
                } catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "error stopping recorder", e);
                }

                try {
                    mMediaRecorder.reset();
                } catch (Exception e){
                    Log.e(TAG, "error resetting recorder", e);
                }

            }
        };

        resetter.start();
        try {
            resetter.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        return output;
    }

    /*
     * Interface originally from SupportCameraPreview View class
     */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (camera != null) {

                int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                int orientation = ORIENTATIONS.get(rotation);
                camera.setDisplayOrientation(orientation);
                camera.setPreviewDisplay(holder);

                addPreviewSurface(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (camera != null) {
            camera.stopPreview();
        }
    }

    @Override
    public void addPreviewSurface(SurfaceHolder holder) {
        try {
            /*
             * New stuff
             */
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size optimalSize = SupportCameraHelper.getOptimalPreviewSize(mSupportedPreviewSizes,
                    mSurfaceView.getWidth(), mSurfaceView.getHeight());

            // Use the same size for recording profile.
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            profile.videoFrameWidth = optimalSize.width;
            profile.videoFrameHeight = optimalSize.height;

            // likewise for the camera object itself.
            //parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
            //mCamera.setParameters(parameters);
            //int width = camera.getParameters().getPreferredPreviewSizeForVideo().width;
            //int height = camera.getParameters().getPreferredPreviewSizeForVideo().height;
            camera.unlock();
            mMediaRecorder.setCamera(camera);
            setupMediaRecorder(holder.getSurface(), profile.videoFrameWidth, profile.videoFrameHeight);
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if(camera != null) {
            try {
            /*
             * New stuff
             */
                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
                Camera.Size optimalSize = SupportCameraHelper.getOptimalPreviewSize(mSupportedPreviewSizes,
                        mSurfaceView.getWidth(), mSurfaceView.getHeight());

                // Use the same size for recording profile.
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                profile.videoFrameWidth = optimalSize.width;
                profile.videoFrameHeight = optimalSize.height;

                parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
                preview.requestLayout();
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (RuntimeException e){
                //Chances are that mediaRecorder used camera.unlock();
                e.printStackTrace();
            }
        }
    }
}