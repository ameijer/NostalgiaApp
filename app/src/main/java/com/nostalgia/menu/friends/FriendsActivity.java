package com.nostalgia.menu.friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.nostalgia.controller.peek.CreatorLauncher;
import com.nostalgia.menu.friends.recycler.ContactSelectorFragment;

/**
 * Created by alex on 12/25/15.
 */
public class FriendsActivity extends AppCompatActivity implements View.OnClickListener, CreatorLauncher {

    public static final int FRIENDS_ACTIVITY_REQUEST_CODE = 2378;

    private Toolbar mToolbar;

    public static final String TAG = "FriendsActivity";

    private ViewPager mPager;

    private SearchAllFragment mSearchAllFrag;
    //private OldNativeFriendsFragment mNativeFriendsFrag;
    private ContactsFragment mContactsFrag;
    private SocialFragment mSocialFrag;

    private ContactSelectorFragment mContactSelectorFrag;

    @Override
    public void launchCreator(String creator){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

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

        return;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(null != mContactsFrag) {
            mContactsFrag.handleIntent(intent);
        }
        if(null != mContactSelectorFrag) {
            mContactSelectorFrag.loadAllPhoneContacts();
        }
    }

    private String[] tabTitles = {"Connected", "Offline", "Phone", "Social"};

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int pos){
            try{
                switch (pos) {
                    default:
                    //case 0:
                        //mSearchAllFrag = (null == mSearchAllFrag) ? new SearchAllFragment().newInstance() : mSearchAllFrag;
                        //return mSearchAllFrag;
                    case 0:
                        mContactsFrag = (null == mContactsFrag) ? new ContactsFragment().newInstance() : mContactsFrag;
                        return mContactsFrag;
                    case 1:
                        mContactSelectorFrag = (null == mContactSelectorFrag) ? new ContactSelectorFragment().newInstance(true) : mContactSelectorFrag;
                        return mContactSelectorFrag;
                    //case 2:
                    //    mSocialFrag = (null == mSocialFrag) ? new SocialFragment().newInstance() : mSocialFrag;
                    //    return mSocialFrag;
                }
            } catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }

    }

    private void updateFriendList() {

    }

    private void quitFriendsActivity() {
        Intent intent = this.getIntent();
        this.setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:
                Log.e(TAG, "error - unhanded onclick for id: " + v.getId());
                return;
        }
    }
}
