package com.nostalgia.controller.peek;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.runnable.ProtectedResourceGetterTask;
import com.vuescape.nostalgia.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by alex on 1/18/16.
 */
public class LocationInfoFragment extends Fragment {

    private static final String TAG = "LOCINFOFRAG";
    //shows consolidated info about friend
    private KnownLocation target;

    //top pic
    private ImageView icon;

    //Name
    private TextView name;

    //num available videos
    private UserRepository userRepo;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        userRepo = ((Nostalgia)activity.getApplication()).getUserRepo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if(b != null){
            this.target = (KnownLocation) b.getSerializable("location");

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.user_info_frag, container, false);

        //bind views
        //profile pic
         icon = (CircleImageView) myView.findViewById(R.id.user_focus_icon);

        //Name
     name = (TextView) myView.findViewById(R.id.user_focus_name);

        User loggedIn = userRepo.getLoggedInUser();

        File img = new File("img.jpg");
        ProtectedResourceGetterTask task = new ProtectedResourceGetterTask(loggedIn.getStreamTokens(), img, target.getThumbnails().get(0));

        task.execute();
        try {
            task.get();
        } catch (Exception e){
            Log.e(TAG, "error getting asynctask");
        }


        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeStream(new FileInputStream(img));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "error opeining file in img loader");
        }
        icon.setImageBitmap(bmp);

        //set up text fields
        //Name
        name.setText(target.getName());

        return myView;
    }

    public static LocationInfoFragment newInstance(KnownLocation key){
        Bundle args = new Bundle();
        args.putSerializable("location", key);
        LocationInfoFragment fragment = new LocationInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
