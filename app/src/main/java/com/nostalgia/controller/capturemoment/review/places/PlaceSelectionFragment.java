package com.nostalgia.controller.capturemoment.review.places;


import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.mediadisplayers.NearbySelectorFragment;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.vuescape.nostalgia.R;

import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * Created by alex on 11/3/15.
 */
public class PlaceSelectionFragment extends Fragment implements LocationCreationFragment.OnLocationCreateListener{

    private Nostalgia app;
    private Video videoUnderReview;
    private User videoOwner;
    private NearbySelectorFragment nearbySelectorFragment;
    private FragmentManager mainFragmentManager;
    public LocationCreationFragment locationCreationFragment;
    private String targetVideoId;

    public static PlaceSelectionFragment newInstance(String key){
        Bundle args = new Bundle();
        args.putString("vidId", key);
        PlaceSelectionFragment fragment = new PlaceSelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        app = ((Nostalgia) activity.getApplication());
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " error casting app class");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if(b != null){
            this.targetVideoId = b.getString("vidId");

        }

        NominatimPOIProvider poiProvider = new NominatimPOIProvider(null);
        Location mLocation = app.getLocation();
        GeoPoint myLocation = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude(), mLocation.getAltitude());

        try {
            ArrayList<POI> pois = poiProvider.getPOICloseTo(myLocation, "restaurant", 10, 0.1);

            if(pois.size() > 0) {
                Toast.makeText(getActivity(), pois.get(0).mDescription, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "No neighborhoods nearby.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            //No points of interest found nearby.
            e.printStackTrace();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.edit_place_options, container, false);

        nearbySelectorFragment = new NearbySelectorFragment();
        mainFragmentManager = getFragmentManager();

        final PlaceSelectionFragment me = this;

        FloatingActionButton createNewLocationButton = (FloatingActionButton) myView.findViewById(R.id.create_location_button);
        createNewLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent mpdIntent = new Intent(getActivity(), LocationCreationActivity.class);
                mpdIntent.putExtra("targetVideoId", targetVideoId);
                startActivityForResult(mpdIntent, LocationCreationActivity.MULTI_SELECT);
            }
        });

        /*
         * Configure Who settings
         */
        FragmentTransaction t = mainFragmentManager.beginTransaction();
        t.add(R.id.nearby_loc_grid_holder, nearbySelectorFragment);
        t.commit();

        return myView;
    }

    public void putProperty(String property, String value){
        videoUnderReview.getProperties().put(property, value);
    }

    @Override
    public void onLocationsChosen(ArrayList<KnownLocation> selected) {
        mainFragmentManager.popBackStack();
    }

    @Override
    public void onLocationCreated(KnownLocation created){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Activity.RESULT_OK:
                ArrayList<KnownLocation> locs = new ArrayList<KnownLocation>();


                locs = app.getLocationRepository().getNearbyLocations();
                ArrayList<String> ids = data.getStringArrayListExtra("selectedLocations");
                onLocationsChosen(locs);
                break;
            default:
                Toast.makeText(getActivity(), "Creation Result Not Implemented.", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
