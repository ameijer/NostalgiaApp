package com.nostalgia.controller.peek.picker.locationdisplayers.recycler.model;

import com.nostalgia.persistence.model.KnownLocation;

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
public class KnownLocationWrapper {
    public enum WrapperType{
        NEARBY,SUBSCRIBED,HEADER
    }

    /*
     * KnownLocationWrapper will be displayed as part of a stupid list, where each item must keep track
     * of what "section" of the list it is in, as well as its position in that section.
     */
    public int mSectionFirstPosition;
    public int mSectionManager;

    private String mHeaderName;
    private String mHeaderDesc;
    private  KnownLocation mKnownLocation;
    private WrapperType mType;
    private boolean mIsSubscribed;

    public KnownLocationWrapper(){
    }

    public KnownLocationWrapper(KnownLocation knownLocation, WrapperType type){
        mKnownLocation = knownLocation;
        mType = type;
    }

    public KnownLocationWrapper(String name, String desc, WrapperType type){
        mHeaderName = name;
        mHeaderDesc = desc;
        mType = type;
    }

    public void setKnownLocation(KnownLocation knownLocation){
        mKnownLocation = knownLocation;
    }

    public void setWrapperType(WrapperType type){
        mType = type;
    }

    public WrapperType getType(){
        return mType;
    }

    public KnownLocation getKnownLocation(){
        return mKnownLocation;
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
}


