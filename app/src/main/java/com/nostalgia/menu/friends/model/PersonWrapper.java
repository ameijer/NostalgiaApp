package com.nostalgia.menu.friends.model;

import com.nostalgia.persistence.model.User;

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
public class PersonWrapper {
    public enum WrapperType{
        PENDING_INC, PENDING_OUT, NATIVE, PHONE, SOCIAL, HEADER, EMPTY_SET, HEADER_PLUS
    }

    public int mSectionFirstPosition;
    public int mSectionManager;

    private String mHeaderName;
    private String mHeaderDesc;

    private User mPerson;
    private ContactCard mContact;

    private WrapperType mType;
    private boolean mIsSubscribed;

    private boolean mIsSelected;

    public PersonWrapper(){
    }

    public PersonWrapper(ContactCard contact, WrapperType type){
        mContact = contact;
        mType = type;
    }

    public PersonWrapper(User person, WrapperType type){
        mPerson = person;
        mType = type;
    }


    public PersonWrapper(String name, String desc, WrapperType type){
        mHeaderName = name;
        mHeaderDesc = desc;
        mType = type;
    }

    public void setPerson(User person){
        mPerson = person;
    }

    public void setContact(ContactCard contact){
        mContact = contact;
    }

    public void setWrapperType(WrapperType type){
        mType = type;
    }

    public WrapperType getType(){
        return mType;
    }

    public User getPerson(){
        return mPerson;
    }
    public ContactCard getContact(){
        return mContact;
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


