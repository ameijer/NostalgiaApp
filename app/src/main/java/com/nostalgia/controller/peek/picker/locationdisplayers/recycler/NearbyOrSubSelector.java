package com.nostalgia.controller.peek.picker.locationdisplayers.recycler;

import android.os.Bundle;

import com.nostalgia.persistence.model.KnownLocation;

import java.util.ArrayList;

/**
 * Created by Aidan on 1/18/16.
 */
public class NearbyOrSubSelector extends LocationSelectorFragment {

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     *
     * Nothing currently stored in the args Bundle, but maybe
     * another time.
     */
    public static NearbyOrSubSelector newInstance() {
        NearbyOrSubSelector f = new NearbyOrSubSelector();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public ArrayList<ArrayList<KnownLocation>> loadSectionedLocationData(){
        int NUM_SECTIONS = 2;
        ArrayList<ArrayList<KnownLocation>> ret = new ArrayList<ArrayList<KnownLocation>>(NUM_SECTIONS);

        ArrayList<KnownLocation> nearby = new ArrayList<>();
        nearby.addAll(getLocationRepo().getNearbyLocations());

        ArrayList<KnownLocation> subscribed = new ArrayList<>();
        subscribed.addAll(getLocationRepo().getSubscribedLocations());

        ret.add(nearby);
        ret.add(subscribed);

        return ret;
    }
}
