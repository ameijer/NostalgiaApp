package com.nostalgia.controller.peek;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.nostalgia.persistence.model.KnownLocation;

/**
 * Created by alex on 1/24/16.
 */
public class LocationFocusActivity extends FragmentActivity {

    private Button backButton;

    public static final String TAG = "LocationFocusAct";

    private LocationMediaListFragment locationVidFragment;
    private LocationInfoFragment locationInfoFragment;
    private KnownLocation focused;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_focus);

        backButton = (Button) findViewById(R.id.location_focus_back);
        backButton.setClickable(true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitLocationFocusActivity();
            }
        });

        //get user from args
        Bundle b = getIntent().getExtras();
        focused = (KnownLocation) b.getSerializable("focused");

        //init frags
        locationVidFragment = LocationMediaListFragment.newInstance(focused);
        locationInfoFragment = LocationInfoFragment.newInstance(focused);

        //bind fragments
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();

        android.support.v4.app.FragmentTransaction trans = manager.beginTransaction();
        trans.add(R.id.location_info_holder, locationInfoFragment, locationInfoFragment.getTag());
        trans.add(R.id.location_media_holder, locationVidFragment, locationVidFragment.getTag());
        trans.commit();

        return;

    }

    private void quitLocationFocusActivity() {
        Intent intent = this.getIntent();
        this.setResult(RESULT_OK, intent);
        finish();
    }
}
