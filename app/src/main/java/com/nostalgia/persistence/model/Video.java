package com.nostalgia.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.geojson.GeoJsonObject;
import org.geojson.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by alex on 11/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Video implements Serializable, Comparable<Video> {

    public static final String WHO_PRIVATE = "PRIVATE";
    public static final String WHO_FRIENDS = "FRIENDS";
    public static final String WHO_EVERYONE = "EVERYONE";
    public static final String WHEN_NOW = "NOW";
    public static final String WHEN_HOUR = "HOUR";
    public static final String WHEN_DAY = "ONE_DAY";
    public static final String WHEN_WIFI = "WIFI";

    public static final String WHERE_HERE = "HERE";
    public static final String WHERE_EVERYWHERE = "EVERYWHERE";
    public static final String SOUND_MUTE = "MUTE";
    public static final String SOUND_ENABLED = "ENABLED";

	@JsonIgnore
	public String getChannelName(){
		return _id.substring(0, 8);
	}
	
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1435169949509311014L;
	
	private String _id = UUID.randomUUID().toString();
    private String type = this.getClass().getSimpleName();
    private long versionNumber;
    private long dateCreated;
    boolean enabled = false;

    private String url;
    public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

    //ptrs to atomic documents

    //atomic counters
	private String favoriteCounterId;
    private String skipCounterId;
    private String viewCounterId;

    //these are updated using atomic prepend
    private String upvoteTrackerId;
    private String downvoteTrackerId;
    private String flagTrackerId;


    private String status;
    private Point location;

    //channels that this document itself is in
    private List<String> channels;

    private Map<String, String> properties;

    private String ownerId;

    private List<String> thumbNails;

    public Video(){
        if(properties == null){
            properties = new HashMap<String, String>();
        }
        if(channels == null){
            channels = new ArrayList<String>();
            channels.add(this.getChannelName());
        }

    }
    public String get_id() {
        return _id;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public List<String> getThumbNails() {
        return thumbNails;
    }

    public void setThumbNails(List<String> thumbs) {
        this.thumbNails = thumbs;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        for (int i = 0; i < _id.length(); i++) {
            hash = hash*17 + _id.charAt(i);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        return this.hashCode() == obj.hashCode();
    }


    @Override
    public int compareTo(Video another) {
        if(another.getDateCreated() > this.getDateCreated()){
            //other video is newer
            return -1;
        }

        if(another.getDateCreated() < this.getDateCreated()){
            return 1;
        } else return 0;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getFavoriteCounterId() {
        return favoriteCounterId;
    }

    public void setFavoriteCounterId(String favoriteCounterId) {
        this.favoriteCounterId = favoriteCounterId;
    }

    public String getSkipCounterId() {
        return skipCounterId;
    }

    public void setSkipCounterId(String skipCounterId) {
        this.skipCounterId = skipCounterId;
    }

    public String getViewCounterId() {
        return viewCounterId;
    }

    public void setViewCounterId(String viewCounterId) {
        this.viewCounterId = viewCounterId;
    }

    public String getUpvoteTrackerId() {
        return upvoteTrackerId;
    }

    public void setUpvoteTrackerId(String upvoteTrackerId) {
        this.upvoteTrackerId = upvoteTrackerId;
    }

    public String getDownvoteTrackerId() {
        return downvoteTrackerId;
    }

    public void setDownvoteTrackerId(String downvoteTrackerId) {
        this.downvoteTrackerId = downvoteTrackerId;
    }

    public String getFlagTrackerId() {
        return flagTrackerId;
    }

    public void setFlagTrackerId(String flagTrackerId) {
        this.flagTrackerId = flagTrackerId;
    }
}
