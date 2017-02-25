package com.nostalgia.persistence.model;


import org.geojson.GeoJsonObject;
import org.geojson.Point;

import java.io.Serializable;

/**
 * Created by alex on 11/8/15.
 */
public class LocationUpdate implements Serializable {

    private GeoJsonObject location;
    private String userId;

    public LocationUpdate(Point location, String userId) {
        this.location = location;
        this.userId = userId;
    }

    public LocationUpdate(){}


    public GeoJsonObject getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
