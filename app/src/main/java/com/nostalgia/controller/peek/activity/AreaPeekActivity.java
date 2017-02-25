package com.nostalgia.controller.peek.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.LocationFocusActivity;
import com.nostalgia.controller.peek.picker.locationdisplayers.grid.LocationGridFragment;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.runnable.DiscreteLocationBboxThread;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.List;

public class AreaPeekActivity extends AppCompatActivity implements LocationGridFragment.LocationPickedListener, MainFragment.OnMapChangedListener, BaseRecyclerFragment.LocationPickedListener {

    public static final int PEEK_ACTIVITY_REQUEST_CODE = 432;
    private static final double DEFAULT_WIDTH_VALUE = 5.0;
    protected FragmentManager mainFragmentManager;
    private Nostalgia app ;
    private LocationRepository locRepo;
    private Button doneButton;



    private MainFragment mapFrag;

    public static final String TAG = "AreaPeekActivity";

    private final String RECYCLER_FRAG_TAG = "RecyclerFragment";
    
    private double focusedLat;
    private double focusedLong;
    private double widthInMi; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peek_map);

        this.doneButton = (Button) findViewById(R.id.peek_map_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitPeekMapActivity();
            }
        });
        this.app = (Nostalgia) getApplication();
        locRepo = this.app.getLocationRepository();
        //get user from args
        Bundle b = getIntent().getExtras();
        Location focused = b.getParcelable("focuspoint");
        double width = b.getDouble("widthmi");
        
        if(focused == null){
            //get current location
            focused = app.getLocation(); 
        }
        
        if(focused != null){
            focusedLat = focused.getLatitude();
            focusedLong = focused.getLongitude(); 
        }
        
        if(width == 0.0 ){
            widthInMi = DEFAULT_WIDTH_VALUE;
        } else {
            widthInMi = width;
        }

        mapFrag =MainFragment.newInstance(null);

        mapFrag.setLocationPickedListener(this);
        mapFrag.setOnMapChangedListener(this);



//        //bind fragments
//        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
//
//        android.support.v4.app.FragmentTransaction trans = manager.beginTransaction();
//        trans.add(R.id.peek_map_holder, mapFrag, mapFrag.getTag());
//        trans.add(R.id.peek_list_holder, locDisplayFrag , locDisplayFrag.getTag());
//        trans.commit();
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.add(R.id.fragment, mapFrag);
        trans.commit();


    }

    private void quitPeekMapActivity() {

            Intent intent = this.getIntent();
            this.setResult(RESULT_OK, intent);
            finish();

    }

    @Override
    public void onMapChanged(double[] visibleArea){
        //query backend for locations within this focused area
        DiscreteLocationBboxThread locationGetter = null;
        try {

            locationGetter = new DiscreteLocationBboxThread(visibleArea);
            locationGetter.start();
            locationGetter.join();
        } catch (Exception e) {
            Log.e(TAG, "error building bbox thread", e);
        }



        List<KnownLocation> visibles = locationGetter.getMatching();

        if(visibles == null){
            visibles = new ArrayList<KnownLocation>();
        }

        //pass list of locations back to mapfragment to plot as POIs
        mapFrag.updateLocations(visibles);


    }


    @Override
    public void onCancelled(){
        //TODO: Nothing.
    }

    @Override
    public void onLocationPicked(KnownLocation selected) {
        //open location focus page
        goToFocusActivity(selected);

    }


    private void goToFocusActivity(KnownLocation focused){
        Intent focusIntent = new Intent(this, LocationFocusActivity.class);
        focusIntent.putExtra("focused", focused);
        startActivity(focusIntent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            default:
                Toast.makeText(AreaPeekActivity.this, "Not yet implemented", Toast.LENGTH_LONG).show();
                break;
        }
    }



}
