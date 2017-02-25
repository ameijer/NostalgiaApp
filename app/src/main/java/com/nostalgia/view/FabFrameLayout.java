package com.nostalgia.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

/**
 * Created by Aidan on 2/16/16.
 */
public class FabFrameLayout extends FrameLayout {

    public FabFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FabFrameLayout(Context context) {
        super(context);
    }

    public FabFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FabFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private FABToolbarLayout mFABListener;

    public void setFabListener(FABToolbarLayout fabListener){
        mFABListener = fabListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean parentDecision = super.onInterceptTouchEvent(ev);

        if(null != mFABListener){
            mFABListener.hide();
        }

        return parentDecision;
    }
}
