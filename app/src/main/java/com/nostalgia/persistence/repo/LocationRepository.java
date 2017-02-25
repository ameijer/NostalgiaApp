package com.nostalgia.persistence.repo;

import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.runnable.LocationGetterThread;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alex on 11/4/15.
 */
public class LocationRepository {

    public boolean registerNearbyLocationListener(NearbyLocationEventListener callback) {

        if(nearbyCallbacks.contains(callback)){
            return false;
        }

        nearbyCallbacks.add(callback);
        return true;
    }
    public boolean unregisterNearbyLocationListener(NearbyLocationEventListener callback) {

        return nearbyCallbacks.remove(callback);

    }

    public boolean registerSubscribedLocationListener(SubscribedLocationEventListener callback) {

        if(subscribedCallbacks.contains(callback)){
            return false;
        }

        subscribedCallbacks.add(callback);
        return true;
    }

    public boolean unregisterSubscribedLocationListener(SubscribedLocationEventListener callback) {

        return subscribedCallbacks.remove(callback);

    }

    public interface NearbyLocationEventListener{
        void onNearbyLocationsChanged(List<KnownLocation> updated);

    }

    public interface  SubscribedLocationEventListener{
        void onSubscribedLocationsChanged(List<KnownLocation> updated);
    }

    private final ArrayList<NearbyLocationEventListener> nearbyCallbacks = new ArrayList<NearbyLocationEventListener>();
    private final ArrayList<SubscribedLocationEventListener> subscribedCallbacks = new ArrayList<SubscribedLocationEventListener>();

    public ArrayList<KnownLocation> getNearbyLocations() {

        this.updateNearbys();
        return nearby;
    }
    public ArrayList<KnownLocation> getSubscribedLocations() {
        this.updateSubscribed();
        return subscribed;
    }
    final private ArrayList<KnownLocation> nearby = new ArrayList<KnownLocation>();
    final private ArrayList<KnownLocation> subscribed = new ArrayList<KnownLocation>();

    public static final String TAG = "LocationRepository";

    private final static ObjectMapper m = new ObjectMapper();
    private final Database database;


    private final Nostalgia app;
    public void updateViews(){
        if(nearbyLocationView != null) {
            nearbyLocationView.deleteIndex();
        }

        if(subscribedLocationView != null){
            subscribedLocationView.deleteIndex();
        }
    }

    private final View nearbyLocationView;
    private LiveQuery nearbyLocationQuery;

    private final View subscribedLocationView;
    private LiveQuery subscribedLocationQuery;

    private final View allLocationsView;

    public LocationRepository(Database db, Nostalgia appParam) {
        this.database = db;
        nearbyLocationView = database.getView("nearbyLocation");
        subscribedLocationView = database.getView("subscribedLocation");
        allLocationsView = database.getView("allLocations");
        this.app = appParam;

        allLocationsView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object obj = document.get("type");
                if(obj == null) return;
                if(!obj.equals("KnownLocation")) return;
                String locId = document.get("_id").toString();
                emitter.emit(locId, null);


            }
        }, "1.0");

        nearbyLocationView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object obj = document.get("type");
                if(obj == null) return;
                if(!obj.equals("KnownLocation")) return;

                User current = app.getUserRepo().getLoggedInUser();
                if(current == null) return;
                String locId = document.get("_id").toString();

                //if the updated location is in the channels
                if(current.getLocation_channels().contains(locId)){

                    emitter.emit(locId, System.currentTimeMillis());

                }
            }
        }, "1.1");

        try {
            this.startNearbyLocationsLiveQuery();
        } catch (Exception e) {
            Log.e(TAG, "error starting live query for logged in user", e);

        }


        subscribedLocationView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object obj = document.get("type");
                if (obj == null) return;
                if (!obj.equals("KnownLocation")) return;

                User current = app.getUserRepo().getLoggedInUser();
                if(current == null) return;
                String locId = document.get("_id").toString();

                //if the updated location is in the channels
                if (current.getUserLocations().keySet().contains(locId)) {
                    emitter.emit(locId, System.currentTimeMillis());
                }
            }
        }, "1.0");

        try {
            this.startSubscribedLocationsLiveQuery();
        } catch (Exception e) {
            Log.e(TAG, "error starting live query for logged in user", e);

        }

    }
    public Collection<KnownLocation> nearbyLocations(){
        Query query = database.getView("nearbyLocation").createQuery();
        query.setLimit(100);
        ArrayList<KnownLocation> loc = new ArrayList<KnownLocation>();
        try {
            QueryEnumerator result = query.run();

            for (Iterator<QueryRow> it = result; it.hasNext(); ) {

                Document cur = it.next().getDocument();
                KnownLocation thisUser = this.docToLocation(cur);
                loc.add(thisUser);
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return loc;
    }

    //locationIds is the collection of ids that you want the full location information for
    //if there is an id in locationIds whose location is not cached locally, set fetchMissingLocations to true to use REST api to get anything we dont have locally from the server. Note this does NOT store missing locations in the database (persistMissingLocations handles that)
    //if you want the fetched locations to persist in the database (and be updated continuously) set persistMissingLocations to true. not that this has side effects in the user object as the user object will now be subscribed to the missing location channels
    public Map<String, KnownLocation> getLocationsById(Collection<String> locationIds, boolean fetchMissingLocations, boolean persistMissingLocations) throws Exception {
        Query query = database.getView("allLocations").createQuery();
        query.setKeys(new ArrayList<Object>(locationIds));

        HashMap<String, KnownLocation> loc = new HashMap<String, KnownLocation>();
        try {
            QueryEnumerator result = query.run();

            for (Iterator<QueryRow> it = result; it.hasNext(); ) {

                Document cur = it.next().getDocument();
                KnownLocation thisUser = this.docToLocation(cur);
                loc.put(cur.getId(), thisUser);
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        Log.i(TAG, locationIds.size() + " ids were passed in, and " + loc.keySet().size() + " locations were found locally");

        if(locationIds.size() > loc.keySet().size()) {
            //then we are missing some locations

            Log.w(TAG, "mismatch in number of locations.");

            ArrayList<String> missing = new ArrayList<String>();

            for(String wanted : locationIds){
                if(!loc.keySet().contains(wanted)){
                    Log.i(TAG, "Missing local location info for location with id: " + wanted);
                    missing.add(wanted);
                }
            }

            Map<String, KnownLocation> found = new HashMap<String, KnownLocation>();
            if (fetchMissingLocations) {
                Log.i(TAG, "retreiving missing locations...");



                for(String missed : missing){
                    LocationGetterThread getter = new LocationGetterThread(missed);
                    getter.start();
                    getter.join();

                    KnownLocation gotten = getter.getMatching();
                    Log.i(TAG, "Retreived location information for missing location " + missed + ": " + gotten.getName());
                    found.put(missed, gotten);

                }

                Log.i(TAG, "missing locations retirevied");
                loc.putAll(found);
            }

            if (fetchMissingLocations && persistMissingLocations) {
                Log.i(TAG, "Saving locations to database");

                for(KnownLocation foundLoc : found.values()) {
                    this.save(foundLoc);
                }

            }
        }

        return loc;
    }

    public Collection<KnownLocation> subscribedLocations(){
        Query query = database.getView("subscribedLocation").createQuery();
        query.setLimit(100);
        ArrayList<KnownLocation> loc = new ArrayList<KnownLocation>();
        try {
            QueryEnumerator result = query.run();

            for (Iterator<QueryRow> it = result; it.hasNext(); ) {

                Document cur = it.next().getDocument();
                KnownLocation thisUser = this.docToLocation(cur);
                loc.add(thisUser);
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return loc;
    }

    private void updateNearbys(){
        nearby.clear();
        nearby.addAll(nearbyLocations());


    }

    private void updateSubscribed(){
        subscribed.clear();
        subscribed.addAll(subscribedLocations());
    }
    private void startNearbyLocationsLiveQuery() throws Exception {


        if (nearbyLocationQuery == null) {

            nearbyLocationQuery = nearbyLocationView.createQuery().toLiveQuery();

            nearbyLocationQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {

                    updateNearbys();
                    for(NearbyLocationEventListener listen : nearbyCallbacks) {
                        if(listen != null) {
                            listen.onNearbyLocationsChanged(nearby);
                        }
                    }


                }
            });

            nearbyLocationQuery.start();

        }

    }

    private void startSubscribedLocationsLiveQuery() throws Exception {


        if (subscribedLocationQuery == null) {

            subscribedLocationQuery = subscribedLocationView.createQuery().toLiveQuery();

            subscribedLocationQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {

                    updateSubscribed();
                    for(SubscribedLocationEventListener listen : subscribedCallbacks) {
                        if(listen != null) {
                            listen.onSubscribedLocationsChanged(subscribed);
                        }
                    }

                }
            });

            subscribedLocationQuery.start();

        }

    }

    public synchronized void save(KnownLocation vid) {

        Map<String, Object> props = m.convertValue(vid, Map.class);
        String id = (String) props.get("_id");

        Document document;
        if (id == null) {
            document = database.createDocument();
        } else {
            document = database.getExistingDocument(id);
            if (document == null) {
                document = database.getDocument(id);
            } else {
                props.put("_rev", document.getProperty("_rev"));
            }
        }

        try {
            document.putProperties(props);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private KnownLocation docToLocation(Document document) {

        return m.convertValue(document.getProperties(), KnownLocation.class);
    }


}



