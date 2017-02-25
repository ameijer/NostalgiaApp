package com.nostalgia.persistence.model;

import android.util.Log;

import java.util.*;

import org.geojson.Point;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;

/**
 * Created by alex on 11/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    private HashSet<String> silentSubscriptions;



    @JsonIgnore
    public String getChannelName(){
        return _id.substring(0, 8);
    }
    /**
     *
     */
    private static final long serialVersionUID = 3672636185090518520L;

    private String type = this.getClass().getSimpleName();
    private long versionNumber;
    private String _id = UUID.randomUUID().toString();
    private String seenVideosPtr;
    private String username;

    private String passwordPtr;

    public String getPasswordPtr() {
        return passwordPtr;
    }

    public void setPasswordPtr(String password) {
        this.passwordPtr = password;
    }

    private String homeRegion = "us_east";

    //these are updated using atomic prepend
    private String upvoteTrackerId;

    //list of channels user has access to
    private List<String> admin_channels;
    private List<String> admin_roles;

    private HashMap<String, String> locationHistory;

    private Map<String, String> streamTokens;

    //channel -> time
    private Map<String, String> video_channels;

    public Map<String, String> getVideo_channels() {
        return video_channels;
    }


    public void setVideo_channels(Map<String, String> video_channels) {
        this.video_channels = video_channels;
    }
    //channels that this document itself is in
    private List<String> channels;

    private boolean disabled = false;

    private String email;

    private Point focusedLocation;


    private Point lastKnownLoc;
    private long lastLocationUpdate;

    private long dateJoined;
    private long lastSeen;

    private Map<String, String> collections;

    private Set<String> location_channels;

    private List<Account> accountsList;

    public Set<String> getLocation_channels() {
        return location_channels;
    }
    public Set<String> silentSubscribeToLocations(Collection<KnownLocation> values) {

        for(KnownLocation toSubTo : values){
            if(!silentSubscriptions.contains(toSubTo.get_id())){
                //add to set
                silentSubscriptions.add(toSubTo.get_id());

                //add to admin chanells
                admin_channels.add(toSubTo.getChannelName());

            }
        }

        return silentSubscriptions;
    }

    public void setLocation_channels(Set<String> location_channels) {
        this.location_channels = location_channels;
    }


    public HashSet<String> getUser_channels() {
        return user_channels;
    }


    public void setUser_channels(HashSet<String> user_channels) {
        this.user_channels = user_channels;
    }

    private HashSet<String> user_channels;

    private HashMap<String, String> friends;
    private HashMap<String, String> pendingFriends;
    private Map<String, String> settings;

    private Map<String, String> accounts;

    //mapping   locId -> timesubscribed
    private Map<String, String> userLocations;
    private String icon;

    private List<String> authorizedDevices;
    private String token;

    private String syncToken;

    private List<String> createdLocations;


    public String getSyncToken(){
        return syncToken;
    }
    public Map<String, String> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, String> accounts) {
        this.accounts = accounts;
    }

    public List<String> getAuthorizedDevices() {
        return authorizedDevices;
    }

    public void setAuthorizedDevices(ArrayList<String> arrayList) {
        this.authorizedDevices = arrayList;
    }


    public User(){
        if(this.userLocations == null){
            userLocations = new HashMap<String, String>();
        }

        if(this.locationHistory== null){
            locationHistory = new HashMap<String, String>();
        }

        if(this.collections == null){
            collections = new HashMap<String, String>();
        }
        if (this.silentSubscriptions == null) {
            this.silentSubscriptions = new HashSet<String>();
        }
        if(this.accountsList == null){
            accountsList = new ArrayList<Account>();
        }
        if(this.createdLocations == null){
            createdLocations = new ArrayList<String>();
        }
        if(friends == null){
            friends = new HashMap<String, String>();
        }
        if(pendingFriends == null){
            pendingFriends = new HashMap<String, String>();
        }
        if(settings == null){
            settings = new HashMap<String,String>();
        }
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public long getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(long dateJoined) {
        this.dateJoined = dateJoined;
    }

    public Map<String, String> getFriends() {
        return friends;
    }

    public void setFriends(HashMap<String, String> friends) {
        this.friends = friends;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Point getLastKnownLoc() {
        return lastKnownLoc;
    }

    public void setLastKnownLoc(Point lastKnownLoc) {
        this.lastKnownLoc = lastKnownLoc;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setSyncToken(String session) {
        this.syncToken = session;

    }

    public List<String> getAdmin_roles() {
        return admin_roles;
    }

    public void setAdmin_roles(List<String> admin_roles) {
        this.admin_roles = admin_roles;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<String> getAdmin_channels() {
        return admin_channels;
    }

    public void setAdmin_channels(List<String> admin_channels) {
        this.admin_channels = admin_channels;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHomeRegion() {
        return homeRegion;
    }

    public void setHomeRegion(String homeRegion) {
        this.homeRegion = homeRegion;
    }


    public long getLastLocationUpdate() {
        return lastLocationUpdate;
    }


    public void setLastLocationUpdate(long lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }


    public Point getFocusedLocation() {
        return focusedLocation;
    }


    public void setFocusedLocation(Point focusedLocation) {
        this.focusedLocation = focusedLocation;
    }


    public Map<String, String> getStreamTokens() {
        return streamTokens;
    }


    public void setStreamTokens(Map<String, String> streamTokens) {
        this.streamTokens = streamTokens;
    }


    public Map<String, String> getUserLocations() {
        return userLocations;
    }


    public void setUserLocations(Map<String, String> userLocations) {
        this.userLocations = userLocations;
    }



    public HashMap<String, String> getLocationHistory() {
        return locationHistory;
    }


    private void setLocationHistory(HashMap<String, String> history) {
        this.locationHistory = history;
    }


    public long getVersionNumber() {
        return versionNumber;
    }


    public void setVersionNumber(long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Map<String, String> getCollections() {
        return collections;
    }


    public void setCollections(Map<String, String> collections) {
        this.collections = collections;
    }

    public List<Account> getAccountsList() {
        return accountsList;
    }


    public void setAccountsList(List<Account> accountsList) {
        this.accountsList = accountsList;
    }

    public String getUpvoteTrackerId() {
        return upvoteTrackerId;
    }

    public void setUpvoteTrackerId(String upvoteTrackerId) {
        this.upvoteTrackerId = upvoteTrackerId;
    }


    public static class Account{
        public String name;
        public String id;
        public String email;
    }


    public List<String> getCreatedLocations() {

        return createdLocations;
    }

    public void setCreatedLocations(List<String> createdLocations) {
        this.createdLocations = createdLocations;

    }


    public HashMap<String, String> getPendingFriends() {
        return pendingFriends;
    }


    public void setPendingFriends(HashMap<String, String> pendingFriends) {
        this.pendingFriends = pendingFriends;
    }

    @JsonIgnore
    public String getPublicVideoCollId() {
        try {
            JSONObject key = new JSONObject();
            JSONObject visibility = new JSONObject();
            visibility.put("visibility", MediaCollection.PUBLIC);
            key.put("key", this.get_id() + "_pub");
            JSONArray  ordered = new JSONArray();
            ordered.put(visibility);
            ordered.put(key);

            String matching = collections.get(ordered.toString());
            return matching;
        } catch (JSONException e) {
            Log.e("user", "json excpetion", e);
        }
        return null;
    }

    @JsonIgnore
    public String getPrivateVideoCollId() {
        try {
            JSONObject key = new JSONObject();
            JSONObject visibility = new JSONObject();
            visibility.put("visibility", MediaCollection.PRIVATE);
            key.put("key", this.get_id() + "_priv");
            JSONArray  ordered = new JSONArray();
            ordered.put(visibility);
            ordered.put(key);
            String matching = collections.get(ordered.toString());
            return matching;
        } catch (JSONException e) {
            Log.e("user", "json excpetion", e);
        }
        return null;
    }

    @JsonIgnore
    public String getSharedVideoCollId() {
        try {
            JSONObject key = new JSONObject();
            JSONObject visibility = new JSONObject();
            visibility.put("visibility", MediaCollection.SHARED);
            key.put("key", this.get_id() + "_shared");
            JSONArray  ordered = new JSONArray();
            ordered.put(visibility);
            ordered.put(key);
            String matching = collections.get(ordered.toString());
            return matching;
        } catch (JSONException e) {
            Log.e("user", "json excpetion", e);
        }
        return null;
    }


    @JsonIgnore
    public String findCollection(String visibility, String key) {


        JSONObject keyobj = new JSONObject();
        JSONObject visibilityobj = new JSONObject();
        try {
            visibilityobj.put("visibility", visibility);

            keyobj.put("key", key);
            JSONArray ordered = new JSONArray();
            ordered.put(visibilityobj);
            ordered.put(keyobj);
            String matching = collections.get(ordered.toString());
            return matching;
        } catch (JSONException e) {
            Log.e("user", "json excpetion", e);
        }
        return null;

    }

    @JsonIgnore
    public List<String> findCollectionbyTag(String key) {
        String[] visibilities = new String[] {MediaCollection.PRIVATE, MediaCollection.PUBLIC, MediaCollection.SHARED};
        ArrayList<String> results = new ArrayList<String>();
        try{
            for(String visibility : visibilities){
                JSONObject keyobj = new JSONObject();
                JSONObject visibilityobj = new JSONObject();
                visibilityobj.put("visibility", visibility);
                keyobj.put("key", key);
                JSONArray  ordered = new JSONArray();
                ordered.put(visibilityobj);
                ordered.put(key);
                String matchingAtViz = collections.get(ordered.toString());
                if(matchingAtViz != null){
                    results.add(matchingAtViz);
                }
            }

            if(results.size() < 1) return null;
            return results;
        } catch (JSONException e) {
            Log.e("user", "json excpetion", e);
        }
        return null;
    }

    @JsonIgnore
    public List<String> getAllPublicCollections(){
        List<String> results = getAllWithVisibilityLevel(MediaCollection.PUBLIC);
        return results;
    }

    @JsonIgnore
    private ArrayList<String> getAllWithVisibilityLevel(String visibilityLevel){
        ArrayList<String> results = new ArrayList<String>();

        for(String key:collections.keySet()){
            if(key.contains(visibilityLevel)){
                //fetch this and add to list
                results.add(collections.get(key));
            }
        }
        return results;
    }

    @JsonIgnore
    public List<String> getAllMySubmittedLocationVideos(){
        ArrayList<String> results = new ArrayList();
        for(String key : collections.keySet()){
            if(key.contains(this._id + ":")){
                results.add(collections.get(key));
            }
        }
        return results;
    }

    @JsonIgnore
    public List<String> getAllSharedCollections(){
        List<String> results = getAllWithVisibilityLevel(MediaCollection.SHARED);
        return results;
    }

    @JsonIgnore
    public List<String> getAllPrivateCollections(){
        List<String> results = getAllWithVisibilityLevel(MediaCollection.PRIVATE);
        return results;
    }

}
