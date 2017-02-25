package com.nostalgia.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.Random;

public class EpochSeeker extends View {
    private class EpochMarkerAdapter{
        private Integer mColor;
        private float mHeightPercentage;
        private boolean mIsSeen;
        private final Integer isSeenColor = Color.rgb(200, 100, 100);
        private final Integer isUnseenColor = Color.rgb(100,100, 200);

        public EpochMarkerAdapter(){
            this(Color.rgb(20, 100, 20), 0, false);
        }
        public EpochMarkerAdapter(Integer color, float heightPercentage, boolean isSeen){
            mColor = color;
            mHeightPercentage = heightPercentage;
            mIsSeen = isSeen;
        }

        public void setIsSeen(boolean isSeen){
            mIsSeen = isSeen;
        }

        public boolean getIsSeen(){
            return mIsSeen;
        }

        public float getHeightPercentage(){
            return mHeightPercentage;
        }
    }

    private String mExampleString;
    private int mExampleColor = R.color.accent_material_dark;
    private float mExampleDimension = 0;
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private float mMarkerWidth = R.dimen.timeline_marker_width;
    private Paint mMarkerPaint;
    // Usually this can be a field rather than a method variable
    private Random rand = new Random(10000L);
    private int[] mRandomNums;
    private ArrayList<EpochMarkerAdapter> mEpochMarkers;

    int mPaddingLeft = getPaddingLeft();
    int mPaddingTop = getPaddingTop();
    int mPaddingRight = getPaddingRight();
    int mPaddingBottom = getPaddingBottom();

    int mContentWidth = getWidth() - mPaddingLeft - mPaddingRight;
    int mContentHeight = getHeight() - mPaddingTop - mPaddingBottom;

    public EpochSeeker(Context context) {
        super(context);
        init(null, 0);
    }

    public EpochSeeker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EpochSeeker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EpochSeeker, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.EpochSeeker_exampleString);
        mExampleColor = a.getColor(
                R.styleable.EpochSeeker_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.EpochSeeker_exampleDimension,
                mExampleDimension);
        mMarkerWidth = a.getDimension(R.styleable.EpochSeeker_marker_width, mMarkerWidth);
        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mMarkerPaint = new Paint();
        mMarkerPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mMarkerPaint.setColor(mExampleColor);
        mMarkerPaint.setStrokeWidth(mMarkerWidth);

        mEpochMarkers = new ArrayList<>();
        float hp;
        for(int i=0; i<100; i++){
            hp = rand.nextFloat();
            EpochMarkerAdapter epochMarkerAdapter = new EpochMarkerAdapter(Color.rgb(20, 10, 50), hp,false);
            epochMarkerAdapter.setIsSeen(false);
            mEpochMarkers.add(epochMarkerAdapter);
        }
        
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    public void setSeekerPercentage(){

    }

    private void invalidateTextPaintAndMeasurements() {
        //mTextPaint.setTextSize(mExampleDimension);
        //mTextPaint.setColor(mExampleColor);
        //mTextWidth = mTextPaint.measureText(mExampleString);

        //Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        //mTextHeight = fontMetrics.bottom;
    }
    private Integer ctr = 0;
    public void setUpdate(boolean should) {
        if (should == true) {
            if(ctr >= 100){
                ctr = 0;
            }
            mEpochMarkers.get(ctr).setIsSeen(true);
            ctr = ctr + 1;
            this.postInvalidate();
        }
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

        mMarkerPaint.setStrokeWidth(mContentWidth/100);

        canvas.drawLine(mPaddingLeft, mContentHeight, mContentWidth+mPaddingLeft, mContentHeight, mMarkerPaint);
        float pos;
        float heightPercentage;

        for(int i=0; i<mEpochMarkers.size();i++){
            EpochMarkerAdapter marker = mEpochMarkers.get(i);
            if(marker.getIsSeen()) {
                mMarkerPaint.setColor(Color.GREEN);
            } else {
                mMarkerPaint.setColor(Color.WHITE);
            }

            pos = mPaddingLeft + i*mContentWidth/100;
            heightPercentage = marker.getHeightPercentage();
            canvas.drawLine(pos, mContentHeight, pos, heightPercentage*mContentHeight, mMarkerPaint);
        }
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
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
        invalidateTextPaintAndMeasurements();
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
        invalidateTextPaintAndMeasurements();
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
