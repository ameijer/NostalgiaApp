package com.nostalgia.controller.peek.picker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.nostalgia.controller.creator.CollectionCreatorActivity;
import com.nostalgia.controller.drawer.MaterialDrawerActivity;
import com.nostalgia.controller.capturemoment.MainCaptureActivity;

import com.nostalgia.controller.exoplayer.VideoPlayerFragment;
import com.nostalgia.controller.peek.CreatorLauncher;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.CollectionSelectorFragment;
import com.nostalgia.controller.peek.playback.ViewerActivity;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.VideoRepository;
import com.nostalgia.runnable.UserAttributeUpdaterThread;
import com.nostalgia.view.FabFrameLayout;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CollectionsActivity extends MaterialDrawerActivity implements BaseRecyclerFragment.SelectionListener, VideoRepository.VideoEventListener, CreatorLauncher {
    protected FragmentManager mainFragmentManager;
    //save our header or result
    private Nostalgia app ;

    private VideoRepository vidRepo;

    private CollectionSelectorFragment mMineSelectorFragment;
    private CollectionSelectorFragment mEveryoneSelectorFragment;

    private User loggedIn;

    private Toolbar mToolbar;

    private final ArrayList<Video> mVideos = new ArrayList<Video>();

    public static final String TAG = "RecyclerUserMedia";

    private ViewPager mPager;

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[] { "All Videos", "Only Mine"};
        public int NUMBER_PAGES = 2;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {
                case 0:
                    if(null == mEveryoneSelectorFragment){
                        mEveryoneSelectorFragment = CollectionSelectorFragment.newInstance(false);
                    }
                    return mEveryoneSelectorFragment;
                case 1:
                    if(null == mMineSelectorFragment){
                        mMineSelectorFragment = CollectionSelectorFragment.newInstance(false);
                    }
                    return mMineSelectorFragment;
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
        setContentView(R.layout.activity_collection_picker);

        app = (Nostalgia) getApplication();
        vidRepo = app.getVidRepo();

        loggedIn = app.getUserRepo().getLoggedInUser();

        mToolbar = (Toolbar) findViewById(R.id.nostalgia_actionbar_container);
        setSupportActionBar(mToolbar);

        final CollectionsActivity me = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        loggedIn = app.getUserRepo().getLoggedInUser();
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

        //ImageView createCollection = (ImageView) findViewById(R.id.add_new_collection);
        ImageView searchForCollections = (ImageView) findViewById(R.id.search_collections);

        searchForCollections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSearchCollection();
            }
        });

        mTextOverlay = (LinearLayout) findViewById(R.id.text_overlay);

        if(null != loggedIn){
            hasSeenIntro = loggedIn.getSettings().get("seen_col_intro");

            //User does not initialize settings map, so must check if null every time.
            if(null == hasSeenIntro || hasSeenIntro.isEmpty()){
                loggedIn.getSettings().put("seen_col_intro", "false");
                hasSeenIntro = "false";

                try {

                    Map<String, String> changed = new HashMap<>();
                    changed.put("seen_col_intro", "false");
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
                    //getAcceptedListener().onAccepted("LOCATION_SEED");
                    mTextOverlay.animate().translationY(-mTextOverlay.getHeight()).setInterpolator(new AccelerateInterpolator(3));
                    loggedIn.getSettings().put("seen_col_intro", "true");

                    try {
                        Map<String, String> changed = new HashMap<>();
                        changed.put("seen_col_intro", "true");
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
         * Hide fab toolbar if somebody touches elsewhere.
         */
        FabFrameLayout pickerRoot = (FabFrameLayout) findViewById(R.id.picker_root);
        pickerRoot.setFabListener(fabToolbarLayout);

        mEveryoneSelectorFragment = (null == mEveryoneSelectorFragment) ? CollectionSelectorFragment.newInstance(false) : mEveryoneSelectorFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.collection_picker, mEveryoneSelectorFragment).commit();
    }

    private void goToSearchCollection(){
        Toast.makeText(this, "Haha, nice try.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCancelled(){
    }

    private String selectedCollectionName;

    @Override
    public void onSelection(Object selection, boolean isSelected) {
        //ignore isSelected parameter.
        MediaCollection collection = (MediaCollection) selection;

        if(vidRepo != null) {
            vidRepo.setCallback(this);
        }

        loadCollectionVideos(collection);
        selectedCollectionName = collection.getName();
        if(0 < mVideos.size()) {
            watchStoryline();
        } else {
            Toast.makeText(this, "No videos in " + selectedCollectionName + ".", Toast.LENGTH_LONG).show();
        }
    }

    private void loadCollectionVideos(MediaCollection mediaCollection){
        mVideos.clear();
        Collection<String> matches = mediaCollection.getMatchingVideos().keySet();

        for(String id : matches){
            Video complete = vidRepo.findOneById(id, true, true);
            mVideos.add(complete);
        }
    }

    public void watchStoryline(){
        //mStart demo player
        Intent mpdIntent = new Intent(this, ViewerActivity.class);
        mpdIntent.putExtra("playlist", mVideos);

        mpdIntent.putExtra("location_name", selectedCollectionName);
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
    }

    private void goToPeekPicker(){
        Intent peekPickerIntent = new Intent(this, LocationsActivity.class);
        startActivity(peekPickerIntent);
    }

    private void goToCaptureNow(){
        Intent captureNowIntent = new Intent(this, MainCaptureActivity.class);
        startActivity(captureNowIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode) {
            case CollectionCreatorActivity.RESULT_COLL_CREATED:
                //No use for colId here, but I'm leaving this for potential future use.
                try {

                    MediaCollection toAdd = (MediaCollection) data.getSerializableExtra("CREATED_COLLECTION");
                    String vis = toAdd.getVisibility();
                    MediaCollectionWrapper.WrapperType type;
                    String groupName;
                    if(vis.equals("Private")) {
                        type = MediaCollectionWrapper.WrapperType.PERSONAL;
                        groupName = "Personal";
                    } else if(vis.equals("Shared")) {
                        type = MediaCollectionWrapper.WrapperType.SHARED;
                        groupName = "Groups";
                    } else if(vis.equals("Public")) {
                        type = MediaCollectionWrapper.WrapperType.PUBLIC;
                        groupName = "Public";
                    } else {
                        Log.e(TAG, "Unrecognized visibility, setting to private. Could be dangerous behavior.");
                        vis = "Private";
                        type = MediaCollectionWrapper.WrapperType.PERSONAL;
                        groupName = "Personal";
                    }

                    mEveryoneSelectorFragment.addNewItem(toAdd, groupName, type);
                    mMineSelectorFragment.addNewItem(toAdd, groupName, type);
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                Log.e(TAG, "User Media unexpected resultCode: " + Integer.toString(resultCode));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_mediacollections, menu);
        menu.findItem(R.id.action_button_peekpicker).getActionView().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToPeekPicker();
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

    @Override
    public void launchCreator(String visibility){
        Intent creator = new Intent(this, CollectionCreatorActivity.class);
        creator.putExtra("visibility", visibility);
        startActivityForResult(creator, 1);
    }
}
