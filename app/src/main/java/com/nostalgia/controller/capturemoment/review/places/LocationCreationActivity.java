package com.nostalgia.controller.capturemoment.review.places;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.nostalgia.persistence.model.KnownLocation;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;


public class LocationCreationActivity extends AppCompatActivity implements LocationCreationFragment.OnLocationCreateListener{
    private static final int PROFILE_SETTING = 1;

    public static final int RESULT_LOC_CREATED = 3;

    /*
     * When using LocationCreationActivity for getting an intent result.
     */
    public static final int MULTI_SELECT = 1;

    int ACTION_TAKE_VIDEO = 1;
    protected FragmentManager mainFragmentManager;
    private Toolbar mToolbar;

    private LocationCreationFragment locationCreationFragment;

    public static final String TAG = "LocationCreationActivity";

    private String mTargetVideoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_creation);


        final LocationCreationActivity me = this;
        locationCreationFragment = LocationCreationFragment.newInstance();
        locationCreationFragment.setCallback(me);

        mToolbar = (Toolbar) findViewById(R.id.nostalgia_actionbar_container);
        setSupportActionBar(mToolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mainFragmentManager = getSupportFragmentManager();

        //initially, mStart with the choice fragment
        FragmentTransaction fragTransaction = mainFragmentManager.beginTransaction();
        fragTransaction.add(R.id.map_wrapper, locationCreationFragment);
        fragTransaction.commit();
    }

    public void backClicked(View view) {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void cancel() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onLocationCreated(KnownLocation added){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("CREATED_LOCATION", added);
        setResult(RESULT_LOC_CREATED, returnIntent);
        finish();
    }

    @Override
    public void onLocationsChosen(ArrayList<KnownLocation> selected) {

        ArrayList<String> ids = new ArrayList<String>();
        for(int i = 0; i <selected.size(); i++){
            ids.add(selected.get(i).get_id());
        }

        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra("selectedLocations", ids);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            cancel();
            return true;
        } else {
            return false;
        }
    }
}
