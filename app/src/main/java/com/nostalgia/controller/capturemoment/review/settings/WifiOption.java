package com.nostalgia.controller.capturemoment.review.settings;

import android.widget.ImageButton;

/**
 * Created by Aidan on 11/23/15.
 */
public class WifiOption extends SettingsOption {
    private Boolean isCurrentlyUsingWifi = false;
    private String noWifiDescription = "Wait until you have wifi to mStart upload.";

    public WifiOption(String type, ImageButton button, String description, PrivacySettings parent){
        super(type, button, description, parent);
    }

    public void setIsCurrentlyUsingWifi(Boolean wifi){
        isCurrentlyUsingWifi = wifi;

        if(isCurrentlyUsingWifi){
            this.hideButton();
        } else {
            this.setDescription(noWifiDescription);
        }
    }

    public boolean getIsCurrentlyUsingWifi(){
        return isCurrentlyUsingWifi;
    }
}
