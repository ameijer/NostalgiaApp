package com.nostalgia.controller.peek.picker.locationdisplayers.support;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters.grid.LocationArrayGridAdapter;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.repo.LocationRepository;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.List;

/**
 * For the love of god, please change this back to LocationGridFragment when possible.
 *
 * For some reason, the string of fragments used in
 */
public abstract class SupportLocationGridFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    public static String TAG = "PastLocationGridFragment";
    private GridView locationGrid;
    private ArrayList<KnownLocation> locations = new ArrayList<KnownLocation>();
    protected LocationArrayGridAdapter locationArrayGridAdapter;
    private LocationRepository locationRepo;
    private LocationPickedListener callback;

    public ArrayList<KnownLocation> getLocations(){
        return this.locations;
    }

    public void notifychanged() {
        locationArrayGridAdapter.notifyDataSetChanged();
    }

    public abstract void subscribeToLocationData();
    public abstract void unsubscribeToLocationData();
    public abstract ArrayList<KnownLocation> loadLocationData();

    public LocationRepository getLocationRepo(){
        return locationRepo;
    }

    public void onLocationChanged(List<KnownLocation> updated) {
        this.locationArrayGridAdapter.notifyDataSetChanged();
    }

    public interface LocationPickedListener{
        void onCancelled();
        void onLocationPicked(KnownLocation selected);
    }

    public void setLocationPickedListener(LocationPickedListener listener){
        this.callback = listener;
    }

    //TODO: Had been "MainActivity"
    private Activity parentActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            parentActivity = activity;
            locationRepo = ((Nostalgia) parentActivity.getApplication()).getLocationRepository();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " incorrect cast");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
//        if(b != null){
//            this.focusedPlayerId = b.getString("playerId");
//
//        }

    }

    private void initVidGridAdapter() {
        locationArrayGridAdapter = new LocationArrayGridAdapter(
                parentActivity.getApplicationContext(),
                locations, ((Nostalgia)getActivity().getApplication())
        );
        locationGrid.setAdapter(locationArrayGridAdapter);
        locationGrid.setOnItemClickListener(this);
        locationGrid.setOnItemLongClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        View myView = inflater.inflate(R.layout.fragment_past_locations_grid, container, false);

        this.locationGrid = (GridView) myView.findViewById(R.id.past_loc_grid);

        loadAllLocations();
        subscribeToLocationData();

        initVidGridAdapter();
        return myView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        KnownLocation focused = this.locations.get(position);

        if(callback != null){
            callback.onLocationPicked(focused);
        }

    }

    @Override
    public void onDestroyView(){
        //locationRepo.setCallback(null);
        super.onDestroyView();
        unsubscribeToLocationData();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    private void loadAllLocations(){
        locations = loadLocationData();
    }

    public KnownLocation getLocation(int position){
        return this.locations.get(position);
    }

}
