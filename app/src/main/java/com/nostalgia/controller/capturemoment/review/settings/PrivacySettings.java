package com.nostalgia.controller.capturemoment.review.settings;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostalgia.persistence.model.User;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by alex on 11/3/15.
 */
public class PrivacySettings implements SettingsOption.OnSelected {
    private LinearLayout groupContainer;
    private TextView description;
    private String mSharingOptionTag;

    private ArrayList<SettingsOption> mSettingsList = new ArrayList<SettingsOption>();

    private SharingOptionsFragment sharingOptionsFragment;

    public PrivacySettings(SharingOptionsFragment sharingOptionsFragment){
        super();
        this.sharingOptionsFragment = sharingOptionsFragment;

    }

    public PrivacySettings(SharingOptionsFragment sharingOptionsFragment, String sharingOptionTag){
        super();
        this.sharingOptionsFragment = sharingOptionsFragment;
        mSharingOptionTag = sharingOptionTag;

    }

    public PrivacySettings(SharingOptionsFragment sharingOptionsFragment, LinearLayout groupContainer, TextView description){
        super();
        this.sharingOptionsFragment = sharingOptionsFragment;
        this.groupContainer = groupContainer;
        this.description = description;

    }

    public void setGroupContainer(LinearLayout groupContainer){this.groupContainer=groupContainer;}

    public void setDescription(TextView description){
        this.description = description;
    }

    public TextView getSettingsDescription(){
        return description;
    }

    public void initializeOptions( User loggedIn){
        String defaultSetting;



        defaultSetting = loggedIn.getSettings().get(mSharingOptionTag);


        Iterator iterator = mSettingsList.iterator();
        SettingsOption target;

        while (iterator.hasNext()) {
            target = (SettingsOption) iterator.next();
            target.setColors(sharingOptionsFragment.getResources().getColor(R.color.md_pink_200), sharingOptionsFragment.getResources().getColor(R.color.md_indigo_700));
            if(target.getType().equals(defaultSetting)){
                setCurrentlySelected(target);
            } else {
                target.setUnchecked();
            }
        }
    }

    public void setSettingsList(ArrayList<SettingsOption> settingsList){
        mSettingsList = settingsList;
    }

    private SettingsOption currentlySelected;

    @Override
    public void settingSelected(String type, SettingsOption selected){
        setCurrentlySelected(selected);

    }

    private void setCurrentlySelected(SettingsOption selected){

        //Only make changes if it's a new selection
        if(selected != null && currentlySelected != null && selected != currentlySelected) {
            sharingOptionsFragment.putProperty(mSharingOptionTag, selected.getType());

            if (null != currentlySelected) {
                currentlySelected.setUnchecked();
            }
            selected.setChecked();
            currentlySelected = selected;

        }
    }

    public void setSharingOptionTag(String tag){
        mSharingOptionTag = tag;
    }

    public SettingsOption getCurrentlySelected(){
        return currentlySelected;
    }
}
