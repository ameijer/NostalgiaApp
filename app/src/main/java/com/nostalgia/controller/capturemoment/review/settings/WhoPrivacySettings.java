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
public class WhoPrivacySettings extends PrivacySettings {
    private LinearLayout groupContainer;
    private UserRepository userRepo;
    private TextView description;

    private static final String mSharingOptionTag = "sharing_who";

    public static final String WHO_PRIVATE = "PRIVATE";
    public static final String WHO_FRIENDS = "FRIENDS";
    public static final String WHO_EVERYONE = "EVERYONE";

    private final String privateDescription = "Private: Only you + people you've tagged can see this video";
    private final String friendsDescription = "Friends: Visible to your friends";
    private final String publicDescription = "Everyone: Visible to everyone";

    private ImageButton privateButton;
    private ImageButton friendsButton;
    private ImageButton publicButton;

    private ArrayList<SettingsOption> mSettingsList = new ArrayList<SettingsOption>();

    private SharingOptionsFragment sharingOptionsFragment;

    public WhoPrivacySettings(SharingOptionsFragment sharingOptionsFragment){
        super(sharingOptionsFragment);
        this.setSharingOptionTag(mSharingOptionTag);
        this.sharingOptionsFragment = sharingOptionsFragment;
    }

    public void initializeView(User loggedIn, View parentView) {
        groupContainer = (LinearLayout) parentView.findViewById(R.id.edit_who_options);
        this.setGroupContainer(groupContainer);

        privateButton = (ImageButton) parentView.findViewById(R.id.who_private);
        friendsButton = (ImageButton) parentView.findViewById(R.id.who_friends);
        publicButton = (ImageButton) parentView.findViewById(R.id.who_everyone);

        description = (TextView) parentView.findViewById(R.id.who_desc);
        this.setDescription(description);

        SettingsOption privateOption = new SettingsOption(WHO_PRIVATE, privateButton, privateDescription, this);
        SettingsOption friendsOption = new SettingsOption(WHO_FRIENDS, friendsButton, friendsDescription, this);
        SettingsOption publicOption = new SettingsOption(WHO_EVERYONE, publicButton, publicDescription, this);

        ArrayList<SettingsOption> whoSettingsList = new ArrayList<SettingsOption>();
        whoSettingsList.add(privateOption);
        whoSettingsList.add(friendsOption);
        whoSettingsList.add(publicOption);

        this.setSettingsList(whoSettingsList);
        this.initializeOptions(loggedIn);

        return;
    }
}
