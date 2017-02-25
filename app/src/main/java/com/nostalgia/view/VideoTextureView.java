package com.nostalgia.view;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;


/**
 * Created by Aidan on 1/2/16.
 */
public class VideoTextureView extends AutoFitTextureView {


    private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f;

    private float videoAspectRatio;

    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
        /**
         * Set the aspect ratio that this {@link VideoTextureView} should satisfy.
         *
         * @param widthHeightRatio The width to height ratio.
         */
        public void setVideoWidthHeightRatio(float widthHeightRatio) {
            if (this.videoAspectRatio != widthHeightRatio) {
                this.videoAspectRatio = widthHeightRatio;
                requestLayout();
            }
        }

    /*
     * onVideoSizeChanged comes from github issue, not the satorufijuwa package
     */
    public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees, float pixelWidthHeightRatio) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        float pivotX = viewWidth / 2f;
        float pivotY = viewHeight / 2f;

        Matrix transform = new Matrix();
        transform.postRotate(unAppliedRotationDegrees, pivotX, pivotY);
        if (unAppliedRotationDegrees == 90 || unAppliedRotationDegrees == 270) {
            float viewAspectRatio = (float) viewHeight / viewWidth;
            transform.postScale(1 / viewAspectRatio, viewAspectRatio, pivotX, pivotY);
        }
        setTransform(transform);
    }

    private void calculateScale(float videoWidth, float videoHeight, Matrix matrix) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float scaleX = 1.0f;
        float scaleY = 1.0f;

        float viewRatio = viewWidth / viewHeight;
        float videoRatio = videoWidth / videoHeight;
        if (viewRatio > videoRatio) {
            // video is higher than view
            scaleY = videoHeight / videoWidth;
        } else {
            //video is wider than view
            scaleX = videoWidth / videoHeight;
        }

        matrix.setScale(scaleX, scaleY, viewWidth / 2, viewHeight / 2);
    }

}