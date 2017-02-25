package com.nostalgia.controller.peek.picker;

import android.content.Intent;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.drawer.MaterialDrawerActivity;
import com.nostalgia.controller.capturemoment.MainCaptureActivity;
import com.nostalgia.controller.capturemoment.review.places.LocationCreationActivity;
import com.nostalgia.controller.exoplayer.VideoPlayerFragment;
import com.nostalgia.controller.peek.CreatorLauncher;
import com.nostalgia.controller.peek.picker.mediadisplayers.NearbyOrSubSelectorFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.SoloLocationsFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.playback.ViewerActivity;

import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.VideoRepository;

import com.nostalgia.runnable.UserAttributeUpdaterThread;
import com.nostalgia.view.FabFrameLayout;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocationsActivity extends MaterialDrawerActivity implements BaseRecyclerFragment.SelectionListener, VideoRepository.VideoEventListener, CreatorLauncher{

    protected FragmentManager mainFragmentManager;

    //save our header or result
    private Nostalgia app ;

    private VideoRepository vidRepo;

    private NearbyOrSubSelectorFragment mNearbyOrSubSelectorFragment;
    private SoloLocationsFragment mSoloLocationsFragment;

    private User loggedIn;
    private Toolbar mToolbar;

    private String locationName;

    private final ArrayList<Video> mVideos = new ArrayList<Video>();

    public static final String TAG = "PeekPicker";

    private ViewPager mPager;

    @Override
    public void launchCreator(String visibility) {
        //visibility doesn't matter for locations, always public.
        Intent creator = new Intent(this, LocationCreationActivity.class);
        startActivityForResult(creator, 1);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[] { "All Videos", "Mine Only"};
        public int NUMBER_PAGES = 2;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {
                case 0:
                    mNearbyOrSubSelectorFragment = (null == mNearbyOrSubSelectorFragment) ? NearbyOrSubSelectorFragment.newInstance(false) : mNearbyOrSubSelectorFragment;
                    return mNearbyOrSubSelectorFragment;
                case 1:
                    mSoloLocationsFragment = (null == mSoloLocationsFragment) ?  SoloLocationsFragment.newInstance(false) : mSoloLocationsFragment;
                    return mSoloLocationsFragment;
                default:
                    Log.e(TAG, "Null fragment added to viewpager");
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            return NUMBER_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    private LinearLayout mTextOverlay;
    private TextView mGotIt;
    private String hasSeenIntro;

    private boolean mIsToolbarShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peek_picker);

        app = (Nostalgia) getApplication();
        vidRepo = app.getVidRepo();

        loggedIn = app.getUserRepo().getLoggedInUser();

        mToolbar = (Toolbar) findViewById(R.id.nostalgia_actionbar_container);
        setSupportActionBar(mToolbar);

        final LocationsActivity me = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        attachDrawer(savedInstanceState, mToolbar);

        FloatingActionButton fabToggleToolbar = (FloatingActionButton) findViewById(R.id.fabtoolbar_fab);
        final FABToolbarLayout fabToolbarLayout = (FABToolbarLayout) findViewById(R.id.fabtoolbar);
        fabToggleToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsToolbarShown){
                    fabToolbarLayout.hide();

                } else {
                    fabToolbarLayout.show();

                }
                mIsToolbarShown = !mIsToolbarShown;
            }
        });


        ImageView createCollection = (ImageView) findViewById(R.id.add_new_collection);
        ImageView searchForCollections = (ImageView) findViewById(R.id.search_collections);

        createCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCreator("create");
            }
        });

        searchForCollections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRoadTrip();
            }
        });

        mTextOverlay = (LinearLayout) findViewById(R.id.text_overlay);

        if(null != loggedIn){
            hasSeenIntro = loggedIn.getSettings().get("seen_loc_intro");

            if(null == hasSeenIntro || hasSeenIntro.isEmpty()){
                loggedIn.getSettings().put("seen_loc_intro", "false");
                hasSeenIntro = "false";

                try {
                    Map<String, String> changed = new HashMap<>();
                    changed.put("seen_loc_intro", "false");
                    UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(loggedIn.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
                    updatr.start();


                    app.getUserRepo().save(loggedIn);

                } catch (Exception e) {
                    Log.e(TAG, "ERROR SAVING USER", e);
                }
            }
        }

        if(hasSeenIntro.equals("false")) {
            mTextOverlay.setVisibility(View.VISIBLE);

            mGotIt = (TextView) findViewById(R.id.got_it_button);
            mGotIt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTextOverlay.animate().translationY(-mTextOverlay.getHeight()).setInterpolator(new AccelerateInterpolator(3));
                    loggedIn.getSettings().put("seen_loc_intro", "true");

                    try {
                        Map<String, String> changed = new HashMap<>();
                        changed.put("seen_loc_intro", "true");
                        UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(loggedIn.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
                        updatr.start();


                        app.getUserRepo().save(loggedIn);
                    } catch (Exception e) {
                        Log.e(TAG, "ERROR SAVING USER", e);

                    }
                }
            });
        }

        /*
         * Hide fabToolbar if somebody clicks off screen.
         */
        FabFrameLayout pickerRoot = (FabFrameLayout) findViewById(R.id.picker_root);
        pickerRoot.setFabListener(fabToolbarLayout);

        /*
         * Before viewpager
         */
        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        int w = mPager.getMeasuredWidth();
        int h = mPager.getMeasuredHeight();

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout t = (TabLayout) findViewById(R.id.tabLayout);
        t.setupWithViewPager(mPager);
    }

    private void goToRoadTrip(){
        Toast.makeText(this, "Haha, nice try.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCancelled(){
    }

    @Override
    public void onSelection(Object selection, boolean isSelected) {
        MediaCollection col = (MediaCollection) selection;
        if(vidRepo != null){
            vidRepo.setCallback(this);
        }

        loadCollectionVideos(col);
        locationName = col.getName();
        if(0 < mVideos.size()){
            watchStoryline();
        } else {
            Toast.makeText(this, "No videos in " + locationName + ".", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * @loadLocationVideos
     * 1. Clear existing video set
     * 2. Map<String (orderKey), String (videoId)> of the location's mVideos.
     * 3. Sort orderKey (which might be by time or popularity, ie. priorities)
     * 4. Convert priority Strings to priority Ints for easier ordering
     * 5. Sort priority Ints
     * 6. Iterate through priorityInts and use as Key to grab videoId from Matches
     * 7. Use videoId to grab Video objects from the vidRepo, add Video to mVideos
     */

    private void loadCollectionVideos(MediaCollection coll){
        //Step 1.
        mVideos.clear();

        //Step 2.
        //String mediaCollId = location.getLocationCollections().get("primary");
        //coll = ((Nostalgia) getApplication()).getCollRepo().findOneById(mediaCollId);

        Map<String, String> matches = coll.getMatchingVideos();

        //Step 3.
        Set<String> priorities = matches.keySet();

        //Step 4.

        for(String rawString : priorities){

            Video complete = vidRepo.findOneById(rawString, true, true);
            mVideos.add(complete);
        }
    }

    public void watchStoryline(){
        //mStart demo player
        Intent mpdIntent = new Intent(this, ViewerActivity.class);
        mpdIntent.putExtra("playlist", mVideos);
        mpdIntent.putExtra("location_name", locationName);
        mpdIntent.putExtra(VideoPlayerFragment.CONTENT_TYPE_EXTRA, VideoPlayerFragment.TYPE_HLS);
        startActivityForResult(mpdIntent, ViewerActivity.VIDEO_FOCUS);
    }

    @Override
    public void onVideosChanged(Map<String, Long> localVideoIds) {

        //update our reference
        mVideos.clear();

        //add all remaining
        for(String newAdded : localVideoIds.keySet()){
            Video toAdd = this.vidRepo.findOneById(newAdded, true, false);
            mVideos.add(toAdd);
        }

        Collections.sort(mVideos);

        if(null != mNearbyOrSubSelectorFragment){
            mNearbyOrSubSelectorFragment.notifyDataSetChanged();
        }

        if(null != mSoloLocationsFragment){
            mSoloLocationsFragment.notifyDataSetChanged();
        }
    }

    private void goToCaptureNow(){
        Intent captureNowIntent = new Intent(this, MainCaptureActivity.class);
        startActivity(captureNowIntent);
    }

    private void goToCollections(){
        Intent collectionsIntent = new Intent(this, CollectionsActivity.class);
        startActivity(collectionsIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        switch(requestCode) {
            case LocationCreationActivity.RESULT_LOC_CREATED:
                try {
                    KnownLocation toAdd = (KnownLocation) data.getSerializableExtra("CREATED_LOCATION");
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                Log.e(TAG, "Unexpected activity result: " + Integer.toString(requestCode));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_peek_picker, menu);
        menu.findItem(R.id.action_button_collections).getActionView().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToCollections();
                    }
                }
        );
        menu.findItem(R.id.action_button_capturenow).getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCaptureNow();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}
