package com.nostalgia.controller.peek.picker.mediadisplayers;

import android.os.Bundle;
import android.util.Log;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.LocationRecyclerAdapter;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.CollectionAdapter;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.persistence.repo.MediaCollectionRepository;
import com.nostalgia.runnable.SubscriberThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aidan on 11/29/15.
 */
public class NearbyOrSubSelectorFragment extends BaseRecyclerFragment {

    private LocationRepository mLocationRepo;
    private MediaCollectionRepository mCollectionRepo;
    protected LocationRecyclerAdapter mLocationRecyclerAdapter;

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     *
     * Nothing currently stored in the args Bundle, but maybe
     * another time.
     */
    public static NearbyOrSubSelectorFragment newInstance(boolean isMultiSelectable) {
        NearbyOrSubSelectorFragment f = new NearbyOrSubSelectorFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMultiSelectable", isMultiSelectable);
        f.setArguments(args);
        return f;
    }

    @Override
    public void loadCollectionGroups() {
        mLocationRepo = ((Nostalgia) getActivity().getApplication()).getLocationRepository();
        mCollectionRepo = ((Nostalgia)getActivity().getApplication()).getCollRepo();
        ArrayList<MediaCollection> nearby = buildNearbyList();

        if(nearby.isEmpty()){
            getCollectionAdapter().addEmptyGroup("Nearby", "Look around", "There don't seem to be any tagged places near you, but you can always make one.", MediaCollectionWrapper.WrapperType.NEARBY, CollectionAdapter.HeaderAction.PLUS);
        } else {
            getCollectionAdapter().addGroup("Nearby", "Look around", nearby, MediaCollectionWrapper.WrapperType.NEARBY, CollectionAdapter.HeaderAction.PLUS);
        }

        ArrayList<MediaCollection> subscribed = buildSubscribedList();

        if(subscribed.isEmpty()){
            getCollectionAdapter().addEmptyGroup("Memory Lane", "The wonderful places you've been.", "You haven't checked in anywhere yet, but when you do, those places will always show up right here.", MediaCollectionWrapper.WrapperType.SUBSCRIBED, CollectionAdapter.HeaderAction.SEARCH);
        } else {
            getCollectionAdapter().addGroup("Memory Lane", "The wonderful places you've been.", subscribed, MediaCollectionWrapper.WrapperType.SUBSCRIBED, CollectionAdapter.HeaderAction.SEARCH);
        }

        return;
    }

    @Override
    public void addNewItem(MediaCollection toAdd, String section, MediaCollectionWrapper.WrapperType type) {
        getCollectionAdapter().addItemToSection(toAdd, "Nearby", MediaCollectionWrapper.WrapperType.NEARBY);
    }

    private ArrayList<MediaCollection> buildNearbyList(){

        List<KnownLocation> allLocs = getLocationRepo().getNearbyLocations();

        ArrayList<MediaCollection> nearby = new ArrayList<MediaCollection>();

        for(KnownLocation loc: allLocs){
            String collId = loc.getLocationCollections().get("primary");
            MediaCollection linked = getMediaCollectionRepo().findOneById(collId);
            linked.setName(loc.getName());

            /*
            Map<String, String> colProps = linked.getProperties();
            if(null != colProps && colProps.get("DESCRIPTION") != null) {
                Map<String, String> locProps = loc.getProperties();
                if(null != locProps && locProps.get("DESCRIPTION") != null) {
                    linked.getProperties().put("DESCRIPTION", locProps.get("DESCRIPTION"));
                }
            }
            */

            nearby.add(linked);
        }

        return nearby;
    }

    private ArrayList<MediaCollection> buildSubscribedList(){

        List<KnownLocation> allLocs = getLocationRepo().getSubscribedLocations();
        ArrayList<MediaCollection> subscribed = new ArrayList<MediaCollection>();

        for(KnownLocation loc: allLocs){
            String collId = loc.getLocationCollections().get("primary");
            MediaCollection linked = getMediaCollectionRepo().findOneById(collId);
            linked.setName(loc.getName());

            subscribed.add(linked);
        }

        return subscribed;
    }

    public MediaCollectionRepository getMediaCollectionRepo(){
        return mCollectionRepo;
    }

    public LocationRepository getLocationRepo(){
        return mLocationRepo;
    }
}
