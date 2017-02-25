package com.nostalgia.menu.friends.viewholder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nostalgia.menu.friends.recycler.BaseRecyclerFragment;
import com.nostalgia.menu.friends.model.PersonWrapper;
import com.nostalgia.persistence.model.User;

/**
 * Created by Aidan on 12/11/15.
 */
public abstract class BaseHolder extends RecyclerView.ViewHolder{

    private int mSectionFirstPosition = 0;
    private Activity mParentActivity;
    private User mCurrentUser;
    private BaseRecyclerFragment.SelectionListener mSelectionListener;

    public enum ViewHolderType {CONTACT, HEADER}

    public ViewHolderType mType;

    public abstract void bindItem(PersonWrapper wrapper);

    public BaseHolder(View itemView) {
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
