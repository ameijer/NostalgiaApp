package com.nostalgia.persistence.repo;

import android.provider.MediaStore;
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
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.runnable.CollectionGetterThread;
import com.nostalgia.runnable.LocationGetterThread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 11/4/15.
 */
public class MediaCollectionRepository {

    public boolean registerCollectionChangedListener(CollectionChangedEventListener callback) {

        if(collectionChangedCallbacks.contains(callback)){
            return false;
        }

        collectionChangedCallbacks.add(callback);
        return true;
    }

    public MediaCollection findOneById(String collectionId) {
        MediaCollection thisVid = null;
                Query query = database.getView("all_collections").createQuery();
            query.setDescending(true);
            List<Object> keys = new ArrayList<Object>();
            keys.add(collectionId);
            query.setKeys(keys);

            QueryEnumerator result = null;
            try {
                result = query.run();
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "error querying for logged in user");

            }

            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                Document cur = it.next().getDocument();
              thisVid =  this.docToCollection(cur);


            }

        if(thisVid == null){
            CollectionGetterThread getter = new CollectionGetterThread(collectionId);
            getter.start();
            try {
                getter.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MediaCollection gotten = getter.getMatching();
            if(gotten != null) return gotten;
        } else return thisVid;
            return null;


    }


    public interface  CollectionChangedEventListener {
        void onCollectionChanged();
    }

    private final ArrayList<CollectionChangedEventListener> collectionChangedCallbacks = new ArrayList<CollectionChangedEventListener>();

    public static final String TAG = "MediaCollRepo";

    private final static ObjectMapper m = new ObjectMapper();
    private final Database database;

    private final Nostalgia app;

    public void updateViews(){
        if(allCollectionsView != null) {
            allCollectionsView.deleteIndex();
        }

    }

    private final View allCollectionsView;
    private LiveQuery allCollectionsQuery;

    public MediaCollectionRepository(Database db, Nostalgia appParam) {
        this.database = db;
        allCollectionsView = database.getView("all_collections");

        this.app = appParam;

        allCollectionsView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object obj = document.get("type");
                if (obj == null) return;
                if (!obj.equals("MediaCollection")) return;
                String collId = document.get("_id").toString();
                emitter.emit(collId, null);
            }
        }, "1.0");


        try {
            this.startAllCollectionsLiveQuery();
        } catch (Exception e) {
            Log.e(TAG, "error starting live query for logged in user", e);

        }

    }
    public Collection<MediaCollection> allMediaCollections(){
        Query query = database.getView("all_collections").createQuery();
        query.setLimit(100);
        ArrayList<MediaCollection> colls = new ArrayList<MediaCollection>();
        try {
            QueryEnumerator result = query.run();

            for (Iterator<QueryRow> it = result; it.hasNext(); ) {

                Document cur = it.next().getDocument();
                MediaCollection thisColl = this.docToCollection(cur);
                colls.add(thisColl);
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return colls;
    }

    public Map<String, MediaCollection> getCollectionsById(Collection<String> collectionIds, boolean fetchMissingCollections) throws Exception {
        Query query = database.getView("all_collections").createQuery();
        query.setKeys(new ArrayList<Object>(collectionIds));

        HashMap<String, MediaCollection> colls = new HashMap<String, MediaCollection>();
        try {
            QueryEnumerator result = query.run();

            for (Iterator<QueryRow> it = result; it.hasNext(); ) {

                Document cur = it.next().getDocument();
                MediaCollection thisColl = this.docToCollection(cur);
                colls.put(cur.getId(), thisColl);
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        Log.i(TAG, collectionIds.size() + " ids were passed in, and " + colls.keySet().size() + " mediacollections were found locally");

        if(collectionIds.size() > colls.keySet().size()) {
            //then we are missing some locations

            Log.w(TAG, "mismatch in number of mediacollections.");

            ArrayList<String> missing = new ArrayList<String>();

            for(String wanted : collectionIds){
                if(!colls.keySet().contains(wanted)){
                    Log.i(TAG, "Missing local info for collections with id: " + wanted);
                    missing.add(wanted);
                }
            }

            Map<String, MediaCollection> found = new HashMap<String, MediaCollection>();
            if (fetchMissingCollections) {
                Log.i(TAG, "retreiving missing collections...");



                for(String missed : missing){
                    CollectionGetterThread getter = new CollectionGetterThread(missed);
                    getter.start();
                    getter.join();

                    MediaCollection gotten = getter.getMatching();
                    Log.i(TAG, "Retreived information for missing collection:" + missed + ": " + gotten.getName());
                    found.put(missed, gotten);

                }

                Log.i(TAG, "missing mediacollections retirevied");
                found.putAll(found);
            }

        }

        return colls;
    }



    private void startAllCollectionsLiveQuery() throws Exception {


        if (allCollectionsQuery == null) {

            allCollectionsQuery = allCollectionsView.createQuery().toLiveQuery();

            allCollectionsQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {

                    for(CollectionChangedEventListener listen : collectionChangedCallbacks) {
                        if(listen != null) {
                            listen.onCollectionChanged();
                        }
                    }


                }
            });

            allCollectionsQuery.start();

        }

    }


    public synchronized void save(MediaCollection collection) {

        Map<String, Object> props = m.convertValue(collection, Map.class);
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

    private MediaCollection docToCollection(Document document) {

        return m.convertValue(document.getProperties(), MediaCollection.class);
    }


}



