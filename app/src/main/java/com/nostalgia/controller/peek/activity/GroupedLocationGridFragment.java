package com.nostalgia.controller.peek.activity;

import com.nostalgia.controller.peek.picker.locationdisplayers.grid.LocationGridFragment;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.repo.LocationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aidan on 11/29/15.
 */
public class GroupedLocationGridFragment extends LocationGridFragment implements LocationRepository.NearbyLocationEventListener{

    //also offer ability to sort by type?

    private final ArrayList<KnownLocation> group = new ArrayList<KnownLocation>();

    @Override
    public void subscribeToLocationData() {

    }

    @Override
    public void unsubscribeToLocationData() {

    }

    @Override
    public ArrayList<KnownLocation> loadLocationData() {
        return group;
    }

    @Override
    public void onNearbyLocationsChanged(List<KnownLocation> updated) {
        group.clear();
        group.addAll(updated);
        super.onLocationChanged(updated);
    }

    public ArrayList<KnownLocation> getGroup() {
        return group;
    }
}
