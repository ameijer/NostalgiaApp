package com.nostalgia.controller.drawer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.RecyclerViewCacheUtil;
import com.mikepenz.octicons_typeface_library.Octicons;
import com.nostalgia.Nostalgia;

import com.nostalgia.controller.SplashActivity;
import com.nostalgia.controller.peek.activity.AreaPeekActivity;

import com.nostalgia.menu.friends.FriendsActivity;
import com.nostalgia.menu.settings.SettingsActivity;
import com.nostalgia.persistence.model.User;

import com.nostalgia.persistence.model.Video;
import com.nostalgia.runnable.AttributeActionThread;
import com.nostalgia.runnable.AttributeGetterTask;
import com.nostalgia.runnable.AuthTokenTestThread;
import com.nostalgia.runnable.FieldType;
import com.nostalgia.runnable.FriendActionThread;
import com.nostalgia.runnable.UserAttributeUpdaterThread;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public abstract class MaterialDrawerActivity extends AppCompatActivity implements AttributeGetterTask.GetterTaskFinishedListener {
    private static final int PROFILE_SETTING = 1;
    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;

    //save our header or mDrawer
    private AccountHeader mAccountHeader = null;
    private Drawer mDrawer = null;
    private Nostalgia mApp;
    IProfile mProfile = new ProfileDrawerItem().withIcon(R.drawable.empty);
    IProfile mDefaultProfile = new ProfileDrawerItem().withIcon(R.drawable.empty);

    public static int FRIENDS_ACTIVITY = 1;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Context context;

    public enum CameraApiVersion {
        OLD, NEW
    }

    private User loggedIn;

    //private Toolbar mToolbar;

    private String BASE_TAG = "MaterialDrawerActivity";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(BASE_TAG, "received intent: " + intent.getAction());

            switch (intent.getAction()) {
                case ("com.nostalgia.update"):
                    updateDrawerAccount();
                    break;
                default:
                    break;
            }

        }
    };

    private void updateDrawerAccount() {
        loggedIn = mApp.getUserRepo().getLoggedInUser();

        if (loggedIn != null) {
            if (loggedIn.getIcon() == null) {
                mProfile = new ProfileDrawerItem().withName(loggedIn.getUsername()).withIcon("https://avatars3.githubusercontent.com/u/1476232?v=3&s=460").withIdentifier(loggedIn.getUsername().hashCode());
            } else {
                //we have an icon
                byte[] pngRaw = Base64.decode(loggedIn.getIcon(), Base64.DEFAULT);//currentUser.getUserImg().getRawData().getBytes(Charsets.US_ASCII);
                Bitmap bmp = BitmapFactory.decodeByteArray(pngRaw, 0, pngRaw.length);
                mProfile = new ProfileDrawerItem().withName(loggedIn.getUsername()).withIcon(bmp).withIdentifier(loggedIn.getUsername().hashCode());
            }
            //set the active mProfile
            mAccountHeader.setActiveProfile(mProfile);
            mDrawer.updateName(13, new StringHolder("Logout"));
            mDrawer.updateIcon(13, new ImageHolder(Octicons.Icon.oct_log_out));
        } else {

            mDrawer.updateName(13, new StringHolder("Login"));
            mDrawer.updateIcon(13, new ImageHolder(Octicons.Icon.oct_log_in));
            mAccountHeader.setActiveProfile(mDefaultProfile);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nostalgia.update");

        registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterReceiver(receiver);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    public static final int LOGIN = 192;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApp = (Nostalgia) getApplication();
        loggedIn = mApp.getUserRepo().getLoggedInUser();

        context = this;
        //initially, mStart with the choice fragment

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    //Must be called after setContentLayout
    public void attachDrawer(Bundle savedInstanceState, Toolbar toolbar){
        // Create the AccountHeader
        mAccountHeader = createAccountHeader(savedInstanceState);
        initializeDrawer(savedInstanceState, toolbar);

        if (((Nostalgia) getApplication()).getUserRepo().getLoggedInUser() != null) {
            updateDrawerAccount();
        }
    }

    public static final int VIDEO_UPLOAD = 111;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case VIDEO_UPLOAD:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MaterialDrawerActivity.this, "video uploaded successfully", Toast.LENGTH_LONG).show();
                }
                break;

            case(SETTINGS_ACTIVITY_REQUEST_CODE):

                if(resultCode == RESULT_OK){
                    Toast.makeText(MaterialDrawerActivity.this, "settings changed successfully", Toast.LENGTH_LONG).show();
                    updateDrawerAccount();
                }
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(MaterialDrawerActivity.this, "settings cancelled", Toast.LENGTH_LONG).show();
                }
                break;
            case(FriendsActivity.FRIENDS_ACTIVITY_REQUEST_CODE):
                if(resultCode == RESULT_OK){
                    Toast.makeText(MaterialDrawerActivity.this, "friends actiivty quit successfully", Toast.LENGTH_LONG).show();
                    updateDrawerAccount();
                }
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(MaterialDrawerActivity.this, "friends activity cancelled", Toast.LENGTH_LONG).show();
                }
                break;

            case(AreaPeekActivity.PEEK_ACTIVITY_REQUEST_CODE):
                if(resultCode == RESULT_OK){
                    Toast.makeText(MaterialDrawerActivity.this, "peek actiivty quit successfully", Toast.LENGTH_LONG).show();
                    updateDrawerAccount();
                }
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(MaterialDrawerActivity.this, "peek activity cancelled", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }



    public User logout() {
        User loggedIn = mApp.getUserRepo().getLoggedInUser();
        if (loggedIn == null) return null;
        User loggedOut = null;
        try {
            loggedOut = mApp.getUserRepo().unAuthorize(loggedIn, mApp.getDeviceId());
        } catch (CouchbaseLiteException e) {
            Log.e(BASE_TAG, "CBLite exception", e);
            e.printStackTrace();
        }

        try {
            mApp.getUserRepo().delete(loggedOut);
        } catch (CouchbaseLiteException e) {
            Log.e(BASE_TAG, "CBLite exception", e);
            e.printStackTrace();
        }

        //announce to system
        Intent i = new Intent("com.nostalgia.LOGOUT");
        sendBroadcast(i);

        Toast.makeText(getApplicationContext(), "User " + loggedOut.getUsername() + " logged out successfully",
                Toast.LENGTH_LONG).show();

        mApp.flushCache();

        goToSplashActivity();

        return loggedOut;
    }

    private void goToSplashActivity(){
        Intent splash = new Intent(this, SplashActivity.class);
        splash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(splash);
        finish();
    }

    private void initializeDrawer(Bundle savedInstanceState, Toolbar toolbar) {
        final PrimaryDrawerItem login_logout;
        if (loggedIn == null) {
            login_logout = new PrimaryDrawerItem().withName("Login").withIcon(Octicons.Icon.oct_log_in).withIdentifier(13).withSelectable(false);
        } else {
            login_logout = new PrimaryDrawerItem().withName("Logout").withIcon(Octicons.Icon.oct_log_out).withIdentifier(13).withSelectable(false);
        }

        final MaterialDrawerActivity self = this;

        ArrayList<PrimaryDrawerItem> items = new ArrayList<PrimaryDrawerItem>();
        items.add(login_logout);
        //Create the drawer
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(mAccountHeader) //set the AccountHeader we created earlier for the header
                .addDrawerItems(new PrimaryDrawerItem().withName("People").withIcon(Octicons.Icon.oct_person_add).withIdentifier(FRIENDS_ACTIVITY).withSelectable(false),
                        new PrimaryDrawerItem().withName("test voting check ").withIcon(R.drawable.ic_settings_black_48dp).withIdentifier(59).withSelectable(false),


                        login_logout
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if(drawerItem.getIdentifier() == 51){
                            AuthTokenTestThread tester = new AuthTokenTestThread(mApp.getUserRepo().getLoggedInUser(), AuthTokenTestThread.TestType.GET_PASS, context, false);
                            tester.start();
                            try {
                                tester.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String result = tester.getMatching();

                            if(tester.getResultCode() != 200) {
                                Toast.makeText(context, "error code: " + tester.getResultCode() +" with contents: " + result, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Success! Response body: " + result, Toast.LENGTH_LONG).show();
                            }

                        }
                        if(drawerItem.getIdentifier() == 50){
                            AuthTokenTestThread tester = new AuthTokenTestThread(mApp.getUserRepo().getLoggedInUser(), AuthTokenTestThread.TestType.GET_QUERY_PASS, context, false);
                            tester.start();
                            try {
                                tester.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String result = tester.getMatching();

                            if(tester.getResultCode() != 200) {
                                Toast.makeText(context, "error code: " + tester.getResultCode() +" with contents: " + result, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Success! Response body: " + result, Toast.LENGTH_LONG).show();
                            }
                        }
                        if(drawerItem.getIdentifier() == 49){
                            AuthTokenTestThread tester = new AuthTokenTestThread(mApp.getUserRepo().getLoggedInUser(), AuthTokenTestThread.TestType.GET_PASS, context, true);
                            tester.start();
                            try {
                                tester.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String result = tester.getMatching();

                            if(tester.getResultCode() != 200) {
                                Toast.makeText(context, "error code: " + tester.getResultCode() +" with contents: " + result, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Success! Response body: " + result, Toast.LENGTH_LONG).show();
                            }
                        }
                        if(drawerItem.getIdentifier() == 59){
                            Video testVid = new Video();
                            testVid.set_id("2dfd87c7-2ee3-4cdf-8f7b-56cd6193ac22");

                           //upvote video
                            AttributeActionThread upvoter = new AttributeActionThread(testVid, mApp.getUserRepo().getLoggedInUser(), FieldType.UPVOTES);
                            upvoter.start();
                            try {
                                upvoter.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            boolean success = upvoter.isSuccessful();
                            if(!success){
                                Log.e("upvote test", "error upvoting");
                            } else {
                                Toast.makeText(context, "upvote success", Toast.LENGTH_LONG).show();
                            }

                            //check upvote
                            AttributeGetterTask getter = new AttributeGetterTask(testVid, FieldType.UPVOTES, mApp.getUserRepo().getLoggedInUser());
                            getter.execute("null");
                            try {
                                getter.get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            String raw = getter.getRawResult();
                            boolean hasVoted = getter.hasUserParticipated(mApp.getUserRepo().getLoggedInUser());

                            Toast.makeText(context, "raw: " + raw + "\n hasvoted: " + hasVoted, Toast.LENGTH_LONG).show();

                        }
                        if(drawerItem.getIdentifier() == 48){
                            AuthTokenTestThread tester = new AuthTokenTestThread(mApp.getUserRepo().getLoggedInUser(), AuthTokenTestThread.TestType.GET_FAIL, context, false);
                            tester.start();
                            try {
                                tester.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String result = tester.getMatching();

                            if(tester.getResultCode() != 200) {
                                Toast.makeText(context, "error code: " + tester.getResultCode() +" with contents: " + result, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Success! Response body: " + result, Toast.LENGTH_LONG).show();
                            }
                        }
                        if(drawerItem.getIdentifier() == 47){
                            AuthTokenTestThread tester = new AuthTokenTestThread(mApp.getUserRepo().getLoggedInUser(), AuthTokenTestThread.TestType.POST_PASS, context, false);
                            tester.start();
                            try {
                                tester.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String result = tester.getMatching();

                            if(tester.getResultCode() != 200) {
                                Toast.makeText(context, "error code: " + tester.getResultCode() +" with contents: " + result, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Success! Response body: " + result, Toast.LENGTH_LONG).show();
                            }
                        }

                        if (drawerItem.getIdentifier() == 1) {
                              startSettingsActivity();
                        }
                        if(drawerItem.getIdentifier() == 2){
                            getDownvoteInfo();
                        }

                        if (drawerItem.getIdentifier() == 9999) {
                            startPeekActivity();
                        }

                        if (drawerItem.getIdentifier() == FRIENDS_ACTIVITY) {
                            startFriendsActivity();
                        }

                        if(drawerItem.getIdentifier() == 133){
                            acceptFriend(drawerItem.getTag());
                        }

                        if(drawerItem.getIdentifier() == 134){
                            requestFriend(drawerItem.getTag());
                        }


                        if (drawerItem.getIdentifier() == 13) {
                            final User loggedIn = mApp.getUserRepo().getLoggedInUser();
                            if (loggedIn == null) {
                            } else {
                                User loggedOut = logout();
                                goToSplashActivity();
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState);


        String pending = null;
        for(Map.Entry<String, String> pendings : loggedIn.getPendingFriends().entrySet()){

            //pending friends I requested have Sent_<time of request> in value
            //pending requests others hae sent me have Received_<time of request>


            if(pendings.getValue().contains("Received")){
                //we have a pending friends request
                pending = pendings.getKey();
                break;
            }
        }

        if(pending != null) {

            //find user
            User requested = mApp.getUserRepo().findOneById(pending);

            final PrimaryDrawerItem acceptfriend = new PrimaryDrawerItem().withTag(pending).withName("Accept Friend: " + requested.getUsername()).withIcon(Octicons.Icon.oct_log_in).withIdentifier(133).withSelectable(false);

            builder.addDrawerItems(acceptfriend);
        }

        //if not friends with testfriend, then offerthe option
        if(!loggedIn.getFriends().keySet().contains("1e374722-898a-42c5-8deb-6bb5a02056ab") && !loggedIn.getPendingFriends().keySet().contains("1e374722-898a-42c5-8deb-6bb5a02056ab") && !loggedIn.get_id().equalsIgnoreCase("1e374722-898a-42c5-8deb-6bb5a02056ab")){
            final PrimaryDrawerItem addfriend = new PrimaryDrawerItem().withTag("1e374722-898a-42c5-8deb-6bb5a02056ab").withName("request Friend: testfriend").withIcon(Octicons.Icon.oct_log_in).withIdentifier(134).withSelectable(false);
            builder.addDrawerItems(addfriend);
        }

        mDrawer = builder.build();

        //if you have many different types of DrawerItems you can magically pre-cache those items to get a better scroll performance
        //make sure to init the cache after the DrawerBuilder was created as this will first clear the cache to make sure no old elements are in
        RecyclerViewCacheUtil.getInstance().withCacheSize(2).init(mDrawer);

        //only set the active selection or active mProfile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 11
            mDrawer.setSelection(21, false);

            //set the active mProfile
            mAccountHeader.setActiveProfile(mProfile);
        }

        mDrawer.updateBadge(4, new StringHolder(10 + ""));
        mDrawer.setStatusBarColor(getResources().getColor(R.color.status_bar_color));
    }

    protected void requestFriend(Object tag){
        FriendActionThread thread = new FriendActionThread(FriendActionThread.ActionType.REQUEST, tag.toString(), mApp.getUserRepo().getLoggedInUser());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e("accept friend", "error", e);
        }


        User hasPendingFriend = thread.getMyUpdatedState();

        if(hasPendingFriend == null || hasPendingFriend.get_id().equalsIgnoreCase(mApp.getUserRepo().getLoggedInUser().get_id())){
            Toast.makeText(MaterialDrawerActivity.this, "friend added successfully", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(MaterialDrawerActivity.this, "error adding friend", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onAttributeGotten(String ownerObjectId, long simpleCount) {

        Log.d("MaterialDrawerActivity", "onAttributeGotten called.");
    }

    @Override
    public void onError(String ownerObjectId) {

        Log.d("MaterialDrawerActivity", "onError called.");
    }
    //End downvote listener implementation

    private void getDownvoteInfo() {
        String someVidId = (String) mApp.getVidRepo().getAllVideos().keySet().toArray()[0];
        Video target = mApp.getVidRepo().findOneById(someVidId, true, false);

        AttributeGetterTask mDownGetter = new AttributeGetterTask(target, FieldType.DOWNVOTES,mApp.getUserRepo().getLoggedInUser());
        mDownGetter.setFinishedListener(this);
        mDownGetter.execute();

    }

    private void acceptFriend(Object tag) {
        FriendActionThread thread = new FriendActionThread(FriendActionThread.ActionType.ACCEPT, tag.toString(), mApp.getUserRepo().getLoggedInUser());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e("accept friend", "error", e);
        }


        User hasFriend = thread.getMyUpdatedState();

        if(hasFriend == null || hasFriend.get_id().equalsIgnoreCase(mApp.getUserRepo().getLoggedInUser().get_id())){
            Toast.makeText(MaterialDrawerActivity.this, "friend added successfully", Toast.LENGTH_LONG).show();
            return;
        } else {

            Toast.makeText(MaterialDrawerActivity.this, "error adding friend", Toast.LENGTH_LONG).show();
        }

    }

    private void startPeekActivity() {
        Intent intent=new Intent(MaterialDrawerActivity.this, AreaPeekActivity.class);

        intent.putExtra("focuspoint", mApp.getLocation());
        intent.putExtra("widthmi", 5.0);
        startActivityForResult(intent, AreaPeekActivity.PEEK_ACTIVITY_REQUEST_CODE);
    }

    private void startFriendsActivity() {
        Intent intent=new Intent(MaterialDrawerActivity.this, FriendsActivity.class);
        startActivityForResult(intent, FriendsActivity.FRIENDS_ACTIVITY_REQUEST_CODE);
    }

    private void startSettingsActivity() {
        Intent intent=new Intent(MaterialDrawerActivity.this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);

    }

    private AccountHeader createAccountHeader(Bundle savedInstanceState) {
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.road_ahead)
                .addProfiles(
                        mProfile,
                        new ProfileSettingDrawerItem().withName("Add Account").withDescription("Add new GitHub Account").withIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_plus).actionBar().paddingDp(5).colorRes(R.color.material_drawer_primary_text)).withIdentifier(PROFILE_SETTING),
                        new ProfileSettingDrawerItem().withName("Manage Account").withIcon(GoogleMaterial.Icon.gmd_settings)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();
        return header;
    }

}
