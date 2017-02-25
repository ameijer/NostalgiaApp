package com.nostalgia.menu.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.runnable.UserAttributeUpdaterThread;
import com.vuescape.nostalgia.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 12/25/15.
 */
public class SyncStatusFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SyncStatusFragment.class.getName();
    private ImageButton syncOffButton;
    private ImageButton syncOnButton;
    private ImageButton wifiSyncButton;

    private TextView app_tx_view;
    private TextView app_rx_view;

    private UserRepository userRepo;
    private Context context;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        userRepo = ((Nostalgia)activity.getApplication()).getUserRepo();
        context = getContext();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.sync_settings_frag, container, false);


        //bind views
        syncOffButton = (ImageButton) myView.findViewById(R.id.sync_off);
        syncOffButton.setOnClickListener(this);
        syncOnButton = (ImageButton) myView.findViewById(R.id.sync_on);
        syncOnButton.setOnClickListener(this);
        wifiSyncButton = (ImageButton) myView.findViewById(R.id.sync_wifi);
        wifiSyncButton.setOnClickListener(this);

        app_rx_view = (TextView) myView.findViewById(R.id.app_rx_bytes);
        app_tx_view = (TextView) myView.findViewById(R.id.app_tx_bytes);


        int uid = Binder.getCallingUid();
        // internet usage for particular app(sent and received)
        double received = (double) TrafficStats.getUidRxBytes(uid)

                / (1024 * 1024);
        double send = (double) TrafficStats.getUidTxBytes(uid)
                / (1024 * 1024);
        app_tx_view.setText("App Upload: " + (int)send + " MB");
        app_rx_view.setText("App Download: " + (int)received + " MB");


        //select proper image
        User current = userRepo.getLoggedInUser();

        if(current == null){
            Log.e(TAG, "ERROR  - NO USER FOUND", new NullPointerException());
            return myView;
        }

        String syncSetting = current.getSettings().get("sync");
        if(syncSetting == null){
            syncSetting = "always";
            current.getSettings().put("sync", "always");
            try {
                Map<String, String> changed = new HashMap<>();
                changed.put("sync", "always");
                UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(current.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
                updatr.start();
                userRepo.save(current);
            } catch (Exception e) {
                Log.e(TAG, "error saving user", e);
            }
        }

        switch(syncSetting){
            case("always"):
                selectOnImage();
                break;
            case("wifi"):
                selectWifiImage();
                break;
            case("off"):
                selectOffImage();
                break;
            default:
                Log.e(TAG, "Error in switch function", new IllegalArgumentException());
                break;
        }

        return myView;
    }

    @Override
    public void onClick(View v) {
        User current = userRepo.getLoggedInUser();
        Thread updater;
        switch(v.getId()) {
            case R.id.sync_off:
                selectOffImage();
                updater = new Thread(){
                    @Override
                    public void run(){
                        turnOffSync();
                    }
                };
                updater.start();

                break;
            case R.id.sync_on:
                selectOnImage();
                updater = new Thread(){
                    @Override
                    public void run(){
                        turnOnSync();
                    }
                };
                updater.start();

                break;
            case R.id.sync_wifi:

                updater = new Thread(){
                @Override
                public void run(){
                    turnOnWifiSync();
                }
            };
                updater.start();
                selectWifiImage();
                break;
            default:
                Log.e(TAG, "Unhandled view onclick: " + v.getTag());
                break;
        }
    }

    private void selectOffImage() {
        syncOnButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        wifiSyncButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        syncOffButton.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void selectOnImage() {
        syncOnButton.setBackgroundColor(getResources().getColor(android.R.color.white));
        wifiSyncButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        syncOffButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void selectWifiImage() {
        syncOnButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        wifiSyncButton.setBackgroundColor(getResources().getColor(android.R.color.white));
        syncOffButton.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    private void turnOffSync() {
        //update user attribute
        User current = userRepo.getLoggedInUser();
        current.getSettings().put("sync", "off");

        try {
            Map<String, String> changed = new HashMap<>();
            changed.put("sync", "off");
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(current.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
            updatr.start();
            userRepo.save(current);
        } catch (Exception e) {
            Log.e(TAG, "error saving user", e);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Log.e(TAG, "interrupted", e);
        }

        //send intent
        Intent i = new Intent("com.nostalgia.sync.update");
        getActivity().sendBroadcast(i);

    }

    private void turnOnSync() {
        //update user attribute
        User current = userRepo.getLoggedInUser();
        current.getSettings().put("sync", "always");

        try {
            Map<String, String> changed = new HashMap<>();
            changed.put("sync", "always");
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(current.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
            updatr.start();
            userRepo.save(current);
        } catch (Exception e) {
            Log.e(TAG, "error saving user", e);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Log.e(TAG, "interrupted", e);
        }

        //send intent
        Intent i = new Intent("com.nostalgia.sync.update");
        getActivity().sendBroadcast(i);
    }

    private void turnOnWifiSync() {
        //update user attribute
        User current = userRepo.getLoggedInUser();
        current.getSettings().put("sync", "wifi");

        try {
            Map<String, String> changed = new HashMap<>();
            changed.put("sync", "wifi");
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(current.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
            updatr.start();
            userRepo.save(current);
        } catch (Exception e) {
            Log.e(TAG, "error saving user", e);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Log.e(TAG, "interrupted", e);
        }

        //send intent
        Intent i = new Intent("com.nostalgia.sync.update");
        getActivity().sendBroadcast(i);
    }
}
