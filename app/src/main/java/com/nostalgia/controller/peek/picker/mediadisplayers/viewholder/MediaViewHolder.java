package com.nostalgia.controller.peek.picker.mediadisplayers.viewholder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.persistence.model.User;

/**
 * Created by Aidan on 12/11/15.
 */
public abstract class MediaViewHolder extends RecyclerView.ViewHolder{

    private int mSectionFirstPosition = 0;
    private Activity mParentActivity;
    private User mCurrentUser;
    private BaseRecyclerFragment.SelectionListener mSelectionListener;

    public enum ViewHolderType {COLLECTION, HEADER}

    public ViewHolderType mType;

    public abstract void onBind();
    public abstract void bindItem(MediaCollectionWrapper wrapper);

    public MediaViewHolder(View itemView) {
        super(itemView);
    }

    public int getSectionFirstPosition(){
        return mSectionFirstPosition;
    }

    public void setSectionFirstPosition(int sectionFirstPosition){
        mSectionFirstPosition = sectionFirstPosition;
    }
    public void bindParentActivity(Activity activity){
        mParentActivity = activity;
    }

    public void bindCurrentUser(User user){
        mCurrentUser = user;
    }

    public Activity getParentActivity(){
        return mParentActivity;
    }
    public User getCurrentUser(){
        return mCurrentUser;
    }
    public void setSelectionListener(BaseRecyclerFragment.SelectionListener selectionListener){
        mSelectionListener = selectionListener;
    }
    public BaseRecyclerFragment.SelectionListener getSelectionListener(){
        return mSelectionListener;
    }

    private boolean mIsToggleable = false;
    public void setIsToggleable(boolean isToggleable){
        mIsToggleable = isToggleable;
    }
    public boolean getIsToggleable(){
        return mIsToggleable;
    }

}
