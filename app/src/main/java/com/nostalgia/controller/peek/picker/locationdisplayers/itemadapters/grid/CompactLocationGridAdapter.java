package com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters.grid;

import android.content.Context;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters.LocationArrayAdapter;
import com.nostalgia.persistence.model.KnownLocation;
import com.vuescape.nostalgia.R;

import java.util.List;

/**
 * Created by alex on 11/19/15.
 */
public class CompactLocationGridAdapter extends LocationArrayAdapter {

    public static final String TAG = "CompactLocationGridAdapter";

    public CompactLocationGridAdapter(Context context, List<KnownLocation> objects, Nostalgia app) {
        super(context, R.layout.location_grid_item_compact, objects, app.getCollRepo());
    }
}
