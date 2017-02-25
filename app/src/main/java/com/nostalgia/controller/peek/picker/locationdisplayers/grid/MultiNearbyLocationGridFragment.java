package com.nostalgia.controller.peek.picker.locationdisplayers.grid;

import android.view.View;
import android.widget.AdapterView;

import com.nostalgia.persistence.model.KnownLocation;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;

/**
 * Class to choose multiple locations. Used to geotag an upload.
 *
 */
public class MultiNearbyLocationGridFragment extends NearbyLocationGridFragment {
    private ArrayList<KnownLocation> mLocationSelections = new ArrayList<KnownLocation>();

    public ArrayList<KnownLocation> getSelectedLocations(){
        return mLocationSelections;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        KnownLocation selected = getLocation(position);

        if(mLocationSelections.contains(selected)) {
            mLocationSelections.remove(selected);

            view.setBackgroundColor(getResources().getColor(R.color.md_red_500));
            view.setElevation(0);
        } else {
            mLocationSelections.add(selected);
            view.setBackgroundColor(getResources().getColor(R.color.md_white_1000));
            view.setElevation(5);
        }
    }
}
