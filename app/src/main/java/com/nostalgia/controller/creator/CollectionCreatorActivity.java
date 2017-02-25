package com.nostalgia.controller.creator;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.runnable.MediaCollectionCreatorThread;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class CollectionCreatorActivity extends AppCompatActivity implements DetailsListener, SharingListener {
    public CollectionCreatorActivity() {
        // Required empty public constructor
    }

    private String mName = "";
    @Override
    public void onNameChange(String name){
        mName = name;
    }

    private VisibilityLevel mVisibility;
    @Override
    public void onVisibilityChanged(VisibilityLevel visibility){
        mVisibility = visibility;
    }

    private ArrayList<User> peopleList = new ArrayList<>();
    @Override
    public void onPersonAdded(User user){
        peopleList.add(user);
    }

    @Override
    public void onPersonRemoved(User user){
        if(peopleList.contains(user)) {
            peopleList.remove(user);
        } else {
            Log.e(TAG, "Tried to remove user that doesn't exist there.");
        }
    }

    @Override
    public void onManyAdded(ArrayList<User> users){
        peopleList.addAll(users);
    }

    @Override
    public void onManyRemoved(ArrayList<User> users){
        peopleList.removeAll(users);
    }

    /*
     * Since each different visibility level might have different settings
     *  (ie. shared might not allow voting), we'll keep track of what the user chooses
     *  for each visibility level, but will only end up using the details
     *  for the visibility level that they ultimately choose.
     */
    private HashMap<String, String> mPrivateDetails = setupPrivateDetails();
    private HashMap<String, String> mSharedDetails = setupSharedDetails();
    private HashMap<String, String> mPublicDetails = setupPublicDetails();
    private HashMap<String, String> mChosenDetails;

    private HashMap<String, String> setupPublicDetails(){
        HashMap<String, String> details = new HashMap<String, String>();
        details.put("description", "");

        return details;
    }

    private HashMap<String, String> setupSharedDetails(){
        HashMap<String, String> details = new HashMap<String, String>();
        details.put("description", "");

        return details;
    }

    private HashMap<String, String> setupPrivateDetails(){
        HashMap<String, String> details = new HashMap<String, String>();
        details.put("description", "");

        return details;
    }

    @Override
    public void onGeneralChange(String param, String value){
        switch(mVisibility) {
            case ALL:
                mPublicDetails.put(param, value);
                break;
            case SHARED:
                mSharedDetails.put(param, value);
                break;
            case PERSONAL:
                mPrivateDetails.put(param, value);
                break;
        }
    }

    private PublicFragment mPublicFragment;
    private SharedFragment mSharedFragment;
    private PrivateFragment mPrivateFragment;

    public static final int RESULT_COLL_CREATED = 2;

    private Toolbar mToolbar;
    private String TAG = "OldCollectionCreatorActivity";

    private ViewPager mPager;

    private final CollectionCreatorActivity self = this;

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[] { "Private", "Groups", "Public"};
        public int NUMBER_PAGES = 3;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {
                case 2:
                    if(null == mPublicFragment)
                        mPublicFragment = PublicFragment.newInstance();
                        mPublicFragment.setDetailsListener(self);
                    }
                    return mPublicFragment;
                case 1:
                    if(null == mSharedFragment){
                        mSharedFragment = SharedFragment.newInstance();
                        mSharedFragment.setDetailsListener(self);
                        mSharedFragment.setSharingListener(self);
                    }
                    return mSharedFragment;
                case 0:
                    if(null == mPrivateFragment){
                        mPrivateFragment = PrivateFragment.newInstance();
                        mPrivateFragment.setDetailsListener(self);
                    }
                    return mPrivateFragment;
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
    private Nostalgia mApp;
    private FloatingActionButton mAcceptButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApp = (Nostalgia) getApplication();

        setContentView(R.layout.activity_create_collection);

        mToolbar = (Toolbar) findViewById(R.id.nostalgia_actionbar_container);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();

        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(new MyPagerAdapter(manager));
        mPager.setOffscreenPageLimit(2);

        TabLayout t = (TabLayout) findViewById(R.id.tabLayout);
        t.setupWithViewPager(mPager);

        Intent intent = getIntent();
        String sectionHeader = intent.getStringExtra("visibility");

        if(sectionHeader != null && !sectionHeader.isEmpty()) {
            if (sectionHeader.equalsIgnoreCase("Public")) {
                mPager.setCurrentItem(2);
                mVisibility = VisibilityLevel.ALL;
            } else if (sectionHeader.equalsIgnoreCase("Groups")) {
                mPager.setCurrentItem(1);
                mVisibility = VisibilityLevel.SHARED;
            } else if (sectionHeader.equalsIgnoreCase("Private")) {
                mPager.setCurrentItem(0);
                mVisibility = VisibilityLevel.PERSONAL;
            } else {
                //default to private
                mPager.setCurrentItem(0);
            }
        }

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch(position) {
                    case 0:
                        mVisibility = VisibilityLevel.ALL;
                        break;
                    case 1:
                        mVisibility = VisibilityLevel.SHARED;
                        break;
                    case 2:
                        mVisibility = VisibilityLevel.PERSONAL;
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mAcceptButton = (FloatingActionButton) findViewById(R.id.fab);
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkIfReady()){
                    prepareForCreation();
                    createCollection();
                }
            }
        });

    }

    private boolean checkIfReady(){
        boolean isReady = true;

        if(null == mName || mName.isEmpty()){
            isReady = false;
        }

        switch(mVisibility) {
            case ALL:
                isReady = mPublicFragment.checkCompleteness();
                break;
            case SHARED:
                isReady = mSharedFragment.checkCompleteness();
                break;
            case PERSONAL:
                isReady = mPrivateFragment.checkCompleteness();
                break;
            default:
                isReady = false;
        }

        return isReady;
    }

    private String mChosenVisibility = "";
    private void prepareForCreation(){
        switch(mVisibility) {
            case ALL:
                mChosenVisibility = "Public";
                mChosenDetails = mPublicDetails;
                break;
            case SHARED:
                mChosenVisibility = "Shared";
                mChosenDetails = mSharedDetails;
                break;
            case PERSONAL:
                mChosenVisibility = "Private";
                mChosenDetails = mPrivateDetails;
                break;
            default:
                Toast.makeText(this, "Error setting visibility.", Toast.LENGTH_LONG).show();
                mChosenVisibility = "Private";
        }
    }

    private void createCollection(){
        MediaCollection col = new MediaCollection();
        col.setName(mName);
        User uploader = mApp.getUserRepo().getLoggedInUser();

        if(uploader == null){
            String msg = "error - must have logged in user to upload location";
            Log.e(TAG, msg);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }

        col.setCreatorId(uploader.get_id());

        col.setVisibility(mChosenVisibility);

        col.setProperties(new HashMap<String,String>());
        col.getProperties().put("CREATOR_NAME", mName);
        col.getProperties().put("DESCRIPTION", mChosenDetails.get("description"));

        /*
         * Add collaborators.
         */
        if(mChosenVisibility.equalsIgnoreCase("Shared")){
            Set<String> readerIds = new TreeSet<>();
            Set<String> writerIds = new TreeSet<>();

            for(User user : peopleList){
                readerIds.add(user.get_id());
                writerIds.add(user.get_id());
            }

            col.setReaders(readerIds);
            col.setWriters(writerIds);
        }


        MediaCollectionCreatorThread creator = new MediaCollectionCreatorThread(col, uploader);
        creator.start();
        try {
            creator.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MediaCollection added = creator.getCreated();
        Toast.makeText(this, "collection: " + added.getName() + " added successfully", Toast.LENGTH_LONG).show();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("CREATED_COLLECTION", added);
        setResult(RESULT_COLL_CREATED, returnIntent);
        finish();
        return;
    }

    public void cancel() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
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
