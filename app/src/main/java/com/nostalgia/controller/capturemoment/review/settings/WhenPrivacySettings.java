package com.nostalgia.controller.capturemoment.review.settings;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;

/**
 * Created by alex on 11/3/15.
 */
public class WhenPrivacySettings extends PrivacySettings {
    private LinearLayout groupContainer;
    private UserRepository userRepo;
    private TextView description;

    private static final String mSharingOptionTag = "sharing_when";

    public static final String WHEN_NOW = "NOW";
    public static final String WHEN_HOUR = "HOUR";
    public static final String WHEN_DAY = "ONE_DAY";
    public static final String WHEN_WIFI = "WIFI";

    private String nowDescription="Now: Upload this video immediately";
    private String soonDescription="Soon: Upload this video in an hour";
    private String dayDescription="Tomorrow: Upload this video tomorrow";
    private String wifiDescription="Wifi: Strict cell data plan? Wait until you have wifi.";

    private ImageButton nowButton;
    private ImageButton soonButton;
    private ImageButton dayButton;
    private ImageButton wifiButton;

    private Boolean isCurrentlyUsingWifi = false;

    private ArrayList<SettingsOption> mSettingsList = new ArrayList<SettingsOption>();

    private SharingOptionsFragment sharingOptionsFragment;

    public WhenPrivacySettings(SharingOptionsFragment sharingOptionsFragment){
        super(sharingOptionsFragment);
        this.setSharingOptionTag(mSharingOptionTag);
        this.sharingOptionsFragment = sharingOptionsFragment;
    }

    public void setIsCurrentlyUsingWifi(Boolean wifi){
        isCurrentlyUsingWifi = wifi;
    }

    public void initializeView(User loggedIn, View parentView) {
        groupContainer = (LinearLayout) parentView.findViewById(R.id.edit_when_options);
        this.setGroupContainer(groupContainer);

        nowButton = (ImageButton) parentView.findViewById(R.id.when_now);
        soonButton = (ImageButton) parentView.findViewById(R.id.when_onehour);
        dayButton = (ImageButton) parentView.findViewById(R.id.when_oneday);
        wifiButton = (ImageButton) parentView.findViewById(R.id.when_wifi);

        description = (TextView) parentView.findViewById(R.id.when_desc);
        this.setDescription(description);

        SettingsOption nowOption = new SettingsOption(WHEN_NOW, nowButton, nowDescription, this);
        SettingsOption soonOption = new SettingsOption(WHEN_HOUR, soonButton, soonDescription, this);
        SettingsOption dayOption = new SettingsOption(WHEN_DAY, dayButton, dayDescription, this);

        WifiOption wifiOption = new WifiOption(WHEN_WIFI, wifiButton, wifiDescription, this);
        wifiOption.setIsCurrentlyUsingWifi(isCurrentlyUsingWifi);

        ArrayList<SettingsOption> whenSettingsList = new ArrayList<SettingsOption>();
        whenSettingsList.add(nowOption);
        whenSettingsList.add(soonOption);
        whenSettingsList.add(dayOption);
        whenSettingsList.add(wifiOption);

        this.setSettingsList(whenSettingsList);
        this.initializeOptions(loggedIn);

        if(wifiOption.getIsCurrentlyUsingWifi()){
            wifiButton.setVisibility(View.GONE);
        }
        return;
    }
}
