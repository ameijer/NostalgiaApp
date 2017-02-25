package com.nostalgia.controller.peek.picker.locationdisplayers.support;

import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.repo.LocationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aidan on 11/29/15.
 */
public class SupportNearbyLocationGridFragment extends SupportLocationGridFragment implements LocationRepository.NearbyLocationEventListener{


    @Override
    public void subscribeToLocationData() {
        getLocationRepo().registerNearbyLocationListener(this);
    }

    @Override
    public void unsubscribeToLocationData() {
        getLocationRepo().unregisterNearbyLocationListener(this);
    }

    @Override
    public ArrayList<KnownLocation> loadLocationData() {
        return getLocationRepo().getNearbyLocations();
    }

    @Override
    public void onNearbyLocationsChanged(List<KnownLocation> updated) {
        onLocationChanged(updated);
    }
}
