package com.nostalgia.menu.friends.model;

import com.nostalgia.persistence.model.User;

import java.util.HashMap;

/**
 * Created by Aidan on 3/22/16.
 */
public class ContactCard {
    private String mName;
    private String mDescription;
    private String mUniqueId;
    private String mEmail;
    private String mPhoneNumber;

    private HashMap<String, String> mMisc = new HashMap<String, String>();

    public String getName(){
        return mName;
    }
    public String getDescription(){
        return mDescription;
    }
    public String getUniqueId(){
        return mUniqueId;
    }
    public String getPhoneNumber(){return mPhoneNumber;}
    public String getEmail(){return mEmail;}
    public void setName(String name){
        mName = name;
    }
    public void setDescription(String description){
        mDescription = description;
    }
    public void setUniqueId(String uniqueId){
        mUniqueId = uniqueId;
    }
    public void setEmail(String email){mEmail = email;}
    public void setPhoneNumber(String number){mPhoneNumber = number;}

    public void extractFromUser(User user){
        mName = user.getUsername();
        mDescription = "Some user tagline";
        mUniqueId = user.get_id();
    }

    public void appendMiscellaneous(String colName, String value){
        mMisc.put(colName, value);
    }

}
