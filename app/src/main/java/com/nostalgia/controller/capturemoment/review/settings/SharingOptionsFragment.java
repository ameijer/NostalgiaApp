package com.nostalgia.controller.capturemoment.review.settings;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.persistence.repo.VideoRepository;
import com.nostalgia.runnable.UserAttributeUpdaterThread;
import com.nostalgia.service.NetworkConnectivityService;
import com.vuescape.nostalgia.R;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 11/3/15.
 */
public class SharingOptionsFragment extends Fragment {
    private static final String TAG = "SharingOptionsFrag";
    private NetworkConnectivityService mNetwork;

    private WhoPrivacySettings whoPrivacySettings;
    private WhenPrivacySettings whenPrivacySettings;

    private String mConnectionType;

    private String videoIdUnderReview;
    private User videoOwner;
    private Nostalgia app;

    private VideoRepository vidRepo;
    private UserRepository userRepo;
    private final static String SHARING_WHEN = "sharing_when";
    private final static String SHARING_WHO = "sharing_who";
    private final static String SHARING_WHERE = "sharing_where";

    public static SharingOptionsFragment newInstance(String key){
        Bundle args = new Bundle();
        args.putString("vidId", key);
        SharingOptionsFragment fragment = new SharingOptionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            app = (Nostalgia)getActivity().getApplication();

            vidRepo = app.getVidRepo();
            userRepo = app.getUserRepo();
            videoOwner = app.getUserRepo().getLoggedInUser();

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MediaPrivacySettingsListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if(b != null){
            this.videoIdUnderReview = b.getString("vidId");

        }
        mNetwork = new NetworkConnectivityService(this.getContext());
        mNetwork.checkConnectivity();
        mConnectionType = mNetwork.getConnectionType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.edit_sharing_options, container, false);

        configureWhoSettings(myView);
        configureWhenSettings(myView);

        return myView;
    }

    private void configureWhoSettings(View myView){
        whoPrivacySettings = new WhoPrivacySettings(this);
        if(null != videoOwner) {
            whoPrivacySettings.initializeView(videoOwner, myView);
        }
    }

    private void configureWhenSettings(View myView){
        mConnectionType = NetworkConnectivityService.NOT_CONNECTED;
        if(mNetwork.checkConnectivity()) {
            mConnectionType = mNetwork.getConnectionType();
        }
        boolean isConnectedToWifi = (mConnectionType.equals(NetworkConnectivityService.WIFI));

        whenPrivacySettings = new WhenPrivacySettings(this);
        whenPrivacySettings.setIsCurrentlyUsingWifi(isConnectedToWifi);
        if(null != videoOwner) {
            whenPrivacySettings.initializeView(videoOwner, myView);
        }
    }

    private void updateSharingSettings(){
        putProperty(SHARING_WHEN, whenPrivacySettings.getCurrentlySelected().getType());
        putProperty(SHARING_WHO, whoPrivacySettings.getCurrentlySelected().getType());
        putProperty(SHARING_WHERE, "EVERYWHERE");
    }

    public void putProperty(String property, String value){

        if(property == null || value == null){
            Log.e(TAG, "error - null key/val passed in, not storing update");
            return;
        }

        //update video
        if(videoIdUnderReview != null) {
            Video toModify = vidRepo.findOneById(videoIdUnderReview, false, false);

            if (toModify == null) {
                Log.e(TAG, "error, null video. not saving changes");
                return;
            }
            toModify.getProperties().put(property, value);
            try {
                vidRepo.save(toModify);
            } catch (Exception e) {
                Log.e(TAG, "error saving video");
            }
        }
        //update user
        User cur = userRepo.getLoggedInUser();

        if(cur == null){
            Log.e(TAG, "error, null loggedinuser");
        }

        cur.getSettings().put(property, value);
        try {
            Map<String, String> changed = new HashMap<>();
            changed.put(property, value);
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(cur.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
            updatr.start();
            userRepo.save(cur);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error saving user");
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
