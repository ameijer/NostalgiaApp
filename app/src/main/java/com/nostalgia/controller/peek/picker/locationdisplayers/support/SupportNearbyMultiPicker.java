package com.nostalgia.controller.peek.picker.locationdisplayers.support;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.nostalgia.persistence.model.KnownLocation;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;

/**
 * Class to choose multiple locations. Used to geotag an upload.
 *
 */
public class SupportNearbyMultiPicker extends SupportNearbyLocationGridFragment {
    private ArrayList<KnownLocation> mLocationSelections = new ArrayList<KnownLocation>();


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        KnownLocation selected = getLocation(position);
        ImageView checkmark = (ImageView) view.findViewById(R.id.loc_select_checkmark);

        if(mLocationSelections.contains(selected)) {
            checkmark.setVisibility(View.INVISIBLE);
            mLocationSelections.remove(selected);
            view.setBackgroundColor(getResources().getColor(R.color.md_red_500));
            try {
                view.setElevation(0);
            } catch (NoSuchMethodError e) {
                //Old SDK doesn't have setElevation
            }
        } else {
            checkmark.setVisibility(View.VISIBLE);
            mLocationSelections.add(selected);
            view.setBackgroundColor(getResources().getColor(R.color.md_white_1000));
            try {
                view.setElevation(5);
            } catch (NoSuchMethodError e){
                //Old SDK doesn't have setElevation
            }
        }
    }

    public ArrayList<KnownLocation> getSelectedLocations(){
        return mLocationSelections;
    }


}
