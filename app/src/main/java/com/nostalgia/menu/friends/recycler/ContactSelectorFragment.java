package com.nostalgia.menu.friends.recycler;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.menu.friends.model.ContactCard;
import com.nostalgia.menu.friends.model.PersonWrapper;
import com.nostalgia.persistence.model.User;
import com.nostalgia.service.ContactablesLoaderCallbacks;

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
public class ContactSelectorFragment extends BaseRecyclerFragment implements ContactablesLoaderCallbacks.ContactDisplayer{

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     *
     * Nothing currently stored in the args Bundle, but maybe
     * another time.
     */
    public static ContactSelectorFragment newInstance(boolean isMultiSelectable) {
        ContactSelectorFragment f = new ContactSelectorFragment();
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
        outState.putParcelableArrayList("COLLECTION_WRAPPERS", mContactAdapter.getCollectionWrappers());
    }


    @Override
    public void loadCollectionGroups() {
        try {
            ArrayList<ContactCard> recentContacts = null;
            recentContacts = buildPendingList();
            if(!recentContacts.isEmpty()){
                getAdapter().addGroup("New", "New to your network", recentContacts, PersonWrapper.WrapperType.PENDING_INC, ContactAdapter.HeaderAction.NONE);
            }

            ArrayList<ContactCard> normalContacts = null;
            normalContacts = buildNormalList();
            if(normalContacts.isEmpty()){
                getAdapter().addEmptyGroup("People", "", "Nobody here, yet!", PersonWrapper.WrapperType.NATIVE, ContactAdapter.HeaderAction.NONE);
            } else {
                getAdapter().addGroup("People", "", normalContacts, PersonWrapper.WrapperType.NATIVE, ContactAdapter.HeaderAction.NONE);
            }

            ArrayList<ContactCard> phoneContacts = null;
            if(mPhoneContacts == null) {
                phoneContacts = buildPhoneList();
            } else {
                phoneContacts = mPhoneContacts;
            }

            if(phoneContacts.isEmpty()){
                getAdapter().addEmptyGroup("Phone Contacts", "Sync your phone contacts", "Build shared collections more easily by using contacts you already have.", PersonWrapper.WrapperType.PHONE, ContactAdapter.HeaderAction.NONE);
            } else {
                getAdapter().addGroup("Phone Contacts", "", phoneContacts, PersonWrapper.WrapperType.PHONE, ContactAdapter.HeaderAction.NONE);
            }

            ArrayList<ContactCard> socialContacts = null;
            socialContacts = buildSocialList();
            if(socialContacts.isEmpty()){
                getAdapter().addEmptyGroup("Social Contacts", "Sync your social contacts", "Build shared collections more easily by using contacts you already have.", PersonWrapper.WrapperType.SOCIAL, ContactAdapter.HeaderAction.NONE);
            } else {
                getAdapter().addGroup("Social Contacts", "", socialContacts, PersonWrapper.WrapperType.SOCIAL, ContactAdapter.HeaderAction.NONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading collection groups.");
        }

        return;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
    private ArrayList<ContactCard> buildPendingList() throws Exception {

        ArrayList<ContactCard> incomingContacts = new ArrayList<>();
        Map<String,String> allPending =  getUser().getPendingFriends();

        List<String> requesterIds = new ArrayList<>();

        for(Map.Entry<String, String> pendings : allPending.entrySet()){

            //pending friends I requested have Sent_<time of request> in value
            //pending requests others hae sent me have Received_<time of request>
            if(pendings.getValue().contains("Received")){
                //we have a pending friends request
                requesterIds.add(pendings.getKey());
            }
        }

        ArrayList<User> users = new ArrayList<>();
        for(String id : requesterIds){
            User requested = getUserRepo().findOneById(id);
            users.add(requested);
        }

        for(User target : users){
            ContactCard cc = new ContactCard();
            cc.extractFromUser(target);
            incomingContacts.add(cc);
        }

        return incomingContacts;
    }

    private ArrayList<ContactCard> buildNormalList() throws Exception {
        Map<String,String> normalIdMap = getUser().getFriends();
        ArrayList<ContactCard> normalContacts = new ArrayList<>();

        List<String> normalIds = new ArrayList<>();
        normalIds.addAll(normalIdMap.keySet());

        ArrayList<User> users = new ArrayList<>();
        for(String id : normalIds){
            User requested = getUserRepo().findOneById(id);
            users.add(requested);
        }

        for(User target : users){
            ContactCard cc = new ContactCard();
            cc.extractFromUser(target);
            normalContacts.add(cc);
        }

        return normalContacts;
    }

    private ArrayList<ContactCard> buildPhoneList() {

        if(mPhoneContacts == null) {
            loadAllPhoneContacts();
        }

        mPhoneContacts = new ArrayList<>();
        return mPhoneContacts;
    }

    @Override
    public void onResume(){
        super.onResume();

        if(null == mPhoneContacts) {
            loadAllPhoneContacts();
        }
    }

    public static final String QUERY_KEY = "query";
    public static final int CONTACT_QUERY_LOADER = 0;
    /**
     * Assuming this activity was started with a new intent, process the incoming information and
     * react accordingly.
     */

    private ContactablesLoaderCallbacks mLoaderCallbacks;
    public void loadAllPhoneContacts() {
        String query = "";

        // We need to create a bundle containing the query string to send along to the
        // LoaderManager, which will be handling querying the database and returning results.
        Bundle bundle = new Bundle();
        bundle.putString(ContactablesLoaderCallbacks.QUERY_KEY, query);

        ContactablesLoaderCallbacks loaderCallbacks = new ContactablesLoaderCallbacks(getContext());
        loaderCallbacks.setContactDisplayer(this);

        // Start the loader with the new query, and an object that will handle all callbacks.
        getActivity().getLoaderManager().restartLoader(CONTACT_QUERY_LOADER, bundle, loaderCallbacks);

    }

    public static <T> void initLoader(final int loaderId, final Bundle args, final LoaderManager.LoaderCallbacks<T> callbacks,
                                      final LoaderManager loaderManager) {
        final Loader<T> loader = loaderManager.getLoader(loaderId);
        if (loader != null && !loader.isReset()) {
            loaderManager.restartLoader(loaderId, args, callbacks);
        } else {
            loaderManager.initLoader(loaderId, args, callbacks);
        }
    }

    private ArrayList<ContactCard> buildSocialList() throws Exception {
        ArrayList<ContactCard> socialContacts = new ArrayList<>();
        return socialContacts;
    }

    private ArrayList<ContactCard> mPhoneContacts;
    @Override
    public void onContactsLoaded(ArrayList<ContactCard> phoneContacts) {
        /*
         * Sort alphabetically
         */
        Collection<String> sortedSet = new TreeSet<String>(Collator.getInstance());
        //Map the collection titles to the collection IDs
        Map<String, ContactCard> nameToCard = new HashMap<String,ContactCard>();

        /*
         * Some contacts have the same display name. Make names unique and add to a sorted treeset.
         */
        //Step 2-3
        for(ContactCard card : phoneContacts){
            String name = card.getName();
            if(null == name || name.isEmpty()){
                name = "0";
            }

            //Find a unique name, in case of duplicates
            Integer iter = 0;
            while(null != nameToCard.get(name)){
                name = name + iter.toString();
                iter = iter + 1;
            }

            nameToCard.put(name, card);
            sortedSet.add(name);
        }

        /*
         * Convert the sortedList of names to a list of contacts that are sorted by names.
         */
        ArrayList<ContactCard> sortedList = new ArrayList<ContactCard>();
        for(String name : sortedSet){
            sortedList.add(nameToCard.get(name));
        }

        mPhoneContacts = sortedList;
        loadCollectionGroups();
    }
}
