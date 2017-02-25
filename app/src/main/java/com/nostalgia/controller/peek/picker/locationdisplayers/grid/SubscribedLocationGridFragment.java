package com.nostalgia.controller.peek.picker.locationdisplayers.grid;

import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.repo.LocationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aidan on 11/29/15.
 */
public class SubscribedLocationGridFragment extends LocationGridFragment implements LocationRepository.SubscribedLocationEventListener{


    @Override
    public void subscribeToLocationData() {
        getLocationRepo().registerSubscribedLocationListener(this);
    }

    @Override
    public void unsubscribeToLocationData() {
        getLocationRepo().unregisterSubscribedLocationListener(this);
    }

    @Override
    public ArrayList<KnownLocation> loadLocationData() {
        return getLocationRepo().getSubscribedLocations();
    }

    @Override
    public void onSubscribedLocationsChanged(List<KnownLocation> updated) {
       super.onLocationChanged(updated);
    }
}
