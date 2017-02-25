package com.nostalgia.controller.peek;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.nostalgia.persistence.model.KnownLocation;

/**
 * Created by alex on 1/25/16.
 */
public class LocationMediaListFragment extends Fragment{
    public static LocationMediaListFragment  newInstance(KnownLocation key){
        Bundle args = new Bundle();
        args.putSerializable("location", key);
        LocationMediaListFragment  fragment = new LocationMediaListFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
