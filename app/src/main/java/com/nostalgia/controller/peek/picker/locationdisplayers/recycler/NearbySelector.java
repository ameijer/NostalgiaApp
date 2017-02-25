package com.nostalgia.controller.peek.picker.locationdisplayers.recycler;

import com.nostalgia.persistence.model.KnownLocation;

import java.util.ArrayList;

/**
 * Created by Aidan on 1/18/16.
 */
public class NearbySelector extends LocationSelectorFragment {
    @Override
    public ArrayList<ArrayList<KnownLocation>> loadSectionedLocationData(){
        int NUM_SECTIONS = 2;
        ArrayList<ArrayList<KnownLocation>> ret = new ArrayList<ArrayList<KnownLocation>>(NUM_SECTIONS);

        ArrayList<KnownLocation> nearby = new ArrayList<>();
        nearby.addAll(getLocationRepo().getNearbyLocations());

        ret.add(nearby);

        return ret;
    }
}
