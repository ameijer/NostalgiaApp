package com.nostalgia.controller.peek.picker.mediadisplayers;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.CollectionAdapter;
import com.nostalgia.persistence.model.MediaCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Created by Aidan on 11/29/15.
 */
public class SoloLocationsFragment extends BaseRecyclerFragment {

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     *
     * Nothing currently stored in the args Bundle, but maybe
     * another time.
     */
    public static SoloLocationsFragment newInstance(boolean isMultiSelectable) {
        SoloLocationsFragment f = new SoloLocationsFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMultiSelectable", isMultiSelectable);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);

        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //Restore the fragment's state here
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
        //outState.putParcelableArrayList("COLLECTION_WRAPPERS", mContactAdapter.getCollectionWrappers());
    }

    @Override
    public void loadCollectionGroups() {

        try {
            ArrayList<MediaCollection> personal = null;
            personal = buildSoloList();
            if(personal.isEmpty()){
                getCollectionAdapter().addEmptyGroup("Solo", "Your own personal tales from the places you've been.", "All your own geo-tagged videos will show up here.", MediaCollectionWrapper.WrapperType.SUBSCRIBED, CollectionAdapter.HeaderAction.NONE);
            } else {
                getCollectionAdapter().addGroup("Solo", "Your own personal tales from the places you've been.", personal, MediaCollectionWrapper.WrapperType.SUBSCRIBED, CollectionAdapter.HeaderAction.NONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }

    @Override
    public void addNewItem(MediaCollection toAdd, String section, MediaCollectionWrapper.WrapperType type) {
        getCollectionAdapter().addItemToSection(toAdd, "Solo", MediaCollectionWrapper.WrapperType.SUBSCRIBED);
    }

    /*
     * Building list process:
     * 1. Start with map (key) String name and (Obj) List<String> ListVideos
     * 2. Convert keys to lowercase for sorting, and keep track of Lower -> Original using treemap
     * 3. Sort lowercase names alphabetically in treemap.
     * 4. Iterate through treemap and grab ListVideos using Original as keys
     * 5. Convert each (Original & ListVideos) to MediaCollection
     * 6. Add MediaCollection to the ArrayList for this group.
     */
    private ArrayList<MediaCollection> buildSoloList() throws Exception {

        ArrayList<MediaCollection> collectionList = new ArrayList<>();
        List<String> soloColls =  getUser().getAllMySubmittedLocationVideos();

        Map<String, MediaCollection> soloMap = getCollRepo().getCollectionsById(soloColls, true);

        //Step 2-3
        for(String collectionKey : soloMap.keySet()){
            MediaCollection target = soloMap.get(collectionKey);
            String locId = target.getLinkedLocation();
            target.setName(getCollectionName(locId));

            collectionList.add(target);
        }

        return collectionList;
    }
}
