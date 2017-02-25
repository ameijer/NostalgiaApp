package com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters.list;

import android.content.Context;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters.LocationArrayAdapter;
import com.nostalgia.persistence.model.KnownLocation;
import com.vuescape.nostalgia.R;

import java.util.List;

/**
 * Created by alex on 11/19/15.
 */
public class LocationArrayListAdapter extends LocationArrayAdapter {

    public static final String TAG = "LocationArrayListdapter";

    public LocationArrayListAdapter(Context context, List<KnownLocation> objects, Nostalgia app) {
        super(context, R.layout.location_list_item, objects, app.getCollRepo());
    }
}
