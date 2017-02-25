package com.nostalgia.controller.capturemoment.review.places;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters.grid.LocationArrayGridAdapter;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;
import com.vuescape.nostalgia.R;

/**
 * Created by alex on 11/3/15.
 */
public class NewLocationDialogFragment extends Fragment implements View.OnClickListener{

    private Nostalgia app;
    private NewLocationConfirmListener callback;

    private Button createButton;
    private EditText name;
    private SeekBar zoomBar;
    private UserRepository userRepo;
    private SeekBar.OnSeekBarChangeListener listener;

    public void setSeekBarListener(SeekBar.OnSeekBarChangeListener listener){
        this.listener = listener;

    }

    public interface NewLocationConfirmListener{
        void onNewLocationsSelected(KnownLocation created);
    }



    private LocationArrayGridAdapter adapter;

    public void setOnConfirmedListener(NewLocationConfirmListener listener){
        this.callback = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        app = ((Nostalgia) activity.getApplication());
        this.userRepo = app.getUserRepo();
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " error casting app class");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.new_location_dialog, container, false);
        //bind views
        this.zoomBar = (SeekBar) myView.findViewById(R.id.seekbar_size);
        zoomBar.setOnSeekBarChangeListener(listener);
        User loggedIn = userRepo.getLoggedInUser();
        if(loggedIn != null){
            String zoomPref = loggedIn.getSettings().get("zoom_level");
            if(zoomPref != null){
                double zoom = Double.parseDouble(zoomPref);

                zoomBar.setProgress((int)zoom);
            } else {
                zoomBar.setProgress(0);
            }
        } else {
            zoomBar.setProgress(0);
        }

        this.createButton = (Button) myView.findViewById(R.id.create_location_submit);
        createButton.setTag("submit");
        createButton.setOnClickListener(this);

        this.name = (EditText) myView.findViewById(R.id.create_location_name);


        return myView;
    }

    @Override
    public void onClick(View v) {
        if(v.getTag().equals("submit")){
            KnownLocation basic = new KnownLocation();
            basic.setName(name.getText().toString());
            if(callback != null){
                callback.onNewLocationsSelected(basic);
            }


        }
    }

}
