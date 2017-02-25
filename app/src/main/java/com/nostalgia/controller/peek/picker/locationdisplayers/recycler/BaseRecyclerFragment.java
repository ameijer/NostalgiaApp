package com.nostalgia.controller.peek.picker.locationdisplayers.recycler;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.model.KnownLocationWrapper;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.view.DividerItemDecoration;
import com.tonicartos.superslim.LayoutManager;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 11/19/15.
 */
public abstract class BaseRecyclerFragment extends Fragment {
    public static String TAG = "BaseLocationSelectorFragment";
    private ArrayList<KnownLocation> locations = new ArrayList<>();
    private ArrayList<KnownLocation> subscribedLocations = new ArrayList<>();
    private LocationRepository locationRepo;
    private UserRepository userRepo;
    private LocationPickedListener callback;
    private RecyclerView mRecyclerView;

    protected LocationRecyclerAdapter mLocationRecyclerAdapter;

    private final int mLayoutId = R.layout.slim_recycler_view_holder;

    public abstract void subscribeToLocationData();
    public abstract void unsubscribeToLocationData();
    public abstract ArrayList<KnownLocation> loadLocationData();
    public abstract ArrayList<KnownLocation> loadSubscribedLocationData();
    public abstract ArrayList<ArrayList<KnownLocation>> loadSectionedLocationData();

    public LocationRepository getLocationRepo(){
        return locationRepo;
    }
    public UserRepository getUserRepo() {return userRepo;}

    public void onLocationChanged(List<KnownLocation> updated) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLocationRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void initVidAdapter(View myView) {
        mRecyclerView = (RecyclerView) myView.findViewById(R.id.recycler_view);

        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity()));

        mRecyclerView.setLayoutManager(new LayoutManager(getActivity()));
        mRecyclerView.setAdapter(mLocationRecyclerAdapter);
    }

    public void clearVidAdapter(){
        mRecyclerView.destroyDrawingCache();
        mRecyclerView = null;
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
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            parentActivity = getActivity();
            locationRepo = ((Nostalgia) parentActivity.getApplication()).getLocationRepository();
            userRepo = ((Nostalgia) parentActivity.getApplication()).getUserRepo();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " incorrect cast");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();

        loadAllLocations();
        subscribeToLocationData();

        //TODO: Nasty activity cast here, necessitated by getVideoRepo()
        mLocationRecyclerAdapter = new LocationRecyclerAdapter((Nostalgia) getActivity().getApplication(), this);

        try {
            setLocationPickedListener((LocationPickedListener) getActivity());
        } catch (ClassCastException e){
            e.printStackTrace();
        }

        mLocationRecyclerAdapter.addGroup("Nearby", "Look around", locations, KnownLocationWrapper.WrapperType.NEARBY);
        mLocationRecyclerAdapter.addGroup("Your Trail", "Where you've been", subscribedLocations, KnownLocationWrapper.WrapperType.SUBSCRIBED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        View myView = inflater.inflate(mLayoutId, container, false);

        return myView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initVidAdapter(view);
    }

    @Override
    public void onDestroyView(){
        //locationRepo.setCallback(null);
        super.onDestroyView();
        unsubscribeToLocationData();
        clearVidAdapter();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mLocationRecyclerAdapter = null;
    }

    private void loadAllLocations(){
        locations = loadLocationData();
        subscribedLocations = loadSubscribedLocationData();
    }

    public Activity getParentActivity(){
        return parentActivity;
    }

    public int getLayoutId(){
        return mLayoutId;
    }

    public LocationPickedListener getCallback(){
        return callback;
    }

}
