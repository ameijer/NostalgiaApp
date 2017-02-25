package com.nostalgia.controller.peek.picker.mediadisplayers;

import android.os.Bundle;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.LocationRecyclerAdapter;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.CollectionAdapter;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.persistence.repo.MediaCollectionRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aidan on 11/29/15.
 */
public class NearbySelectorFragment extends BaseRecyclerFragment {

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
    public static NearbySelectorFragment newInstance(boolean isMultiSelectable) {
        NearbySelectorFragment f = new NearbySelectorFragment();
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
            nearby.add(linked);
        }

        return nearby;
    }

    public MediaCollectionRepository getMediaCollectionRepo(){
        return mCollectionRepo;
    }

    public LocationRepository getLocationRepo(){
        return mLocationRepo;
    }
}
