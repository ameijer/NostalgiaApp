package com.nostalgia.controller.introduction;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;
import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.persistence.model.User;
import com.vuescape.nostalgia.R;

public class IntroductionActivity extends AppCompatActivity implements IntroFragment.OnAcceptedListener, BaseRecyclerFragment.SelectionListener {

    private ViewPager mPager;
    private HelloFragment mHelloFragment;
    private CollectionSeedFragment mCollectionSeedFragment;
    private LocationSeedFragment mLocationSeedFragment;
    //private IntroFragment mTextEmailAuthFragment;

    private Nostalgia mApp;
    private User mLoggedIn;

    private int NUMBER_PAGES = 3;

    private IntroFragment.OnAcceptedListener self = this;

    @Override
    public void onCancelled() {
        //TODO: nothing.
    }

    @Override
    public void onSelection(Object selection, boolean isSelected) {
        //TODO: Add to user's chosen channels.
    }

    private class IntroPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[] { "All Videos", "Only Mine"};

        public IntroPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment item;
            switch(pos) {
                default:
                case 0:
                    mHelloFragment = (null == mHelloFragment) ?  HelloFragment.newInstance() : mHelloFragment;
                    mHelloFragment.setAcceptedListener(self);
                    item = mHelloFragment;
                    break;
                case 1:
                    mCollectionSeedFragment = (null == mCollectionSeedFragment) ?  CollectionSeedFragment.newInstance() : mCollectionSeedFragment;
                    mCollectionSeedFragment.setAcceptedListener(self);
                    mCollectionSeedFragment.setSelectionListener((BaseRecyclerFragment.SelectionListener) self);
                    item = mCollectionSeedFragment;
                    break;
                case 2:
                    mLocationSeedFragment = (null == mLocationSeedFragment) ?  LocationSeedFragment.newInstance() : mLocationSeedFragment;
                    mLocationSeedFragment.setAcceptedListener(self);
                    mCollectionSeedFragment.setSelectionListener((BaseRecyclerFragment.SelectionListener) self);
                    item = mLocationSeedFragment;
                    break;
                //case 3:
                //    mTextEmailAuthFragment = (null == mTextEmailAuthFragment) ?  TextEmailAuthFragment.newInstance() : mTextEmailAuthFragment;
                //    mTextEmailAuthFragment.setAcceptedListener(self);
                //    item = mHelloFragment;
                //    break;
            }
            if(null == item){
                Log.e("IntroductionActivity", "Null Fragment created for viewpager pos: " + Integer.toString(pos) );
            }
            return item;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mApp = (Nostalgia) getApplication();
        mLoggedIn = mApp.getUserRepo().getLoggedInUser();

        if(mLoggedIn == null){
            Toast.makeText(this, "Error logging in.", Toast.LENGTH_LONG);
        } else {

            /*
             * Before viewpager
             */
            mPager = (ViewPager) findViewById(R.id.viewPager);
            mPager.setAdapter(new IntroPagerAdapter(getSupportFragmentManager()));

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
        }
    }

    private boolean mPrivate = false;
    private int ctr = 0;
    private void advancePage(){
        ctr = ctr+1;
        mPager.setCurrentItem(ctr,true);
    }

    @Override
    public void onAccepted(String name) {
        //ignore name for now, don't need it.
        if (ctr == NUMBER_PAGES - 1){
            advancePage();
        }
    }

}
