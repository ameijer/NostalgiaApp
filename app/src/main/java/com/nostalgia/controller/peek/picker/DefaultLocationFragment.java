package com.nostalgia.controller.peek.picker;

import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Aidan on 11/29/15.
 */
public class DefaultLocationFragment extends BaseRecyclerFragment {

    private LocationRepository mLocationRepo;
    private MediaCollectionRepository mCollectionRepository;
    protected LocationRecyclerAdapter mLocationRecyclerAdapter;

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     *
     * Nothing currently stored in the args Bundle, but maybe
     * another time.
     */
    public static DefaultLocationFragment newInstance() {
        DefaultLocationFragment f = new DefaultLocationFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMultiSelectable", true);
        f.setArguments(args);
        return f;
    }

    @Override
    public void loadCollectionGroups() {
        mLocationRepo = ((Nostalgia) getActivity().getApplication()).getLocationRepository();
        ArrayList<MediaCollection> nearby = buildDefaultList();
        getCollectionAdapter().addGroup("Popular Locations", "We'll get some better places up at some point.", nearby, MediaCollectionWrapper.WrapperType.PUBLIC, CollectionAdapter.HeaderAction.NONE);

        return;
    }

    private ArrayList<MediaCollection> buildDefaultList(){
        ArrayList<MediaCollection> aggregate = new ArrayList<>();
        User nostalgiaOfficial = getUserRepo().findOneById("c799306a-9f28-4497-901b-82e6a84f07fd");
        List<String> locs = nostalgiaOfficial.getCreatedLocations();


        if(null != locs) {
            try {
                Map<String, KnownLocation> d =  getLocationRepo().getLocationsById(locs, true, true);

                for(KnownLocation loc : d.values()){
                    Map<String, String> colls = loc.getLocationCollections();
                    Map<String, MediaCollection> mapColls = mCollectionRepository.getCollectionsById(colls.values(), true);
                    aggregate.addAll(mapColls.values());
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return aggregate;
    }

    public LocationRepository getLocationRepo(){
        return mLocationRepo;
    }


    @Override
    public void addNewItem(MediaCollection toAdd, String section, MediaCollectionWrapper.WrapperType type) {

    }
}
