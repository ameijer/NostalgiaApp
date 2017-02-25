package com.nostalgia.controller.peek.picker.mediadisplayers.model;

import com.nostalgia.persistence.model.MediaCollection;

/**
 * Created by Aidan on 12/11/15.
 *
 * This is a class designed to wrap KnownLocations and Headers so we can use the SLiM library
 * to create a sectioned list or GridView
 *
 * This class must wrap KnownLocation (nearby), KnownLocation(subscribed)
 * and header classes that will be sticky at the top of each section.
 *
 * Sections will only be "nearby" and "subscribed" at first,
 * but this will be designed to support future categories (perhaps: peek, bars, My Summer Vacation, etc.)
 *
 * Part
 *
 */
public class MediaCollectionWrapper {
    public enum WrapperType{
        PERSONAL,SHARED,PUBLIC,HEADER,EMPTY_SET,HEADER_PLUS, NEARBY, SUBSCRIBED
    }

    /*
     * KnownLocationWrapper will be displayed as part of a stupid list, where each item must keep track
     * of what "section" of the list it is in, as well as its position in that section.
     */
    public int mSectionFirstPosition;
    public int mSectionManager;

    private String mHeaderName;
    private String mHeaderDesc;
    private MediaCollection mMediaCollection;
    private WrapperType mType;
    private boolean mIsSubscribed;

    private boolean mIsSelected;

    public MediaCollectionWrapper(){
    }

    public MediaCollectionWrapper(MediaCollection mediaCollection, WrapperType type){
        mMediaCollection = mediaCollection;
        mType = type;
    }

    public MediaCollectionWrapper(String name, String desc, WrapperType type){
        mHeaderName = name;
        mHeaderDesc = desc;
        mType = type;
    }

    public void setMediaCollection(MediaCollection collection){
        mMediaCollection = collection;
    }

    public void setWrapperType(WrapperType type){
        mType = type;
    }

    public WrapperType getType(){
        return mType;
    }

    public MediaCollection getMediaCollection(){
        return mMediaCollection;
    }

    public boolean isHeader(){
        return (mType == WrapperType.HEADER);
    }

    public void setSectionFirstPosition(int firstPosition){
        mSectionFirstPosition = firstPosition;
    }

    public void setSectionManager(int sectionManager){
        mSectionManager = sectionManager;
    }

    public int getSectionFirstPosition(){
        return mSectionFirstPosition;
    }

    public int getSectionManager(){
        return mSectionManager;
    }

    private String mEmptySetDesc = "Nothing to see here.";

    public String getHeaderName(){
        return mHeaderName;
    }
    public void setHeaderName(String name){
        mHeaderName = name;
    }
    public void setHeaderDescription(String desc){
        mHeaderDesc = desc;
    }
    public String getHeaderDescription(){
        return mHeaderDesc;
    }
    public void setIsSubscribed(boolean value){
        mIsSubscribed = value;
    }
    public boolean getIsSubscribed(){
        return mIsSubscribed;
    }
    public void setIsSelected(boolean isSelected){
        mIsSelected = isSelected;
    }
    public boolean getIsSelected(){
        return mIsSelected;
    }
    public String getEmptySetDescription(){return mEmptySetDesc;}
    public void setEmptySetDescription(String desc){mEmptySetDesc = desc;}
}


