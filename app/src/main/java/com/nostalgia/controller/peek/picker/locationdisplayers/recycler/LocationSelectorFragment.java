package com.nostalgia.controller.peek.picker.locationdisplayers.recycler;

import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.repo.LocationRepository;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aidan on 11/29/15.
 */
public abstract class LocationSelectorFragment extends BaseRecyclerFragment implements LocationRepository.NearbyLocationEventListener, LocationRepository.SubscribedLocationEventListener{

    @Override
    public void subscribeToLocationData() {
        getLocationRepo().registerNearbyLocationListener(this);
        getLocationRepo().registerSubscribedLocationListener(this);
    }

    @Override
    public void unsubscribeToLocationData() {
        getLocationRepo().unregisterNearbyLocationListener(this);
        getLocationRepo().unregisterSubscribedLocationListener(this);
    }

    @Override
    public abstract ArrayList<ArrayList<KnownLocation>> loadSectionedLocationData();

    @Override
    public ArrayList<KnownLocation> loadLocationData() {
        ArrayList<KnownLocation> aggregate = new ArrayList<>();

        aggregate.addAll(getLocationRepo().getNearbyLocations());

        return aggregate;
    }

    @Override
    public ArrayList<KnownLocation> loadSubscribedLocationData(){
        ArrayList<KnownLocation> aggregate = new ArrayList<>();
        aggregate.addAll(getLocationRepo().getSubscribedLocations());
        return aggregate;
    }

    @Override
    public void onNearbyLocationsChanged(List<KnownLocation> updated) {
        super.onLocationChanged(updated);
    }

    @Override
    public void onSubscribedLocationsChanged(List<KnownLocation> updated) {
        super.onLocationChanged(updated);
    }
}
