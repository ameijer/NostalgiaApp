package com.nostalgia.menu.settings;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import com.nostalgia.controller.capturemoment.review.settings.SharingOptionsFragment;

/**
 * Created by alex on 12/25/15.
 */
public class SettingsActivity extends FragmentActivity {

    private UserInfoFragment userInfoFrag = new UserInfoFragment();
    private AccountsFragment accountsFrag = new AccountsFragment();
    private SharingOptionsFragment sharingSettingsFrag = new SharingOptionsFragment();
    private SyncStatusFragment syncFrag = new SyncStatusFragment();
    private AppSettingsFragment appSettingsFrag = new AppSettingsFragment();

    private Button confirmButton;

    public static final String TAG = "SettingsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        confirmButton = (Button) findViewById(R.id.settings_confirm_button);
        confirmButton.setClickable(true);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitSettings();
            }
        });

        //bind fragments
        FragmentManager manager = getSupportFragmentManager();

        FragmentTransaction trans = manager.beginTransaction();
        trans.add(R.id.user_info_frag_holder, userInfoFrag, userInfoFrag.getTag());
        trans.add(R.id.account_frag_holder, accountsFrag, accountsFrag.getTag());
        trans.add(R.id.sharing_settings_frag_holder, sharingSettingsFrag, sharingSettingsFrag.getTag());
        trans.add(R.id.sync_settings_frag_holder, syncFrag, syncFrag.getTag());
        trans.add(R.id.app_settings_frag_holder, appSettingsFrag, appSettingsFrag.getTag());
        trans.commit();


        return;

    }


    private void quitSettings() {
        Intent intent = this.getIntent();
        this.setResult(RESULT_OK, intent);
        finish();
    }


}
