package com.nostalgia.controller.peek.picker;

import android.os.Bundle;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.LocationRecyclerAdapter;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.CollectionAdapter;
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
public class DefaultCollectionFragment extends BaseRecyclerFragment {

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
    public static DefaultCollectionFragment newInstance() {
        DefaultCollectionFragment f = new DefaultCollectionFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMultiSelectable", true);
        f.setArguments(args);
        return f;
    }

    @Override
    public void loadCollectionGroups() {
        mLocationRepo = ((Nostalgia) getActivity().getApplication()).getLocationRepository();
        mCollectionRepo = ((Nostalgia) getActivity().getApplication()).getCollRepo();

        ArrayList<MediaCollection> defaultPub = buildDefaultList();
        getCollectionAdapter().addGroup("Public Collections", "Default ", defaultPub, MediaCollectionWrapper.WrapperType.PUBLIC, CollectionAdapter.HeaderAction.PLUS);

        return;
    }

    @Override
    public void addNewItem(MediaCollection toAdd, String section, MediaCollectionWrapper.WrapperType type) {

    }

    private ArrayList<MediaCollection> buildDefaultList(){
        ArrayList<MediaCollection> aggregate = new ArrayList<>();
        User nostalgiaOfficial = getUserRepo().findOneById("c799306a-9f28-4497-901b-82e6a84f07fd");
        List<String> officialPublic = nostalgiaOfficial.getAllPublicCollections();
        try {
            Map<String, MediaCollection> collectionMap = mCollectionRepo.getCollectionsById(officialPublic, true);
            aggregate.addAll(collectionMap.values());
        } catch(Exception e){
            e.printStackTrace();
        }

        return aggregate;
    }

    public LocationRepository getLocationRepo(){
        return mLocationRepo;
    }

}
