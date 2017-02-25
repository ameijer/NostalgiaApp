package com.nostalgia.controller.peek.picker.mediadisplayers.recycler;

import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.persistence.model.MediaCollection;
import com.vuescape.nostalgia.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/*
 * Created by Aidan on 11/29/15.
 */
public class CollectionSelectorFragment extends BaseRecyclerFragment {

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     *
     * Nothing currently stored in the args Bundle, but maybe
     * another time.
     */
    public static CollectionSelectorFragment newInstance(boolean isMultiSelectable) {
        CollectionSelectorFragment f = new CollectionSelectorFragment();
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
     }

    @Override
    public void addNewItem(MediaCollection toAdd, String section, MediaCollectionWrapper.WrapperType type) {
        getCollectionAdapter().addItemToSection(toAdd, section, type);
    }

    @Override
    public void loadCollectionGroups() {
        try {
            ArrayList<MediaCollection> personal = null;
            personal = buildPersonalList();
            if(personal.isEmpty()){
                getCollectionAdapter().addEmptyGroup("Personal", "Only you can use these.", "Privately remember fun trips, track progress towards fitness goals, and anything else you can think of!", MediaCollectionWrapper.WrapperType.PERSONAL, CollectionAdapter.HeaderAction.PLUS);
            } else {
                getCollectionAdapter().addGroup("Personal", "Only you can use these.", personal, MediaCollectionWrapper.WrapperType.PERSONAL, CollectionAdapter.HeaderAction.PLUS);
            }

            ArrayList<MediaCollection> shared = buildSharedList();
            if(shared.isEmpty()){
                getCollectionAdapter().addEmptyGroup("Group", "Must know the secret handshake.", "You don't seem to be in any private communities.", MediaCollectionWrapper.WrapperType.SHARED, CollectionAdapter.HeaderAction.PLUS);
            } else {
                getCollectionAdapter().addGroup("Group", "Must know the secret handshake.", shared, MediaCollectionWrapper.WrapperType.SHARED, CollectionAdapter.HeaderAction.PLUS);
            }
            ArrayList<MediaCollection> pub = buildPublicList();
            if(pub.isEmpty()){
                getCollectionAdapter().addEmptyGroup("Public", "Anybody can use these.", "Find the best videos for your interests.", MediaCollectionWrapper.WrapperType.PUBLIC, CollectionAdapter.HeaderAction.PLUS);
            } else {
                getCollectionAdapter().addGroup("Public", "Anybody can use these.", pub, MediaCollectionWrapper.WrapperType.PUBLIC, CollectionAdapter.HeaderAction.PLUS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading collection groups.");
        }

        return;
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
    private ArrayList<MediaCollection> buildPersonalList() throws Exception {
        //Step 1.
        ArrayList<MediaCollection> collectionList = new ArrayList<>();
        List<String> personalColls =  getUser().getAllPrivateCollections();

        Map<String, MediaCollection> personalMap = getCollRepo().getCollectionsById(personalColls, true);

        Collection<String> sortedList = new TreeSet<String>(Collator.getInstance());

        //Map the collection titles to the collection IDs
        Map<String, MediaCollection> titleToColl = new HashMap<String,MediaCollection>();

        //Step 2-3
        for(String collectionKey : personalMap.keySet()){
            MediaCollection target = personalMap.get(collectionKey);

            if(target.getName().contains(getUser().get_id() + "_priv")){
                //remove default "all my public" media collection
            } else {
                //collectionList.add(target);
                titleToColl.put(target.getName(), target);
                sortedList.add(target.getName());
            }
        }

        /*
         * Convert the sortedList of titles to a sorted list of collections
         */
        for(String title : sortedList){
            collectionList.add(titleToColl.get(title));
        }

        return collectionList;
    }

    private ArrayList<MediaCollection> buildSharedList() throws Exception {
        ArrayList<MediaCollection> collectionList = new ArrayList<>();
        List<String> sharedColls =  getUser().getAllSharedCollections();

        Map<String, MediaCollection> sharedMap = getCollRepo().getCollectionsById(sharedColls, true);

        Collection<String> sortedList = new TreeSet<String>(Collator.getInstance());

        //Map the collection titles to the collection IDs
        Map<String, MediaCollection> titleToColl = new HashMap<String,MediaCollection>();

        //Step 2-3
        for(String collectionKey : sharedMap.keySet()){
            MediaCollection target = sharedMap.get(collectionKey);

            if(target.getName().contains(getUser().get_id() + "_shared")){
                //remove default "all my public" media collection
            } else {
                //collectionList.add(target);
                titleToColl.put(target.getName(), target);
                sortedList.add(target.getName());
            }
        }

        /*
         * Convert the sortedList of titles to a sorted list of collections
         */
        for(String title : sortedList){
            collectionList.add(titleToColl.get(title));
        }

        return collectionList;
    }

    private ArrayList<MediaCollection> buildPublicList() throws Exception {
        //getUser().getPublicVideos();
        ArrayList<MediaCollection> collectionList = new ArrayList<>();
        List<String> publicColls =  getUser().getAllPublicCollections();
        Map<String, MediaCollection> publicMap = getCollRepo().getCollectionsById(publicColls, true);


        Collection<String> sortedList = new TreeSet<String>(Collator.getInstance());

        //Map the collection titles to the collection IDs
        Map<String, MediaCollection> titleToColl = new HashMap<String,MediaCollection>();

        //Step 2-3
        for(String collectionKey : publicMap.keySet()){
            MediaCollection target = publicMap.get(collectionKey);

            if(target.getName().contains(getUser().get_id() + "_pub")){
                //remove default "all my public" media collection
            } else {
                //collectionList.add(target);
                titleToColl.put(target.getName(), target);
                sortedList.add(target.getName());
            }
        }

        /*
         * Convert the sortedList of titles to a sorted list of collections
         */
        for(String title : sortedList){
            collectionList.add(titleToColl.get(title));
        }


        return collectionList;
    }
}
