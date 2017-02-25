package com.nostalgia.controller.capturemoment.review.places;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters.grid.CompactLocationGridAdapter;
import com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters.grid.LocationArrayGridAdapter;
import com.nostalgia.persistence.model.KnownLocation;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by alex on 11/3/15.
 */
public class ExistingLocationDialogFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private Nostalgia app;
    private ExistingLocationConfirmListener callback;
    protected CompactLocationGridAdapter locationArrayGridAdapter;
    private GridView locationGrid;
    private Button doneButton;

    public static final String TAG = "existingDialog";
    private LocationArrayGridAdapter adapter;
    private final ArrayList<KnownLocation> display = new ArrayList<KnownLocation>();
    private final HashSet<String> selectedId = new HashSet<String>();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        KnownLocation selected = display.get(position);
        view.setBackgroundColor(getResources().getColor(android.R.color.white));

        if(selected == null){
            Log.e(TAG, "null selected value");
        }
        if(!selectedId.contains(selected.get_id())) {
            //select
            selectedId.add(selected.get_id());
            locationGrid.setItemChecked(position, true);
        } else {
            //unselect
            selectedId.remove(selected.get_id());
            locationGrid.setItemChecked(position, false);
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    public interface ExistingLocationConfirmListener{
        void onExistingLocationsSelected(ArrayList<KnownLocation> selected);
    }



    public void setOnConfirmedListener(ExistingLocationConfirmListener listener){
        this.callback = listener;
    }
    public ArrayList<KnownLocation> getDisplay() {
        return display;
    }

    public void notifyChanged(){
        if(adapter!= null) {
            adapter.notifyDataSetChanged();

        }
    }

    private void initLocGridAdapter() {
        locationArrayGridAdapter = new CompactLocationGridAdapter(
                getActivity().getApplicationContext(),
                display, ((Nostalgia)getActivity().getApplication())
        );
        locationGrid.setAdapter(locationArrayGridAdapter);
        locationGrid.setOnItemClickListener(this);
        locationGrid.setOnItemLongClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        app = ((Nostalgia) activity.getApplication());
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " error casting app class");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.existing_location_dialog, container, false);


        this.doneButton = (Button) myView.findViewById(R.id.existing_location_submit);

        this.locationGrid = (GridView) myView.findViewById(R.id.existing_location_selected_grid);


        initLocGridAdapter();
        this.doneButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(callback != null){

                    ArrayList<KnownLocation> selected = findDisplayedWithId();
                    callback.onExistingLocationsSelected(selected);
                } else {
                    Log.e("Existinglocation", "No callback found for confirmation");
                }


            }
        });

        return myView;
    }

    private ArrayList<KnownLocation> findDisplayedWithId() {
        ArrayList<KnownLocation> result = new ArrayList<KnownLocation>();

        for(String key :selectedId){
            for(KnownLocation loc : display){
                if(loc.get_id().equals(key)){
                    result.add(loc);
                }
            }
        }
        return result;
    }

}
