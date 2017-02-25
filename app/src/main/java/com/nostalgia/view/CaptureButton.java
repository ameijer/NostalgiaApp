package com.nostalgia.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class CaptureButton extends FrameLayout {
    private class CaptureButtonAdapter{
        private Integer mColor;
        private float mHeightPercentage;
        private boolean mIsSeen;
        private final Integer isSeenColor = Color.rgb(200, 100, 100);
        private final Integer isUnseenColor = Color.rgb(100, 100, 200);

        public CaptureButtonAdapter(){
            this(Color.rgb(20, 100, 20), false);
        }
        public CaptureButtonAdapter(Integer color, boolean isSeen){
            mColor = color;
            mIsSeen = isSeen;
        }

        public void setIsSeen(boolean isSeen){
            mIsSeen = isSeen;
        }

        public boolean getIsSeen(){
            return mIsSeen;
        }
    }

    private int mExampleColor = R.color.accent_material_dark;
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mMarkerWidth = 10;
    private Paint mMarkerPaint;
    // Usually this can be a field rather than a method variable
    private ArrayList<CaptureButtonAdapter> mEpochMarkers;

    int mPaddingLeft;
    int mPaddingTop;
    int mPaddingRight;
    int mPaddingBottom;

    int mContentWidth;
    int mContentHeight;

    int mCenter;

    float mProgressLeft;
    float mProgressRight;
    float mProgressTop;
    float mProgressBottom;

    private Integer mProgressColor = Color.argb(100, 5, 75, 200);
    private Integer mNumberSteps = 254;

    public CaptureButton(Context context) {
        super(context);
        init(null, 0);
    }

    public CaptureButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CaptureButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        //Do nothing.
    }

    private float oldPercentage = -1;
    private Integer ctr = 0;
    private int oldNumberActiveBars = 0;

    private int mProgress = 0;
    /*
     * We will expand progress bar out from center, so each step activates
     * two new bars. One on each side of center, symmetrically.
     */
    private boolean oddNumBars;
    public void setPercentageDecimal(float percentage) {
        if(oldPercentage != percentage) {
            oldPercentage = percentage;

            int activeBars =  (int) Math.floor(percentage * mNumberSteps /2);
            if (activeBars > mNumberSteps /2) {
                activeBars = 0;
            }

            if(activeBars != mProgress) {
                mProgress = activeBars;
                postInvalidate();
            }
        }
    }

    public void clearProgress(){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();

        mContentWidth = getWidth() - mPaddingLeft - mPaddingRight;
        mContentHeight = getHeight() - mPaddingTop - mPaddingBottom;

        mCenter = (getWidth() + mPaddingLeft + mPaddingRight) / 2;
        if(mProgress > 0) {
            mMarkerWidth = mContentWidth/ mNumberSteps;

            mMarkerPaint = new Paint();
            mMarkerPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mMarkerPaint.setStrokeWidth(mMarkerWidth);
            mMarkerPaint.setColor(Color.argb(100, 5, 75, 200));
            mMarkerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

            mProgressLeft = mCenter - mProgress * mMarkerWidth;
            mProgressRight = mCenter + mProgress * mMarkerWidth;
            mProgressTop = mContentHeight;
            mProgressBottom = 0;

            //canvas.drawLine(mProgressLeft, mProgressBottom, mProgressRight, mProgressTop, mMarkerPaint);
            canvas.drawRect(mProgressLeft, mProgressBottom, mProgressRight, mProgressTop, mMarkerPaint);
        } else {
            //No progress, clear button.
            canvas.drawColor(getResources().getColor(R.color.lighttransparentblack));
        }
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;

    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
