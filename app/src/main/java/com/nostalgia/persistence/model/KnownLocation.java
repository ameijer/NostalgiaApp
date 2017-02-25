package com.nostalgia.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by alex on 11/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnownLocation implements Serializable {
    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    private  List<String> categories;

    @JsonIgnore
    public String getChannelName(){
        return _id.substring(0, 8);
    }
    private String _id = UUID.randomUUID().toString();

    private String name;

    //channels that this document itself is in
    private List<String> channels;
    private Map<String, String> locationCollections;
    private Feature location;
    private long versionNumber;
    private Map<Long, String> sponsoredVideos;

    private Map<String, String> properties;
    private List<String> thumbnails;

    //these are updated using atomic prepend
    private String upvoteTrackerId;
    private String downvoteTrackerId;

    private String creatorId;

    public KnownLocation(){
        if(channels == null){
            channels = new ArrayList<String>();
            channels.add(this.getChannelName());
        }

        if(thumbnails == null){
            thumbnails = new ArrayList<String>();
        }
        if(locationCollections == null){
            locationCollections = new HashMap<String, String>();
        }
        if(properties == null){
            properties = new HashMap<String, String>();
        }

        if(categories == null){
            categories = new ArrayList<String>();
        }

    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
        this.channels.clear();
        channels.add(this.getChannelName());

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Feature getLocation() {
        return location;
    }

    public void setLocation(Feature location) {
        this.location = location;
    }

    public Map<Long, String> getSponsoredVideos() {
        return sponsoredVideos;
    }

    public void setSponsoredVideos(Map<Long, String> sponsoredVideos) {
        this.sponsoredVideos = sponsoredVideos;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }


    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }


    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    @Override
    @JsonIgnore
    public String toString(){
        StringBuffer buf = new StringBuffer();
        buf.append("Name: " + name + "\n");
        buf.append("ID: " + _id + "\n");
        buf.append("Location: " + location + "\n");

        return buf.toString();
    }

    public List<String> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<String> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Map<String, String> getLocationCollections() {
        return locationCollections;
    }

    public void setLocationCollections(Map<String, String> locationCollections) {
        this.locationCollections = locationCollections;
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
}