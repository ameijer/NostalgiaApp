package com.nostalgia.controller.peek.picker.locationdisplayers.recycler.viewholder;

import android.app.Activity;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.model.KnownLocationWrapper;
import com.nostalgia.persistence.model.User;

/**
 * Created by Aidan on 12/11/15.
 */
public abstract class PeekViewHolder extends RecyclerView.ViewHolder{

    private int mSectionFirstPosition = 0;
    private Activity mParentActivity;
    private User mCurrentUser;

    private BaseRecyclerFragment.LocationPickedListener mSelectionListener;

    public enum ViewHolderType {LOCATION, HEADER}

    public ViewHolderType mType;

    public abstract void onBind();
    public abstract void bindItem(KnownLocationWrapper wrapper);

    public PeekViewHolder(View itemView) {
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
    public void setSelectionListener(BaseRecyclerFragment.LocationPickedListener selectionListener){
        mSelectionListener = selectionListener;
    }
    public BaseRecyclerFragment.LocationPickedListener getSelectionListener(){
        return mSelectionListener;
    }

    public ViewHolderType getType(){
        return mType;
    }
}
