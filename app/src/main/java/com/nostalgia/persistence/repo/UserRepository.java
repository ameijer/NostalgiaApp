package com.nostalgia.persistence.repo;

import android.annotation.TargetApi;
import android.os.Build;
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
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.nostalgia.runnable.UserGetterThread;

/**
 * Created by alex on 11/4/15.
 */
public class UserRepository {

    public void setCallback(UserEventListener callback) {
        this.callback = callback;
    }


    public User unAuthorize(User toUnAuth, final String deviceIdToRemove) throws CouchbaseLiteException {

        Document current = database.getDocument(toUnAuth.get_id());
        if(current == null) return null;
        current.update(new Document.DocumentUpdater() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public boolean update(UnsavedRevision newRevision) {
                Map<String, Object> properties = newRevision.getUserProperties();
                String devices = properties.get("authorizedDevices").toString();
                JSONArray authorized = null;

                try {
                    authorized = new JSONArray(devices);

                    if(authorized == null){
                        return true;
                    }

                    int len = authorized.length();

                    for (int i=0;i<len;i++) {
                        if (authorized.get(i).toString().equalsIgnoreCase(deviceIdToRemove)) {
                            authorized.remove(i);
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "error deserializing authorizeddevices array", e);
                    return true;
                }

                if(authorized.length() > 0) {
                    properties.put("authorizedDevices", authorized.toString());
                } else {
                    properties.put("authorizedDevices", null);
                }
                newRevision.setUserProperties(properties);
                return true;
            }
        });


        return this.docToUser(current);
    }

    public boolean delete(User toDelete) throws CouchbaseLiteException {
        Document matching = database.getDocument(toDelete.get_id());
        if(matching == null) return false;
        matching.purge();
        return true;

    }

    public User findOneById(String loggingInId) {
        Query query = database.getView("usersbyid").createQuery();
        query.setDescending(true);
        List<Object> keys = new ArrayList<Object>();
        keys.add(loggingInId);
        query.setKeys(keys);

        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for logged in user");
            return null;
        }


        ArrayList<User> all = new ArrayList<User>();
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            Document cur = it.next().getDocument();
            User thisUser = this.docToUser(cur);
            return thisUser;

        }


        //otherwise, find friend from api
        UserGetterThread getter = new UserGetterThread(loggingInId);
        getter.start();
        try {
            getter.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "error joining thread");
        }

        User matching = getter.getMatching();

        return matching;


    }

    public interface UserEventListener{
        void onUpdate(String updatedUserId);
        void onLogout();

        void onChannelUpdate(List<String> channels);
    }

    private UserEventListener callback;

    public enum State {
        CONNECTED, DISCONNECTED
    }

    public static final String TAG = "UserRepository";

    private final static ObjectMapper m = new ObjectMapper();
    static {
        m.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }
    private final Database database;
    private Synchronize sync;

    //views
    private final View authorizedUserView;
    private LiveQuery authorizedUserliveQuery;

    //views
    private final View userChannelView;
    private LiveQuery userChannelliveQuery;

    private final View allUsersById;


    private State state;

    private final Nostalgia app;

    private final String deviceId;

    public UserRepository(Database db, Nostalgia app) {
        this.database = db;
        authorizedUserView = database.getView("authorized");
        this.app = app;
        this.deviceId = app.getDeviceId();
        authorizedUserView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object type = document.get("type");
                if(type == null) return;

                if(!type.equals("User")) return;
                Object raw = document.get("authorizedDevices");
                if(raw != null && raw.toString().length() > 3) {

                    if (raw.toString().contains(deviceId)) {
                        emitter.emit("deviceId", raw.toString());
                    }
                }
            }
        }, "1.0");

        allUsersById = database.getView("usersbyid");
        allUsersById.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                Object type = document.get("type");
                if(type == null) return;
                if(!type.equals("User")) return;
                Object raw = document.get("_id");
                if(raw != null && raw.toString().length() > 3) {
                    emitter.emit(raw.toString(), null);
                }
            }
        }, "1.0");

        userChannelView = database.getView("userchannel");
        userChannelView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object type = document.get("type");
                if(type == null) return;
                if(!type.equals("User")) return;
                Object rawChannels = document.get("admin_channels");
                Object raw = document.get("authorizedDevices");
                Object id = document.get("_id");
                if (raw.toString().contains(deviceId)) {
                    emitter.emit(id, rawChannels);
                }
            }
        }, "1.0");

        try {
            this.startUserLiveQuery();
        } catch (Exception e) {
            Log.e(TAG, "error starting live query for logged in user", e);

        }

        try {
            this.startChannelLiveQuery();
        } catch (Exception e) {
            Log.e(TAG, "error starting live query for logged in user", e);

        }
    }

    public synchronized void save(User user) throws Exception {


        Map<String, Object> props = m.convertValue(user, Map.class);
        String id = (String) props.get("_id");

        if (id == null) {
            throw new Exception("error - id field required");
        }

        User existing = this.findOneById(user.get_id());

        Document document;

        document = database.getExistingDocument(id);
        if (document == null) {
            document = database.getDocument(id);
        } else {
            props.put("_rev", document.getProperty("_rev"));
        }


        try {
            document.putProperties(props);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error updating properties", e);
            final List<SavedRevision> conflicts = document.getConflictingRevisions();
            Log.i(TAG, "number of conflicts: " + conflicts.size());
            if (conflicts.size() > 1) {
                // There is more than one current revision, thus a conflict!
                handleUserConflict(document);
            }
        }

        if(!document.getProperties().keySet().contains("_id")){
            throw new Exception("NO USER ID SAVED - THIS IS BAD BAD BAD");
        }

    }

    private void handleUserConflict(final Document document) throws Exception {
        final List<SavedRevision> conflicts = document.getConflictingRevisions();

        String id = document.getId();
        Log.w(TAG, "conflict detected in document " + id +". The Local document will ALWAYS ALWAYS lose. To save changes, POST them to the API");

        document.delete();


        Log.w(TAG, "document deleted. triggering re-sync to download server's version of document " + id);
        app.restartReplication();
    }

    private Map<String, Object> mergeUserRevisions(List<SavedRevision> conflicts) {

        //simply take all the fields from the most recent sequence number
        SavedRevision latest = conflicts.get(0);
        for(SavedRevision rev : conflicts){
            if(rev.getSequence() > latest.getSequence()){
                latest = rev;
            }
        }
        return latest.getProperties();
    }

    private User docToUser(Document document) {
        Map<String, Object> props = document.getProperties();
        try {
            User result = m.convertValue(props, User.class);
            // User newUser = new JSONDeserializer<User>().deserialize(, User.class);
            return result;
        } catch (Exception e){
            Log.e(TAG, "error converting user value");
            //attempt repair
            try {
                document.purge();
            } catch (CouchbaseLiteException e1) {
                e1.printStackTrace();
            }
            return null;
        }

    }


    public User getLoggedInUser(){
        Query query = authorizedUserView.createQuery();
        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for logged in user");
            return null;
        }


        HashMap<String, Document> saved = new HashMap<String, Document>();
        ArrayList<User> loggedIns = new ArrayList<User>();
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            Document cur = it.next().getDocument();
            User thisUser = this.docToUser(cur);
            loggedIns.add(thisUser);
            saved.put(thisUser.get_id(), cur);
        }

        if(loggedIns.size() == 0){
            return null;
        }


        if (loggedIns.size() > 1) {
            Log.w(TAG, "too many authorized users. logging all but the most recent out.");
            int i = 0;
            long mostRecent = 0;
            int index = 0;

            for(; i < loggedIns.size(); i ++){
                User cur = loggedIns.get(i);
                if(cur.getLastSeen() > mostRecent){
                    index = i;
                    mostRecent = cur.getLastSeen();
                }
            }

            User toSave = loggedIns.remove(index);

            for(User tologout : loggedIns){
                Document toPurge = saved.get(tologout.get_id());
                if(toPurge != null){
                    try {
                        toPurge.purge();
                    } catch (Exception e){
                        Log.e(TAG, "unable to purge doc", e);
                    }
                }

            }

            return toSave;


        } else {
            return loggedIns.get(0);
        }

    }

    private void startChannelLiveQuery() throws Exception {


        if (userChannelliveQuery == null) {

            userChannelliveQuery= userChannelView.createQuery().toLiveQuery();

            userChannelliveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    if(callback != null){
                        User loggedIn = getLoggedInUser();
                        if(loggedIn == null){
                            //callback.onLogout();
                        } else {
                            List<String> channels = loggedIn.getAdmin_channels();
                            callback.onChannelUpdate(channels);
                        }
                    }
                }
            });
            userChannelliveQuery.start();
        }
    }

    private void startUserLiveQuery() throws Exception {


        if (authorizedUserliveQuery == null) {

            authorizedUserliveQuery = authorizedUserView.createQuery().toLiveQuery();

            authorizedUserliveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    if(callback != null){
                        User loggedIn = getLoggedInUser();
                        if(loggedIn == null){
                        } else {
                            String id = getLoggedInUser().get_id();
                            callback.onUpdate(id);
                        }


                    }
                }
            });

            authorizedUserliveQuery.start();

        }

    }
}



