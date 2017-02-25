package com.nostalgia.controller.capturemoment.review.settings;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by Aidan on 11/15/15.
 */
public class SettingsOption {
    private String mType;
    private ImageButton mButton;
    private PrivacySettings mParent;
    private String mDescription;
    private TextView mDescView;
    private boolean mIsChecked = false;

    private int mActiveColor;
    private int mInactiveColor;

    private int normalElevation = 0;

    private OnSelected onSelectedListener;
    public interface OnSelected {
        void settingSelected(String type, SettingsOption calledBy);
    }

    public SettingsOption(String type, ImageButton button, String description, PrivacySettings parent){
        mType = type;
        mButton = button;
        mParent = parent;
        mDescription = description;

        setOnSelectedListener(parent);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performClick();
            }
        });

        //Assume not checked on initialization.
    }

    public void setChecked() {
        mParent.getSettingsDescription().setText(mDescription);
        mButton.setBackgroundColor(mActiveColor);

        mIsChecked = true;
    }

    public void setUnchecked() {
        mButton.setBackgroundColor(mInactiveColor);
        mIsChecked = false;
    }

    public SettingsOption setColors(int activeColor, int inactiveColor){
        setActiveColor(activeColor);
        setInactiveColor(inactiveColor);

        setUnchecked();

        return this;
    }

    public void performClick(){
        onSelectedListener.settingSelected(mType, this);
    }

    public SettingsOption setInactiveColor(int inactiveColor){
        if(-1 != inactiveColor) {
            mInactiveColor = inactiveColor;
        } else {
            if(mButton.getBackground() instanceof ColorDrawable) {
                mInactiveColor = ((ColorDrawable) mButton.getBackground()).getColor();
            } else {
                mInactiveColor = Color.rgb(255, 255, 255);
            }
        }
        return this;
    }

    public SettingsOption setActiveColor(int activeColor){
        if(-1 != activeColor) {
            mActiveColor = activeColor;
        } else {
            mActiveColor = Color.rgb(0, 255, 0);
        }
        return this;
    }

    public void setOnSelectedListener(OnSelected listener){
        onSelectedListener = listener;
    }

    public String getType(){
        return mType;
    }

    public void setDescription(String description){
        mDescription = description;
        return;
    }

    public void hideButton(){
        mButton.setVisibility(View.GONE);
    }
    public void showButton(){
        mButton.setVisibility(View.VISIBLE);
    }
}